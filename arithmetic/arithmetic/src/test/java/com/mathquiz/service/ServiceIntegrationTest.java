package com.mathquiz.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 端到端集成测试：生成 → 批改。
 * <p>本类所有 API 均兼容 Java 8。</p>
 */
class ServiceIntegrationTest {

    /** JUnit 5 提供，为每个测试方法分配独立临时目录 */
    @TempDir
    Path tempDir;

    /** Java 8 兼容的读文件工具（替代 Java 11 的 Files.readString） */
    private static String readString(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    @Test
    void generateThenGradeAllCorrect() throws IOException {
        Path ex = tempDir.resolve("Exercises.txt");
        Path ans = tempDir.resolve("Answers.txt");
        Path grade = tempDir.resolve("Grade.txt");

        // 生成 20 道题
        new ProblemGenerateService().generate(20, 10, ex, ans);

        // 文件存在且行数正确
        assertTrue(Files.exists(ex));
        assertTrue(Files.exists(ans));
        List<String> exLines = Files.readAllLines(ex, StandardCharsets.UTF_8);
        List<String> ansLines = Files.readAllLines(ans, StandardCharsets.UTF_8);
        assertEquals(20, exLines.size());
        assertEquals(20, ansLines.size());

        // 每行题目格式：以 " =" 结尾
        for (String line : exLines) {
            assertTrue(line.endsWith(" ="), "题目格式错误: " + line);
        }

        // 用生成的答案去批改，应该全对
        new AnswerGradeService().grade(ex, ans, grade);
        String gradeText = readString(grade);
        assertTrue(gradeText.contains("Correct: 20"), "应为全对，实际: " + gradeText);
        assertTrue(gradeText.contains("Wrong: 0"));
    }

    @Test
    void gradeDetectsWrongAnswers() throws IOException {
        // 手工构造 3 道题
        Path ex = tempDir.resolve("Exercises.txt");
        Path ans = tempDir.resolve("Answers.txt");
        Path grade = tempDir.resolve("Grade.txt");

        Files.write(ex, Arrays.asList(
                "1 + 2 = ",
                "3 × 4 = ",
                "1/2 + 1/3 = "
        ), StandardCharsets.UTF_8);

        Files.write(ans, Arrays.asList(
                "3",       // 对
                "11",      // 错（应为 12）
                "5/6"      // 对
        ), StandardCharsets.UTF_8);

        new AnswerGradeService().grade(ex, ans, grade);
        String text = readString(grade);

        assertTrue(text.contains("Correct: 2"));
        assertTrue(text.contains("Wrong: 1"));
        assertTrue(text.contains("(2)"), "第 2 题应为错题");
    }

    @Test
    void gradeHandlesMixedNumberFormat() throws IOException {
        // 答案以带分数形式给出也应被正确识别
        Path ex = tempDir.resolve("Exercises.txt");
        Path ans = tempDir.resolve("Answers.txt");
        Path grade = tempDir.resolve("Grade.txt");

        Files.write(ex, Arrays.asList("19/8 + 0 = "), StandardCharsets.UTF_8);
        Files.write(ans, Arrays.asList("2'3/8"), StandardCharsets.UTF_8);

        new AnswerGradeService().grade(ex, ans, grade);
        String text = readString(grade);
        assertTrue(text.contains("Correct: 1"));
    }

    @Test
    void gradeTreatsInvalidAnswerAsWrong() throws IOException {
        Path ex = tempDir.resolve("Exercises.txt");
        Path ans = tempDir.resolve("Answers.txt");
        Path grade = tempDir.resolve("Grade.txt");

        Files.write(ex, Arrays.asList("1 + 1 = "), StandardCharsets.UTF_8);
        Files.write(ans, Arrays.asList("不是数字"), StandardCharsets.UTF_8);

        new AnswerGradeService().grade(ex, ans, grade);
        String text = readString(grade);
        assertTrue(text.contains("Wrong: 1"));
    }

    @Test
    void generateLargeCount() throws IOException {
        // 一万道题验证性能与稳定性
        Path ex = tempDir.resolve("Exercises.txt");
        Path ans = tempDir.resolve("Answers.txt");

        long start = System.currentTimeMillis();
        new ProblemGenerateService().generate(10000, 20, ex, ans);
        long elapsed = System.currentTimeMillis() - start;

        List<String> exLines = Files.readAllLines(ex, StandardCharsets.UTF_8);
        assertEquals(10000, exLines.size());

        System.out.println("生成 10000 道题耗时: " + elapsed + " ms");
    }
}