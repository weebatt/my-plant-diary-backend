#!/usr/bin/env bash
set -euo pipefail

ALLOWED_TYPES_REGEX=${1:-"feat|refactor|fix|chore|test|docs|ci"}
EVENT_NAME=${2:-"push"}
BASE_REF=${3:-"main"}

if [[ "$EVENT_NAME" == "pull_request" && -n "$BASE_REF" ]]; then
  RANGE="origin/${BASE_REF}..HEAD"
else
  RANGE="HEAD~1..HEAD"
fi

echo "Validating commit messages in range: $RANGE"

FAIL=0
while IFS= read -r SUBJECT; do
  if [[ -z "$SUBJECT" ]]; then
    continue
  fi
  if [[ ! "$SUBJECT" =~ ^($ALLOWED_TYPES_REGEX)(\([a-zA-Z0-9_-]+\))?:\ .+ ]]; then
    echo "Invalid commit subject: '$SUBJECT'"
    FAIL=1
  fi
done < <(git log --pretty=%s $RANGE)

if [[ $FAIL -ne 0 ]]; then
  echo "\nCommit message check failed. Expected format: type(scope)?: subject"
  echo "Allowed types: $ALLOWED_TYPES_REGEX"
  exit 1
fi

echo "Commit messages are valid."
exit 0

