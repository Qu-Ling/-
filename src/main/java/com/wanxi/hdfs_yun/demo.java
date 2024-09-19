package com.wanxi.hdfs_yun;

import com.wanxi.hdfs_yun.pojo.FileIndex;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.wanxi.hdfs_yun.pojo.BuildMethods.memoryConversion;
import static com.wanxi.hdfs_yun.pojo.BuildMethods.timeConversion;

public class demo {
    private FileSystem fs;

    @Before
    public void getFileSystem() throws IOException, URISyntaxException, InterruptedException {
        URI uri = new URI("hdfs://xingtie2:8020");
        Configuration configuration = new Configuration();
        configuration.set("dfs.client.block.write.replace-datanode-on-failure.policy", "NEVER");
        configuration.set("dfs.client.block.write.replace-datanode-on-failure.enable", "true");
        String user = "root";
        fs = FileSystem.get(uri, configuration, user);
    }

    @After
    public void close() throws IOException {
        fs.close();
    }

    @Test
    public void demo1() throws IOException {
        FileStatus[] list = fs.listStatus(new Path("/home/fuhua"));
        for (FileStatus file : list) {


            System.out.println(file.getPath().toUri().getPath());
            System.out.println(file.getPath().getName());

        }
    }

    /**
     * 展示用户名单
     * @throws IOException
     */
    @Test
    public void demo2() throws IOException{
        //将用户文档转换成能每行每行读取文件数据的BufferedReader类
        FSDataInputStream in = fs.open(new Path("/data/userinfo.dat"));  //1
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));  //2
        String line;
        System.out.println("id,name,pwd");
        while ((line = reader.readLine()) != null) {
            String[] userinfo = line.split(",");
            System.out.println(line);
        }
        fs.close();
    }

    @Test
    public void demo3() throws IOException {
        List<FileIndex> list = new ArrayList<FileIndex>();
        FileStatus[] filesStatus = fs.listStatus(new Path("/home/2995924975"));
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
        for (FileIndex fileIndex : list) {
            System.out.println(fileIndex.toString());
        }
    }
    @Test
    public void demo4() {
        String originalString = "1223010103/demo1/subfolder//ddd";
        int lastIndex = originalString.lastIndexOf('/');
        // 如果找到了'/'，则截取到该位置之前（不包括'/'）
        if (lastIndex != -1) {
            String modifiedString = originalString.substring(0, lastIndex);
            System.out.println(modifiedString); // 输出: 1223010103/demo1/subfolder
        } else {
            // 如果没有找到'/'，则原始字符串可能不包含'/'，或者整个字符串就是'/'
            System.out.println("No '/' found in the string.");
        }
    }

    @Test
    public void updata() throws IOException {
        fs.copyFromLocalFile(false,true,new Path("G:\\漫画\\下雪那天路遇的奇怪女孩.pdf"),new Path("/home/1223010103/下雪那天路遇的奇怪女孩.pdf"));
    }

    @Test
    public void download() throws IOException {
        String hdfs = "/home/1223010103/";
        String local="C:\\Users\\符华\\Pictures\\壁纸头像\\117657717_p0.jpg";

    }

    @Test
    public  void getFpath() {
        String path="1223010101/asad/demo/QQ截图202";
        java.nio.file.Path url = Paths.get(path);
        java.nio.file.Path parentPath = url.getParent();
        System.out.println(parentPath.toString());
        List<FileIndex> list = new ArrayList<FileIndex>();
        System.out.println(list.toString());
    }
}
