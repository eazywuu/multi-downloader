package xyz.easy.core;

import xyz.easy.constant.Constant;
import xyz.easy.util.LocalFileUtils;
import xyz.easy.util.LogUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Ethan Wu
 */
public class Downloader {

    // 计划执行器服务，开辟线程去执行计算文件信息
    private final ScheduledExecutorService scheduledExecutorService;

    // 线程池，用于多线程分块下载
    private final ThreadPoolExecutor threadPoolExecutor;

    // 列表，记录多线程下载块的下载状态
    private final List<Future<Boolean>> futureList;

    // 下载信息线程
    private DownloadInfoThread downloadInfoThread;

    // 分块下载线程状态监控
    private final CountDownLatch countDownLatch;

    public Downloader() {
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        threadPoolExecutor = new ThreadPoolExecutor(Constant.THREAD_NUM, Constant.THREAD_NUM, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(Constant.THREAD_NUM));
        futureList = new ArrayList<>();
        countDownLatch = new CountDownLatch(Constant.THREAD_NUM);
    }

    /**
     * @param url 要下载文件的url
     * @description 多线程分块下载
     * @date 2023/12/22 18:13
     */
    public void download(String url) {
        // 获取文件名
        String httpFileName = HttpFileCore.getHttpFileName(url);
        LogUtils.info("文件名：{}", httpFileName);

        // 获取文件本地存储路径
        String localFilePath = Constant.PATH + httpFileName;
        LogUtils.info("文件存储路径：{}", localFilePath);

        // 获取本地文件大小
        long localFileLength = LocalFileUtils.getLocalFileContentLength(localFilePath);

        // 获取http文件的总大小
        long httpFileContentLength = HttpFileCore.getHttpFileContentLength(url);

        // 判断文件在本地是否已存在
//        if (localFileLength >= httpFileContentLength) {
//            LogUtils.info("{}文件已存在！", localFilePath);
//            return;
//        }

        // 创建线程并执行，获取文件下载信息
        downloadInfoThread = new DownloadInfoThread(httpFileContentLength);
        scheduledExecutorService.scheduleAtFixedRate(downloadInfoThread, 1, 1, TimeUnit.SECONDS);

        // 多线程分块下载文件
        split(url, futureList);

        // 等待分块下载完成
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (merge(localFilePath)) {
            boolean cleared = clearTempFiles(localFilePath);
            if (!cleared) {
                LogUtils.error("临时文件删除失败，请手动删除！");
            }
        }

        System.out.print("\rFILE DOWNLOAD COMPLETE！");

        // 关闭线程
        scheduledExecutorService.shutdownNow();
        threadPoolExecutor.shutdownNow();
    }

    /**
     * @param url        要下载文件的url
     * @param futureList 分块文件下载后确认信息
     * @description 多线程分块下载文件方法
     * @date 2023/12/22 2:28
     */
    public void split(String url, List<Future<Boolean>> futureList) {
        // 通过url获取要下载文件的总大小
        long httpFileContentLength = HttpFileCore.getHttpFileContentLength(url);

        // 计算切分后的每块文件大小
        long size = httpFileContentLength / Constant.THREAD_NUM;

        long startPos = 0;
        long endPos;

        for (int i = 0; i < Constant.THREAD_NUM; i++) {
            if (i != 0) {
                // 计算下载起始位置
                startPos = i * size;
            }

            if (i == Constant.THREAD_NUM - 1) {
                // 此时，endPos设置为0表示最后一块
                endPos = 0;
            } else {
                // 不是最后一块
                endPos = startPos + size;
            }

            if (startPos != 0) {
                startPos++;
            }

            // 创建多线程下载任务
            MultiDownloadTask downloaderTask = new MultiDownloadTask(url, startPos, endPos, i, downloadInfoThread, countDownLatch);

            // 添加多线程下载任务到线程池，并获取任务执行状态
            Future<Boolean> submit = threadPoolExecutor.submit(downloaderTask);

            // 将任务执行状态添加到队列
            futureList.add(submit);
        }
    }

    /**
     * @param localFilePath 本地分块文件存储路径
     * @return boolean 是否合并成功
     * @description 分块文件合并
     * @date 2023/12/22 18:32
     */
    public boolean merge(String localFilePath) {
        LogUtils.info("\r分块下载完成，开始合并文件：{}", localFilePath);
        byte[] buffer = new byte[Constant.BYTE_ARRAY_LENGTH];
        int len;
        // 创建随机文件读取输出流
        try (RandomAccessFile raf = new RandomAccessFile(localFilePath, "rw")) {
            for (int i = 0; i < Constant.THREAD_NUM; i++) {
                try (// 创建每个分块的输入流
                     FileInputStream fis = new FileInputStream(LocalFileUtils.getLocalTempFilePath(localFilePath, i));
                     BufferedInputStream bis = new BufferedInputStream(fis)) {
                    // 输出文件到指定目录
                    while ((len = bis.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);
                    }
                } catch (FileNotFoundException e) {
                    LogUtils.error("指定文件不存在：{}, 合并失败!", localFilePath);
                }
            }
            LogUtils.info("\r文件合并完成！");
        } catch (Exception e) {
            LogUtils.error("文件合并出现错误, 暂停合并!");
            return false;
        }

        return true;
    }

    /**
     * @param localFilePath 文件存储路径
     * @return boolean 清理操作
     * @description 清理分块文件
     * @date 2023/12/22 18:34
     */
    public boolean clearTempFiles(String localFilePath) {
        for (int i = 0; i < Constant.THREAD_NUM; i++) {
            try {
                Files.delete(Paths.get(LocalFileUtils.getLocalTempFilePath(localFilePath, i)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }
}
