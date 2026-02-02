#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# Navigate to the project root (assuming script is in .local/)
cd "$(dirname "$0")/.."

echo "----------------------------------------------------------------"
echo "Starting local documentation preview"
echo "----------------------------------------------------------------"

# --- Dependency Checks ---

# 1. Check for Python/MkDocs dependencies
if ! command -v mkdocs &> /dev/null; then
    echo "Error: 'mkdocs' is not installed."
    echo "Please install it using: pip install mkdocs-material mike"
    exit 1
fi

if ! pip show mike &> /dev/null; then
    echo "Error: 'mike' python package is not installed."
    echo "Please install it using: pip install mike"
    exit 1
fi


echo "All dependencies found."

# --- Build Process ---

# 1. Generate KDoc (API documentation)
echo "Generating KDoc using Gradle..."
# Using --warning-mode all to see potential issues, but allowing continuation
./gradlew dokkaGenerateHtml --no-daemon

# 2. Prepare destination directory
echo "Preparing docs/api directory..."
mkdir -p docs/api
# Clean old API docs to ensure no stale files remain
rm -rf docs/api/*

# 3. Copy generated docs
echo "Copying generated KDocs to docs/api..."
if [ -d "build/dokka/html" ]; then
    cp -r build/dokka/html/* docs/api/
else
    echo "Error: KDoc build directory (build/dokka/html) not found!"
    exit 1
fi

# 4. Serve the site
echo "Starting MkDocs server..."
echo "Your site will be available at http://127.0.0.1:8000"
mkdocs serve
