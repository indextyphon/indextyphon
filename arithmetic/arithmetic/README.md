# 小学四则运算题目生成器

Maven 构建的命令行程序，生成符合约束的小学四则运算题目并支持自动批改。

## 构建

```bash
mvn clean package
```

产物：`target/arithmetic.jar`

## 生成题目

```bash
java -jar target/arithmetic.jar -n 10 -r 10
```

参数：
- `-n` 题目数量（默认 10）
- `-r` 数值范围 `[0, r)`，必填

输出：`Exercises.txt`、`Answers.txt`

## 批改答案

```bash
java -jar target/arithmetic.jar -e Exercises.txt -a Answers.txt
```

输出：`Grade.txt`

格式：
```
Correct: 8 (1, 2, 3, 5, 6, 7, 9, 10)
Wrong: 2 (4, 8)
```

## 特性

- 精确分数运算，无浮点误差
- 输出真分数 `3/5`、带分数 `2'3/8`
- 减法结果非负，除法结果 ≤ 1
- 单题运算符数量 ≤ 3
- 题目去重（支持交换律）
- 支持一万道题目生成