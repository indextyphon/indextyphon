package com.mathquiz.service;

import com.mathquiz.generator.ExpressionGenerator;
import com.mathquiz.model.BinaryExpression;
import com.mathquiz.model.Expression;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * 生成模式的服务层。
 * <p>编排职责：</p>
 * <ol>
 *   <li>循环调用 {@link ExpressionGenerator} 生成题目</li>
 *   <li>用 {@code canonical()} 去重（判重逻辑在模型层）</li>
 *   <li>汇总题目和答案，一次性写两个文件</li>
 * </ol>
 * <p>自身不含任何算法：随机性交给 generator，判重交给 Expression，
 * 分数运算交给 Fraction，格式化交给 toString。</p>
 */
public final class ProblemGenerateService {

    public void generate(int count, int range, Path exerciseFile, Path answerFile)
            throws IOException {

        Random random = new Random();
        ExpressionGenerator generator = new ExpressionGenerator(range, random);

        // 判重集合：存放每道题的 canonical() 串
        Set<String> seen = new HashSet<>();
        // 保序保存（按生成顺序输出）
        List<Expression> problems = new ArrayList<>(count);

        // 如果 range 太小可能凑不齐 count 道不重复题，设上限防止死循环
        long maxAttempts = Math.max(200_000L, count * 2_000L);
        long attempts = 0;

        while (problems.size() < count && attempts++ < maxAttempts) {
            Expression expr = generator.generate(3);   // 最多 3 个运算符

            // 拒绝纯数字表达式（单个 "5" 不算一道四则运算题）
            if (!(expr instanceof BinaryExpression)) continue;

            // canonical() 相同即视为重复
            if (seen.add(expr.canonical())) {
                problems.add(expr);
            }
        }

        if (problems.size() < count) {
            System.err.printf("警告: r=%d 范围内仅能生成 %d 道不重复题目（请求 %d 道）%n",
                    range, problems.size(), count);
        }

        // 一次性构建两个文件的字符串，避免多次 IO
        StringBuilder exerciseBuf = new StringBuilder();
        StringBuilder answerBuf = new StringBuilder();
        for (Expression e : problems) {
            exerciseBuf.append(e).append(" =").append('\n');   // 题目格式：expr =
            answerBuf.append(e.evaluate()).append('\n');       // 答案只写结果
        }

        // UTF-8 编码，保证中文/特殊符号正确
        Files.write(exerciseFile, exerciseBuf.toString().getBytes(StandardCharsets.UTF_8));
        Files.write(answerFile,   answerBuf.toString().getBytes(StandardCharsets.UTF_8));

        System.out.printf("已生成 %d 道题目 -> %s%n", problems.size(), exerciseFile);
        System.out.printf("已生成 %d 个答案 -> %s%n", problems.size(), answerFile);
    }
}