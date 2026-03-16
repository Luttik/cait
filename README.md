# Cait

**Cait** (pronounced _Kate_) is an AI assistant that lives in your car — loosely inspired by K.I.T.T. from Knight Rider.

It ships as an **Android Auto** application. You press a voice button, speak a request, and Cait's AI agent processes it, asks for your approval, and then acts on your behalf.

## Current Scope (v1)

- **Voice input** via the car microphone, triggered by a single button on the Android Auto UI.
- **One tool — `write_document`**: the agent drafts a Markdown document and saves it to Google Drive.
- **Human-in-the-loop approval**: every tool execution is shown to the driver for explicit approve / reject before anything happens.

## Architecture

```
┌─────────────────────────────┐         ┌──────────────────────────────┐
│  Android Auto App (Kotlin)  │  AG-UI  │  Python Backend              │
│                             │◄───────►│  FastAPI + LangGraph Agent   │
│  • Voice button + mic       │   SSE   │  • write_document tool       │
│  • Approval screen          │         │  • Google Drive integration  │
│  • AG-UI Kotlin SDK         │         │  • ag-ui-langgraph           │
└─────────────────────────────┘         └──────────────────────────────┘
```

| Layer    | Tech                                                                 |
|----------|----------------------------------------------------------------------|
| Frontend | Kotlin, Android Car App Library 1.7, AG-UI Kotlin SDK                |
| Protocol | AG-UI (SSE-based agent ↔ user interaction)                           |
| Backend  | Python 3.12+, FastAPI, LangGraph, LangChain                          |
| Storage  | Google Drive API v3                                                  |

## Project Structure

```
cait/
├── android/            # Android Auto app (Kotlin + Gradle)
│   ├── app/
│   └── build.gradle.kts
├── backend/            # Python backend (Poetry)
│   ├── cait_backend/
│   └── pyproject.toml
├── scripts/            # Environment setup helpers
├── AGENTS.md           # Instructions for AI coding agents
└── README.md           # ← you are here
```

## Prerequisites

- **JDK 17+**
- **Android SDK** with platform 35, build-tools, and the Android Auto DHU
- **Python 3.12+** with Poetry
- **Google Cloud** project with Drive API enabled and OAuth credentials

## Getting Started

### Backend

```bash
cd backend
poetry install
poetry run uvicorn cait_backend.server:app --reload
```

### Android App

```bash
cd android
./gradlew installDebug
# Then start the DHU:
adb forward tcp:5277 tcp:5277
desktop-head-unit
```

## Roadmap

- [x] Project scaffolding
- [ ] Voice input → agent pipeline
- [ ] Approval UI in Android Auto
- [ ] Google Drive document creation
- [ ] Additional tools (calendar, navigation, messaging)
