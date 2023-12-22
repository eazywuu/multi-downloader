package xyz.easy.core;

import xyz.easy.constant.Constant;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author Ethan Wu
 * @description 展示文件下载相关信息
 * @date 2023/12/20 1:50
 */
public class DownloadInfoThread implements Runnable {
    // 下载文件总大小
    private final long httpFileContentLength;

    // 本地已下载文件大小
    private final LongAdder finishedSize;

    // 本地累计文件下载大小,强制主线程读取
    private final LongAdder curSize;

    // 前一次文件下载大小
    private double preSize;

    public DownloadInfoThread(long httpFileContentLength) {
        this.httpFileContentLength = httpFileContentLength;
        this.finishedSize = new LongAdder();
        this.curSize = new LongAdder();

    }

    @Override
    public void run() {
        // 每秒下载文件大小
        double sizePerSecond = curSize.doubleValue() - preSize;

        // 剩余文件大小
        double remainSize = httpFileContentLength - finishedSize.doubleValue() - curSize.doubleValue();

        // 单位换算：kb
        int speed = (int) (sizePerSecond / Constant.KB);
        preSize = curSize.doubleValue();

        // 文件总大小，单位：mb
        String fileTotalSize = String.format("%.2f", httpFileContentLength / Constant.MB);

        // 计算剩余时间，单位s
        String remainTime = String.format("%.1f s", remainSize / speed / Constant.KB);

        // 判断时间是否为无穷大
        if ("Infinity".equalsIgnoreCase(remainTime)) {
            remainTime = "∞";
        }

        // 已下载文件大小
        String downloadedSize = String.format("%.2f", (curSize.doubleValue() - finishedSize.doubleValue()) / Constant.MB);

        // 控制台输出
        String downloadInfo = String.format("已下载 %s MB/%s MB, 速度 %s KB/s, 剩余时间 %s", downloadedSize, fileTotalSize, speed, remainTime);
        System.out.print("\r" + downloadInfo);
    }

    public void setCurSize(long curSize) {
        this.curSize.add(curSize);
    }
}
