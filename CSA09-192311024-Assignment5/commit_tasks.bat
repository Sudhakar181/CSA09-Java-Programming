@echo off
git init
git branch -M main
git add .gitignore README.md
git commit -m "Initial commit: README and .gitignore"
git add TaskA1
git commit -m "Complete TaskA1"
git add TaskA2
git commit -m "Complete TaskA2"
git add TaskA3
git commit -m "Complete TaskA3"
git add TaskA4
git commit -m "Complete TaskA4"
git add TaskA5
git commit -m "Complete TaskA5"
git add TaskB1
git commit -m "Complete TaskB1"
git add TaskB2
git commit -m "Complete TaskB2"
git add TaskB3
git commit -m "Complete TaskB3"
git add TaskB4
git commit -m "Complete TaskB4"
git add Assignment_Report.md
git commit -m "Add Assignment_Report.md"
echo.
echo Done! Now run: git remote add origin https://github.com/YOUR_USERNAME/CSA09-192311024-Assignment5.git
echo Then run: git push -u origin main
