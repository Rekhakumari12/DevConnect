# SSH Setup for Multiple GitHub Accounts

This document provides a complete guide for setting up SSH authentication when working with multiple GitHub accounts on the same machine.

## Problem Statement

When trying to push to GitHub, you may encounter authentication errors:

```bash
$ git push
fatal: could not read username for 'https://github.com': device not configured
# OR
remote: Invalid username or token. Password authentication is not supported for Git operations.
fatal: Authentication failed for 'https://github.com/...'
```

## Initial Diagnosis

### Step 1: Check Existing SSH Keys

```bash
ls -la ~/.ssh/id_*.pub
```

**Output example:**

```
-rw-r--r--@ 1 user staff 115 Apr  7  2025 /Users/user/.ssh/id_ed25519.pub
```

If you have existing keys, display them:

```bash
cat ~/.ssh/id_ed25519.pub
```

### Step 2: Check Current Git Remote Configuration

```bash
git remote -v
```

**Output:**

```
origin  https://github.com/Username/Repository.git (fetch)
origin  https://github.com/Username/Repository.git (push)
```

**Issue identified:** Using HTTPS instead of SSH.

### Step 3: Test Existing SSH Connection

```bash
ssh -T git@github.com
```

**Output:**

```
Hi work-account! You've successfully authenticated, but GitHub does not provide shell access.
```

**Issue identified:** SSH key is associated with a different GitHub account (`work-account`) than the target repository account (`personal-account`).

## Solution: Multiple GitHub Accounts Setup

When you need to work with multiple GitHub accounts on the same machine (e.g., personal and work accounts), you need to:

1. Create separate SSH keys for each account
2. Configure SSH to use the correct key for each account
3. Update git remotes to use the appropriate SSH configuration

### Step 1: Check Existing SSH Configuration

```bash
ls -la ~/.ssh/
```

**Look for:**

- `id_ed25519` and `id_ed25519.pub` (existing key pair)
- `config` (SSH configuration file)

View current SSH config:

```bash
cat ~/.ssh/config
```

**Example output:**

```
Host github.com
  AddKeysToAgent yes
  UseKeychain yes
  IdentityFile ~/.ssh/id_ed25519
```

### Step 2: Generate New SSH Key for Specific Account

Generate a new SSH key with a descriptive filename:

```bash
ssh-keygen -t ed25519 -C "your-github-email@example.com" -f ~/.ssh/id_ed25519_personal -N ""
```

**Parameters explained:**

- `-t ed25519`: Use Ed25519 algorithm (recommended for GitHub)
- `-C "email"`: Comment to identify the key
- `-f ~/.ssh/id_ed25519_personal`: Custom filename to distinguish from other keys
- `-N ""`: No passphrase (optional - you can set one for extra security)

**Output:**

```
Generating public/private ed25519 key pair.
Your identification has been saved in /Users/user/.ssh/id_ed25519_personal
Your public key has been saved in /Users/user/.ssh/id_ed25519_personal.pub
The key fingerprint is:
SHA256:<key> your-github-email@example.com
```

### Step 3: Display the New Public Key

```bash
cat ~/.ssh/id_ed25519_personal.pub
```

**Copy the entire output** (example):

```
ssh-ed25519 AAAAC3Nza...rest-of-key... your-github-email@example.com
```

### Step 4: Add SSH Key to GitHub Account

1. Go to GitHub → Settings → SSH and GPG keys: https://github.com/settings/keys
2. Click **"New SSH key"**
3. **Title**: Give it a descriptive name (e.g., "MacBook Pro - DevConnect")
4. **Key type**: Authentication Key
5. **Key**: Paste the public key copied from Step 3
6. Click **"Add SSH key"**
7. Confirm with your GitHub password if prompted

### Step 5: Configure SSH for Multiple Accounts

Add a new host entry to your SSH config:

