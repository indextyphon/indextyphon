package com.mathquiz;

import com.mathquiz.cli.CliException;
import com.mathquiz.cli.CliOptions;
import com.mathquiz.cli.CliParser;
import com.mathquiz.service.AnswerGradeService;
import com.mathquiz.service.ProblemGenerateService;

import java.nio.file.Paths;

/**
 * 程序唯一入口。
 * <p>职责边界：</p>
 * <ul>
 *   <li>解析命令行参数（委托给 {@link CliParser}）</li>
 *   <li>根据参数模式分派到对应 Service</li>
 *   <li>作为唯一异常出口，打印友好提示并决定退出码</li>
 * </ul>
 * <p>本类刻意保持"薄"，不做任何业务处理，方便测试与维护。</p>
 */
public final class Main {

    public static void main(String[] args) {

        // ---------- 第一步：参数解析 ----------
        CliOptions options;
        try {
            options = CliParser.parse(args);
        } catch (CliException e) {
            // 参数类错误：打印帮助信息，退出码 1
            System.err.println("参数错误: " + e.getMessage());
            CliParser.printHelp();
            System.exit(1);
            return; // 编译器无法推断 System.exit 会终止，显式 return
        }

        // ---------- 第二步：根据模式分派 ----------
        try {
            if (options.isGradeMode()) {
                // 批改模式：同时提供 -e 和 -a
                new AnswerGradeService().grade(
                        options.getExerciseFile(),
                        options.getAnswerFile(),
                        Paths.get("Grade.txt"));
            } else {
                // 生成模式：-n 与 -r
                new ProblemGenerateService().generate(
                        options.getProblemCount(),
                        options.getRange(),
                        Paths.get("Exercises.txt"),
                        Paths.get("Answers.txt"));
            }
        } catch (Exception e) {
            // 运行期错误（IO 失败、解析失败等）：退出码 2
            System.err.println("运行失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }
}