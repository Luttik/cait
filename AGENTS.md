# Agent Instructions

This file provides context for AI coding agents (e.g. Cursor Background Agents) working on this repository.

## Project Context

Always read `README.md` first — it contains the current project goals, architecture, and structure.

## Tech Stack

- **Android frontend**: Kotlin, Android Car App Library (androidx.car.app), AG-UI Kotlin SDK
- **Backend**: Python 3.12+, FastAPI, LangGraph, LangChain, ag-ui-langgraph
- **Protocol**: AG-UI (agent-user interaction protocol over SSE)
- **Storage**: Google Drive API v3

## Conventions

### Kotlin / Android

- Target Android API 35, min SDK 29
- Use Kotlin coroutines and Flows for async work
- Package: `com.cait.auto`
- Follow standard Android project layout under `android/`

### Python

- Use `ruff` for formatting and linting
- Use `pytest` for tests
- Use `poetry` for dependency management
- Always add return type annotations (including `-> None`)
- Python 3.12+ — use built-in generics (`list`, `dict`) not `typing.List`, `typing.Dict`

## Key Files

| File | Purpose |
|------|-------|
| `android/app/src/main/kotlin/com/cait/auto/CaitCarAppService.kt` | Android Auto entry point |
| `android/app/src/main/kotlin/com/cait/auto/CaitScreen.kt` | Main UI with voice button |
| `android/app/src/main/kotlin/com/cait/auto/ApprovalScreen.kt` | Tool approval UI |
| `android/app/src/main/kotlin/com/cait/auto/AgentClient.kt` | SSE client for AG-UI backend |
| `backend/cait_backend/server.py` | FastAPI + AG-UI endpoint |
| `backend/cait_backend/agent.py` | LangGraph agent definition |
| `backend/cait_backend/tools/google_drive.py` | write_document tool |

## When Making Changes

1. Update `README.md` if the change affects architecture, features, or setup instructions.
2. Run `ruff check` and `ruff format` on Python changes.
3. Run `./gradlew build` to verify Android changes compile.
4. Add or update tests for new backend functionality.
