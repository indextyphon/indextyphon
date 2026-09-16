package com.mathquiz.service;

import com.mathquiz.model.Fraction;
import com.mathquiz.parser.ExpressionParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 批改模式的服务层。
 * <p>流程：</p>
 * <ol>
 *   <li>读题目文件和答案文件</li>
 *   <li>逐行：解析题目求"标准答案"，解析答案行得到"考生答案"</li>
 *   <li>用 Fraction.equals 精确比较，记录对/错的行号</li>
 *   <li>按指定格式写 Grade.txt</li>
 * </ol>
 */
public final class AnswerGradeService {

    public void grade(Path exerciseFile, Path answerFile, Path gradeFile)
            throws IOException {

        List<String> exerciseLines = Files.readAllLines(exerciseFile, StandardCharsets.UTF_8);
        List<String> answerLines   = Files.readAllLines(answerFile,   StandardCharsets.UTF_8);

        // 行数不匹配时告警，但不中断（按较少行数批改）
        if (exerciseLines.size() != answerLines.size()) {
            System.err.printf("警告: 题目文件 %d 行，答案文件 %d 行，按较少者计算%n",
                    exerciseLines.size(), answerLines.size());
        }

        int total = Math.min(exerciseLines.size(), answerLines.size());
        List<Integer> correctIndices = new ArrayList<>();   // 题目编号从 1 开始
        List<Integer> wrongIndices   = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            String line = exerciseLines.get(i).trim();
            if (line.isEmpty()) continue;   // 跳过空行

            // 题目行格式："expr = "，取 '=' 之前的部分作为表达式
            int eq = line.indexOf('=');
            String exprText = eq >= 0 ? line.substring(0, eq) : line;

            // 求标准答案
            Fraction expected = ExpressionParser.parse(exprText).evaluate();

            // 求考生答案（解析失败视为错答，不中断批改）
            Fraction given = parseSafely(answerLines.get(i).trim());

            if (expected.equals(given)) {
                correctIndices.add(i + 1);   // 编号 = 行号（从 1 起）
            } else {
                wrongIndices.add(i + 1);
            }
        }

        // 按题目要求的格式输出
        StringBuilder sb = new StringBuilder();
        sb.append("Correct: ").append(correctIndices.size()).append(' ')
                .append(formatIndices(correctIndices)).append('\n');
        sb.append("Wrong: ").append(wrongIndices.size()).append(' ')
                .append(formatIndices(wrongIndices)).append('\n');

        Files.write(gradeFile, sb.toString().getBytes(StandardCharsets.UTF_8));
        System.out.printf("批改完成 -> %s%n", gradeFile);
    }

    /** 尝试解析答案文本；任何异常返回 null（在 equals 中自然判为不等） */
    private Fraction parseSafely(String text) {
        if (text == null || text.isEmpty()) return null;
        try {
            return ExpressionParser.parse(text).evaluate();
        } catch (Exception e) {
            return null;
        }
    }

    /** 把编号列表格式化为 "(1, 3, 5)"，空列表返回 "()" */
    private String formatIndices(List<Integer> indices) {
        if (indices.isEmpty()) return "()";
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < indices.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(indices.get(i));
        }
        return sb.append(')').toString();
    }
}