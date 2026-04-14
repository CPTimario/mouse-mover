#!/usr/bin/env bash
set -euo pipefail

# Build macOS dynamic library for JNI (libidle_time_mac.dylib)
# Usage: ./build-mac.sh

if [ -z "${JAVA_HOME:-}" ]; then
  echo "Please set JAVA_HOME to your JDK path" >&2
  exit 1
fi

OUT=libidle_time_mac.dylib
SRC=idle_time_mac.c

clang -dynamiclib -o ${OUT} \
  -I"${JAVA_HOME}/include" \
  -I"${JAVA_HOME}/include/darwin" \
  ${SRC} \
  -framework ApplicationServices

echo "Built ${OUT}"

