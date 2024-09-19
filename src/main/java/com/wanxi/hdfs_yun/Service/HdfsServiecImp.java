package com.wanxi.hdfs_yun.Service;

import com.wanxi.hdfs_yun.pojo.FileIndex;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.wanxi.hdfs_yun.pojo.BuildMethods.memoryConversion;
import static com.wanxi.hdfs_yun.pojo.BuildMethods.timeConversion;

@Service
public class HdfsServiecImp implements HdfsSrevice {

    @Value("${hadoop.namenode}")
    private String url;
    @Value("${hadoop.userdata}")
    private String userdata;

    /**
     * 文件系统实例化
     *
     * @return hdfs文件系统
     * @throws IOException
     */
    public FileSystem getFileSystem() throws IOException, URISyntaxException, InterruptedException {
        URI uri = new URI(url);
        Configuration configuration = new Configuration();
        configuration.set("dfs.client.block.write.replace-datanode-on-failure.policy", "NEVER");
        configuration.set("dfs.client.block.write.replace-datanode-on-failure.enable", "true");
        String user = "root";
        return FileSystem.get(uri, configuration, user);
    }

    @Override
    public boolean login(String userId, String userpwd) throws IOException, URISyntaxException, InterruptedException {
        FileSystem fs = getFileSystem();
        //将用户文档转换成能每行每行读取文件数据的BufferedReader类
        FSDataInputStream in = fs.open(new Path(userdata));  //1
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));  //2
        String line;
        while ((line = reader.readLine()) != null) {
            String[] userinfo = line.split(",");
            if (userinfo[0].equals(userId) && userinfo[2].equals(userpwd)) {
                fs.close();
                return true;
            }
        }
        fs.close();
        return false;
    }

    /**
     * 判断用户ID是否存在
     *
     * @param userId
     * @return 存在返回true，不存在返回false
     */
    @Override
    public boolean userIdExist(String userId) throws IOException, URISyntaxException, InterruptedException {
        FileSystem fs = getFileSystem();
        //将用户文档转换成能每行每行读取文件数据的BufferedReader类
        FSDataInputStream in = fs.open(new Path(userdata));  //1
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));  //2
        String line;
        while ((line = reader.readLine()) != null) {
            String[] userinfo = line.split(",");
            if (userinfo[0].equals(userId)) {
                fs.close();
                return true;
            }
        }
        fs.close();
        return false;
    }

    /**
     * 加入用户
     *
     * @param userId
     * @param userName
     * @param userPwd
     */
    @Override
    public boolean insertUser(String userId, String userName, String userPwd) throws IOException, URISyntaxException, InterruptedException {
        FileSystem fs = getFileSystem();
        String user = userId + "," + userName + "," + userPwd + "\n";
        FSDataOutputStream out = fs.append(new Path(userdata));
        out.write(user.getBytes(StandardCharsets.UTF_8));
        // 创建用户的同时，以用户的id(唯一标识)创建单独文件夹
        fs.mkdirs(new Path("/home/" + userId));
        out.close();
        if (userIdExist(userId)) {
            fs.close();
            return true;
        } else {
            fs.close();
            return false;
        }
    }

    /**
     * 展示给出目录下的文件和目录
     *
     * @param path
     * @return 文件列表
     */
    @Override
    public List<FileIndex> listFlie(Path path) throws IOException, URISyntaxException, InterruptedException {
        FileSystem fs = getFileSystem();
        List<FileIndex> list = new ArrayList<FileIndex>();
        FileStatus[] filesStatus = fs.listStatus(path);
        for (FileStatus file : filesStatus) {
            // 创建一个文件记录表记录信息
            FileIndex fileIndex = new FileIndex();
            //==================================================================
            // 获取文件名称
            fileIndex.setFileName(file.getPath().getName());
            // 获取文件路径
            fileIndex.setFilePath(file.getPath().toUri().getPath());
            // 获取修改时间并转换格式
            fileIndex.setLastTime(timeConversion(file.getModificationTime()));
            // 判断是不是文件夹
            fileIndex.setDir(file.isDirectory());
            //文件大小
            fileIndex.setSize(memoryConversion(file.getLen()));
            //==================================================================
            // 添加到文件信息表
            list.add(fileIndex);
        }
        fs.close();
        return list;
    }

    @Override
    public boolean isFile(Path path) throws IOException, URISyntaxException, InterruptedException {
        FileSystem fs = getFileSystem();
        return fs.isFile(path);
    }

    /**
     * 新建文件夹
     *
     * @param path
     */
    @Override
    public boolean mkdir(Path path) throws IOException, URISyntaxException, InterruptedException {
        FileSystem fs = getFileSystem();
        if (fs.mkdirs(path)) {
            fs.close();
            return true;
        } else {
            fs.close();
            return false;
        }
    }

    /**
     * 删除
     *
     * @param path
     */
    @Override
    public boolean deleteFile(Path path) throws IOException, URISyntaxException, InterruptedException {
        FileSystem fs = getFileSystem();
        if (fs.delete(path, true)) {
            fs.close();
            return true;
        } else {
            fs.close();
            return false;
        }
    }

    /**
     * 重命名
     *
     * @param oldpath newpath
     */
    @Override
    public boolean reviseName(Path oldpath, Path newpath) throws IOException, URISyntaxException, InterruptedException {
        FileSystem fs = getFileSystem();
        if (fs.rename(oldpath, newpath)) {
            fs.close();
            return true;
        } else {
            fs.close();
            return false;
        }
    }

    /**
     * 上传
     *
     * @param hdfspath localpath
     */
    @Override
    public boolean cloudUpload(MultipartFile localIn, Path hdfspath) {
        try (
                FileSystem fs = getFileSystem();
                InputStream localInStream = localIn.getInputStream();
                FSDataOutputStream hdfsOutStream = fs.create(hdfspath)
        ) {
            //将输入流复制到输出流
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = localInStream.read(buffer)) != -1) {
                hdfsOutStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            // 处理异常
            e.printStackTrace();
        }
//      fs.copyFromLocalFile(localpath,hdfspath);//复制本地文件,不返回任何值（void）
//      boolean delSrc,  是否删除本地文件（可选）
//      boolean overwrite,  是否覆盖hdfs中原有文件（可选）
//      Path[] srcs,  本地多个文件（一个也行）
//      Path dst   hdfs路径
        // fs.moveFromLocalFile()剪切本地文件，支持同时上传多个本地文件
        System.out.println("上传成功");
        return true;
    }

    /**
     * 下载
     *
     * @param hdfspath localpath
     */
    @Override
    public boolean download(Path hdfspath, Path localpath) throws IOException, URISyntaxException, InterruptedException {
        FileSystem fs = getFileSystem();
        fs.copyToLocalFile(false, hdfspath, localpath, true);//不返回任何值（void）
//        boolean delSrc, 是否删除源文件
//        Path src,  hdfs文件
//        Path dst  本地文件
//        boolean useRawLocalFileSystem 是否关闭校验（是否生成crc文件）
        //fs.moveToLocalFile();
        fs.close();
        return true;
    }
}
