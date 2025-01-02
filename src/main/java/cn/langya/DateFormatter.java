package cn.langya;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static cn.langya.Logger.DATE_FORMAT;

/**
 * @author LangYa466
 * @since 2025/1/2
 */
public class DateFormatter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    // 格式化当前时间为字符串
    public static String format(LocalDateTime dateTime) {
        return dateTime.format(DATE_FORMATTER);
    }

    // 解析字符串为LocalDateTime
    public static LocalDateTime parse(String dateStr) {
        return LocalDateTime.parse(dateStr, DATE_FORMATTER);
    }

    public static void main(String[] args) {
        // 测试格式化和解析
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = format(now);
        System.out.println("Formatted Date: " + formattedDate);

        LocalDateTime parsedDate = parse(formattedDate);
        System.out.println("Parsed Date: " + parsedDate);
    }
}
