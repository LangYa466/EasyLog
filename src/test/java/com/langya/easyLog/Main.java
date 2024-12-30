package com.langya.easyLog;

import cn.langya.Logger;

/**
 * @author LangYa
 * @since 2024/12/27 07:16
 */
public class Main {
    public static void main(String[] args) {
        Logger.setLogLevel(Logger.LogLevel.DEBUG);
        Logger.setLogFile("mylog.log");
        Logger.info("Application started");
        Logger.debug("Debugging variable x={ }, y={ }", 42, 84);
        Logger.warn("This is a warning!");
        Logger.error("An error occurred: { }", "File not found");

        try {
            throw 
                    new RuntimeException("Test Exception");
        } catch (Exception e) {
            Logger.error("Caught exception", e);
        }
    }
}
