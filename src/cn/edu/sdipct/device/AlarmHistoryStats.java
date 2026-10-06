/*
 * 文件名：AlarmHistoryStats.java
 * 程序功能：验证并连续读取温度，统计有效监测次数、报警次数和最高温度。
 */
package cn.edu.sdipct.device;

import java.util.Scanner;

public class AlarmHistoryStats {

    private static final double EXIT_READING = -99999.0;
    private static final double ALARM_THRESHOLD = 85.0;
    private static final double MIN_TEMPERATURE = -50.0;
    private static final double MAX_TEMPERATURE = 150.0;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int readingCount = 0;
        int alarmCount = 0;
        double maxTemperature = 0.0;

        System.out.println("请依次输入温度（℃），输入 -99999 结束：");
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

        // 哨兵值不进入循环体，也不参与次数和最高温度统计。
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
            if (scanner.hasNextDouble()) {
                reading = scanner.nextDouble();
                // 退出值单独放行；有效温度包含 -50.0 和 150.0 两个边界。
                validReading = reading == EXIT_READING
                        || (reading >= MIN_TEMPERATURE && reading <= MAX_TEMPERATURE);
            } else {
                // 消耗非数字输入，避免重复读取同一个错误内容。
                scanner.next();
                validReading = false;
            }

            if (!validReading) {
                System.out.println("Invalid reading.");
            }
        } while (!validReading);

        return reading;
    }
}
