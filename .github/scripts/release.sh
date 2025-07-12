#!/bin/bash
set -euo pipefail

version_file="build.gradle"

run_fastlane() {
  local version_code
  version_code=$(grep -o "versionCode: [0-9]*" $version_file | grep -o "[0-9]*")

  export VERSION_CODE=$version_code
  fastlane release
}

run_fastlane
