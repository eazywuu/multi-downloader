package xyz.easy;

import xyz.easy.core.Downloader;
import xyz.easy.util.LogUtils;

import java.util.Scanner;

/**
 * @author will_wu
 */
public class Main {
    public static void main(String[] args) {
        String url = null;

        if (args == null || args.length == 0) {
            for ( ; ;) {
                LogUtils.info("请输入下载链接：");
                Scanner scanner = new Scanner(System.in);
                url = scanner.next();
                if (url != null) {
                    break;
                }

            }
        }else {
            url = args[0];
        }

        Downloader downloader = new Downloader();
        downloader.download(url);
    }
}