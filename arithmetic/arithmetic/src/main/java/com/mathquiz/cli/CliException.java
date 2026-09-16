package com.mathquiz.cli;

/**
 * 命令行参数异常。
 * <p>继承 {@link RuntimeException} 而不是受检异常，因为参数错误是一种
 * "使用者错误"，调用方无法恢复，只需在入口处捕获并终止。</p>
 */
public class CliException extends RuntimeException {
    public CliException(String message) {
        super(message);
    }
}