package cn.langya;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author LangYa
 * @since 2024/12/27
 */
public class Logger {
    private static final String DEFAULT_LOG_FILE = "langya.log";
    private static LogLevel currentLogLevel = LogLevel.INFO;
    public static String logFilePath = DEFAULT_LOG_FILE;
    private static final String LOG_SERVER_HOST = "localhost";
    private static final int LOG_SERVER_PORT = 46666;
    private static String format = "{}";
    private static boolean hasColorInfo;
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String BLUE = "\u001B[34m";

    private static boolean writeFile;
    private static final ExecutorService logExecutor = Executors.newSingleThreadExecutor();

    static {
        LogServer.init();
        TimeUpdater.init();
    }

    public static void setHasColorInfo(boolean hasColorInfo) {
        Logger.hasColorInfo = hasColorInfo;
    }

    public static void setFormat(String format) {
        Logger.format = format;
    }

    public static void setWriteFile(boolean isWrite) {
        Logger.writeFile = isWrite;
    }

    public static void setLogFilePath(String logFilePath) {
        Logger.logFilePath = logFilePath;
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

    private static void log(LogLevel level, String color, String message, Object... args) {
        if (level.getLevel() < currentLogLevel.getLevel()) {
            return;
        }
        asyncPrint(level, color, message, args);
    }

    private static String formatMessage(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }
        return String.format(message.replace(format, "%s"), args);
    }

    private static void asyncPrint(LogLevel level, String color, String message, Object... args) {
        String timestamp = TimeUpdater.getCurrentTime();
        String logLevel = level.toString();
        String formattedMessage = formatMessage(message, args); // 直接格式化日志消息
        String threadName = Thread.currentThread().getName();

        StringBuilder finalMessage = new StringBuilder(256);
        if (hasColorInfo) {
            finalMessage.append(color)
                    .append('[').append(timestamp).append(']')
                    .append("[").append(threadName).append("] [").append(logLevel).append("]: ")
                    .append(formattedMessage).append(RESET);
        } else {
            finalMessage.append('[').append(timestamp).append(']')
                    .append("[").append(threadName).append("] [").append(logLevel).append("]: ")
                    .append(formattedMessage);
        }

        // 异步发送到日志服务器
        if (writeFile) {
            logExecutor.submit(() -> sendToLogServer(finalMessage.toString(), color));
        }

        // 最终打印日志
        System.out.println(finalMessage);
    }

    private static void sendToLogServer(String message, String color) {
        try (Socket socket = new Socket(LOG_SERVER_HOST, LOG_SERVER_PORT);
             OutputStream outputStream = socket.getOutputStream()) {
            // 如果不需要颜色信息，可以直接拼接清理后的日志信息
            String logMessage = hasColorInfo ? message : message.replace(color, "").replace(RESET, "");
            outputStream.write((logMessage + "\n").getBytes());
            outputStream.flush();
        } catch (IOException e) {
            System.err.println("Log server connection failed: " + e.getMessage());
        }
    }

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
