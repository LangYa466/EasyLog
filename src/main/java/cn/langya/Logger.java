package cn.langya;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.*;

/**
 * @author LangYa
 * @since 2024/12/27
 */
public class Logger {
    private static final String DEFAULT_LOG_FILE = "langya.log";
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);  // 使用多线程执行器
    private static final ExecutorService printExecutor = Executors.newFixedThreadPool(8);  // 用于异步打印日志的线程池
    public static final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();  // 使用阻塞队列
    private static LogLevel currentLogLevel = LogLevel.INFO;
    private static String logFilePath = DEFAULT_LOG_FILE;

    public static void setLogFile(String filePath) {
        logFilePath = filePath;
    }

    public static void setLogLevel(LogLevel level) {
        currentLogLevel = level;
    }

    public static void trace(String message, Object... args) {
        log(LogLevel.TRACE, message, args);
    }

    public static void debug(String message, Object... args) {
        log(LogLevel.DEBUG, message, args);
    }

    public static void info(String message, Object... args) {
        log(LogLevel.INFO, message, args);
    }

    public static void warn(String message, Object... args) {
        log(LogLevel.WARN, message, args);
    }

    public static void error(String message, Object... args) {
        log(LogLevel.ERROR, message, args);
    }

    private static void log(LogLevel level, String message, Object... args) {
        if (level.getLevel() < currentLogLevel.getLevel()) {
            return;  // 如果当前日志级别低于设置的级别，则不输出
        }
        asyncPrint(level, message, args);
    }

    // 异步打印日志到控制台并写入文件
    private static void asyncPrint(LogLevel level, String message, Object... args) {
        printExecutor.submit(() -> {
            // 时间戳
            String timestamp = String.format("[%s]", LocalDateTime.now());

            // 日志类型
            String logLevel = String.format("[%s]", level);

            // 格式化消息内容
            String formattedMessage = String.format(message, args);

            // 最终的日志信息
            String finalMessage = String.format("%s %s %s", timestamp, logLevel, formattedMessage);

            try {
                logQueue.put(finalMessage);  // 将日志消息放入队列
                writeToFile(finalMessage);  // 写入文件
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 输出到控制台
            System.out.println(finalMessage);
        });
    }

    // 将日志写入文件
    private static void writeToFile(String message) {
        File logFile = new File(logFilePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    public static void shutdown() {
        executor.shutdown();  // 关闭线程池
        printExecutor.shutdown();  // 关闭打印线程池
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            if (!printExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                printExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            printExecutor.shutdownNow();
        }
    }

    // 日志级别枚举
    public enum LogLevel {
        TRACE(0), DEBUG(1), INFO(2), WARN(3), ERROR(4);

        private final int level;

        LogLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }
}
