package com.wanxi.hdfs_yun.pojo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BuildMethods {
    // 常量定义
    public static final long GB = 1073741824L;
    public static final long MB = 1048576L;
    public static final long KB = 1024L;

    /**
     * 内存大小转换
     *
     * @param bt
     * @return KB, MB, GB
     */
    public static String memoryConversion(long bt) {
        String size;
        double a;
        if (bt >= GB) {
            a = (bt / (double) GB);
            return String.format("%.2f", a) + "G";
        } else if (bt >= MB) {
            a = (bt / (double) MB);
            return String.format("%.2f", a) + "M";
        } else {
            a = (bt / (double) KB);
            return String.format("%.2f", a) + "KB";
        }
    }

    /**
     * 将时间戳转换成"yyyy-MM-dd HH:mm:ss"格式
     *
     * @param te
     * @return "yyyy-MM-dd HH:mm:ss"
     */
    public static String timeConversion(long te) {
        // 将Unix时间戳转换为Instant对象
        Instant instant = Instant.ofEpochMilli(te);
        // 将Instant对象转换为系统默认时区的ZonedDateTime对象
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
        // 转换为LocalDateTime对象（如果需要忽略时区信息）
        LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 格式化日期时间
        String formattedDateTime = localDateTime.format(formatter);
        // 输出结果
        return formattedDateTime;
    }

    public static String getFpath(String path) {
        Path url = Paths.get(path);
        Path parentPath = url.getParent();
        // 如果你需要 String 类型的父路径，你可以调用 toString 方法
        return parentPath.toString();
    }

    /**
     * 面包屑制作
     *
     * @param path
     * @return urlList：【【跳转路径1，面包屑展示1】，【跳转路径2，面包屑展示2】】
     */
    public static List<List<String>> breadCrumb(String path) {
        List<List<String>> urlList = new ArrayList<>();
        if (path == null || path.isEmpty()) {
            return urlList; // 返回一个空列表
        }
        String[] parts = path.split("/");
        StringBuilder currentPath = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (!part.isEmpty()) { // 忽略空的部分
                if (i > 0) {
                    currentPath.append("/");
                }
                currentPath.append(part);

                List<String> pathPair = Arrays.asList(currentPath.toString(), part);
                urlList.add(pathPair);
            }
        }
        return urlList;
    }
}
