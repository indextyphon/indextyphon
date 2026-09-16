package com.mathquiz.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/** 分数运算与格式化单元测试。 */
class FractionTest {

    // ---------- 四则运算 ----------

    @Test
    void addFractions() {
        // 题目原文示例：1/6 + 1/8 = 7/24
        assertEquals("7/24", Fraction.of(1, 6).add(Fraction.of(1, 8)).toString());
    }

    @Test
    void subtractFractions() {
        assertEquals("1/6", Fraction.of(1, 2).sub(Fraction.of(1, 3)).toString());
    }

    @Test
    void multiplyFractions() {
        assertEquals("1/6", Fraction.of(1, 2).mul(Fraction.of(1, 3)).toString());
    }

    @Test
    void divideFractions() {
        // 1/2 ÷ 1/3 = 3/2，按题目要求假分数转带分数输出为 "1'1/2"
        assertEquals("1'1/2", Fraction.of(1, 2).div(Fraction.of(1, 3)).toString());
    }

    // ---------- 构造与约分 ----------

    @Test
    void autoReduce() {
        // 4/8 应约分为 1/2
        assertEquals("1/2", Fraction.of(4, 8).toString());
    }

    @Test
    void negativeDenominatorNormalized() {
        // 1/-2 应规范化为 -1/2
        assertEquals("-1/2", Fraction.of(1, -2).toString());
    }

    @Test
    void divideByZeroThrows() {
        assertThrows(ArithmeticException.class, () -> Fraction.of(1, 0));
    }

    // ---------- 格式化 ----------

    @ParameterizedTest
    @CsvSource({
            "0, 1,    0",        // 整数 0
            "5, 1,    5",        // 整数 5
            "3, 5,    3/5",      // 真分数
            "1, 2,    1/2",      // 真分数
            "19, 8,   2'3/8",    // 带分数
            "8, 4,    2",        // 约分后为整数
            "10, 5,   2"         // 约分后为整数
    })
    void format(long n, long d, String expected) {
        assertEquals(expected, Fraction.of(n, d).toString());
    }

    // ---------- 比较与判等 ----------

    @Test
    void compareTo() {
        assertTrue(Fraction.of(1, 3).compareTo(Fraction.of(1, 2)) < 0);
        assertTrue(Fraction.of(2, 4).compareTo(Fraction.of(1, 2)) == 0);
        assertTrue(Fraction.of(3, 4).compareTo(Fraction.of(1, 2)) > 0);
    }

    @Test
    void equalsIgnoresUnreducedForm() {
        // of() 会先约分，所以二者相等
        assertEquals(Fraction.of(2, 4), Fraction.of(1, 2));
    }

    @Test
    void isZero() {
        assertTrue(Fraction.of(0, 7).isZero());
        assertFalse(Fraction.of(1, 7).isZero());
    }
}