"""LangGraph agent definition for Cait.

The agent uses an LLM to process user requests and has access to the
``write_document`` tool.  Every tool invocation is gated by a
human-in-the-loop interrupt so the driver can approve or reject before
any side-effect occurs.
"""

from __future__ import annotations

import operator
from typing import Annotated, Any

from langchain_core.messages import AnyMessage, SystemMessage
from langchain_openai import ChatOpenAI
from langgraph.checkpoint.memory import MemorySaver
from langgraph.graph import END, StateGraph
from langgraph.prebuilt import ToolNode
from langgraph.types import interrupt

from cait_backend.tools.google_drive import write_document

SYSTEM_PROMPT = (
    "You are Cait, a helpful AI assistant that lives in the driver's car. "
    "You were inspired by K.I.T.T. from Knight Rider. "
    "Keep responses concise — the driver is on the road. "
    "When the user asks you to write or draft a document, use the "
    "write_document tool with an appropriate title and Markdown body."
)

TOOLS = [write_document]


class AgentState(dict):
    """Typed state for the Cait agent graph."""

    messages: Annotated[list[AnyMessage], operator.add]


def _make_llm() -> ChatOpenAI:
    return ChatOpenAI(model="gpt-4o-mini", temperature=0, streaming=True)


def assistant_node(state: dict[str, Any]) -> dict[str, Any]:
    """Invoke the LLM with the current conversation history."""
    llm = _make_llm().bind_tools(TOOLS)
    messages = state["messages"]

    if not messages or not isinstance(messages[0], SystemMessage):
        messages = [SystemMessage(content=SYSTEM_PROMPT), *messages]

    response = llm.invoke(messages)
    return {"messages": [response]}


def approval_node(state: dict[str, Any]) -> dict[str, Any]:
    """Pause execution and ask the human to approve the pending tool call.

    The interrupt value contains the tool call details so the frontend
    can display them.  The resumed value is expected to be a boolean
    (``True`` = approved).
    """
    last_message = state["messages"][-1]
    tool_calls = getattr(last_message, "tool_calls", None) or []

    if not tool_calls:
        return state

    approved = interrupt(
        {
            "action": "approve_tool_call",
            "tool_calls": [
                {"name": tc["name"], "args": tc["args"]} for tc in tool_calls
            ],
        }
    )

    if not approved:
        from langchain_core.messages import AIMessage

        return {
            "messages": [
                AIMessage(content="Understood — I've cancelled that action."),
            ]
        }

    return state


def _should_continue(state: dict[str, Any]) -> str:
    """Route after the assistant node: if there are tool calls go to
    approval, otherwise finish."""
    last = state["messages"][-1]
    if getattr(last, "tool_calls", None):
        return "approval"
    return END


def _after_approval(state: dict[str, Any]) -> str:
    """Route after approval: if the last message still has tool calls,
    execute them; otherwise the user rejected and we end."""
    last = state["messages"][-1]
    if getattr(last, "tool_calls", None):
        return "tools"
    return END


def build_graph() -> StateGraph:
    """Construct and compile the Cait agent graph."""
    builder = StateGraph(dict)

    builder.add_node("assistant", assistant_node)
    builder.add_node("approval", approval_node)
    builder.add_node("tools", ToolNode(TOOLS))

    builder.set_entry_point("assistant")

    builder.add_conditional_edges("assistant", _should_continue)
    builder.add_conditional_edges("approval", _after_approval)
    builder.add_edge("tools", "assistant")

    memory = MemorySaver()
    return builder.compile(checkpointer=memory)


graph = build_graph()
