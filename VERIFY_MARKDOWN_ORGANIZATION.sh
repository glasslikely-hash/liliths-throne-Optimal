#!/bin/bash
# Markdown Files Organization - Verification Script
# Shows what will be moved and provides pre/post organization stats

echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║  Markdown Files Organization - Verification Report           ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

cd /workspaces/liliths-throne-Optimal || { echo "Cannot cd to workspace"; exit 1; }

echo "CURRENT STATE (Before Organization)"
echo "═══════════════════════════════════════════════════════════════"
echo ""

# Count markdown files in root
root_md=$(ls -1 *.md 2>/dev/null | wc -l)
echo "Markdown files in root: $root_md"

# Count by type
step_count=$(ls -1 STEP_*.md 2>/dev/null | wc -l)
phase_count=$(ls -1 PHASE_*.md 2>/dev/null | wc -l)
session_count=$(ls -1 SESSION_*.md 2>/dev/null | wc -l)
refactor_count=$(ls -1 *REFACTOR*.md *REFACTORING*.md 2>/dev/null | wc -l)

echo "  - STEP files: $step_count"
echo "  - PHASE files: $phase_count"
echo "  - SESSION files: $session_count"
echo "  - REFACTOR files (to exclude): $refactor_count"

# Check if folders exist
echo ""
echo "FOLDER STATUS"
echo "═══════════════════════════════════════════════════════════════"
for i in {1..6}; do
    if [ -d "STEP_$i" ]; then
        count=$(ls -1 STEP_$i/*.md 2>/dev/null | wc -l)
        echo "  ✓ STEP_$i exists ($count files)"
    else
        echo "  ✗ STEP_$i NOT created"
    fi
done

for i in {1..4}; do
    if [ -d "PHASE_$i" ]; then
        count=$(ls -1 PHASE_$i/*.md 2>/dev/null | wc -l)
        echo "  ✓ PHASE_$i exists ($count files)"
    else
        echo "  ✗ PHASE_$i NOT created"
    fi
done

if [ -d "GENERAL" ]; then
    count=$(ls -1 GENERAL/*.md 2>/dev/null | wc -l)
    echo "  ✓ GENERAL exists ($count files)"
else
    echo "  ✗ GENERAL NOT created"
fi

echo ""
echo "FILES TO BE ORGANIZED"
echo "═══════════════════════════════════════════════════════════════"
echo ""

# List files by category
echo "STEP_1 Files:"
ls -1 STEP_1_*.md SESSION_BINARY_COMPLETION_FINAL.md 2>/dev/null | grep -v "^$" | sed 's/^/  /'
echo ""

echo "STEP_2 Files:"
ls -1 STEP_2_*.md SESSION_FINAL_VERIFICATION_STEP_2.md 2>/dev/null | grep -v "^$" | sed 's/^/  /'
echo ""

echo "STEP_3 Files:"
ls -1 STEP_3_*.md SESSION_STEP_3_*.md 2>/dev/null | grep -v "^$" | sed 's/^/  /'
echo ""

echo "STEP_4 Files:"
ls -1 STEP_4_*.md FINAL_STEP_4_SUMMARY.md SESSION_SUMMARY_STEP_4.md VERIFICATION_REPORT_STEP_4.md 2>/dev/null | grep -v "^$" | sed 's/^/  /'
echo ""

echo "STEP_5 Files:"
ls -1 STEP_5_*.md 2>/dev/null | grep -v "^$" | sed 's/^/  /'
echo ""

echo "STEP_6 Files:"
ls -1 STEP_6_*.md 2>/dev/null | grep -v "^$" | sed 's/^/  /'
echo ""

echo "PHASE Files:"
ls -1 PHASE_*.md 2>/dev/null | grep -v "^$" | sed 's/^/  /'
echo ""

echo "OTHER Files (going to GENERAL):"
other_count=0
for f in *.md; do
    if [[ ! "$f" =~ ^STEP_[0-9] ]] && [[ ! "$f" =~ ^PHASE_[0-9] ]] && [[ ! "$f" =~ ^SESSION_ ]] && [[ ! "$f" =~ FINAL_STEP_4 ]] && [[ ! "$f" =~ VERIFICATION_REPORT_STEP_4 ]] && [[ "$f" != "README.md" ]] && [[ "$f" != "license.md" ]] && [[ "$f" != "disclaimer.md" ]] && [[ "$f" != "MD_ORGANIZATION_MANIFEST.md" ]]; then
        echo "  $f"
        ((other_count++))
    fi
done
echo "  (Total: ~44 files)"

echo ""
echo "REFACTOR FILES (TO EXCLUDE - REMAIN IN ROOT)"
echo "═══════════════════════════════════════════════════════════════"
ls -1 *REFACTOR*.md *REFACTORING*.md 2>/dev/null | sed 's/^/  ✗ /'
echo ""

echo "ROOT FILES (TO PRESERVE)"
echo "═══════════════════════════════════════════════════════════════"
echo "  ✓ README.md"
echo "  ✓ license.md"
echo "  ✓ disclaimer.md"
echo ""

echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║  NEXT STEP: Run the organization script                      ║"
echo "║                                                               ║"
echo "║  bash ORGANIZE_MARKDOWN_FILES.sh                             ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
