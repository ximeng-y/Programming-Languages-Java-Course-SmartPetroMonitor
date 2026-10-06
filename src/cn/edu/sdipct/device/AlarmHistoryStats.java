/*
 * 文件名：AlarmHistoryStats.java
 * 程序功能：解析并验证监测日志，统计有效监测次数、报警次数和最高温度。
 */
package cn.edu.sdipct.device;

import java.util.Scanner;

public class AlarmHistoryStats {

    private static final double EXIT_READING = -99999.0;
    private static final double ALARM_THRESHOLD = 85.0;
    private static final double MIN_TEMPERATURE = -50.0;
    private static final double MAX_TEMPERATURE = 150.0;

    // 记录本次运行中跳过的非法日志行数。
    private static int invalidLineCount;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int readingCount = 0;
        int alarmCount = 0;
        double maxTemperature = 0.0;
        invalidLineCount = 0;

        System.out.println("请输入监测日志（编号,读数,状态），每行一条，单独输入 -99999 结束：");
        double celsius = readValidReading(scanner);

        // 输入次数事先未知，不适合按固定次数控制的 for 循环。
        // 是否继续由哨兵值决定，while 可以在处理每条读数前检查条件。
        // 首次输入就可能是退出值，循环体应允许执行零次，因此选用 while。
        while (celsius != EXIT_READING) {
            boolean isAlarm = celsius > ALARM_THRESHOLD;
            readingCount++;

            if (isAlarm) {
                alarmCount++;
            }

            // 用第一条实际读数初始化最高温度，避免全是负温度时错误地保留 0。
            if (readingCount == 1 || celsius > maxTemperature) {
                maxTemperature = celsius;
            }

            celsius = readValidReading(scanner);
        }

        System.out.printf("已跳过非法日志%d行%n", invalidLineCount);
        // 哨兵值和非法日志不进入统计循环体。
        if (readingCount > 0) {
            System.out.printf("共监测%d次，报警%d次，最高温度%.1f℃%n",
                    readingCount, alarmCount, maxTemperature);
        } else {
            System.out.println("共监测0次，报警0次，最高温度无数据");
        }

        scanner.close();
    }

    private static double readValidReading(Scanner scanner) {
        double reading = 0.0;
        boolean validReading;

        do {
            String logLine = scanner.nextLine().trim();
            if (logLine.equals("-99999")) {
                return EXIT_READING;
            }

            Double parsedReading = parseLogReading(logLine);
            // 保留输入范围验证，-50.0 和 150.0 两个边界都有效。
            validReading = parsedReading != null
                    && parsedReading >= MIN_TEMPERATURE
                    && parsedReading <= MAX_TEMPERATURE;

            if (validReading) {
                reading = parsedReading;
            } else {
                invalidLineCount++;
                System.out.println("Invalid reading.");
            }
        } while (!validReading);

        return reading;
    }

    private static Double parseLogReading(String logLine) {
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
            return Double.parseDouble(readingText);
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
}
