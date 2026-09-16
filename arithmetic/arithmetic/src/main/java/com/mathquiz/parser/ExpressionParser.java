package com.mathquiz.parser;

import com.mathquiz.model.*;

/**
 * 表达式解析器：文本 → {@link Expression} 抽象语法树。
 * <p>采用经典的递归下降法，文法如下（EBNF）：</p>
 * <pre>
 *   Expr   ::= Term   (('+' | '-') Term)*
 *   Term   ::= Factor (('×' | '÷') Factor)*
 *   Factor ::= '(' Expr ')' | Number
 *   Number ::= Integer | Integer '/' Integer | Integer "'" Integer '/' Integer
 * </pre>
 *
 * <p>主要用途：</p>
 * <ul>
 *   <li>批改时把 "1/2 + 3 = " 的左边解析回 AST 求值</li>
 *   <li>也用于把用户输入的答案文本解析成 Fraction 进行比较</li>
 * </ul>
 */
public final class ExpressionParser {

    private final String src;   // 原始文本
    private int pos;            // 当前扫描位置

    public ExpressionParser(String src) {
        this.src = src;
        this.pos = 0;
    }

    /** 便捷入口：直接解析字符串返回表达式树 */
    public static Expression parse(String text) {
        return new ExpressionParser(text).parseExpr();
    }

    // ---------- 文法对应的三个层次 ----------

    /** Expr ::= Term (('+' | '-') Term)* */
    public Expression parseExpr() {
        Expression left = parseTerm();
        while (true) {
            skipSpaces();
            if (pos < src.length() &&
                    (src.charAt(pos) == '+' || src.charAt(pos) == '-')) {
                Operator op = Operator.fromChar(src.charAt(pos++));
                left = new BinaryExpression(op, left, parseTerm());
            } else {
                return left;
            }
        }
    }

    /** Term ::= Factor (('×' | '÷') Factor)* */
    private Expression parseTerm() {
        Expression left = parseFactor();
        while (true) {
            skipSpaces();
            if (pos >= src.length()) return left;
            char c = src.charAt(pos);
            if (c != '×' && c != '*' && c != '÷') return left;
            pos++;
            left = new BinaryExpression(Operator.fromChar(c), left, parseFactor());
        }
    }

    /** Factor ::= '(' Expr ')' | Number */
    private Expression parseFactor() {
        skipSpaces();
        if (pos < src.length() && src.charAt(pos) == '(') {
            pos++;                            // 吃掉 '('
            Expression inner = parseExpr();
            skipSpaces();
            if (pos < src.length() && src.charAt(pos) == ')') pos++;  // 吃掉 ')'
            return inner;
        }
        return new NumberExpression(parseNumber());
    }

    // ---------- 数值解析 ----------

    /**
     * 支持三种形式：
     * <pre>
     *   "5"         → 5
     *   "3/5"       → 真分数
     *   "2'3/8"     → 带分数（2 + 3/8 = 19/8）
     * </pre>
     */
    private Fraction parseNumber() {
        skipSpaces();
        long whole = readLong();
        skipSpaces();

        // 带分数
        if (pos < src.length() && src.charAt(pos) == '\'') {
            pos++;
            long num = readLong();
            long den = 1;
            skipSpaces();
            if (pos < src.length() && src.charAt(pos) == '/') {
                pos++;
                den = readLong();
            }
            return Fraction.of(whole * den + num, den);
        }

        // 纯分数
        if (pos < src.length() && src.charAt(pos) == '/') {
            pos++;
            return Fraction.of(whole, readLong());
        }

        // 整数
        return Fraction.integer(whole);
    }

    // ---------- 扫描工具 ----------

    private void skipSpaces() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
    }

    private long readLong() {
        skipSpaces();
        int start = pos;
        while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
        if (start == pos) {
            throw new IllegalArgumentException(
                    "期望数字，位置 " + pos + "，原文: \"" + src + "\"");
        }
        return Long.parseLong(src.substring(start, pos));
    }
}