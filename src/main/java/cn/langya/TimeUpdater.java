package cn.langya;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author LangYa466
 * @since 2/1/2025
 */
public class TimeUpdater {
    public static void init() {
        // 创建一个定时任务调度线程池
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // 定义每秒执行一次的任务
        Runnable timeTask = () -> {
            // 获取当前时间
            String currentTime = getCurrentTime();
            System.out.println(currentTime); // 输出时间
        };

        // 每秒更新一次时间
        scheduler.scheduleAtFixedRate(timeTask, 0, 1, TimeUnit.SECONDS);
    }

    // 获取当前时间并格式化为指定格式
    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }
}
