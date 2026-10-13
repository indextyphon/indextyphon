# mcp_server.py
import sqlite3
import requests
from fastmcp import FastMCP

mcp = FastMCP("个人事务助手工具集")
DB_PATH = "todos.db"


def init_db():
    conn = sqlite3.connect(DB_PATH)
    conn.execute("""
        CREATE TABLE IF NOT EXISTS todos(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            content TEXT NOT NULL,
            due_date TEXT,
            status TEXT DEFAULT 'pending',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)
    conn.commit()
    conn.close()


@mcp.tool()
def add_todo(content: str, due_date: str = "") -> str:
    """添加一条待办事项。

    Args:
        content: 待办事项的内容描述
        due_date: 截止日期，格式为 YYYY-MM-DD，不填则无截止日期
    """
    conn = sqlite3.connect(DB_PATH)
    conn.execute(
        "INSERT INTO todos (content, due_date) VALUES (?, ?)",
        (content, due_date)
    )
    conn.commit()
    conn.close()
    return f"已添加待办：{content}（截止：{due_date or '未设定'}）"


@mcp.tool()
def list_todos(status: str = "pending") -> str:
    """列出待办事项。

    Args:
        status: 筛选状态，可选 pending（未完成）、completed（已完成）、all（全部）
    """
    conn = sqlite3.connect(DB_PATH)
    if status == "all":
        rows = conn.execute(
            "SELECT id, content, due_date, status FROM todos ORDER BY id"
        ).fetchall()
    else:
        rows = conn.execute(
            "SELECT id, content, due_date, status FROM todos WHERE status = ? ORDER BY id",
            (status,)
        ).fetchall()
    conn.close()

    if not rows:
        return "当前没有符合条件的待办事项。"

    lines = []
    for r in rows:
        check = "✅" if r[3] == "completed" else "⬜"
        lines.append(f"{check} [{r[0]}] {r[1]}  |  截止: {r[2] or '—'}")
    return "\n".join(lines)


@mcp.tool()
def complete_todo(todo_id: int) -> str:
    """将指定 ID 的待办事项标记为已完成。

    Args:
        todo_id: 待办事项的 ID 编号
    """
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.execute(
        "UPDATE todos SET status = 'completed' WHERE id = ?", (todo_id,)
    )
    conn.commit()
    affected = cursor.rowcount
    conn.close()
    if affected == 0:
        return f"没有找到 ID 为 {todo_id} 的待办事项。"
    return f"待办 [{todo_id}] 已标记为完成 ✅"


@mcp.tool()
def delete_todo(todo_id: int) -> str:
    """删除指定 ID 的待办事项。

    Args:
        todo_id: 待办事项的 ID 编号
    """
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.execute("DELETE FROM todos WHERE id = ?", (todo_id,))
    conn.commit()
    affected = cursor.rowcount
    conn.close()
    if affected == 0:
        return f"没有找到 ID 为 {todo_id} 的待办事项。"
    return f"待办 [{todo_id}] 已删除 🗑️"


@mcp.tool()
def get_weather(city: str) -> str:
    """查询指定城市的当前天气信息。

    Args:
        city: 城市名称，例如 北京、上海、广州
    """
    try:
        resp = requests.get(f"https://wttr.in/{city}?format=j1", timeout=8)
        data = resp.json()
        cur = data["current_condition"][0]
        desc = cur["weatherDesc"][0]["value"]
        return (
            f"📍 {city} 当前天气：{desc}，"
            f"温度 {cur['temp_C']}°C，"
            f"体感 {cur['FeelsLikeC']}°C，"
            f"湿度 {cur['humidity']}%，"
            f"风速 {cur['windspeedKmph']} km/h"
        )
    except Exception as e:
        return f"天气查询失败：{str(e)}"


@mcp.tool()
def export_todos(filepath: str = "todos_export.md") -> str:
    """将当前所有待办事项导出为 Markdown 文件。

    Args:
        filepath: 导出的文件路径，默认为 todos_export.md
    """
    conn = sqlite3.connect(DB_PATH)
    rows = conn.execute(
        "SELECT content, due_date, status FROM todos ORDER BY id"
    ).fetchall()
    conn.close()

    from datetime import datetime
    with open(filepath, "w", encoding="utf-8") as f:
        f.write("# 📋 待办清单\n\n")
        f.write(f"> 导出时间：{datetime.now():%Y-%m-%d %H:%M}\n\n")
        for r in rows:
            check = "x" if r[2] == "completed" else " "
            f.write(f"- [{check}] {r[0]}  (截止: {r[1] or '—'})\n")

    return f"已导出 {len(rows)} 条待办到 {filepath}"


if __name__ == "__main__":
    init_db()
    mcp.run()