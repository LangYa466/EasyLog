package com.langya;

import cn.langya.Logger;

/**
 * @author LangYa
 * @since 2024/12/27 07:16
 */
public class Main {
    public static void main(String[] args) {
        // 设置日志文件路径和日志级别
        Logger.setLogFilePath("mylog.log");
        Logger.setLogLevel(Logger.LogLevel.DEBUG);

        // 测试日志
        Logger.info("Application started");
        Logger.debug("Debugging info");
        Logger.warn("This is a warning");
        Logger.error("An error occurred");

        Logger.shutdown();
    }
}
