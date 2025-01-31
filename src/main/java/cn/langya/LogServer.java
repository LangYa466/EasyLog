package cn.langya;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author LangYa
 * @since 2024/12/27
 */
public class LogServer {
    private static final int PORT = 46666;
    private static volatile boolean running = true;
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void init() {
        Thread thread = new Thread(LogServer::start);
        thread.setDaemon(true);
        thread.start();
    }

    public static void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            threadPool.shutdown();
        }));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (running) {
                if (!threadPool.isShutdown()) {  // Ensure thread pool is not shut down before accepting a client
                    Socket clientSocket = serverSocket.accept();
                    if (running) {
                        threadPool.execute(() -> handleClient(clientSocket));
                    } else {
                        clientSocket.close();  // Close the socket if server is shutting down
                    }
                } else {
                    break;  // Exit if the thread pool is shut down
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = Files.newBufferedWriter(Paths.get(Logger.logFilePath),
                     java.nio.file.StandardOpenOption.CREATE,
                     java.nio.file.StandardOpenOption.APPEND)) {
            String log;
            while ((log = reader.readLine()) != null) {
                writer.write(log);
                writer.newLine();
                writer.flush();
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
}
