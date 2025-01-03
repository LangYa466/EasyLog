# EasyLog

EasyLog 是一个适合高并发项目的日志框架的 Java 日志库、格式化消息、日志文件记录等功能，适用于 Java 1.8。

## 功能特点

- **日志级别**：支持 `INFO`、`DEBUG`、`WARN` 和 `ERROR`。
- **时间戳**：日志消息包含详细的时间戳（格式：`yyyy-MM-dd HH:mm:ss`）。
- **日志文件**：默认输出到 `langya.log`，支持自定义文件路径。
- **格式化消息**：支持 `{}` 占位符，用于动态插入变量。
- **异常堆栈**：可记录完整的异常堆栈信息。

### 引入库

[云端仓库](https://jitpack.io/#LangYa466/EasyLog/1.0) 或 [Github](https://github.com/LangYa466/EasyLog/releases)

### 输出示例

自定义日志等级
```java
Logger.setLogLevel(LogLevel.WARN);
```

自定义日期格式(默认 yyyy-MM-dd HH:mm:ss)
```java
Logger.setDateFormat("yyyy-MM-dd HH:mm");
```

控制台输出：
![image](https://github.com/user-attachments/assets/981d3b7f-3902-45fa-8d64-d30e016a8627)


默认日志文件输出
`langya.log`：

```
[2024-12-27 12:00:00] [INFO] Application started
[2024-12-27 12:00:01] [DEBUG] Debugging variable x=42, y=84
[2024-12-27 12:00:02] [WARN] This is a warning!
[2024-12-27 12:00:03] [ERROR] An error occurred: File not found
```

## 使用方法

### 设置日志文件路径

```java
Logger.setLogFile("filePath");
Logger.setLogFile(new File("filePath"));
```

### 记录日志

#### INFO
```java
Logger.info("Message", Object... args);
```
记录普通信息。

#### DEBUG
```java
Logger.debug("Message", Object... args);
```
记录调试信息

#### WARN
```java
Logger.warn("Message", Object... args);
```
记录警告信息

#### ERROR
```java
Logger.error("Message", Object... args);
```
记录错误信息

#### 记录异常
```java
Logger.error("Message", Throwable throwable, Object... args);
```
记录错误信息并附加异常堆栈。

## 许可证

此项目采用 MIT 许可证开源。
