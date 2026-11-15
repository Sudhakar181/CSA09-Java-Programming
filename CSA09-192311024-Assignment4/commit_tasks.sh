#!/usr/bin/env bash
set -e
git init
git branch -M main
git add .gitignore README.md
git commit -m "Initial commit: README and .gitignore"
git add Task01
git commit -m "Complete Task01"
git add Task02
git commit -m "Complete Task02"
git add Task03
git commit -m "Complete Task03"
git add Task04
git commit -m "Complete Task04"
git add Task05
git commit -m "Complete Task05"
git add Task06
git commit -m "Complete Task06"
git add Assignment_Report.md
git commit -m "Add Assignment_Report.md"
echo "Done! Now run: git remote add origin https://github.com/YOUR_USERNAME/CSA09-192311024-Assignment4.git && git push -u origin main"
