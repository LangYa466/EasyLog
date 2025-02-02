package cn.langya;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.concurrent.*;

/**
 * @author LangYa466
 * @since 2024/12/27
 */
public class LogServer {
    private static final int PORT = 46666;
    private static volatile boolean running = true;

    // 线程池（限制最大 16 个线程，避免无限增长）
    private static final ExecutorService threadPool = new ThreadPoolExecutor(
            4, 16, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
    );

    // 日志写入队列（使用阻塞队列避免同步 IO 瓶颈）
    private static final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>(10000);

    public static void init() {
        Thread thread = new Thread(LogServer::start);
        thread.setDaemon(true);
        thread.start();

        // 启动异步日志写入线程
        startLogWriter();
    }

    public static void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            threadPool.shutdown();
        }));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (running) {
                Socket clientSocket = serverSocket.accept();
                if (running) {
                    threadPool.execute(() -> handleClient(clientSocket));
                } else {
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String log;
            while ((log = reader.readLine()) != null) {
                if (!logQueue.offer(log)) {
                    System.err.println("日志队列已满，丢弃日志: " + log);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 异步日志写入线程
    private static void startLogWriter() {
        new Thread(() -> {
            try (BufferedWriter writer = Files.newBufferedWriter(
                    Paths.get(Logger.logFilePath),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {
                while (running || !logQueue.isEmpty()) {
                    String log = logQueue.poll(1, TimeUnit.SECONDS);
                    if (log != null) {
                        writer.write(log);
                        writer.newLine();
                        writer.flush();
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }, "LogWriter-Thread").start();
    }

    public static void shutdown() {
        running = false; // 标记服务器停止
        try {
            // 关闭线程池
            threadPool.shutdown();
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow(); // 如果线程池在指定时间内没有停止，则强制关闭
            }

            // 等待日志写入线程完成
            while (!logQueue.isEmpty()) {
                Thread.sleep(100); // 等待日志队列清空
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
