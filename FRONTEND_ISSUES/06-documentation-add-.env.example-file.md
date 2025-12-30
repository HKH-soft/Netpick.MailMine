# [DOCUMENTATION] Add .env.example File

**Labels**: documentation, configuration, frontend, low

---

## Description
The frontend uses environment variables but there's no `.env.example` file.

## Impact
- Confusion for new developers
- May lead to misconfiguration

## Solution
Create `Frontend/.env.example` with:
```env
# Backend API Configuration
BACKEND_URL=http://localhost:8080

# Application Settings
NEXT_PUBLIC_APP_NAME=MailMine
NEXT_PUBLIC_APP_VERSION=1.0.0
```

## Priority
**LOW** - Improves developer experience.
