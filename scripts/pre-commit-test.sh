#!/bin/bash

# Pre-commit testing script
# Run this before pushing to ensure CI will pass

echo "🧪 Running pre-commit tests..."

# Change to Frontend directory
cd Frontend

echo "📦 Installing dependencies..."
npm ci

echo "🔍 Running frontend tests..."
npm run test:ci

# Check if tests passed
if [ $? -eq 0 ]; then
    echo "✅ All tests passed! Ready to push."
    echo ""
    echo "💡 To push your changes:"
    echo "   git add ."
    echo "   git commit -m 'Your commit message'"
    echo "   git push origin main"
else
    echo "❌ Tests failed! Please fix issues before pushing."
    exit 1
fi
