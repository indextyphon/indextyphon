package com.mathquiz.generator;

import com.mathquiz.model.*;

import java.util.Random;

/**
 * 受约束的随机表达式生成器。
 * <p>核心约束（生成过程中就地保证）：</p>
 * <ol>
 *   <li>运算符总数 ≤ budget</li>
 *   <li>任何子表达式求值结果非负（减法自动交换左右操作数）</li>
 *   <li>任何除法结果 ≤ 1，即真分数（自动交换或重试）</li>
 *   <li>除数不为 0</li>
 *   <li>所有数值（自然数、真分数分子分母）都在 [0, range) 内</li>
 * </ol>
 *
 * <p>为什么用递归而不是"先随机生成再过滤"？因为过滤法在高约束下
 * 接受率极低（尤其除法），递归生成能在失败时只重试局部分支。</p>
 */
public final class ExpressionGenerator {

    private final int range;
    private final Random random;

    public ExpressionGenerator(int range, Random random) {
        this.range = range;
        this.random = random;
    }

    /**
     * 生成一棵运算符数量不超过 {@code budget} 的表达式树。
     *
     * @param budget 剩余可用的运算符数量
     */
    public Expression generate(int budget) {

        // 递归终止 1：预算耗尽，只能是叶子
        if (budget <= 0) return new NumberExpression(randomValue());

        // 递归终止 2：1/5 概率提前收敛，避免表达式总是用满预算
        if (random.nextInt(5) == 0) return new NumberExpression(randomValue());

        // 随机分配左右子树预算，保证 leftBudget + rightBudget + 1 = budget
        int leftBudget  = random.nextInt(budget);
        int rightBudget = budget - 1 - leftBudget;

        // 除法约束可能连续失败，最多重试 60 次
        for (int attempt = 0; attempt < 60; attempt++) {

            Expression left  = generate(leftBudget);
            Expression right = generate(rightBudget);
            Operator op = Operator.values()[random.nextInt(Operator.values().length)];

            Fraction lv = left.evaluate();
            Fraction rv = right.evaluate();

            switch (op) {
                case ADD:
                    // 加法天然满足所有约束
                    return new BinaryExpression(op, left, right);

                case SUB:
                    // 保证 e1 >= e2，否则交换左右
                    return lv.compareTo(rv) >= 0
                            ? new BinaryExpression(op, left, right)
                            : new BinaryExpression(op, right, left);

                case MUL:
                    // 乘法在非负数域上天然满足约束
                    return new BinaryExpression(op, left, right);

                case DIV:
                    // 尝试 lv / rv，要求结果 ∈ [0, 1]
                    if (!rv.isZero() && lv.div(rv).compareTo(Fraction.ONE) <= 0) {
                        return new BinaryExpression(op, left, right);
                    }
                    // 交换后尝试 rv / lv
                    if (!lv.isZero() && rv.div(lv).compareTo(Fraction.ONE) <= 0) {
                        return new BinaryExpression(op, right, left);
                    }
                    // 两种都不行 → 重试本轮
                    break;
            }
        }

        // 兜底：连续失败后降级为叶子，保证函数一定返回
        return new NumberExpression(randomValue());
    }

    /**
     * 随机取一个数值：自然数或真分数，均在 [0, range) 内。
     * <p>策略：</p>
     * <ul>
     *   <li>range == 1：只能返回 0</li>
     *   <li>range >= 3：1/2 概率生成真分数，1/2 概率生成整数</li>
     *   <li>range == 2：只能生成 0 或 1</li>
     * </ul>
     */
    private Fraction randomValue() {
        if (range <= 1) return Fraction.ZERO;

        // 真分数分支：分母 2..range-1，分子 1..分母-1
        if (range >= 3 && random.nextBoolean()) {
            int den = 2 + random.nextInt(range - 2);
            int num = 1 + random.nextInt(den - 1);
            return Fraction.of(num, den);
        }

        // 整数分支
        return Fraction.integer(random.nextInt(range));
    }
}