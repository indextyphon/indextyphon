package com.mathquiz.model;

/**
 * 表达式抽象基类（组合模式）。
 * <p>两个子类：</p>
 * <ul>
 *   <li>{@link NumberExpression} 叶子节点，持有一个分数</li>
 *   <li>{@link BinaryExpression} 内部节点，持有运算符和两个子表达式</li>
 * </ul>
 *
 * <p>三个核心方法：</p>
 * <ul>
 *   <li>{@link #evaluate()}  —— 递归求值</li>
 *   <li>{@link #canonical()} —— 规范化字符串，用于判重</li>
 *   <li>{@link #precedence()} —— 自身优先级，用于决定 toString 时是否加括号</li>
 * </ul>
 */
public abstract class Expression {

    /** 求值：返回精确的分数结果 */
    public abstract Fraction evaluate();

    /**
     * 规范形式：把 + 和 × 两侧按字典序排序后拼接。
     * <p>这样 3+(2+1) 和 1+2+3 会生成相同的规范串；
     * 而 1+2+3 和 3+2+1 因为左结合结构不同，仍为不同串。</p>
     */
    public abstract String canonical();

    /** 优先级：3=原子（数字/括号），2=乘除，1=加减 */
    public abstract int precedence();
}