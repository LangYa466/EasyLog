package cn.langya;

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
    public static final BlockingQueue<String> logQueue = new LinkedTransferQueue<>();  // 使用无界队列
    private static final LogWriter logWriter;
    private static String logFile = DEFAULT_LOG_FILE;
    private static LogLevel currentLogLevel = LogLevel.INFO;

    static {
        // 启动日志处理线程
        logWriter = new LogWriter(DEFAULT_LOG_FILE);
        executor.execute(logWriter);
    }

    public static void setLogFile(String filePath) {
        logFile = filePath;
        logWriter.updateLogFile(filePath);  // 更新日志文件路径
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
        String logMessage = String.format("[%s] [%s] %s", LocalDateTime.now(), level, String.format(message, args));
        try {
            logQueue.put(logMessage);  // 将日志消息放入队列
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void shutdown() {
        logWriter.stopWriting();  // 停止日志写入线程
        executor.shutdown();  // 关闭线程池
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
