package com.mathquiz.model;

/**
 * 不可变的精确分数。
 * <p>设计要点：</p>
 * <ul>
 *   <li>始终以最简形式存储：{@code num/den}，其中 {@code den > 0} 且 gcd = 1</li>
 *   <li>构造私有，通过 {@link #of(long, long)} 工厂方法创建，保证不变式</li>
 *   <li>{@link #toString()} 自动区分真分数、带分数、整数三种输出</li>
 *   <li>实现 {@link Comparable} 以便比较大小（用于减法交换、除法结果 ≤ 1 判定）</li>
 * </ul>
 * <p>为什么不用 double？因为题目要求精确输出 7/24 这类分数，浮点会有误差。</p>
 */
public final class Fraction implements Comparable<Fraction> {

    public static final Fraction ZERO = new Fraction(0, 1);
    public static final Fraction ONE  = new Fraction(1, 1);

    private final long num;   // 分子（可正可负）
    private final long den;   // 分母（恒 > 0）

    /** 私有构造：外部只能通过 of() 创建，避免出现未约分/负分母的对象 */
    private Fraction(long num, long den) {
        this.num = num;
        this.den = den;
    }

    /**
     * 工厂方法：创建已约分的分数。
     *
     * @param n 分子（可为负）
     * @param d 分母（不可为 0，可为负，会自动规范化）
     */
    public static Fraction of(long n, long d) {
        if (d == 0) throw new ArithmeticException("分母不能为 0");
        if (d < 0) { n = -n; d = -d; }        // 统一符号到分子
        long g = gcd(Math.abs(n), d);
        if (g == 0) g = 1;                    // 处理 n=0 的边界
        return new Fraction(n / g, d / g);
    }

    /** 创建整数分数 */
    public static Fraction integer(long n) {
        return new Fraction(n, 1);
    }

    /** 欧几里得算法求最大公约数 */
    private static long gcd(long a, long b) {
        while (b != 0) {
            long t = a % b;
            a = b;
            b = t;
        }
        return a;
    }

    // ---------- 四则运算 ----------

    public Fraction add(Fraction o) { return of(num * o.den + o.num * den, den * o.den); }
    public Fraction sub(Fraction o) { return of(num * o.den - o.num * den, den * o.den); }
    public Fraction mul(Fraction o) { return of(num * o.num, den * o.den); }
    public Fraction div(Fraction o) { return of(num * o.den, den * o.num); }

    public boolean isZero() { return num == 0; }

    // ---------- 比较 & 判等 ----------

    /** 交叉相乘比较大小（避免浮点误差） */
    @Override
    public int compareTo(Fraction o) {
        return Long.compare(num * o.den, o.num * den);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Fraction)) return false;
        Fraction f = (Fraction) obj;
        return num == f.num && den == f.den;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(num * 31 + den);
    }

    // ---------- 格式化 ----------

    /**
     * 三种输出格式：
     * <pre>
     *   den == 1        →  "5"
     *   num < den       →  "3/5"        真分数
     *   否则            →  "2'3/8"      带分数
     * </pre>
     */
    @Override
    public String toString() {
        if (den == 1) return String.valueOf(num);
        if (num < den) return num + "/" + den;

        long whole = num / den;
        long rem = num % den;
        return rem == 0 ? String.valueOf(whole) : whole + "'" + rem + "/" + den;
    }
}