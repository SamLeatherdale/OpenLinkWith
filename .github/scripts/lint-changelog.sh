#!/bin/bash
set -euo pipefail

version_file="build.gradle"
changelog_dir="fastlane/metadata/android/en-US/changelogs"

check_changelog() {
  local version_code
  version_code=$(grep -o "versionCode: [0-9]*" $version_file | grep -o "[0-9]*")

  local changelog_file="${changelog_dir}/${version_code}.txt"
  if [ -f "$changelog_file" ]; then
    echo "Changelog file $changelog_file exists."
  else
    echo "Changelog file $changelog_file does not exist."
    exit 1
  fi
}

check_changelog
