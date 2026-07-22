#!/usr/bin/env bash
set -euo pipefail

if ! command -v adb >/dev/null 2>&1; then
  echo "adb is required" >&2
  exit 1
fi

device="$(adb devices | awk 'NR > 1 && $2 == "device" { print $1; exit }')"
if [[ -z "$device" ]]; then
  echo "Connect one unlocked Android 10+ device or start an emulator" >&2
  exit 1
fi

arguments=(
  :baselineprofile:connectedBenchmarkReleaseAndroidTest
  -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=Macrobenchmark
  --console=plain
)
if [[ "$device" == emulator-* ]]; then
  if [[ "${ALLOW_EMULATOR_BENCHMARK:-0}" != "1" ]]; then
    echo "Macrobenchmark requires a physical device for representative numbers." >&2
    echo "Use ALLOW_EMULATOR_BENCHMARK=1 only for an instrumentation smoke test." >&2
    exit 1
  fi
  arguments+=(
    -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR
  )
fi

./gradlew "${arguments[@]}"

echo "Macrobenchmark reports: baselineprofile/build/outputs/connected_android_test_additional_output/"
