package com.wanxi.hdfs_yun.Service;

import com.wanxi.hdfs_yun.pojo.FileIndex;
import org.apache.hadoop.fs.Path;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

public interface HdfsSrevice {
    /**
     * 登入
     */
    boolean login(String userId,String userpwd) throws IOException, URISyntaxException, InterruptedException;

    /**
     * 注册
     */
    //判断用户是否存在
    boolean userIdExist(String userId) throws IOException, URISyntaxException, InterruptedException;
    //加入用户
    boolean insertUser(String userId,String userName,String userPwd) throws IOException, URISyntaxException, InterruptedException;

    /**
     * 展示
     * @return
     */
    List<FileIndex> listFlie(Path path)throws IOException, URISyntaxException, InterruptedException;

    boolean isFile(Path path)throws IOException, URISyntaxException, InterruptedException;

    /**
     * 新建文件夹
     */
    boolean mkdir(Path path)throws IOException, URISyntaxException, InterruptedException;

    /**
     * 删除
     */
    boolean deleteFile(Path path)throws IOException, URISyntaxException, InterruptedException;

    /**
     * 重命名
     */
    boolean reviseName(Path oldpath,Path newpath)throws IOException, URISyntaxException, InterruptedException;

    /**
     * 上传
     */
    boolean cloudUpload(MultipartFile localIn, Path hdfspath)throws IOException, URISyntaxException, InterruptedException;

    /**
     * 下载
     */
    boolean download(Path hdfspath,Path localpath)throws IOException, URISyntaxException, InterruptedException;

}
