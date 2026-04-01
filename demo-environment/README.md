# Demo engineering environment (Android Auto, Kotlin)

This folder supports **Cursor agents** and humans working on **Android Auto** apps using the same stack as the main Cait app: **Kotlin**, **Android for Cars App Library**, and Gradle.

## Google-supported reference code

The official samples for the **Android for Cars App Library** live here:

- [https://github.com/android/car-samples](https://github.com/android/car-samples)

Clone them side-by-side for full examples (navigation, parking, etc.). This repo keeps a **small Kotlin scaffold** under `android/` so agents can build without copying all of `car-samples` into git.

```bash
./demo-environment/scripts/clone-car-samples.sh
# → demo-environment/vendor/car-samples (gitignored)
```

## Prerequisites

- **JDK 17+** (Gradle uses JVM 17 in this project)
- **Android SDK** with **API 35** platform and **build-tools 35.x**
- Set **`ANDROID_HOME`** to your SDK root (required for command-line `./gradlew` builds)

On **Windows**, use PowerShell from the repo root:

```powershell
$env:ANDROID_HOME = "C:\Users\You\AppData\Local\Android\Sdk"
cd android
.\gradlew.bat :app:assembleDebug
```

## Quick start

From the repository root (Linux / macOS):

```bash
export ANDROID_HOME="$HOME/Android/Sdk"   # adjust to your install
./demo-environment/scripts/bootstrap.sh     # runs :app:assembleDebug
```

Or build only the app module:

```bash
cd android
./gradlew :app:assembleDebug
```

Use the **Desktop Head Unit (DHU)** to exercise Android Auto UI (see root `README.md`).

## Docker (optional, JDK-only)

The image provides a consistent **JDK 17** for Gradle. You still need an **Android SDK** on the host (mount it into the container and set `ANDROID_HOME`), or run builds on the host / Android Studio instead.

```bash
cd demo-environment
docker compose build
docker compose run --rm -e ANDROID_HOME=/android-sdk -v "$ANDROID_HOME:/android-sdk:ro" agent \
  bash -lc "cd /workspace/android && ./gradlew :app:assembleDebug"
```

## Layout

| Path | Purpose |
|------|---------|
| `../android/` | **Kotlin** Android Auto app (Car App Library) — primary boilerplate |
| `Dockerfile` | Agent image: JDK 17 + git |
| `docker-compose.yml` | `agent` service; mount repo at `/workspace` |
| `scripts/bootstrap.sh` | Verify SDK, run `assembleDebug` |
| `scripts/clone-car-samples.sh` | Clone Google `android/car-samples` into `vendor/` (gitignored) |
| `artifacts/` | Optional local outputs (gitignored) |
| `vendor/` | Cloned reference repos (gitignored) |

## Demo videos

Screen recordings are **not** generated from Python in this repo. Use **Android Studio screen record**, **adb screenrecord**, or the **DHU** capture flow when you need demo footage of the Kotlin UI.
