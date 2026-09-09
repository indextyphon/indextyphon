#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
论文查重程序
用法：python main.py [原文文件] [抄袭版论文文件] [答案文件]
输出：答案文件中写入相似度（浮点数，保留两位小数）
算法：基于字符 bigram 的余弦相似度
"""

import sys
import re
from collections import Counter


def parse_args():
    """解析命令行参数，返回 (原文路径, 抄袭版路径, 答案路径)"""
    if len(sys.argv) != 4:
        print("用法: python main.py <原文文件> <抄袭版论文文件> <答案文件>")
        sys.exit(1)
    return sys.argv[1], sys.argv[2], sys.argv[3]


def read_file(file_path):
    """读取文件内容，尝试多种编码"""
    encodings = ['utf-8', 'gbk', 'gb2312', 'utf-16']
    for enc in encodings:
        try:
            with open(file_path, 'r', encoding=enc) as f:
                return f.read()
        except UnicodeDecodeError:
            continue
        except FileNotFoundError:
            print(f"错误：文件不存在 - {file_path}")
            sys.exit(1)
    print(f"错误：无法解码文件 - {file_path}")
    sys.exit(1)


def clean_text(text):
    """
    清洗文本：只保留中文、英文字母、数字，去除空格、换行、标点等。
    """
    pattern = re.compile(r'[^\u4e00-\u9fffA-Za-z0-9]')
    cleaned = pattern.sub('', text)
    return cleaned


def get_bigram_counts(text):
    """
    生成字符 bigram 的计数 Counter。
    text: 清洗后的字符串（仅含中英数字）
    """
    bigrams = []
    for i in range(len(text) - 1):
        bigram = text[i:i+2]
        bigrams.append(bigram)
    return Counter(bigrams)


def cosine_similarity(counter1, counter2):
    """
    计算两个 bigram Counter 的余弦相似度。
    返回 float 值，范围 [0,1]。
    """
    all_bigrams = set(counter1.keys()) | set(counter2.keys())
    if not all_bigrams:
        return 0.0

    vec1 = [counter1.get(bg, 0) for bg in all_bigrams]
    vec2 = [counter2.get(bg, 0) for bg in all_bigrams]

    dot_product = sum(v1 * v2 for v1, v2 in zip(vec1, vec2))
    norm1 = sum(v ** 2 for v in vec1) ** 0.5
    norm2 = sum(v ** 2 for v in vec2) ** 0.5

    if norm1 == 0 or norm2 == 0:
        return 0.0

    return dot_product / (norm1 * norm2)


def main():
    orig_path, plag_path, ans_path = parse_args()

    orig_text = read_file(orig_path)
    orig_clean = clean_text(orig_text)
    orig_counter = get_bigram_counts(orig_clean)

    plag_text = read_file(plag_path)
    plag_clean = clean_text(plag_text)
    plag_counter = get_bigram_counts(plag_clean)

    similarity = cosine_similarity(orig_counter, plag_counter)

    with open(ans_path, 'w', encoding='utf-8') as f:
        f.write(f"{similarity:.2f}")

    print(f"相似度: {similarity:.2f}")


if __name__ == "__main__":
    main()
