package com.mathquiz.model;

/**
 * 二元运算节点。
 * <p>负责：</p>
 * <ul>
 *   <li>递归求值（分派到 Fraction 的四则运算）</li>
 *   <li>规范形式生成（判重用，可交换运算符排序左右）</li>
 *   <li>toString：根据左右子节点的优先级自动决定是否加括号</li>
 * </ul>
 */
public final class BinaryExpression extends Expression {

    private final Operator operator;
    private final Expression left;
    private final Expression right;

    public BinaryExpression(Operator operator, Expression left, Expression right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    public Operator   getOperator() { return operator; }
    public Expression getLeft()     { return left; }
    public Expression getRight()    { return right; }

    /** 递归求值 */
    @Override
    public Fraction evaluate() {
        Fraction a = left.evaluate();
        Fraction b = right.evaluate();
        switch (operator) {
            case ADD: return a.add(b);
            case SUB: return a.sub(b);
            case MUL: return a.mul(b);
            case DIV: return a.div(b);
            default:  throw new IllegalStateException("未知运算符: " + operator);
        }
    }

    @Override
    public int precedence() {
        return operator.precedence();
    }

    /**
     * 规范形式：
     * <pre>
     *   可交换运算符 (+ ×)：先排序左右子树的 canonical 串，再拼接
     *   不可交换 (- ÷)：保持原顺序
     * </pre>
     * 用括号包裹是为了避免 "12" 和 "1" + "2" 的歧义。
     */
    @Override
    public String canonical() {
        String a = left.canonical();
        String b = right.canonical();
        if (operator.isCommutative() && a.compareTo(b) > 0) {
            String tmp = a; a = b; b = tmp;
        }
        return "(" + a + operator.symbol() + b + ")";
    }

    /**
     * 输出时最小化括号：
     * <pre>
     *   左子优先级 < 自身 → 左子加括号      例：(1+2)×3
     *   右子优先级 < 自身 → 右子加括号      例：3×(1+2)
     *   右子同优先级且是二元运算 → 右子加括号  例：3-(1-2)
     * </pre>
     * 因为四则运算左结合，左子同优先级不需要加括号（1-2-3 即 (1-2)-3）。
     */
    @Override
    public String toString() {
        // 左子节点
        String ls = left.precedence() < precedence()
                ? "(" + left + ")"
                : left.toString();

        // 右子节点
        boolean needParen = right.precedence() < precedence()
                || (right.precedence() == precedence() && right instanceof BinaryExpression);
        String rs = needParen ? "(" + right + ")" : right.toString();

        return ls + " " + operator.symbol() + " " + rs;
    }
}