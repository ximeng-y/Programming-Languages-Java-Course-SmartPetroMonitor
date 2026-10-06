/*
 * 文件名：AlarmHistoryStats.java
 * 程序功能：解析并验证监测日志，统计有效监测次数、报警次数和最高温度，输出监测日报。
 */
package cn.edu.sdipct.device;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AlarmHistoryStats {

    private static final String EXIT_COMMAND = "-99999";
    private static final double ALARM_THRESHOLD = 85.0;
    private static final double MIN_TEMPERATURE = -50.0;
    private static final double MAX_TEMPERATURE = 150.0;

    // 报表格式串集中管理；表头使用英文列名，避免中英文字符显示宽度不同导致错位。
    private static final String TITLE_FMT_STR = "%n========== 监测日报 ==========%n";
    private static final String HEADING_FMT_STR = "%-16s %10s %-12s %-5s %s%n";
    private static final String DATA_FMT_STR = "%-16s %10.1f %-12s %-5c %s%n";
    private static final String INVALID_SUMMARY_FMT_STR = "%n已跳过非法日志%d行%n";
    private static final String SUMMARY_FMT_STR = "共监测%d次，报警%d次，最高温度%.1f℃%n";
    private static final String EMPTY_SUMMARY_FMT_STR = "共监测%d次，报警%d次，最高温度无数据%n";

    // 记录本次运行中跳过的非法日志行数。
    private static int invalidLineCount;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入监测日志（编号,读数,状态），每行一条，单独输入 -99999 结束：");
        runMonitoring(scanner);
        scanner.close();
    }

    // 手动输入和随机模拟共用日志解析、输入验证、统计及日报流程。
    public static void runMonitoring(Scanner scanner) {
        int readingCount = 0;
        int alarmCount = 0;
        double maxTemperature = 0.0;
        invalidLineCount = 0;
        List<SensorReading> readings = new ArrayList<>();

        SensorReading reading = readValidReading(scanner);

        // 输入次数事先未知，不适合按固定次数控制的 for 循环。
        // 是否继续由哨兵值决定，while 可以在处理每条读数前检查条件。
        // 首次输入就可能是退出值，循环体应允许执行零次，因此选用 while。
        // 读到退出指令时返回 null，结束哨兵循环。
        while (reading != null) {
            double celsius = reading.temperature();
            boolean isAlarm = celsius > ALARM_THRESHOLD;
            readingCount++;
            readings.add(reading);

            if (isAlarm) {
                alarmCount++;
            }

            // 用第一条实际读数初始化最高温度，避免全是负温度时错误地保留 0。
            if (readingCount == 1 || celsius > maxTemperature) {
                maxTemperature = celsius;
            }

            reading = readValidReading(scanner);
        }

        printDailyReport(readings, readingCount, alarmCount, maxTemperature);
    }

    private static SensorReading readValidReading(Scanner scanner) {
        SensorReading reading;
        boolean validReading;

        do {
            String logLine = scanner.nextLine().trim();
            if (logLine.equals(EXIT_COMMAND)) {
                return null;
            }

            reading = parseLogReading(logLine);
            // 保留输入范围验证，-50.0 和 150.0 两个边界都有效。
            validReading = reading != null
                    && reading.temperature() >= MIN_TEMPERATURE
                    && reading.temperature() <= MAX_TEMPERATURE;

            if (!validReading) {
                invalidLineCount++;
                System.out.println("Invalid reading.");
            }
        } while (!validReading);

        return reading;
    }

    private static SensorReading parseLogReading(String logLine) {
        int firstComma = logLine.indexOf(',');
        if (firstComma < 0) {
            return null;
        }

        int secondComma = logLine.indexOf(',', firstComma + 1);
        // 日志必须恰好有三个字段，先检查分隔符，再使用 substring。
        if (secondComma < 0 || logLine.indexOf(',', secondComma + 1) >= 0) {
            return null;
        }

        String deviceId = logLine.substring(0, firstComma).trim();
        String readingText = logLine.substring(firstComma + 1, secondComma).trim();
        String status = logLine.substring(secondComma + 1).trim();
        if (deviceId.isEmpty() || readingText.isEmpty() || status.isEmpty()) {
            return null;
        }

        if (!hasNumericCharacters(readingText)) {
            return null;
        }

        try {
            double temperature = Double.parseDouble(readingText);
            return new SensorReading(deviceId, temperature, status);
        } catch (NumberFormatException exception) {
            // 字符检查不能保证数字结构正确，例如 1.2.3 仍需在转换时拒绝。
            return null;
        }
    }

    private static boolean hasNumericCharacters(String readingText) {
        boolean hasDigit = false;
        for (int i = 0; i < readingText.length(); i++) {
            char character = readingText.charAt(i);
            if (Character.isDigit(character)) {
                hasDigit = true;
            } else if (character != '+' && character != '-' && character != '.'
                    && character != 'e' && character != 'E') {
                return false;
            }
        }
        // 允许正负号、小数点和指数符号，具体数字格式由 parseDouble 检查。
        return hasDigit;
    }

    private static void printDailyReport(List<SensorReading> readings, int readingCount,
                                         int alarmCount, double maxTemperature) {
        // 第一段：标题和表头。
        System.out.printf(TITLE_FMT_STR);
        System.out.printf(HEADING_FMT_STR, "Device ID", "Temp(C)", "Status", "Level", "Action");

        // 第二段：只展示有效日志，报警级别按温度计算，不依赖日志中的状态文本。
        for (SensorReading reading : readings) {
            char levelCode = SensorCalc.determineAlarmLevel(reading.temperature());
            System.out.printf(DATA_FMT_STR, reading.deviceId(), reading.temperature(),
                    reading.status(), levelCode, SensorCalc.getActionText(levelCode));
        }

        // 第三段：汇总。哨兵值和非法日志均不参与监测统计。
        System.out.printf(INVALID_SUMMARY_FMT_STR, invalidLineCount);
        if (readingCount > 0) {
            System.out.printf(SUMMARY_FMT_STR, readingCount, alarmCount, maxTemperature);
        } else {
            System.out.printf(EMPTY_SUMMARY_FMT_STR, readingCount, alarmCount);
        }
    }

    // 一条日志同时保留编号、温度和原始状态，供统计和报表共用。
    private record SensorReading(String deviceId, double temperature, String status) {
    }
}
