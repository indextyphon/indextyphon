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


def calc_similarity(orig_text, copy_text):
    """计算两篇文本的重复率，保留两位小数"""
    cleaned_orig = clean_text(orig_text)
    cleaned_copy = clean_text(copy_text)
    counter1 = get_bigram_counts(cleaned_orig)
    counter2 = get_bigram_counts(cleaned_copy)
    sim = cosine_similarity(counter1, counter2)
    return round(sim, 2)


def write_result(file_path, rate):
    """将结果写入答案文件，保留两位小数"""
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(f"{rate:.2f}")


def main():
    if len(sys.argv) != 4:
        print("用法: python main.py <原文文件> <抄袭版论文文件> <答案文件>")
        sys.exit(1)

    orig_path = sys.argv[1]
    copy_path = sys.argv[2]
    ans_path = sys.argv[3]

    orig = read_file(orig_path)
    copy = read_file(copy_path)
    rate = calc_similarity(orig, copy)
    write_result(ans_path, rate)


if __name__ == "__main__":
    main()
