package cn.langya;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日志记录器类，用于记录不同级别的日志信息。
 *
 * @author LangYa
 * @since 2024/12/27
 */
public class Logger {
    private static final String DEFAULT_LOG_FILE = "langya.log";
    private static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String BLUE = "\u001B[34m"; // 新增：用于TRACE日志的颜色

    private static String logFile = DEFAULT_LOG_FILE;
    private static String format = "{}";
    private static LogLevel currentLogLevel = LogLevel.INFO; // 默认日志等级为INFO

    /**
     * 设置日志格式。
     *
     * @param format 日志格式字符串
     */
    public static synchronized void setFormat(String format) {
        Logger.format = format;
    }

    /**
     * 设置日志文件路径。
     *
     * @param filePath 日志文件路径
     */
    public static synchronized void setLogFile(String filePath) {
        logFile = filePath;
    }

    /**
     * 设置日期格式。
     *
     * @param dateFormat 日期格式字符串
     */
    public static synchronized void setDateFormat(String dateFormat) {
        DATE_FORMAT = dateFormat;
    }

    /**
     * 设置当前日志等级。
     *
     * @param level 日志等级
     */
    public static synchronized void setLogLevel(LogLevel level) {
        currentLogLevel = level;
    }

    /**
     * TRACE日志，用于详细的调试信息。
     */
    public static void trace(String message, Object... args) {
        if (currentLogLevel.ordinal() <= LogLevel.TRACE.ordinal()) {
            log("TRACE", BLUE, message, args);
        }
    }

    public static void info(String message, Object... args) {
        if (currentLogLevel.ordinal() <= LogLevel.INFO.ordinal()) {
            log("INFO", GREEN, message, args);
        }
    }

    public static void debug(String message, Object... args) {
        if (currentLogLevel.ordinal() <= LogLevel.DEBUG.ordinal()) {
            log("DEBUG", CYAN, message, args);
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
        writeToFile(logMessage);
    }

    private static String format(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }
        return String.format(message.replace(format, "%s"), args); // 使用 String.format 进行替换
    }

    private static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static void writeToFile(String message) {
        int retryCount = 3;
        while (retryCount > 0) {
            try {
                Files.write(Paths.get(logFile), (message + "\n").getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                break; // 写入成功，退出循环
            } catch (IOException e) {
                retryCount--;
                if (retryCount == 0) {
                    System.err.println("Failed to write log to file after retries: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 日志等级枚举
     */
    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
}
