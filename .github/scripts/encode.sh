#!/bin/bash
set -euo pipefail

# Hardcoded list of files
files=(
    "app/google-services.json"
    "fastlane/iam-key.json"
    "gradle/release.keystore"
    "gradle/release.signing"
)

# Empty the output directory
rm -rf out
# Create the output directory if it doesn't exist
mkdir -p out

# Function to translate file path to environment variable name
translate_var_name() {
  local file=$1
  echo "$file" | tr '[:lower:]/.-' '[:upper:]___'
}

# Function to encode files to base64 and write them to new files in the 'out' directory
encode_files() {
  local test_mode=${1:-false}
  for file in "${files[@]}"; do
    if [ -f "$file" ]; then
      local var_name
      var_name=$(translate_var_name "$file")
      local encoded_content
      encoded_content=$(base64 -w 0 "$file")
      echo "$encoded_content" > "out/$var_name"
      echo "Encoded content written to out/$var_name"
      # Also save to GitHub Actions
      if [ "$test_mode" = false ]; then
        gh secret set "$var_name" -b "$encoded_content"
      fi
      # Also save to environment variable
      export "$var_name"="$encoded_content"
    else
      echo "File $file does not exist."
      exit 1
    fi
  done
}

# Function to decode environment variables back to files
decode_files() {
  local test_mode=${1:-false}
  for file in "${files[@]}"; do
    local var_name
    var_name=$(translate_var_name "$file")
    local encoded_content
    encoded_content=$(eval echo "\$$var_name")
    if [ -n "$encoded_content" ]; then
      if [ "$test_mode" = true ]; then
        echo "Decoded content for $file:"
        echo "$encoded_content" | base64 --decode
      else
        local out_file="$file"
        local out_dir
        out_dir=$(dirname "$out_file")
        mkdir -p "$out_dir"
        echo "$encoded_content" | base64 --decode > "$out_file"
        echo "Decoded content written to $out_file"
      fi
    else
      echo "Environment variable $var_name is not set."
      exit 1
    fi
  done
}

# Main script logic
if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <encode|decode|test>"
  exit 1
fi

mode=$1

case $mode in
  encode)
    encode_files
    ;;
  decode)
    decode_files
    ;;
  test)
    encode_files true
    decode_files true
    ;;
  *)
    echo "Invalid mode: $mode. Use 'encode', 'decode', or 'test'."
    exit 1
    ;;
esac
