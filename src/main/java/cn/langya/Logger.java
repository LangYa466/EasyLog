package cn.langya;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author LangYa
 * @since 2024/12/27
 */
public class Logger {
    private static final String DEFAULT_LOG_FILE = "langya.log";
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final int INITIAL_BUFFER_SIZE = 1024 * 1024; // 初始 1MB 缓冲区大小
    private static final int THREAD_COUNT = 4; // 使用的线程数量

    private static String logFile = DEFAULT_LOG_FILE;
    private static LogLevel currentLogLevel = LogLevel.INFO;
    private static final ConcurrentLinkedQueue<String> logQueue = new ConcurrentLinkedQueue<>();
    private static final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    private static MappedByteBuffer[] buffers;
    private static long[] filePositions;
    private static FileChannel fileChannel;

    static {
        initializeBuffer();
        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.execute(() -> processLogQueue(Thread.currentThread().getName()));
        }
    }

    private static void initializeBuffer() {
        try (RandomAccessFile file = new RandomAccessFile(logFile, "rw")) {
            fileChannel = file.getChannel();
            buffers = new MappedByteBuffer[THREAD_COUNT];
            filePositions = new long[THREAD_COUNT];
            for (int i = 0; i < THREAD_COUNT; i++) {
                filePositions[i] = fileChannel.size() + i * INITIAL_BUFFER_SIZE;
                buffers[i] = fileChannel.map(FileChannel.MapMode.READ_WRITE, filePositions[i], INITIAL_BUFFER_SIZE);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize log buffers: " + e.getMessage(), e);
        }
    }

    public static void setLogFile(String filePath) {
        logFile = filePath;
        initializeBuffer();
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
        if (currentLogLevel.ordinal() > level.ordinal()) {
            return;
        }
        String timestamp = DateFormatter.format(LocalDateTime.now());
        String logMessage = String.format("[%s] [%s] %s", timestamp, level, format(message, args));
        logQueue.add(logMessage);
    }

    private static String format(String message, Object... args) {
        return String.format(message, args);
    }

    private static void processLogQueue(String threadName) {
        int threadIndex = Integer.parseInt(threadName.split("-")[1]) % THREAD_COUNT;
        MappedByteBuffer threadBuffer = buffers[threadIndex];

        while (true) {
            String logMessage;
            while ((logMessage = logQueue.poll()) != null) {
                writeToBuffer(threadBuffer, logMessage + "\n", threadIndex);
            }
            try {
                Thread.sleep(1); // 避免空循环占用 CPU
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private static synchronized void writeToBuffer(MappedByteBuffer buffer, String message, int threadIndex) {
        try {
            byte[] bytes = message.getBytes();
            if (buffer.remaining() < bytes.length) {
                buffer.force();
                filePositions[threadIndex] += buffer.position();
                buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, filePositions[threadIndex], Math.max(INITIAL_BUFFER_SIZE, bytes.length));
                buffers[threadIndex] = buffer; // 更新线程缓冲区
            }
            buffer.put(bytes);
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }

    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
}