```bash
cat >> ~/.ssh/config << 'EOF'

Host github-personal
  HostName github.com
  User git
  IdentityFile ~/.ssh/id_ed25519_personal
  AddKeysToAgent yes
  UseKeychain yes
EOF
```

**Configuration explained:**

- `Host github-personal`: Alias you'll use in git remotes (can be any name)
- `HostName github.com`: Actual GitHub server
- `User git`: Always "git" for GitHub
- `IdentityFile`: Path to the specific SSH key for this account
- `AddKeysToAgent`: Automatically add key to SSH agent
- `UseKeychain`: Store passphrase in macOS Keychain (if using passphrase)

**Final SSH config structure:**

```
Host github.com
  AddKeysToAgent yes
  UseKeychain yes
  IdentityFile ~/.ssh/id_ed25519

Host github-personal
  HostName github.com
  User git
  IdentityFile ~/.ssh/id_ed25519_personal
  AddKeysToAgent yes
  UseKeychain yes
```

### Step 6: Update Git Remote to Use SSH

Update your repository's remote URL to use the SSH host alias:

```bash
cd /path/to/repository
git remote set-url origin git@github-personal:YourUsername/YourRepository.git
```

**Format:** `git@<SSH-HOST-ALIAS>:<GitHub-Username>/<Repository>.git`

Verify the change:

```bash
git remote -v
```

**Expected output:**

```
origin  git@github-personal:YourUsername/YourRepository.git (fetch)
origin  git@github-personal:YourUsername/YourRepository.git (push)
```

### Step 7: Test SSH Connection

```bash
ssh -T git@github-personal
```

**First attempt may show wrong account:**

```
Hi work-account! You've successfully authenticated...
```

**Issue:** SSH agent is using the wrong key from cache.

### Step 8: Fix SSH Agent Key Selection

Clear SSH agent and add the correct key:

```bash
ssh-add -D  # Remove all identities
ssh-add ~/.ssh/id_ed25519_personal  # Add specific key
```

**Output:**

```
All identities removed.
Identity added: /Users/user/.ssh/id_ed25519_personal (your-github-email@example.com)
```

### Step 9: Verify Correct Account Connection

Test again:

```bash
ssh -T git@github-personal
```

**Expected output:**

```
Hi YourGitHubUsername! You've successfully authenticated, but GitHub does not provide shell access.
```

✅ **Success!** The correct account is now authenticated.

### Step 10: Push Your Changes

```bash
git push
```

**Expected output:**

```
Enumerating objects: 396, done.
Counting objects: 100% (396/396), done.
Delta compression using up to 12 threads
Compressing objects: 100% (327/327), done.
Writing objects: 100% (363/363), 244.06 KiB | 1015.00 KiB/s, done.
Total 363 (delta 162), reused 0 (delta 0), pack-reused 0 (from 0)
remote: Resolving deltas: 100% (162/162), completed with 18 local objects.
To github-personal:YourUsername/YourRepository.git
   64b1ac8..ae00c3e  main -> main
```

🎉 **Success!** Your commits are now pushed to GitHub.

## Common Issues and Resolutions

### Issue 1: "device not configured" Error

**Symptom:**

```bash
fatal: could not read username for 'https://github.com': device not configured
```

**Cause:** Using HTTPS URL instead of SSH.

**Resolution:** Change remote to SSH (see Step 6).

---

### Issue 2: "Authentication failed" with HTTPS

**Symptom:**

```bash
remote: Invalid username or token. Password authentication is not supported
fatal: Authentication failed for 'https://github.com/...'
```

**Cause:** GitHub no longer supports password authentication for Git operations.

**Resolution:** Use SSH keys or Personal Access Tokens. SSH is recommended.

---

### Issue 3: Wrong GitHub Account Authenticated

**Symptom:**

```bash
Hi wrong-account! You've successfully authenticated...
```

**Cause:** SSH agent is using a different key (first key it finds that works).

**Resolution:**

