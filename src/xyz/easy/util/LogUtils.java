package xyz.easy.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author will_wu
 */
public class LogUtils {

    public static void info(String msg, Object... args) {
        print(msg, "-INFO-", args);
    }

    public static void error(String msg, Object... args) {
        print(msg, "-ERROR-", args);
    }

    private static void print(String msg, String level, Object... args) {
        if (args != null && args.length > 0) {
            msg = String.format(msg.replace("{}", "%s"), args);
        }


        // 获取当前的日期和时间
        LocalDateTime currentDateTime = LocalDateTime.now();

        // 使用 DateTimeFormatter 格式化日期和时间，可指定使用 IsoChronology
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 格式化日期和时间，输出结果
        String formattedDateTime = currentDateTime.format(formatter);

        // 获取当前线程名称
        String name = Thread.currentThread().getName();

        System.out.println(formattedDateTime + " " + name + level + msg);
    }
}
