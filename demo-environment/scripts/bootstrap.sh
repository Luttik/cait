#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ANDROID_DIR="$ROOT/android"

mkdir -p "$ROOT/demo-environment/artifacts"

if [[ ! -d "$ANDROID_DIR" ]]; then
  echo "Expected Kotlin project at $ANDROID_DIR" >&2
  exit 1
fi

if [[ -z "${ANDROID_HOME:-}" ]]; then
  echo "ANDROID_HOME is not set. Install Android SDK + platform 35 + build-tools, then export ANDROID_HOME." >&2
  echo "You can still open the project in Android Studio; Gradle will use its embedded JDK." >&2
  exit 1
fi

(cd "$ANDROID_DIR" && ./gradlew --version)
(cd "$ANDROID_DIR" && ./gradlew :app:assembleDebug)

echo "Bootstrap complete. Build: cd android && ./gradlew :app:assembleDebug"
