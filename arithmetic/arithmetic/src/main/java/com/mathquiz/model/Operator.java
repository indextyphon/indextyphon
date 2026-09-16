package com.mathquiz.model;

/**
 * 四则运算符枚举。
 * <p>把运算符的"符号字符、优先级、是否可交换"三个属性集中管理，
 * 避免在代码各处散落 {@code if (op == '+') ...} 这类判断。</p>
 */
public enum Operator {

    ADD('+', 1, true),    // 加减同优先级，加法可交换
    SUB('-', 1, false),
    MUL('×', 2, true),    // 乘除同优先级，乘法可交换
    DIV('÷', 2, false);

    private final char symbol;         // 显示字符
    private final int precedence;      // 数值越大优先级越高
    private final boolean commutative; // 是否满足交换律（判重时用于排序）

    Operator(char symbol, int precedence, boolean commutative) {
        this.symbol = symbol;
        this.precedence = precedence;
        this.commutative = commutative;
    }

    public char symbol()           { return symbol; }
    public int precedence()        { return precedence; }
    public boolean isCommutative() { return commutative; }

    /** 从字符解析运算符，* 视为 ×，兼容手写答案 */
    public static Operator fromChar(char c) {
        switch (c) {
            case '+': return ADD;
            case '-': return SUB;
            case '×':
            case '*': return MUL;
            case '÷': return DIV;
            default:  throw new IllegalArgumentException("未知运算符: " + c);
        }
    }
}