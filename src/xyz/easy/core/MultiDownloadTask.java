package xyz.easy.core;

import xyz.easy.constant.Constant;
import xyz.easy.util.LocalFileUtils;
import xyz.easy.util.LogUtils;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * @author Ethan Wu
 * @description 文件多线程分块下载任务
 * @date 2023/12/21 23:41
 */
public class MultiDownloadTask implements Callable<Boolean> {
    private final String url;

    private final long startPos;

    private final long endPos;

    private final int part;

    private final DownloadInfoThread downloadInfoThread;

    private final CountDownLatch countDownLatch;

    public MultiDownloadTask(String url, long startPos, long endPos, int part, DownloadInfoThread downloadInfoThread, CountDownLatch countDownLatch) {
        this.url = url;
        this.startPos = startPos;
        this.endPos = endPos;
        this.part = part;
        this.downloadInfoThread = downloadInfoThread;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public Boolean call() {
        // 获取文件名
        String httpFileName = HttpFileCore.getHttpFileName(url);

        // 分块的文件名
        String tempFileName = LocalFileUtils.getLocalTempFilePath(httpFileName, part);

        // 文件下载路径
        String httpFilePath = Constant.PATH + tempFileName;

        // 创建http连接
        HttpURLConnection httpUrlConnection = HttpFileCore.getHttpUrlConnection(url, startPos, endPos);

        try (
                // 使用RandomAccessFile作为输出流，便于拓展功能
                InputStream inputStream = httpUrlConnection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                RandomAccessFile raf = new RandomAccessFile(httpFilePath, "rw")
        ) {
            byte[] buffer = new byte[Constant.BYTE_ARRAY_LENGTH];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                downloadInfoThread.setCurSize(len);
                raf.write(buffer, 0, len);
            }
        } catch (FileNotFoundException e) {
            LogUtils.error("文件不存在：{}", url);
        } catch (Exception e) {
            LogUtils.error("发生错误,下载失败!");
            e.printStackTrace();
        } finally {
            httpUrlConnection.disconnect();
            countDownLatch.countDown();
        }

        return true;
    }
}
