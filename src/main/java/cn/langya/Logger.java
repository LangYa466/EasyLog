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
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_LOG_FILE = "langya.log";
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);  // 用于日志生成的线程池
    private static final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>(1000);  // 日志队列，最多存储1000条日志
    private static LogLevel currentLogLevel = LogLevel.INFO;
    private static String logFilePath = DEFAULT_LOG_FILE;

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String BLUE = "\u001B[34m";


    private static Thread logWriterThread;  // 日志写入线程

    static {
        // 启动日志写入线程
        logWriterThread = new Thread(Logger::writeLogsToFile);
        logWriterThread.start();
    }

    public static void setNoWriteFile() {
        Logger.logWriterThread = null;
    }

    public static void setLogFilePath(String logFilePath) {
        Logger.logFilePath = logFilePath;
    }

    public static void setLogFile(String filePath) {
        logFilePath = filePath;
    }

    public static void setLogLevel(LogLevel level) {
        currentLogLevel = level;
    }

    public static void trace(String message, Object... args) {
        log(LogLevel.TRACE, BLUE, message, args);
    }

    public static void debug(String message, Object... args) {
        log(LogLevel.DEBUG, CYAN, message, args);
    }

    public static void info(String message, Object... args) {
        log(LogLevel.INFO, GREEN, message, args);
    }

    public static void warn(String message, Object... args) {
        log(LogLevel.WARN, YELLOW, message, args);
    }

    public static void error(String message, Object... args) {
        log(LogLevel.ERROR, RED, message, args);
    }

    private static void log(LogLevel level,String color, String message, Object... args) {
        if (level.getLevel() < currentLogLevel.getLevel()) {
            return;  // 如果当前日志级别低于设置的级别，则不输出
        }
        asyncPrint(level, color, message, args);
    }

    // 异步打印日志到控制台并放入队列
    private static void asyncPrint(LogLevel level, String color, String message, Object... args) {
        executor.submit(() -> {
            // 时间戳
            String timestamp = String.format("[%s]", LocalDateTime.now());

            // 日志类型
            String logLevel = String.format("[%s]", level);

            // 格式化消息内容
            String formattedMessage = String.format(message, args);

            // 最终的日志信息
            String finalMessage = String.format("%s%s %s %s %s", color, timestamp, logLevel, formattedMessage, RESET);  // 修正拼接

            try {
                logQueue.put(finalMessage);  // 将日志消息放入队列
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 输出到控制台
            System.out.println(finalMessage);
        });
    }

    // 专门的线程负责将日志写入文件
    private static void writeLogsToFile() {
        while (true) {
            try {
                String logMessage = logQueue.take();  // 从队列中取出日志
                writeToFile(logMessage);  // 写入文件
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;  // 线程中断，退出循环
            }
        }
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
        // 关闭线程池
        executor.shutdown();
        boolean fileWriter = logWriterThread != null;
        if (fileWriter) logWriterThread.interrupt();  // 中断日志写入线程

        try {
            // 等待线程池关闭
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }

            // 等待日志写入线程完成
            if (fileWriter) logWriterThread.join();
        } catch (InterruptedException e) {
            executor.shutdownNow();
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
