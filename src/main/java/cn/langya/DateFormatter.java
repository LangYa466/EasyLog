package cn.langya;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

import static cn.langya.Logger.DATE_FORMAT;

/**
 * @author LangYa466
 * @since 2025/1/2
 */
public class DateFormatter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final ExecutorService executorService = Executors.newFixedThreadPool(8);  // 线程池大小根据需求调整

    // 格式化当前时间为字符串，异步执行
    public static Future<String> formatAsync(LocalDateTime dateTime) {
        return executorService.submit(() -> dateTime.format(DATE_FORMATTER));
    }

    // 解析字符串为LocalDateTime，异步执行
    public static Future<LocalDateTime> parseAsync(String dateStr) {
        return executorService.submit(() -> LocalDateTime.parse(dateStr, DATE_FORMATTER));
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 测试异步格式化和解析
        LocalDateTime now = LocalDateTime.now();
        Future<String> formattedDateFuture = formatAsync(now);
        System.out.println("Formatted Date: " + formattedDateFuture.get());

        String formattedDate = formattedDateFuture.get();
        Future<LocalDateTime> parsedDateFuture = parseAsync(formattedDate);
        System.out.println("Parsed Date: " + parsedDateFuture.get());
    }
}
