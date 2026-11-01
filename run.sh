#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
rm -rf build && mkdir -p build
javac -encoding UTF-8 -d build $(find src -name '*.java')
java -cp build conveyor.Main "$@"
