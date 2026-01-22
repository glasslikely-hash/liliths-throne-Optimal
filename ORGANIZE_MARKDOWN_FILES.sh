#!/bin/bash
# Markdown Files Organization Script
# This script organizes all markdown files into step/phase folders
# Excludes refactor-related files as requested

set -e

cd /workspaces/liliths-throne-Optimal || exit 1

echo "Starting markdown files organization..."
echo ""

# Refactor files to EXCLUDE
REFACTOR_FILES=(
    "REFACTORING_PROGRESS_DETAILED.md"
    "REFACTOR_EXECUTIVE_SUMMARY.md"
    "UNFINISHED_REFACTORING.md"
    "COMPREHENSIVE_REFACTOR_REVIEW.md"
    "STEP_3_UI_REFACTORING_CORRECT_PLAN.md"
    "REFACTORING_PROGRESS.md"
    "REFACTORING_SUMMARY.md"
    "PROPER_REFACTORING_PLAN.md"
    "REFACTOR_REMEDIATION_CHECKLIST.md"
)

# Helper function
is_refactor_file() {
    local file=$1
    for refactor in "${REFACTOR_FILES[@]}"; do
        [[ "$file" == "$refactor" ]] && return 0
    done
    return 1
}

# Create directories if they don't exist
mkdir -p STEP_{1..6} PHASE_{1..4} GENERAL

# === STEP_1 ===
echo "[STEP_1]"
for f in STEP_1_*.md SESSION_BINARY_COMPLETION_FINAL.md SESSION_SUMMARY_STEP_1_AND_2_INTEGRATION.md; do
    if [[ -f "$f" ]] && ! is_refactor_file "$f"; then
        mv "$f" STEP_1/ 2>/dev/null && echo "  ✓ $f"
    fi
done

# === STEP_2 ===
echo "[STEP_2]"
for f in STEP_2_*.md SESSION_FINAL_VERIFICATION_STEP_2.md; do
    if [[ -f "$f" ]] && ! is_refactor_file "$f"; then
        mv "$f" STEP_2/ 2>/dev/null && echo "  ✓ $f"
    fi
done

# === STEP_3 ===
echo "[STEP_3]"
for f in STEP_3_*.md SESSION_STEP_3_*.md; do
    if [[ -f "$f" ]] && ! is_refactor_file "$f"; then
        mv "$f" STEP_3/ 2>/dev/null && echo "  ✓ $f"
    fi
done

# === STEP_4 ===
echo "[STEP_4]"
for f in STEP_4_*.md FINAL_STEP_4_SUMMARY.md SESSION_SUMMARY_STEP_4.md VERIFICATION_REPORT_STEP_4.md; do
    if [[ -f "$f" ]] && ! is_refactor_file "$f"; then
        mv "$f" STEP_4/ 2>/dev/null && echo "  ✓ $f"
    fi
done

# === STEP_5 ===
echo "[STEP_5]"
for f in STEP_5_*.md; do
    if [[ -f "$f" ]] && ! is_refactor_file "$f"; then
        mv "$f" STEP_5/ 2>/dev/null && echo "  ✓ $f"
    fi
done

# === STEP_6 ===
echo "[STEP_6]"
for f in STEP_6_*.md; do
    if [[ -f "$f" ]] && ! is_refactor_file "$f"; then
        mv "$f" STEP_6/ 2>/dev/null && echo "  ✓ $f"
    fi
done

# === PHASE_1 ===
echo "[PHASE_1]"
for f in PHASE_1_*.md; do
    if [[ -f "$f" ]] && ! is_refactor_file "$f"; then
        mv "$f" PHASE_1/ 2>/dev/null && echo "  ✓ $f"
    fi
done

# === PHASE_2 ===
echo "[PHASE_2]"
for f in PHASE_2_*.md; do
    if [[ -f "$f" ]] && ! is_refactor_file "$f"; then
        mv "$f" PHASE_2/ 2>/dev/null && echo "  ✓ $f"
    fi
done

# === PHASE_3 ===
echo "[PHASE_3]"
for f in PHASE_3_*.md; do
    if [[ -f "$f" ]] && ! is_refactor_file "$f"; then
        mv "$f" PHASE_3/ 2>/dev/null && echo "  ✓ $f"
    fi
done

# === PHASE_4 ===
echo "[PHASE_4]"
for f in PHASE_4_*.md; do
    if [[ -f "$f" ]] && ! is_refactor_file "$f"; then
        mv "$f" PHASE_4/ 2>/dev/null && echo "  ✓ $f"
    fi
done

# === GENERAL ===
echo "[GENERAL]"
for f in *.md; do
    # Skip if already in a folder or is a root essential file
    if [[ -f "$f" ]] && ! is_refactor_file "$f"; then
        if [[ "$f" != "README.md" ]] && [[ "$f" != "license.md" ]] && [[ "$f" != "disclaimer.md" ]] && [[ "$f" != "MD_ORGANIZATION_MANIFEST.md" ]]; then
            mv "$f" GENERAL/ 2>/dev/null && echo "  ✓ $f"
        fi
    fi
done

echo ""
echo "=========================================="
echo "✓ Markdown files organization COMPLETE!"
echo "=========================================="
echo ""
echo "Directory structure:"
echo "  STEP_1/    - Step 1 deliverables"
echo "  STEP_2/    - Step 2 deliverables"
echo "  STEP_3/    - Step 3 deliverables"
echo "  STEP_4/    - Step 4 deliverables"
echo "  STEP_5/    - Step 5 deliverables"
echo "  STEP_6/    - Step 6 deliverables"
echo "  PHASE_1/   - Phase 1 deliverables"
echo "  PHASE_2/   - Phase 2 deliverables"
echo "  PHASE_3/   - Phase 3 deliverables"
echo "  PHASE_4/   - Phase 4 deliverables"
echo "  GENERAL/   - Other non-refactor documents"
echo ""
echo "Root level (preserved):"
echo "  README.md"
echo "  license.md"
echo "  disclaimer.md"
echo ""
echo "Refactor files (EXCLUDED - still in root):"
for refactor in "${REFACTOR_FILES[@]}"; do
    echo "  $refactor"
done
