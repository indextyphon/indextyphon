package com.mathquiz.parser;

import com.mathquiz.model.Expression;
import com.mathquiz.model.Fraction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** 递归下降解析器单元测试。 */
class ExpressionParserTest {

    @Test
    void parseInteger() {
        Expression e = ExpressionParser.parse("42");
        assertEquals(Fraction.integer(42), e.evaluate());
    }

    @Test
    void parseSimpleFraction() {
        Expression e = ExpressionParser.parse("3/5");
        assertEquals(Fraction.of(3, 5), e.evaluate());
    }

    @Test
    void parseMixedNumber() {
        // 2'3/8 = 19/8
        Expression e = ExpressionParser.parse("2'3/8");
        assertEquals(Fraction.of(19, 8), e.evaluate());
    }

    @Test
    void parseAddition() {
        Expression e = ExpressionParser.parse("1/2 + 1/3");
        assertEquals(Fraction.of(5, 6), e.evaluate());
    }

    @Test
    void parsePrecedence() {
        // 1 + 2 × 3 = 7（× 优先级更高）
        Expression e = ExpressionParser.parse("1 + 2 × 3");
        assertEquals(Fraction.integer(7), e.evaluate());
    }

    @Test
    void parseParentheses() {
        // (1 + 2) × 3 = 9
        Expression e = ExpressionParser.parse("(1 + 2) × 3");
        assertEquals(Fraction.integer(9), e.evaluate());
    }

    @Test
    void parseNestedParentheses() {
        // ((1 + 2) × (3 - 1)) = 6
        Expression e = ExpressionParser.parse("((1 + 2) × (3 - 1))");
        assertEquals(Fraction.integer(6), e.evaluate());
    }

    @Test
    void parseAsteriskAsMultiplication() {
        // 兼容手写答案中的 *
        Expression e = ExpressionParser.parse("2 * 3");
        assertEquals(Fraction.integer(6), e.evaluate());
    }

    @Test
    void parseExtraSpaces() {
        Expression e = ExpressionParser.parse("  1  +   2/3   ");
        assertEquals(Fraction.of(5, 3), e.evaluate());
    }

    @Test
    void parseMixedComplex() {
        // 1/2 + 2'3/8 × 2 = 1/2 + 19/4 = 21/4
        Expression e = ExpressionParser.parse("1/2 + 2'3/8 × 2");
        assertEquals(Fraction.of(21, 4), e.evaluate());
    }

    @Test
    void invalidInputThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> ExpressionParser.parse("+ +"));
    }
}