/*
 * 文件名：RandomTemperatureSimulator.java
 * 程序功能：生成可复现的随机温度日志，送入监测日志解析流程并输出日报。
 */
package cn.edu.sdipct.device;

import java.util.Random;
import java.util.Scanner;

public class RandomTemperatureSimulator {

    private static final long RANDOM_SEED = 12345L;
    private static final int SAMPLE_COUNT = 10;
    private static final String SIMULATION_INFO_FMT_STR = "固定种子：%d，模拟读数：%d条%n";

    public static void main(String[] args) {
        // 固定种子使相同调用顺序下的数据可复现，便于定位问题和对比修改结果。
        // 需要每次运行产生不同样本时，改用不指定种子的 new Random()。
        Random random = new Random(RANDOM_SEED);
        StringBuilder logStream = new StringBuilder();

        for (int i = 1; i <= SAMPLE_COUNT; i++) {
            double temperature = 55.0 + 45.0 * random.nextGaussian();
            // 超出区间时直接限制到边界，不重新抽样。
            temperature = Math.max(0.0, Math.min(150.0, temperature));

            char levelCode = SensorCalc.determineAlarmLevel(temperature);
            String status = switch (levelCode) {
                case 'R' -> "ALARM";
                case 'Y' -> "WARN";
                case 'B' -> "LOW";
                default -> "NORMAL";
            };

            // 保留原始小数精度组成日志；仅在日报展示时保留一位小数。
            logStream.append("D-").append(1000 + i)
                    .append(',').append(temperature)
                    .append(',').append(status).append('\n');
        }
        logStream.append("-99999\n");

        System.out.printf(SIMULATION_INFO_FMT_STR, RANDOM_SEED, SAMPLE_COUNT);
        Scanner scanner = new Scanner(logStream.toString());
        AlarmHistoryStats.runMonitoring(scanner);
        scanner.close();
    }
}
