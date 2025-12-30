# Frontend Issues to Create

This directory contains issue templates for frontend problems and improvements identified in the MailMine codebase.

## How to Create These Issues

Review each markdown file and create corresponding GitHub issues manually, or use the GitHub CLI:

```bash
# For each issue file, run:
gh issue create --title "$(head -n 1 filename.md | sed 's/# //')" \
                --body-file filename.md \
                --label frontend,code-quality

# Or use the create-issues.sh script
bash create-issues.sh
```

## Summary

- **Critical Issues**: 1 (Security vulnerabilities)
- **High Priority Bugs**: 1 (React Hooks violation)
- **Medium Priority**: 10 items
- **Low Priority**: 8 items  
- **Total**: 20 issues

## Priority Breakdown

### Critical (Fix Immediately)
1. Security Vulnerabilities in npm Dependencies

### High (Fix Soon)  
2. React Hooks Rules Violation

### Medium (Plan to Fix)
3-12. Various code quality, features, and improvements

### Low (Nice to Have)
13-20. Documentation, accessibility, and enhancements
