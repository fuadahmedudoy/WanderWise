@echo off
REM Pre-commit testing script for Windows
REM Run this before pushing to ensure CI will pass

echo 🧪 Running pre-commit tests...

REM Change to Frontend directory
cd Frontend

echo 📦 Installing dependencies...
call npm ci

echo 🔍 Running frontend tests...
call npm run test:ci

REM Check if tests passed
if %errorlevel% equ 0 (
    echo ✅ All tests passed! Ready to push.
    echo.
    echo 💡 To push your changes:
    echo    git add .
    echo    git commit -m "Your commit message"
    echo    git push origin main
) else (
    echo ❌ Tests failed! Please fix issues before pushing.
    exit /b 1
)
