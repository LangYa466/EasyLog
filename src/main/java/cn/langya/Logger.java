package cn.langya;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author LangYa
 * @since 2024/12/27
 */
public class Logger {
    private static final String DEFAULT_LOG_FILE = "langya.log";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String BLUE = "\u001B[34m";

    private static String logFile = DEFAULT_LOG_FILE;
    private static LogLevel currentLogLevel = LogLevel.INFO;
    private static final ConcurrentLinkedQueue<String> logQueue = new ConcurrentLinkedQueue<>();
    private static final ExecutorService logExecutor = Executors.newSingleThreadExecutor();

    static {
        logExecutor.submit(Logger::processLogQueue);
    }

    /**
     * 设置日志文件路径。
     *
     * @param filePath 日志文件路径
     */
    public static void setLogFile(String filePath) {
        logFile = filePath;
    }

    /**
     * 设置当前日志等级。
     *
     * @param level 日志等级
     */
    public static void setLogLevel(LogLevel level) {
        currentLogLevel = level;
    }

    public static void trace(String message, Object... args) {
        if (currentLogLevel.ordinal() <= LogLevel.TRACE.ordinal()) {
            log("TRACE", BLUE, message, args);
        }
    }

    public static void debug(String message, Object... args) {
        if (currentLogLevel.ordinal() <= LogLevel.DEBUG.ordinal()) {
            log("DEBUG", CYAN, message, args);
        }
    }

    public static void info(String message, Object... args) {
        if (currentLogLevel.ordinal() <= LogLevel.INFO.ordinal()) {
            log("INFO", GREEN, message, args);
        }
    }

    public static void warn(String message, Object... args) {
        if (currentLogLevel.ordinal() <= LogLevel.WARN.ordinal()) {
            log("WARN", YELLOW, message, args);
        }
    }

    public static void error(String message, Object... args) {
        if (currentLogLevel.ordinal() <= LogLevel.ERROR.ordinal()) {
            log("ERROR", RED, message, args);
        }
    }

    public static void error(String message, Throwable throwable, Object... args) {
        if (currentLogLevel.ordinal() <= LogLevel.ERROR.ordinal()) {
            String stackTrace = getStackTrace(throwable);
            log("ERROR", RED, message + "\n" + stackTrace, args);
        }
    }

    private static void log(String level, String color, String message, Object... args) {
        String formattedMessage = format(message, args);
        String timestamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        String logMessage = String.format("[%s] [%s] %s", timestamp, level, formattedMessage);
        System.out.println(color + logMessage + RESET);
        logQueue.add(logMessage);
    }

    private static String format(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }
        return String.format(message, args);
    }

    private static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static void processLogQueue() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            while (true) {
                String logMessage;
                while ((logMessage = logQueue.poll()) != null) {
                    writer.write(logMessage);
                    writer.newLine();
                }
                writer.flush();
                Thread.sleep(100); // 减少磁盘 I/O 频率
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Logger encountered an error: " + e.getMessage());
        }
    }

    /**
     * 日志等级枚举
     */
    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
}
