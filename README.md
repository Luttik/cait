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
├── android/            # Android Auto app (Kotlin + Gradle) — runnable Car App Library demo
│   ├── app/
│   └── build.gradle.kts
├── demo-environment/   # Agent bootstrap + link to Google car-samples
├── backend/            # Python backend (Poetry) — planned
│   ├── cait_backend/
│   └── pyproject.toml
├── scripts/            # Environment setup helpers
├── AGENTS.md           # Instructions for AI coding agents
└── README.md           # ← you are here
```

The **Kotlin** scaffold under `android/` is the boilerplate for Android Auto work. For full Google-maintained examples, clone [android/car-samples](https://github.com/android/car-samples) (see `demo-environment/scripts/clone-car-samples.sh`).

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

**Windows (PowerShell):** set `ANDROID_HOME`, then `cd android` and `.\gradlew.bat :app:assembleDebug` (or `installDebug` with a device).

```bash
cd android
./gradlew :app:assembleDebug
# Then start the DHU:
adb forward tcp:5277 tcp:5277
desktop-head-unit
```

Optional: `./demo-environment/scripts/bootstrap.sh` checks `ANDROID_HOME` and runs the same Gradle task.

## Roadmap

- [x] Project scaffolding
- [ ] Voice input → agent pipeline
- [ ] Approval UI in Android Auto
- [ ] Google Drive document creation
- [ ] Additional tools (calendar, navigation, messaging)
