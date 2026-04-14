#!/usr/bin/env bash
set -euo pipefail

# Build Windows DLL (using MinGW-w64)
# Usage: ./build-win.sh

if [ -z "${JAVA_HOME:-}" ]; then
  echo "Please set JAVA_HOME to your JDK path" >&2
  exit 1
fi

OUT=idle_time_win.dll
SRC=idle_time_win.c

gcc -shared -o ${OUT} \
  -I"${JAVA_HOME}/include" \
  -I"${JAVA_HOME}/include/win32" \
  ${SRC} -Wl,--kill-at

echo "Built ${OUT}"

