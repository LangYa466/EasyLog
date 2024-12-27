package cn.langya;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author LangYa
 * @since 2024/12/27 07:14
 */
public class Logger {
    private static final File DEFAULT_LOG_FILE = new File("langya.log");
    private static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String RESET = "\u001B[0m";

    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";

    private static File logFile = DEFAULT_LOG_FILE;

    public static void setDateFormat(String dateFormat) {
        DATE_FORMAT = dateFormat;
    }

    public static void setLogFile(File file) {
        logFile = file;
    }

    public static void setLogFile(String filePath) {
        logFile = new File(filePath);
    }

    public static void info(String message, Object... args) {
        log("INFO", GREEN, message, args);
    }

    public static void debug(String message, Object... args) {
        log("DEBUG", CYAN, message, args);
    }

    public static void warn(String message, Object... args) {
        log("WARN", YELLOW, message, args);
    }

    public static void error(String message, Object... args) {
        log("ERROR", RED, message, args);
    }

    public static void error(String message, Throwable throwable, Object... args) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();
        log("ERROR", RED, message + "\n" + stackTrace, args);
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
        return String.format(message.replace("{}", "%s"), args);
    }

    private static void writeToFile(String message) {
        try {
            Files.write(logFile.toPath(), (message + "\n").getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to write log to file: " + e.getMessage());
        }
    }
}
