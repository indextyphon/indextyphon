package com.mathquiz.generator;

import com.mathquiz.model.BinaryExpression;
import com.mathquiz.model.Expression;
import com.mathquiz.model.Fraction;
import com.mathquiz.model.NumberExpression;
import com.mathquiz.model.Operator;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** 生成器约束验证。 */
class ExpressionGeneratorTest {

    /** 递归统计表达式中运算符个数 */
    private static int countOperators(Expression e) {
        if (e instanceof NumberExpression) return 0;
        BinaryExpression b = (BinaryExpression) e;
        return 1 + countOperators(b.getLeft()) + countOperators(b.getRight());
    }

    /** 递归检查所有除法子表达式结果是否 ≤ 1 */
    private static boolean divisionOk(Expression e) {
        if (e instanceof NumberExpression) return true;
        BinaryExpression b = (BinaryExpression) e;

        if (b.getOperator() == Operator.DIV) {
            Fraction q = b.evaluate();
            if (q.compareTo(Fraction.ONE) > 0) return false;
        }
        return divisionOk(b.getLeft()) && divisionOk(b.getRight());
    }

    @RepeatedTest(50)
    void generatedExprIsNeverNegative() {
        ExpressionGenerator gen = new ExpressionGenerator(10, new Random());
        Expression e = gen.generate(3);
        assertTrue(e.evaluate().compareTo(Fraction.ZERO) >= 0);
    }

    @RepeatedTest(50)
    void generatedExprHasAtMostThreeOperators() {
        ExpressionGenerator gen = new ExpressionGenerator(10, new Random());
        Expression e = gen.generate(3);
        assertTrue(countOperators(e) <= 3);
    }

    @RepeatedTest(30)
    void divisionAlwaysProducesProperFraction() {
        ExpressionGenerator gen = new ExpressionGenerator(10, new Random());
        Expression e = gen.generate(3);
        assertTrue(divisionOk(e), "存在除法结果 > 1 的子表达式：" + e);
    }

    @Test
    void smallRangeDoesNotCrash() {
        // r=1 时仅能生成 0
        ExpressionGenerator gen = new ExpressionGenerator(1, new Random());
        for (int i = 0; i < 20; i++) {
            Expression e = gen.generate(3);
            assertNotNull(e);
            assertEquals(Fraction.ZERO, e.evaluate());
        }
    }

    @Test
    void randomValueStaysInRange() {
        // 生成 200 个值，全部 < range
        int range = 10;
        ExpressionGenerator gen = new ExpressionGenerator(range, new Random());
        for (int i = 0; i < 200; i++) {
            Expression e = gen.generate(0);   // budget=0 → 必为叶子
            Fraction v = e.evaluate();
            assertTrue(v.compareTo(Fraction.ZERO) >= 0);
            assertTrue(v.compareTo(Fraction.integer(range)) < 0,
                    "数值必须 < range: " + v);
        }
    }
}