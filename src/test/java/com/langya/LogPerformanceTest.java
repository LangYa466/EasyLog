package com.langya;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author LangYa
 * @since 2025/1/2
 */
public class LogPerformanceTest {
    private static final Logger log4jLogger = LogManager.getLogger(LogPerformanceTest.class);

    public static void main(String[] args) {
        int logCount = 1000000;

        // Measure EasyLog performance
        long startTimeEasyLog = System.nanoTime();
        for (int i = 0; i < logCount; i++) {
            cn.langya.Logger.info("Test message {}", i);
        }
        long endTimeEasyLog = System.nanoTime();

        // Measure log4j performance
        long startTimeLog4j = System.nanoTime();
        for (int i = 0; i < logCount; i++) {
            log4jLogger.info("Test message {}", i);
        }
        long endTimeLog4j = System.nanoTime();

        // Measure TinyLog performance
        long startTimeTinyLog = System.nanoTime();
        for (int i = 0; i < logCount; i++) {
            org.tinylog.Logger.info("Test message {}", i);
        }
        long endTimeTinyLog = System.nanoTime();

        System.out.println("EasyLog Time: " + (endTimeEasyLog - startTimeEasyLog) / 1_000_000 + " ms");
        System.out.println("Log4j Time: " + (endTimeLog4j - startTimeLog4j) / 1_000_000 + " ms");
        System.out.println("TinyLog Time: " + (endTimeTinyLog - startTimeTinyLog) / 1_000_000 + " ms");
        System.exit(0);
    }
}
