#!/bin/bash

echo "🚀 Setting up Light Alarm App automation..."

# Initialize git repository if not already done
if [ ! -d ".git" ]; then
    echo "📝 Initializing git repository..."
    git init
    git add .
    git commit -m "Initial commit: Light Alarm App with automation setup

- Added GitHub Actions workflows for CI/CD
- Configured Husky pre-commit hooks
- Set up ESLint and Prettier
- Added automated documentation validation
- Created backup and security workflows"
fi

# Install Husky
echo "🔧 Setting up Husky git hooks..."
npx husky install

# Create pre-commit hook
echo "🎣 Creating pre-commit hook..."
cat > .husky/pre-commit << 'EOF'
#!/usr/bin/env sh
. "$(dirname -- "$0")/_/husky.sh"

npx lint-staged
EOF

chmod +x .husky/pre-commit

echo "✅ Automation setup complete!"
echo ""
echo "📋 What was set up:"
echo "  • GitHub Actions workflows for CI/CD"
echo "  • Pre-commit hooks with ESLint + Prettier"
echo "  • Automated documentation validation"
echo "  • Security scanning and dependency updates"
echo "  • Project backup system"
echo ""
echo "🎯 Next steps:"
echo "  1. Push to GitHub: git push -u origin main"
echo "  2. Enable Actions in GitHub repository settings"
echo "  3. Add any secrets needed (Android keystore, etc.)"
echo ""
echo "Your project is now fully automated! 🤖"


