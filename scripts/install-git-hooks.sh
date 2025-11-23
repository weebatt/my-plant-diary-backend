#!/usr/bin/env bash
set -euo pipefail

git config core.hooksPath .githooks
chmod +x .githooks/* || true
echo "Git hooks installed. Pre-commit will run Detekt before each commit."

