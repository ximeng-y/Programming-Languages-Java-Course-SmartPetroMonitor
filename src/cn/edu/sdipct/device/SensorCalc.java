/*
 * 文件名：SensorCalc.java
 * 程序功能：传感器数据计算与阈值报警。
 */
package cn.edu.sdipct.device;

import java.util.Scanner;

public class SensorCalc {

    // 题目未给出设备功率，暂假设以 1.5 kW 的恒定功率运行，用于估算电耗。
    private static final double ASSUMED_POWER_KW = 1.5;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("请输入温度（℃）：");
        double celsius = scanner.nextDouble();

        System.out.print("请输入运行时长（整数，h）：");
        int operatingHours = scanner.nextInt();

        // 按从左到右的顺序计算，celsius 使乘除运算使用浮点数。
        double fahrenheit = celsius * 9 / 5 + 32;

        // 故意保留错误写法作对照：括号内两个整数相除，9 / 5 被截断为 1。
        double incorrectFahrenheit = celsius * (9 / 5) + 32;

        // 电耗（kWh）= 功率（kW）× 运行时长（h）。
        double energyConsumption = ASSUMED_POWER_KW * operatingHours;

        System.out.println();
        System.out.println("========== 计算结果 ==========");
        System.out.printf("输入温度：%.2f ℃%n", celsius);
        System.out.printf("运行时长：%d h%n", operatingHours);
        System.out.printf("正确换算 celsius * 9 / 5 + 32：%.2f ℉%n", fahrenheit);
        System.out.printf("错误对照 celsius * (9 / 5) + 32：%.2f ℉%n", incorrectFahrenheit);
        System.out.println("差异原因：正确写法先与 double 类型的 celsius 相乘，再进行浮点除法；"
                + "错误写法先算整数除法 9 / 5，结果为 1，而不是 1.8。");
        System.out.printf("假设恒定功率：%.2f kW（题目未指定）%n", ASSUMED_POWER_KW);
        System.out.printf("估算电耗：%.2f kWh%n", energyConsumption);

        System.out.println();
        System.out.println("========== 阈值检测 ==========");
        char levelCode;
        // 从高到低判断，四档无重叠、无遗漏；85.0 和 70.0 属于黄色，5.0 属于正常。
        if (celsius > 85.0) {
            levelCode = 'R'; // 红色：温度 > 85.0。
        } else if (celsius >= 70.0) {
            levelCode = 'Y'; // 黄色：70.0 <= 温度 <= 85.0。
        } else if (celsius >= 5.0) {
            levelCode = 'G'; // 正常：5.0 <= 温度 < 70.0。
        } else {
            levelCode = 'B'; // 蓝色：温度 < 5.0。
        }

        String actionText = switch (levelCode) {
            case 'R' -> "红色报警：立即停机检查";
            case 'Y' -> "黄色预警：加强巡检";
            case 'G' -> "正常";
            case 'B' -> "蓝色提示：防冻保护";
            default -> throw new IllegalStateException("未知报警级别：" + levelCode);
        };
        System.out.println("报警级别：" + levelCode);
        System.out.println(actionText);

        scanner.close();
    }
}
