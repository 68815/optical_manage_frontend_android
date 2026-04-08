# Git Branch Convention

## Branch Types
- `main` - production
- `develop` - development
- `feature/<module>-<desc>` - new features
- `bugfix/<module>-<desc>` - bug fixes
- `hotfix/<ver>-<desc>` - urgent fixes
- `release/<ver>` - releases

## Modules
- `user`, `auth`, `product`, `order`, `cart`, `payment`, `common`

## Rules
1. Lowercase only
2. Use hyphens `-`
3. 3-5 words max
4. No special chars

## Examples
```
✅ feature/user-oauth-login
✅ bugfix/order-payment-fix
❌ feature/NewLogin (uppercase)
```

## Commits (English)
Format: `<type>(<scope>): <subject>`
Types: feat, fix, docs, style, refactor, perf, test, chore

Example:
```
feat(payment): integrate WeChat Pay
- Add SDK
- Implement API
Closes PROJ-123
```

## Workflow
```bash
git checkout -b feature/user-login develop
git commit -m "feat(user): add login"
git checkout develop
git merge --no-ff feature/user-login
```
