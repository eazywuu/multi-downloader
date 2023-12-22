package xyz.easy.util;

import xyz.easy.constant.Constant;

import java.io.File;

/**
 * @author Ethan Wu
 * @description 文件信息工具类
 * @date 2023/12/21 19:10
 */
public class LocalFileUtils {

    /**
     * 获取本地文件内容大小
     * @param path 文件本地存储路径
     * @return long
     * @date 2023/12/22 1:50
     */
    public static long getLocalFileContentLength(String path) {
        File file = new File(path);

        return file.exists() && file.isFile() ? file.length() : 0;
    }

    public static String getLocalTempFilePath(String localFilePath , int i) {
        return localFilePath + Constant.FILE_TEMP_NAME + i;
    }
}
