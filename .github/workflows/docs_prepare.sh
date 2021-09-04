#!/bin/bash
# From: https://github.com/cashapp/sqldelight

# The website is built using MkDocs with the Material theme.
# https://squidfunk.github.io/mkdocs-material/
# It requires Python to run.
# Install the packages with the following command:
# pip install mkdocs mkdocs-material

PROJECT_NAME='VK SDK Kotlin'

# Should be changed after each major and minor release.
# Also, previous docs subdirectory should be added to .gitignore file.
CURRENT_VERSION_DOCS_DIR_PREFIX='0.0'

set -ex

# Generate the API docs
./gradlew dokkaGfm

# Dokka filenames like `-http-url/index.md` don't work well with MkDocs <title> tags.
# Assign metadata to the file's first Markdown heading.
# https://www.mkdocs.org/user-guide/writing-your-docs/#meta-data
title_markdown_file() {
  TITLE_PATTERN="s/^[#]+ *(.*)/title: \1 - $PROJECT_NAME/"
  echo "---"                                                     > "$1.fixed"
  # shellcheck disable=SC2129
  # shellcheck disable=SC2002
  cat "$1" | sed -E "$TITLE_PATTERN" | grep "title: " | head -n 1 >> "$1.fixed"
  echo "---"                                                    >> "$1.fixed"
  echo                                                          >> "$1.fixed"
  cat "$1"                                                      >> "$1.fixed"
  mv "$1.fixed" "$1"
}

set +x
# shellcheck disable=SC2044
for MARKDOWN_FILE in $(find docs/$CURRENT_VERSION_DOCS_DIR_PREFIX.x -name '*.md'); do
  echo "$MARKDOWN_FILE"
  title_markdown_file "$MARKDOWN_FILE"
done
set -x

# Copy in special files that GitHub wants in the project root.
cp README.md docs/index.md
cp CHANGELOG.md docs/changelog.md

# Project README.md is also used as the main page for the docs,
# but all links are relative, so we must fix it when copying
sed -i -e 's+docs/images+images+g' docs/index.md