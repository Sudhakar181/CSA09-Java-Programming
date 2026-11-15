@echo off
git init
git branch -M main
git add .gitignore README.md
git commit -m "Initial commit: README and .gitignore"
git add Q1
git commit -m "Complete Q1"
git add Q2
git commit -m "Complete Q2"
git add Q3
git commit -m "Complete Q3"
git add Q4
git commit -m "Complete Q4"
git add Q5
git commit -m "Complete Q5"
git add Q6
git commit -m "Complete Q6"
git add Q7
git commit -m "Complete Q7"
git add Assignment_Report.md
git commit -m "Add Assignment_Report.md"
echo.
echo Done! Now run: git remote add origin https://github.com/YOUR_USERNAME/CSA09-192311024-Assignment2.git
echo Then run: git push -u origin main
