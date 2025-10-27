# Memory Bank - Light Alarm App

## Purpose
This Memory Bank serves as the **complete knowledge base** for the Light Alarm App project. When the AI assistant's memory resets between sessions, these files are the **only source of truth** for understanding the project's current state, history, and future direction.

## Structure

### Core Files (Required)
These files must be maintained and kept up-to-date:

#### 📋 `projectbrief.md`
**Foundation document** - Defines what the project is and why it exists
- Core requirements and goals
- Target platform and users
- Success criteria
- Project scope (in/out)

**Update when:** Project direction changes, new core requirements added, scope changes

---

#### 🎯 `productContext.md`
**User experience blueprint** - Explains how the product should work
- Problems the project solves
- User journeys (wake mode, sleep mode, comfort light)
- UX goals and principles
- Value propositions

**Update when:** User flows change, new features affect UX, design philosophy evolves

---

#### ⚡ `activeContext.md`
**Current work snapshot** - What's happening right now
- Recent work completed
- Known issues (prioritized)
- Active focus areas
- Pending decisions
- Development environment details

**Update when:** Completing work, discovering issues, making decisions, changing focus

---

#### 🏗️ `systemPatterns.md`
**Architecture guide** - How the system is built
- System architecture overview
- Key technical decisions (and why)
- Design patterns in use
- Component relationships
- Data flow

**Update when:** Adding new patterns, making architectural changes, introducing new components

---

#### 🔧 `techContext.md`
**Technology reference** - What technologies are used and how
- Complete technology stack
- Development setup instructions
- Build/deployment process
- Technical constraints
- Dependencies

**Update when:** Adding dependencies, changing build process, updating versions, modifying setup

---

#### 📊 `progress.md`
**Status tracker** - What works, what doesn't, what's next
- Completed features (with ✅)
- Partially working features (with ⚠️)
- What's left to build (with 🚧)
- Known issues
- Milestone tracking

**Update when:** Completing features, discovering bugs, changing priorities, hitting milestones

---

## Additional Context Files
Create additional files/folders as needed for:
- Complex feature documentation
- Integration specifications
- API documentation
- Testing strategies
- Deployment procedures

## Usage Guidelines

### Reading on Session Start
**MUST read ALL core files** when starting a new task to understand:
1. What the project is (`projectbrief.md`)
2. Current status (`activeContext.md`, `progress.md`)
3. How it's built (`systemPatterns.md`, `techContext.md`)
4. User experience goals (`productContext.md`)

### Updating Files

#### After Completing Work
- Update `progress.md` - Mark features complete, update status
- Update `activeContext.md` - Record what was done, what's next

#### When Discovering Issues
- Update `activeContext.md` - Add to known issues
- Update `progress.md` - Update status if needed

#### When Making Decisions
- Update `systemPatterns.md` - If architectural/pattern decision
- Update `activeContext.md` - Record decision and rationale

#### When User Says "Update Memory Bank"
- **MUST review ALL core files**
- Update any files that need changes
- Focus especially on `activeContext.md` and `progress.md`

## Current Status

### Last Full Update
**Date:** October 9, 2025  
**Phase:** Bug fixes and UI refinements  
**Completeness:** ~85% toward v1.0

### Key Files Status
- ✅ `projectbrief.md` - Complete, comprehensive
- ✅ `productContext.md` - Complete, detailed user journeys
- ✅ `activeContext.md` - Up-to-date with recent work
- ✅ `systemPatterns.md` - Complete, documents all patterns
- ✅ `techContext.md` - Complete, includes setup instructions
- ✅ `progress.md` - Detailed status tracking

### Project Intelligence
- ✅ `.cursorrules` created with critical patterns and preferences
- Contains implementation gotchas, user preferences, workflows

## Quick Reference

### Project At-a-Glance
- **Name:** Light Alarm App
- **Type:** Mobile alarm app with gradual light transitions
- **Tech:** React 19 + Capacitor 7 + TailwindCSS
- **Platform:** Android (primary), Web (development)
- **Status:** Functional, minor bugs, needs testing

### Current Focus
- Fixing AnimatedBackground color display across all animation types
- Testing all themes and animations
- Preparing for comprehensive device testing

### Known Issues
1. **High Priority:** AnimatedBackground base layer missing (6 animation types)
2. **Medium Priority:** Large App.js file (59K tokens)
3. **Low Priority:** Multiple backup files, README not updated

### Next Milestone
**v1.0 Complete** - Estimated 4-7 hours remaining
- Fix all animation color displays
- Comprehensive device testing
- Update documentation

## Maintenance Best Practices

### Keep It Fresh
- Update after every significant work session
- Don't let `activeContext.md` get stale
- Mark completed items immediately

### Keep It Accurate
- Remove outdated information
- Update decisions if they change
- Correct errors when discovered

### Keep It Useful
- Write for someone with no prior context
- Be specific (file paths, line numbers, exact issues)
- Include code snippets for critical patterns

### Keep It Organized
- Use consistent formatting
- Keep related information together
- Use clear section headers

## Memory Bank Health Check

Use this checklist when updating:

- [ ] `activeContext.md` reflects current work
- [ ] `progress.md` status is accurate
- [ ] Recent changes documented
- [ ] Known issues up-to-date
- [ ] No contradicting information across files
- [ ] Next steps clearly defined
- [ ] All code references are accurate
- [ ] Development environment info current

## Integration with .cursorrules

The `.cursorrules` file complements the Memory Bank by capturing:
- **Tactical knowledge:** Specific implementation patterns
- **User preferences:** Design choices, style preferences
- **Common workflows:** Development cycles, testing patterns
- **Gotchas:** Known bugs and their fixes

**Relationship:**
- Memory Bank = What & Why (strategic)
- .cursorrules = How (tactical)

Both should be maintained together for complete project knowledge.

---

*This Memory Bank was initialized on October 9, 2025, and captures the Light Alarm App at ~85% completion toward v1.0.*




