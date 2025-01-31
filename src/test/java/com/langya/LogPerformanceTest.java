package com.langya;

import cn.langya.Logger;

/**
 * @author LangYa
 * @since 2025/1/2
 */
public class LogPerformanceTest {
    public static void main(String[] args) {
        int logCount = 100;
        int testRounds = 2;

        Logger.setHasColorInfo(false);
        Logger.setWriteFile(true);

        long[] tinyLogTimes = new long[testRounds];
        long[] easyLogTimes = new long[testRounds];

        for (int round = 0; round < testRounds; round++) {
            // Measure TinyLog performance
            long startTimeTinyLog = System.nanoTime();
            for (int i = 0; i < logCount; i++) {
                org.tinylog.Logger.info("Test message {}", i);
            }
            long endTimeTinyLog = System.nanoTime();
            long tinyLogTime = endTimeTinyLog - startTimeTinyLog;
            tinyLogTimes[round] = tinyLogTime;

            // Measure EasyLog performance
            long startTimeEasyLog = System.nanoTime();
            for (int i = 0; i < logCount; i++) {
                cn.langya.Logger.info("Test message {}", i);
            }
            long endTimeEasyLog = System.nanoTime();
            long easyLogTime = endTimeEasyLog - startTimeEasyLog;
            easyLogTimes[round] = easyLogTime;
        }

        // Print the results for each round
        for (int round = 0; round < testRounds; round++) {
            System.out.println("Round " + (round + 1) + " - TinyLog Time: " + tinyLogTimes[round] / 1_000_000 + " ms");
            System.out.println("Round " + (round + 1) + " - EasyLog Time: " + easyLogTimes[round] / 1_000_000 + " ms");
        }

        // Calculate the average times
        long totalTinyLogTime = 0;
        long totalEasyLogTime = 0;
        for (int i = 0; i < testRounds; i++) {
            totalTinyLogTime += tinyLogTimes[i];
            totalEasyLogTime += easyLogTimes[i];
        }

        // Calculate and print the average times
        long averageTinyLogTime = totalTinyLogTime / testRounds;
        long averageEasyLogTime = totalEasyLogTime / testRounds;

        System.out.println("\nAverage TinyLog Time: " + averageTinyLogTime / 1_000_000 + " ms");
        System.out.println("Average EasyLog Time: " + averageEasyLogTime / 1_000_000 + " ms");

        System.exit(0);
    }
}
