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

## Cursor Cloud specific instructions

### Services

| Service | How to run | Notes |
|---------|-----------|-------|
| **Python backend** | `cd backend && poetry run uvicorn cait_backend.server:app --reload` | Serves on port 8000. Health check: `GET /health`. AG-UI agent endpoint: `POST /agent`. |
| **Android app (build)** | `cd android && ./gradlew build` | Requires `ANDROID_HOME=$HOME/android-sdk`. |
| **Android app (install on emulator)** | `cd android && ./gradlew installDebug` | Requires a running emulator (see below). |

### Running checks

- **Backend lint**: `cd backend && poetry run ruff check . && poetry run ruff format --check .`
- **Backend tests**: `cd backend && poetry run pytest -v`
- **Android build**: `cd android && ./gradlew build`
- **Android unit tests (fast, JVM)**: `cd android && ./gradlew testDebugUnitTest` — runs Robolectric + MockWebServer tests, no emulator needed.

### Android emulator in Cloud VMs (no KVM)

Cloud VMs do not have KVM. The emulator runs in software-only mode (`-no-accel`) which is slow (~9 min cold boot) but functional.

**Start the emulator:**
```bash
export ANDROID_HOME="$HOME/android-sdk"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

emulator -avd CaitTestDevice -no-window -no-audio -gpu swiftshader_indirect \
  -no-accel -no-boot-anim -no-snapshot -cores 4 -memory 2048 &
```

**Wait for boot:**
```bash
adb wait-for-device
while [ "$(adb shell getprop sys.boot_completed | tr -d '\r')" != "1" ]; do sleep 10; done
echo "Booted"
```

**Install and verify:**
```bash
cd android && ./gradlew installDebug
adb shell pm list packages | grep cait   # should print: package:com.cait.auto
```

**Key caveats:**
- The AVD `CaitTestDevice` (API 35, x86_64) is pre-created. If missing, create it: `echo no | avdmanager create avd -n CaitTestDevice -k "system-images;android-35;google_apis;x86_64"`
- Cold boot takes ~9 minutes without KVM. Budget for this in your workflow.
- For **fast feedback**, prefer `./gradlew testDebugUnitTest` (JVM tests, ~3 seconds) over emulator-based testing.
- The Android Auto Desktop Head Unit (DHU) is not available in Cloud VMs. For Car App UI testing, use the car-app-testing library in JVM unit tests.

### Environment notes

- `ANDROID_HOME` must be set to `$HOME/android-sdk` for both Gradle and emulator commands.
- JDK 21 (system default) works fine for this project despite `compileOptions` targeting Java 17.
- Poetry virtualenvs are stored in `~/.cache/pypoetry/virtualenvs/`.
- The backend starts without an `OPENAI_API_KEY` (the `/health` endpoint works), but LLM calls via `/agent` require a valid key.
- No Google Drive credentials are needed for tests — they mock the Drive API.