1. Clear SSH agent: `ssh-add -D`
2. Add specific key: `ssh-add ~/.ssh/id_ed25519_personal`
3. Test again: `ssh -T git@github-personal`

---

### Issue 4: "Permission denied (publickey)"

**Symptom:**

```bash
git@github.com: Permission denied (publickey).
```

**Cause:** SSH key not added to GitHub account or SSH config incorrect.

**Resolution:**

1. Verify public key is added to GitHub: https://github.com/settings/keys
2. Check SSH config syntax: `cat ~/.ssh/config`
3. Test with verbose mode: `ssh -vT git@github-personal`

---

### Issue 5: Email Verification Required

**Symptom:**

```bash
ERROR: You must verify your email address.
See https://github.com/settings/emails.
```

**Cause:** GitHub account email not verified.

**Resolution:**

1. Go to https://github.com/settings/emails
2. Click "Resend verification email"
3. Check your email and click the verification link

---

## Quick Reference Commands

### Check which SSH keys are loaded

```bash
ssh-add -l
```

### Add all keys to agent

```bash
ssh-add --apple-use-keychain ~/.ssh/id_ed25519
ssh-add --apple-use-keychain ~/.ssh/id_ed25519_personal
```

### Test specific SSH host

```bash
ssh -T git@github-personal
```

### View git remote configuration

```bash
git remote -v
```

### Change git remote URL

```bash
git remote set-url origin git@github-personal:Username/Repository.git
```

## Setting Up Additional GitHub Accounts

If you need to add more GitHub accounts, repeat the process:

1. **Generate new key:**

   ```bash
   ssh-keygen -t ed25519 -C "account@email.com" -f ~/.ssh/id_ed25519_accountname -N ""
   ```

2. **Add to SSH config:**

   ```bash
   cat >> ~/.ssh/config << 'EOF'

   Host github-accountname
     HostName github.com
     User git
     IdentityFile ~/.ssh/id_ed25519_accountname
     AddKeysToAgent yes
     UseKeychain yes
   EOF
   ```

3. **Add public key to GitHub account**

4. **Use in repository:**
   ```bash
   git remote set-url origin git@github-accountname:Username/Repository.git
   ```

## Best Practices

1. **Use descriptive SSH host aliases** (e.g., `github-work`, `github-personal`)
2. **Name SSH keys descriptively** (e.g., `id_ed25519_companyname`, `id_ed25519_personal`)
3. **Document your setup** - keep track of which keys are for which accounts
4. **Use passphrases** for SSH keys (optional but recommended for security)
5. **Back up your SSH keys** securely
6. **Regularly audit** your GitHub SSH keys and remove unused ones

## Troubleshooting

### Enable SSH Debug Mode

```bash
ssh -vvv git@github-personal
```

This provides detailed output showing:

- Which SSH config is being used
- Which key files are being tried
- Authentication process details

### Check SSH Config Syntax

```bash
ssh -G github-personal
```

Shows the effective configuration for a specific host.

### List All Keys in Agent

```bash
ssh-add -L
```

Shows all public keys currently loaded in the SSH agent.

---

## Summary

**Problem:** Can't push to GitHub due to authentication issues with multiple accounts.

**Solution:**

1. Generate separate SSH keys for each GitHub account
2. Configure SSH to use specific keys via host aliases
3. Update git remotes to use the appropriate SSH host
4. Ensure SSH agent is using the correct key

**Key Concept:** By using SSH host aliases in your SSH config, you can have multiple GitHub accounts on the same machine, each with its own SSH key, and Git will use the correct key based on the remote URL.

---

**Last Updated:** February 11, 2026  
**Related Documentation:**

- [GitHub SSH Documentation](https://docs.github.com/en/authentication/connecting-to-github-with-ssh)
- [Running Fullstack](RUNNING_FULLSTACK.md)
- [Proxy Troubleshooting](PROXY_TROUBLESHOOTING.md)
