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
- Demo scaffold package: `com.cait.auto.demo` (production target: `com.cait.auto`)
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
| `android/app/src/main/kotlin/com/cait/auto/demo/DemoCarAppService.kt` | Android Auto demo entry (Car App Library) |
| `android/app/src/main/kotlin/com/cait/auto/demo/DemoScreen.kt` | Demo pane UI |
| `demo-environment/README.md` | Agent env + Google car-samples clone instructions |
| `backend/cait_backend/server.py` | FastAPI + AG-UI endpoint (planned) |
| `backend/cait_backend/agent.py` | LangGraph agent definition (planned) |
| `backend/cait_backend/tools/google_drive.py` | write_document tool (planned) |

## When Making Changes

1. Update `README.md` if the change affects architecture, features, or setup instructions.
2. Run `ruff check` and `ruff format` on Python changes.
3. Run `./gradlew build` to verify Android changes compile.
4. Add or update tests for new backend functionality.
