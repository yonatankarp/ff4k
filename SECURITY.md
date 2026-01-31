# Security Policy

## Reporting a Vulnerability

We take security vulnerabilities seriously. If you discover a security issue, please report it responsibly.

### How to Report

**Please do NOT report security vulnerabilities through public GitHub issues.**

Instead, please report them use [GitHub's private vulnerability reporting](https://github.com/yonatankarp/ff4k/security/advisories/new).

### What to Include

- Description of the vulnerability
- Steps to reproduce the issue
- Potential impact
- Suggested fix (if any)

### What to Expect

- **Acknowledgment**: We will acknowledge receipt of your report within 48 hours.
- **Updates**: We will keep you informed of our progress.
- **Resolution**: We aim to resolve critical vulnerabilities within 30 days.
- **Credit**: We will credit you in the release notes (unless you prefer to remain anonymous).

### Scope

This security policy applies to:
- The FF4K core library (`ff4k-core`)
- Official FF4K modules and extensions
- Build and CI/CD configurations

## Security Best Practices

When using FF4K in your applications:

1. **Keep dependencies updated**: Regularly update to the latest FF4K version
2. **Validate feature flag sources**: If loading flags from external sources, validate and sanitize inputs
3. **Audit permissions**: Review feature flag permissions in production configurations
