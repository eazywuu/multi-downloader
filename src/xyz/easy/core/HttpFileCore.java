package xyz.easy.core;

import xyz.easy.util.LogUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * http工具类
 *
 * @author Ethan Wu
 * @date 2023/12/22 1:46
 */
public class HttpFileCore {


    /**
     * @param url      要下载文件url
     * @param startPos 分块文件开始下标
     * @param endPos   分块文件结束下标
     * @return java.net.HttpURLConnection 要下载文件的http连接
     * @description 获取多线程分块下载文件http连接
     * @date 2023/12/22 1:43
     */
    public static HttpURLConnection getHttpUrlConnection(String url, long startPos, long endPos) {
        HttpURLConnection httpUrlConnection = getHttpUrlConnection(url);
        LogUtils.info("下载的区间是：{}-{}", startPos, endPos);

        if (endPos != 0) {
            // example: bytes=100-200
            // endPos != 0, means not the last file block
            httpUrlConnection.setRequestProperty("RANGE", "bytes=" + startPos + "-" + endPos);
        } else {
            // endPos = 0, means this is the last file block to download
            httpUrlConnection.setRequestProperty("RANGE", "bytes=" + startPos + "-");
        }
        return httpUrlConnection;
    }

    /**
     * @param url 要下载文件的url
     * @return java.net.HttpURLConnection 要下载文件的http连接
     * @description 获取单线程下载文件http连接
     * @date 2023/12/22 1:43
     */
    public static HttpURLConnection getHttpUrlConnection(String url) {
        HttpURLConnection httpUrlConnection;
        try {
            URL httpUrl = new URL(url);
            httpUrlConnection = (HttpURLConnection) httpUrl.openConnection();
        } catch (IOException e) {
            LogUtils.error("http连接创建失败！");
            throw new RuntimeException(e);
        }

        //向文件所在的服务器发送标识信息
        httpUrlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko");

        return httpUrlConnection;
    }

    /**
     * @param url 要下载文件的url
     * @return long 要下载文件的总大小
     * @description 获取要下载文件的总大小
     * @date 2023/12/22 2:02
     */
    public static long getHttpFileContentLength(String url) {

        HttpURLConnection httpUrlConnection = getHttpUrlConnection(url);

        int contentLength = httpUrlConnection.getContentLength();

        httpUrlConnection.disconnect();

        return contentLength;
    }

    /**
     * @param url 要下载文件的url
     * @return String 文件名
     * @description 获取要下载文件的文件名
     * @date 2023/12/22 1:49
     */
    public static String getHttpFileName(String url) {
        int index = url.lastIndexOf("/");
        return url.substring(index + 1);
    }
}
