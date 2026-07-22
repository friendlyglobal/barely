#!/usr/bin/env bash
set -euo pipefail

required_variables=(
  BARELY_KEYSTORE_FILE
  BARELY_KEYSTORE_PASSWORD
  BARELY_KEY_ALIAS
  BARELY_KEY_PASSWORD
)
for variable in "${required_variables[@]}"; do
  if [[ -z "${!variable:-}" ]]; then
    echo "Missing required signing variable: ${variable}" >&2
    exit 1
  fi
done

if [[ -z "${ANDROID_HOME:-}" ]]; then
  echo "ANDROID_HOME must point to the Android SDK." >&2
  exit 1
fi

./gradlew clean testDebugUnitTest lintDebug assembleDebug assembleRelease bundleRelease

apk="app/build/outputs/apk/release/app-release.apk"
aab="app/build/outputs/bundle/release/app-release.aab"
if [[ ! -f "$apk" || ! -f "$aab" ]]; then
  echo "Signed release APK or AAB was not produced." >&2
  exit 1
fi

build_tools="$(find "$ANDROID_HOME/build-tools" -mindepth 1 -maxdepth 1 -type d | sort -V | tail -1)"
"$build_tools/apksigner" verify --verbose --print-certs "$apk"

mkdir -p dist
cp "$apk" dist/Barely-release.apk
cp "$aab" dist/Barely-release.aab
if command -v sha256sum >/dev/null 2>&1; then
  sha256sum dist/Barely-release.apk dist/Barely-release.aab > dist/SHA256SUMS
else
  shasum -a 256 dist/Barely-release.apk dist/Barely-release.aab > dist/SHA256SUMS
fi
cat dist/SHA256SUMS
