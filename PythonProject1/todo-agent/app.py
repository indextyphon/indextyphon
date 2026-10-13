# app.py
import asyncio
import threading
import os
from pathlib import Path
import gradio as gr
from dotenv import load_dotenv
from langchain_openai import ChatOpenAI
from langchain.agents import create_agent
from langchain.mcp import MCPAdapter

load_dotenv()

SYSTEM_PROMPT = """你是一个个人事务智能助手，能帮助用户管理待办事项和查询天气。
你可以调用工具来添加/查看/完成/删除待办、查询天气、导出清单。
请始终用中文回复，语气友好简洁。当用户请求涉及以上操作时，主动调用对应工具。"""

_memory = []


def build_llm():
    return ChatOpenAI(
        model="qwen3.5-122b-a10b",
        api_key=os.getenv("DASHSCOPE_API_KEY"),
        base_url=os.getenv("DASHSCOPE_BASE_URL"),
        temperature=0,
        timeout=60,
        max_retries=0,
    )


def extract_reply(messages) -> str:
    for msg in reversed(messages):
        cls_name = type(msg).__name__
        content = getattr(msg, "content", None)
        if cls_name in ("AIMessage", "AIMessageChunk") and content:
            if isinstance(content, str):
                return content
            parts = []
            for block in content:
                if isinstance(block, dict) and block.get("type") == "text":
                    parts.append(block.get("text", ""))
                elif isinstance(block, str):
                    parts.append(block)
            if parts:
                return "".join(parts)
    return ""


async def handle_message(user_message: str) -> str:
    """在常驻 loop 里完整跑一遍：连接 → 工具加载 → agent → 调用。"""
    messages = _memory + [{"role": "user", "content": user_message}]

    async with MCPAdapter(Path("mcp_server.py")) as adapter:
        tools = await adapter.list_tools()
        agent = create_agent(build_llm(), tools, system_prompt=SYSTEM_PROMPT)
        result = await agent.ainvoke({"messages": messages})

    reply = extract_reply(result["messages"])

    _memory.append({"role": "user", "content": user_message})
    _memory.append({"role": "assistant", "content": reply})
    if len(_memory) > 40:
        del _memory[:-40]

    return reply


# ---- 常驻后台 event loop ----
_bg_loop = asyncio.new_event_loop()


def _run_bg_loop(loop):
    asyncio.set_event_loop(loop)
    loop.run_forever()


_bg_thread = threading.Thread(target=_run_bg_loop, args=(_bg_loop,), daemon=True)
_bg_thread.start()


def chat(message, history):
    """Gradio 同步回调：把任务提交到常驻 loop，等结果。"""
    future = asyncio.run_coroutine_threadsafe(handle_message(message), _bg_loop)
    try:
        return future.result(timeout=180)
    except Exception as e:
        return f"❌ 调用失败：{type(e).__name__}: {e}"


demo = gr.ChatInterface(
    fn=chat,
    title="📋 个人事务智能助手3.0-Astra",
    description="我可以帮你管理待办、查询天气、导出清单。试试说「帮我加一条待办：明天开会」",
    examples=[
        "帮我加一条待办：后天交数据库作业",
        "看看我有哪些待办",
        "北京今天天气怎么样",
        "把待办导出成文件",
    ],
)

if __name__ == "__main__":
    demo.launch(server_name="127.0.0.1", server_port=7860)