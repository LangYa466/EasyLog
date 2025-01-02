package cn.langya;

import java.io.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.*;

/**
 * @author LangYa466
 * @since 2025/1/2
 */
public class LogWriter implements Runnable {
    private FileChannel fileChannel;
    private final BlockingQueue<String> logQueue;
    private volatile boolean running = true;
    private static final int BATCH_SIZE = 1000;  // 每次写入的批量日志条数
    private final List<String> batchLogs = new ArrayList<>(BATCH_SIZE);  // 批量缓存日志
    private final ExecutorService executorService;
    private final ExecutorService writeExecutorService;  // 用于处理日志写入的线程池

    public LogWriter(String logFile) {
        this.logQueue = Logger.logQueue;
        this.executorService = Executors.newFixedThreadPool(4);  // 使用线程池来执行批量任务
        this.writeExecutorService = Executors.newCachedThreadPool();  // 为每个日志写入任务分配独立线程
        // 初始化文件通道
        initializeFileChannel(logFile);
    }

    @Override
    public void run() {
        while (running) {
            try {
                // 收集日志消息，直到达到批量大小
                batchLogs.clear();
                while (!logQueue.isEmpty() && batchLogs.size() < BATCH_SIZE) {
                    String logMessage = logQueue.poll(10, TimeUnit.MILLISECONDS);  // 超时获取
                    if (logMessage != null) {
                        batchLogs.add(logMessage);
                    }
                }

                // 批量写入日志
                if (!batchLogs.isEmpty()) {
                    StringBuilder logBatch = new StringBuilder();
                    for (String logMessage : batchLogs) {
                        logBatch.append(logMessage).append(System.lineSeparator());
                    }
                    ByteBuffer buffer = ByteBuffer.wrap(logBatch.toString().getBytes(StandardCharsets.UTF_8));

                    // 使用反射调用异步写入日志方法
                    Method writeMethod = this.getClass().getDeclaredMethod("writeLog", ByteBuffer.class);
                    writeMethod.setAccessible(true);
                    writeMethod.invoke(this, buffer);  // 反射调用
                }

            } catch (InterruptedException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopWriting() {
        running = false;
        try {
            fileChannel.close();  // 关闭文件通道
        } catch (IOException e) {
            System.err.println("Error closing log file: " + e.getMessage());
        }
        executorService.shutdown();  // 关闭执行器
        writeExecutorService.shutdown();  // 关闭写入执行器
    }

    // 更新日志文件路径
    public void updateLogFile(String filePath) {
        try {
            // 关闭现有的 fileChannel
            if (fileChannel != null) {
                fileChannel.close();
            }
            // 初始化新的 fileChannel
            initializeFileChannel(filePath);
        } catch (IOException e) {
            System.err.println("Error updating log file: " + e.getMessage());
        }
    }

    // 初始化文件通道
    private void initializeFileChannel(String logFile) {
        Path path = Paths.get(logFile);
        try {
            fileChannel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 使用反射写入日志
    private void writeLog(ByteBuffer buffer) {
        // 通过反射来控制是否同步写入
        try {
            synchronized (fileChannel) {  // 保证文件写入操作是线程安全的
                fileChannel.write(buffer);
            }
        } catch (IOException e) {
            System.err.println("Error writing log asynchronously: " + e.getMessage());
        }
    }
}
