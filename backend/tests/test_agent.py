"""Tests for the Cait LangGraph agent graph structure."""

from __future__ import annotations

from unittest.mock import MagicMock, patch

from langchain_core.messages import AIMessage, HumanMessage

from cait_backend.agent import (
    SYSTEM_PROMPT,
    TOOLS,
    _after_approval,
    _should_continue,
    assistant_node,
    build_graph,
)


def test_tools_list_contains_write_document() -> None:
    assert len(TOOLS) == 1
    assert TOOLS[0].name == "write_document"


def test_system_prompt_mentions_cait() -> None:
    assert "Cait" in SYSTEM_PROMPT


def test_should_continue_returns_end_without_tool_calls() -> None:
    ai_msg = AIMessage(content="Hello!")
    state = {"messages": [ai_msg]}
    assert _should_continue(state) == "__end__"


def test_should_continue_returns_approval_with_tool_calls() -> None:
    ai_msg = AIMessage(
        content="",
        tool_calls=[
            {
                "id": "1",
                "name": "write_document",
                "args": {"title": "t", "markdown_body": "b"},
            }
        ],
    )
    state = {"messages": [ai_msg]}
    assert _should_continue(state) == "approval"


def test_after_approval_routes_to_tools_when_tool_calls_present() -> None:
    ai_msg = AIMessage(
        content="",
        tool_calls=[
            {
                "id": "1",
                "name": "write_document",
                "args": {"title": "t", "markdown_body": "b"},
            }
        ],
    )
    state = {"messages": [ai_msg]}
    assert _after_approval(state) == "tools"


def test_after_approval_routes_to_end_when_no_tool_calls() -> None:
    ai_msg = AIMessage(content="Cancelled.")
    state = {"messages": [ai_msg]}
    assert _after_approval(state) == "__end__"


def test_build_graph_compiles() -> None:
    g = build_graph()
    assert g is not None


@patch("cait_backend.agent._make_llm")
def test_assistant_node_invokes_llm(mock_make_llm: MagicMock) -> None:
    mock_response = AIMessage(content="Hi there!")
    mock_llm_instance = MagicMock()
    mock_llm_instance.bind_tools.return_value = mock_llm_instance
    mock_llm_instance.invoke.return_value = mock_response
    mock_make_llm.return_value = mock_llm_instance

    state = {"messages": [HumanMessage(content="Hello")]}
    result = assistant_node(state)

    assert len(result["messages"]) == 1
    assert result["messages"][0].content == "Hi there!"
    mock_llm_instance.invoke.assert_called_once()
