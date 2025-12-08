# [CRITICAL] Security Vulnerabilities in npm Dependencies

**Labels**: security, critical, dependencies, frontend

---

## Description
Multiple critical and moderate security vulnerabilities have been identified in npm dependencies.

## Affected Dependencies

### Next.js (15.2.3) - CRITICAL
- **GHSA-g5qg-72qw-gw5v**: Cache Key Confusion for Image Optimization API Routes
- **GHSA-xv57-4mr9-wg8v**: Content Injection Vulnerability for Image Optimization
- **GHSA-4342-x723-ch2f**: Improper Middleware Redirect Handling Leads to SSRF
- **GHSA-223j-4rm8-mrmf**: May leak x-middleware-subrequest-id to external hosts
- **GHSA-9qr9-h5gf-34mp**: Vulnerable to RCE in React flight protocol

### ESLint - LOW
- **GHSA-xffm-g5w8-qvg7**: Regular Expression Denial of Service attacks through ConfigCommentParser

### brace-expansion - LOW
- **GHSA-v6h2-p8h4-qcjw**: Regular Expression Denial of Service vulnerability

### js-yaml (4.0.0 - 4.1.0) - MODERATE
- **GHSA-mh29-5h37-fv8m**: Prototype pollution in merge

## Impact
- **Next.js vulnerabilities**: Can lead to Remote Code Execution (RCE), Server-Side Request Forgery (SSRF), and information disclosure
- **Other vulnerabilities**: Can cause Denial of Service attacks

## Solution
```bash
cd Frontend
# First try safe fixes
npm audit fix

# For critical Next.js update (may have breaking changes):
# Test in development environment first, then run:
npm audit fix --force
# Review and test all changes before committing
```

**⚠️ Note**: The `--force` flag may introduce breaking changes. Test thoroughly in development before deploying.

Expected updates:
- Next.js: 15.2.3 → 15.5.7+
- Other packages: Latest patched versions

## Priority
**CRITICAL** - Should be fixed immediately before deploying to production.
