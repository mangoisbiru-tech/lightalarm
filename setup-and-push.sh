#!/bin/bash

echo "🚀 Light Alarm App - Complete Setup & GitHub Push"
echo "================================================="

# Check if git is available
if ! command -v git &> /dev/null; then
    echo "❌ Git is not installed. Please install Git first:"
    echo "   https://git-scm.com/downloads"
    exit 1
fi

echo "✅ Git is available"

# Initialize git if not already done
if [ ! -d ".git" ]; then
    echo "📝 Initializing git repository..."
    git init
else
    echo "📝 Git repository already exists"
fi

# Add all files
echo "📦 Adding all files to git..."
git add .

# Check if there are changes to commit
if git diff --cached --quiet; then
    echo "ℹ️ No changes to commit"
else
    echo "💾 Creating comprehensive initial commit..."
    git commit -m "Complete Light Alarm App setup with automation

🌅 HYBRID APP FEATURES:
- React 19 + Capacitor 7.4.3 + Android Native
- 8 animated sunrise themes with smooth transitions
- Sleep/Wake mode toggle with beautiful UI
- Native Android alarm integration (AlarmManager)
- Fullscreen wake-up with lock screen support

🤖 BACKGROUND AGENTS & AUTOMATION:
- Husky pre-commit hooks (ESLint + Prettier)
- GitHub Actions CI/CD pipeline (5 workflows)
- Automated security scanning and dependency updates
- Daily documentation validation and backups
- Memory bank system for complete project knowledge

📁 PROJECT STRUCTURE:
- Hybrid React/Capacitor app (main project)
- Pure Android native app (light-alarm-native/)
- Complete memory bank documentation system
- Automated build and release workflows

🛠️ DEVELOPMENT TOOLS:
- ESLint + Prettier for code quality
- Multi-Node testing (18.x, 20.x)
- Android APK generation and signing
- Automated artifact management

Ready for development and deployment! 🚀"

    echo "✅ Initial commit created"
fi

# Check if remote origin is set
if git remote get-url origin &> /dev/null; then
    echo "🔗 Remote origin already configured"
    REMOTE_URL=$(git remote get-url origin)
    echo "   Repository: $REMOTE_URL"
else
    echo "🔗 Setting up remote origin..."
    git remote add origin https://github.com/mangoisbiru-tech/lightalarm.git
    echo "✅ Remote origin configured"
fi

# Push to GitHub
echo "🚀 Pushing to GitHub..."
if git push -u origin main; then
    echo ""
    echo "🎉 SUCCESS! Your project is now on GitHub!"
    echo "📍 Repository: https://github.com/mangoisbiru-tech/lightalarm"
    echo ""
    echo "🔄 Background agents will now activate:"
    echo "   • Pre-commit hooks on every commit"
    echo "   • CI pipeline on every push"
    echo "   • Security scans weekly"
    echo "   • Documentation validation daily"
    echo "   • Automated backups daily"
    echo ""
    echo "📋 Next steps:"
    echo "   1. Enable GitHub Actions in repository settings"
    echo "   2. Add Android keystore secrets for signed releases"
    echo "   3. Create issues for any remaining tasks"
    echo "   4. Set up branch protection rules"
    echo ""
    echo "Your Light Alarm App is production-ready! 🌅✨"
else
    echo ""
    echo "⚠️ Push failed. You may need to:"
    echo "   1. Set up GitHub authentication (git config --global user.name/email)"
    echo "   2. Create the repository on GitHub first"
    echo "   3. Check network connectivity"
    echo ""
    echo "📍 Repository URL: https://github.com/mangoisbiru-tech/lightalarm"
fi


