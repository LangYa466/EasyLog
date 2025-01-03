package com.langya;

/**
 * @author LangYa
 * @since 2025/1/2
 */
public class LogPerformanceTest {
    public static void main(String[] args) {
        int logCount = 1000000;

        // Measure EasyLog performance
        long startTimeEasyLog = System.nanoTime();
        for (int i = 0; i < logCount; i++) {
            cn.langya.Logger.info("Test message {}", i);
        }
        cn.langya.Logger.shutdown();
        long endTimeEasyLog = System.nanoTime();

        // Measure TinyLog performance
        long startTimeTinyLog = System.nanoTime();
        for (int i = 0; i < logCount; i++) {
            org.tinylog.Logger.info("Test message {}", i);
        }
        long endTimeTinyLog = System.nanoTime();

        System.out.println("EasyLog Time: " + (endTimeEasyLog - startTimeEasyLog) / 1_000_000 + " ms");
        System.out.println("TinyLog Time: " + (endTimeTinyLog - startTimeTinyLog) / 1_000_000 + " ms");
        System.exit(0);
    }
}
