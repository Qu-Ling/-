package com.wanxi.hdfs_yun.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FileIndex {
    private String fileName;
    private String filePath;
    private String lastTime;  //最后操作时间
    private boolean isDir;//是否为目录?true:false
    private String size;//文件大小
}
