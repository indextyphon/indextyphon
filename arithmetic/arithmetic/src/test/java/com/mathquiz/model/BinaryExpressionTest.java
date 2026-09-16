package com.mathquiz.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** 表达式树：求值、判重、括号输出。 */
class BinaryExpressionTest {

    /** 便捷构造：整数表达式 */
    private static Expression num(long n) {
        return new NumberExpression(Fraction.integer(n));
    }

    /** 便捷构造：分数表达式 */
    private static Expression frac(long n, long d) {
        return new NumberExpression(Fraction.of(n, d));
    }

    // ---------- 求值 ----------

    @Test
    void evaluateSimple() {
        // 3 + 5 = 8
        Expression e = new BinaryExpression(Operator.ADD, num(3), num(5));
        assertEquals(Fraction.integer(8), e.evaluate());
    }

    @Test
    void evaluateNested() {
        // (1 + 2) × 3 = 9
        Expression inner = new BinaryExpression(Operator.ADD, num(1), num(2));
        Expression e = new BinaryExpression(Operator.MUL, inner, num(3));
        assertEquals(Fraction.integer(9), e.evaluate());
    }

    // ---------- 判重（canonical） ----------

    @Test
    void commutativeAdditionIsSame() {
        // 23 + 45 与 45 + 23 规范形式相同
        Expression a = new BinaryExpression(Operator.ADD, num(23), num(45));
        Expression b = new BinaryExpression(Operator.ADD, num(45), num(23));
        assertEquals(a.canonical(), b.canonical());
    }

    @Test
    void commutativeMultiplicationIsSame() {
        // 6 × 8 与 8 × 6 规范形式相同
        Expression a = new BinaryExpression(Operator.MUL, num(6), num(8));
        Expression b = new BinaryExpression(Operator.MUL, num(8), num(6));
        assertEquals(a.canonical(), b.canonical());
    }

    @Test
    void subtractionNotCommutative() {
        // 5 - 3 与 3 - 5 是不同题目
        Expression a = new BinaryExpression(Operator.SUB, num(5), num(3));
        Expression b = new BinaryExpression(Operator.SUB, num(3), num(5));
        assertNotEquals(a.canonical(), b.canonical());
    }

    @Test
    void repeatedAdditionViaDifferentGroupingIsSame() {
        // 3 + (2 + 1) 与 1 + 2 + 3（即 (1+2)+3）判为重复
        Expression innerA = new BinaryExpression(Operator.ADD, num(2), num(1));
        Expression a = new BinaryExpression(Operator.ADD, num(3), innerA);

        Expression innerB = new BinaryExpression(Operator.ADD, num(1), num(2));
        Expression b = new BinaryExpression(Operator.ADD, innerB, num(3));

        assertEquals(a.canonical(), b.canonical());
    }

    @Test
    void reversedChainIsDifferent() {
        // 1+2+3 与 3+2+1 不重复（题目明确要求）
        Expression innerA = new BinaryExpression(Operator.ADD, num(1), num(2));
        Expression a = new BinaryExpression(Operator.ADD, innerA, num(3));

        Expression innerB = new BinaryExpression(Operator.ADD, num(3), num(2));
        Expression b = new BinaryExpression(Operator.ADD, innerB, num(1));

        assertNotEquals(a.canonical(), b.canonical());
    }

    // ---------- toString 括号最小化 ----------

    @Test
    void parensForLowerPrecedenceLeft() {
        // (1 + 2) × 3
        Expression inner = new BinaryExpression(Operator.ADD, num(1), num(2));
        Expression e = new BinaryExpression(Operator.MUL, inner, num(3));
        assertEquals("(1 + 2) × 3", e.toString());
    }

    @Test
    void parensForLowerPrecedenceRight() {
        // 3 × (1 + 2)
        Expression inner = new BinaryExpression(Operator.ADD, num(1), num(2));
        Expression e = new BinaryExpression(Operator.MUL, num(3), inner);
        assertEquals("3 × (1 + 2)", e.toString());
    }

    @Test
    void parensForSamePrecedenceRight() {
        // 1 - (2 - 3)，右子同优先级必须加括号
        Expression inner = new BinaryExpression(Operator.SUB, num(2), num(3));
        Expression e = new BinaryExpression(Operator.SUB, num(1), inner);
        assertEquals("1 - (2 - 3)", e.toString());
    }

    @Test
    void noParensForSamePrecedenceLeft() {
        // (1 - 2) - 3 写成 "1 - 2 - 3"，左结合默认语义正确
        Expression inner = new BinaryExpression(Operator.SUB, num(1), num(2));
        Expression e = new BinaryExpression(Operator.SUB, inner, num(3));
        assertEquals("1 - 2 - 3", e.toString());
    }

    @Test
    void mixedFractionFormat() {
        // 1/2 + 2'3/8
        Expression e = new BinaryExpression(Operator.ADD, frac(1, 2), frac(19, 8));
        assertEquals("1/2 + 2'3/8", e.toString());
    }
}