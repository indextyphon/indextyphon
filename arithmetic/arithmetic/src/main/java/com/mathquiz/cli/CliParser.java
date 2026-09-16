package com.mathquiz.cli;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 命令行参数解析器。
 * <p>手写解析而非引用第三方库，原因：</p>
 * <ul>
 *   <li>参数格式极简（4 个选项），引库反而增加部署负担</li>
 *   <li>便于对非法输入给出定制化的中文错误信息</li>
 * </ul>
 */
public final class CliParser {

    /** 工具类禁止实例化 */
    private CliParser() {}

    /**
     * 解析 args 数组为 {@link CliOptions}。
     *
     * @throws CliException 参数缺失、格式错误、未知选项、模式冲突
     */
    public static CliOptions parse(String[] args) {
        // 默认值：-n 默认为 10，其余需显式提供
        int n = 10;
        Integer r = null;
        Path exFile = null, ansFile = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-n":
                    // 前置自增后取下一个 token，越界 nextArg 会抛异常
                    n = parseInt(nextArg(args, ++i, "-n"), "-n");
                    break;
                case "-r":
                    r = parseInt(nextArg(args, ++i, "-r"), "-r");
                    break;
                case "-e":
                    exFile = Paths.get(nextArg(args, ++i, "-e"));
                    break;
                case "-a":
                    ansFile = Paths.get(nextArg(args, ++i, "-a"));
                    break;
                case "-h":
                case "--help":
                    printHelp();
                    System.exit(0);
                    break;
                default:
                    throw new CliException("未知参数: " + args[i]);
            }
        }

        // ---------- 模式判定 ----------
        if (exFile != null || ansFile != null) {
            // 批改模式：两个参数必须成对出现
            if (exFile == null || ansFile == null) {
                throw new CliException("批改模式必须同时提供 -e 和 -a");
            }
            return new CliOptions(0, null, exFile, ansFile);
        }

        // ---------- 生成模式参数校验 ----------
        if (r == null) {
            throw new CliException("生成模式必须指定 -r 参数（数值范围）");
        }
        if (r < 1) {
            throw new CliException("-r 必须 >= 1，实际为 " + r);
        }
        if (n < 1) {
            throw new CliException("-n 必须 >= 1，实际为 " + n);
        }
        return new CliOptions(n, r, null, null);
    }

    /** 取下一个参数，越界时抛出统一格式的异常 */
    private static String nextArg(String[] args, int index, String option) {
        if (index >= args.length) {
            throw new CliException(option + " 缺少参数");
        }
        return args[index];
    }

    /** 字符串转整数，失败时给出友好提示 */
    private static int parseInt(String text, String option) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new CliException(option + " 需要一个整数，实际为: " + text);
        }
    }

    /** 打印帮助信息（-h 或参数错误时调用） */
    public static void printHelp() {
        System.out.println("用法:");
        System.out.println("  生成题目:");
        System.out.println("    java -jar arithmetic.jar -n <数量> -r <范围>");
        System.out.println("      -n  题目数量（默认 10）");
        System.out.println("      -r  数值范围 [0, r)，必须指定");
        System.out.println("  批改答案:");
        System.out.println("    java -jar arithmetic.jar -e <题目文件> -a <答案文件>");
    }
}