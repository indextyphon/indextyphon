package com.mathquiz.model;

/**
 * 叶子表达式：一个常量分数。
 * <p>本类非常"薄"，因为全部重量级逻辑都在 {@link Fraction} 里。</p>
 */
public final class NumberExpression extends Expression {

    private final Fraction value;

    public NumberExpression(Fraction value) {
        this.value = value;
    }

    @Override
    public Fraction evaluate() {
        return value;
    }

    /** 原子优先级最高 */
    @Override
    public int precedence() {
        return 3;
    }

    @Override
    public String canonical() {
        return value.toString();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}