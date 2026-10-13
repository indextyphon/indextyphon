# agent_main.py
from pathlib import Path

import asyncio
import os
from dotenv import load_dotenv
from langchain_openai import ChatOpenAI
from langchain.agents import create_agent
from langchain.mcp import MCPAdapter

load_dotenv()

SYSTEM_PROMPT = """# 角色
你是个人事务智能助手，拥有一组MCP工具，可以管理待办清单、查询城市天气。

## 可用工具
1. add_todo：添加一条待办事项
2. list_todos：列出全部待办事项
3. complete_todo：将指定ID待办标记为完成
4. delete_todo：删除指定ID的待办
5. get_weather：查询指定城市当前天气
6. export_todos：导出所有待办为Markdown文件

## 约束
1. 所有待办、天气信息**必须调用工具获取，禁止凭空编造**。
2. 思考过程不用展示给用户，只返回自然中文回答。
3. 用户如果指令模糊，主动询问补充信息。
    - 添加待办：确认待办内容
    - 查询天气：确认城市名称
    - 修改/删除待办：确认待办ID
4. 导出完成后，告知用户生成的md文件名。
5. 用户输入`quit`，结束对话。
"""


def build_llm():
    """构造 LLM 实例。"""
    return ChatOpenAI(
        model="qwen3.5-122b-a10b",                              # 使用百炼支持的模型
        api_key=os.getenv("DASHSCOPE_API_KEY"),          # 从环境变量读取 Key
        base_url=os.getenv("DASHSCOPE_BASE_URL"),        # 从环境变量读取 Base URL
        temperature=0,
    )


async def chat_loop():
    """命令行交互循环。"""
    # MCPAdapter 使用 async with 管理连接生命周期
    async with MCPAdapter(Path("mcp_server.py")) as adapter:
        tools = await adapter.list_tools()
        print(f"✅ 已加载 {len(tools)} 个工具：")
        for t in tools:
            desc = (t.description or "")[:60].replace("\n", " ")
            print(f"   - {t.name}: {desc}")

        agent = create_agent(
            build_llm(),
            tools,
            system_prompt=SYSTEM_PROMPT,
        )

        messages = []
        print("\n" + "=" * 50)
        print("🤖 个人事务智能助手已就绪（输入 quit 退出）")
        print("=" * 50)

        while True:
            try:
                user_input = input("\n你: ").strip()
            except (EOFError, KeyboardInterrupt):
                print("\n👋 再见！")
                break

            if not user_input:
                continue
            if user_input.lower() in ("quit", "exit", "q"):
                print("👋 再见！")
                break

            messages.append({"role": "user", "content": user_input})

            try:
                result = await agent.ainvoke({"messages": messages})
            except Exception as e:
                print(f"\n❌ 调用失败：{e}")
                messages.pop()   # 回滚未成功的用户消息
                continue

            # 取最后一条 AI 回复
            reply = ""
            for msg in reversed(result["messages"]):
                # LangChain 1.0 使用 .type；老版本可能是 .role
                msg_type = getattr(msg, "type", None) or getattr(msg, "role", None)
                if msg_type in ("ai", "assistant") and getattr(msg, "content", None):
                    reply = msg.content
                    break

            messages.append({"role": "assistant", "content": reply})
            print(f"\n助手: {reply}")


if __name__ == "__main__":
    asyncio.run(chat_loop())