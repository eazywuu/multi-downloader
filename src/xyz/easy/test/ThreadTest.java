package xyz.easy.test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author will_wu
 */
public class ThreadTest {

    // 创建线程任务，数量为1
    public static final ScheduledExecutorService SEC = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) {
        // 计划固定率,如果任务本身执行时间超过间隔执行时间period，则period无效
        SEC.scheduleAtFixedRate(() -> {
            try {
                TimeUnit.SECONDS.sleep(6);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 2, 3, TimeUnit.SECONDS);

        // 计划固定延迟，如果任务本身执行时间超过间隔执行时间period，任然严格再等待时间period
        SEC.scheduleWithFixedDelay(() -> {
            try {
                TimeUnit.SECONDS.sleep(6);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 2, 3, TimeUnit.SECONDS);
    }

    private static void scheduled() {

        SEC.schedule(() -> System.out.println(Thread.currentThread().getName()), 2, TimeUnit.SECONDS);

        SEC.shutdown();
    }
}
