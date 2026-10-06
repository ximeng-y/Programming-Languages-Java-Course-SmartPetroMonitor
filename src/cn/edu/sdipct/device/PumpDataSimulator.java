package cn.edu.sdipct.device;

/**
 * 抽油机泵压数据模拟器。
 *
 * <p>用于在无真实采集设备接入的情况下，模拟产生抽油机的泵压数据，
 * 便于后续的监控界面、数据存储等模块进行联调与演示。</p>
 */
public class PumpDataSimulator {

    /** 模拟输出的数据条数：共输出 5 次泵压数据 */
    private static final int SAMPLE_COUNT = 5;

    /** 随机压力的下限（单位：MPa），取值包含该值 */
    private static final double PRESSURE_MIN = 10.0;

    /** 随机压力的上限（单位：MPa），取值不包含该值 */
    private static final double PRESSURE_MAX = 15.0;

    /**
     * 程序入口方法。
     *
     * @param args 命令行参数，本示例中未使用
     */
    public static void main(String[] args) {
        // 循环 SAMPLE_COUNT 次，模拟连续采集到的多组压力数据
        // 循环变量 i 从 0 开始，便于直接作为"第几次"的序号参与输出
        for (int i = 0; i < SAMPLE_COUNT; i++) {
            // 生成一个 [10.0, 15.0) 区间内的随机小数作为本次的泵压值：
            // Math.random() 返回 [0.0, 1.0) 之间的随机小数，
            // 乘以区间跨度 (PRESSURE_MAX - PRESSURE_MIN) 后再加上下限，
            // 即可把取值范围从 [0, 1) 平移缩放到 [10.0, 15.0)
            double pressure = PRESSURE_MIN + Math.random() * (PRESSURE_MAX - PRESSURE_MIN);

            // 输出本次的序号与压力值：i 从 0 计数，因此加 1 后展示为第 1~5 次；
            // 使用 printf 并保留两位小数，使模拟数据更贴近真实仪表读数
            System.out.printf("第 %d 次 抽油机泵压数据：%.2f MPa%n", i + 1, pressure);
        }
    }
}
