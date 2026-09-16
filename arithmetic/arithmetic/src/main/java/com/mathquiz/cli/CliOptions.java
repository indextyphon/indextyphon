package com.mathquiz.cli;

import java.nio.file.Path;

/**
 * 命令行参数的不可变值对象（Value Object）。
 * <p>作用：把"散落在 args 数组里的字符串"转成"结构化的强类型字段"，
 * 让 Service 层无需感知命令行细节。</p>
 *
 * <p>两种模式通过字段是否为 null 区分：</p>
 * <ul>
 *   <li>生成模式：range != null，exerciseFile == null</li>
 *   <li>批改模式：exerciseFile 和 answerFile 都非 null</li>
 * </ul>
 */
public final class CliOptions {

    private final int problemCount;      // -n：生成题目数量
    private final Integer range;         // -r：数值范围 [0, r)
    private final Path exerciseFile;     // -e：待批改的题目文件
    private final Path answerFile;       // -a：待批改的答案文件

    public CliOptions(int problemCount, Integer range,
                      Path exerciseFile, Path answerFile) {
        this.problemCount = problemCount;
        this.range = range;
        this.exerciseFile = exerciseFile;
        this.answerFile = answerFile;
    }

    /** 若提供了 -e/-a 则进入批改模式 */
    public boolean isGradeMode() {
        return exerciseFile != null || answerFile != null;
    }

    public int     getProblemCount() { return problemCount; }
    public Integer getRange()        { return range; }
    public Path    getExerciseFile() { return exerciseFile; }
    public Path    getAnswerFile()   { return answerFile; }
}