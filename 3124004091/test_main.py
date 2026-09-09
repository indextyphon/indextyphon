#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import unittest
import os
import tempfile
import sys
from unittest.mock import patch
import main


class TestPaperChecker(unittest.TestCase):

    def setUp(self):
        self.temp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp_dir.cleanup)

    def _write_temp_file(self, content, filename, encoding='utf-8'):
        path = os.path.join(self.temp_dir.name, filename)
        with open(path, 'w', encoding=encoding) as f:
            f.write(content)
        return path

    def test_identical_texts(self):
        orig = "今天是星期天，天气晴，今天晚上我要去看电影。"
        plag = "今天是星期天，天气晴，今天晚上我要去看电影。"
        orig_path = self._write_temp_file(orig, "orig.txt")
        plag_path = self._write_temp_file(plag, "plag.txt")
        ans_path = os.path.join(self.temp_dir.name, "ans.txt")
        test_args = ["main.py", orig_path, plag_path, ans_path]
        with patch.object(sys, 'argv', test_args):
            main.main()
        with open(ans_path, 'r', encoding='utf-8') as f:
            result = f.read().strip()
        self.assertEqual(result, "1.00")

    def test_completely_different(self):
        orig = "abcdefg"
        plag = "hijklmn"
        orig_path = self._write_temp_file(orig, "orig.txt")
        plag_path = self._write_temp_file(plag, "plag.txt")
        ans_path = os.path.join(self.temp_dir.name, "ans.txt")
        test_args = ["main.py", orig_path, plag_path, ans_path]
        with patch.object(sys, 'argv', test_args):
            main.main()
        with open(ans_path, 'r', encoding='utf-8') as f:
            result = f.read().strip()
        self.assertEqual(result, "0.00")

    def test_partial_similarity(self):
        # 期望相似度为 0.60
        orig = "今天天气很好"
        plag = "今天天气不错"
        orig_path = self._write_temp_file(orig, "orig.txt")
        plag_path = self._write_temp_file(plag, "plag.txt")
        ans_path = os.path.join(self.temp_dir.name, "ans.txt")
        test_args = ["main.py", orig_path, plag_path, ans_path]
        with patch.object(sys, 'argv', test_args):
            main.main()
        with open(ans_path, 'r', encoding='utf-8') as f:
            result = f.read().strip()
        self.assertEqual(result, "0.60")

    def test_empty_files(self):
        orig_path = self._write_temp_file("", "orig.txt")
        plag_path = self._write_temp_file("", "plag.txt")
        ans_path = os.path.join(self.temp_dir.name, "ans.txt")
        test_args = ["main.py", orig_path, plag_path, ans_path]
        with patch.object(sys, 'argv', test_args):
            main.main()
        with open(ans_path, 'r', encoding='utf-8') as f:
            result = f.read().strip()
        self.assertEqual(result, "0.00")

    def test_one_empty_one_not(self):
        orig_path = self._write_temp_file("你好世界", "orig.txt")
        plag_path = self._write_temp_file("", "plag.txt")
        ans_path = os.path.join(self.temp_dir.name, "ans.txt")
        test_args = ["main.py", orig_path, plag_path, ans_path]
        with patch.object(sys, 'argv', test_args):
            main.main()
        with open(ans_path, 'r', encoding='utf-8') as f:
            result = f.read().strip()
        self.assertEqual(result, "0.00")

    def test_punctuation_only(self):
        orig = "，。！？；：""''【】"
        plag = "！？。，"
        orig_path = self._write_temp_file(orig, "orig.txt")
        plag_path = self._write_temp_file(plag, "plag.txt")
        ans_path = os.path.join(self.temp_dir.name, "ans.txt")
        test_args = ["main.py", orig_path, plag_path, ans_path]
        with patch.object(sys, 'argv', test_args):
            main.main()
        with open(ans_path, 'r', encoding='utf-8') as f:
            result = f.read().strip()
        self.assertEqual(result, "0.00")

    def test_single_character_text(self):
        orig = "a"
        plag = "a"
        orig_path = self._write_temp_file(orig, "orig.txt")
        plag_path = self._write_temp_file(plag, "plag.txt")
        ans_path = os.path.join(self.temp_dir.name, "ans.txt")
        test_args = ["main.py", orig_path, plag_path, ans_path]
        with patch.object(sys, 'argv', test_args):
            main.main()
        with open(ans_path, 'r', encoding='utf-8') as f:
            result = f.read().strip()
        self.assertEqual(result, "0.00")

    def test_gbk_encoding(self):
        orig = "中文测试内容"
        plag = "中文测试内容"
        orig_path = self._write_temp_file(orig, "orig_gbk.txt", encoding='gbk')
        plag_path = self._write_temp_file(plag, "plag_gbk.txt", encoding='gbk')
        ans_path = os.path.join(self.temp_dir.name, "ans.txt")
        test_args = ["main.py", orig_path, plag_path, ans_path]
        with patch.object(sys, 'argv', test_args):
            main.main()
        with open(ans_path, 'r', encoding='utf-8') as f:
            result = f.read().strip()
        self.assertEqual(result, "1.00")

    def test_nonexistent_file(self):
        orig_path = os.path.join(self.temp_dir.name, "nonexistent.txt")
        plag_path = self._write_temp_file("test", "plag.txt")
        ans_path = os.path.join(self.temp_dir.name, "ans.txt")
        test_args = ["main.py", orig_path, plag_path, ans_path]
        with patch.object(sys, 'argv', test_args):
            with self.assertRaises(SystemExit):
                main.main()

    def test_wrong_number_of_args(self):
        with patch.object(sys, 'argv', ["main.py"]):
            with self.assertRaises(SystemExit):
                main.main()

    def test_long_text_performance(self):
        orig = "今天天气很好" * 1000
        plag = "今天天气不错" * 1000
        orig_path = self._write_temp_file(orig, "orig_long.txt")
        plag_path = self._write_temp_file(plag, "plag_long.txt")
        ans_path = os.path.join(self.temp_dir.name, "ans.txt")
        import time
        start = time.time()
        test_args = ["main.py", orig_path, plag_path, ans_path]
        with patch.object(sys, 'argv', test_args):
            main.main()
        elapsed = time.time() - start
        self.assertLess(elapsed, 5.0)
        with open(ans_path, 'r', encoding='utf-8') as f:
            result = f.read().strip()
        self.assertRegex(result, r'^\d+\.\d{2}$')


if __name__ == '__main__':
    unittest.main()
