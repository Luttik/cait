#!/usr/bin/env bash
set -euo pipefail

# Clones Google's official car samples (Apache-2.0) for local reference — not vendored into git.
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
DEST="$ROOT/demo-environment/vendor/car-samples"
REPO="https://github.com/android/car-samples.git"

mkdir -p "$ROOT/demo-environment/vendor"

if [[ -d "$DEST/.git" ]]; then
  echo "Already cloned: $DEST"
  git -C "$DEST" pull --ff-only || true
else
  git clone --depth 1 "$REPO" "$DEST"
fi

echo "Car samples at: $DEST"
