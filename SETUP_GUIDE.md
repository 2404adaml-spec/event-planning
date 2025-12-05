# Git Repository & CI/CD Setup Guide

This guide explains how to set up this project on GitHub or Azure DevOps with automated builds and tests.

## Option 1: GitHub Setup (Recommended)

### Step 1: Create a GitHub Repository

1. Go to [GitHub](https://github.com) and log in
2. Click the "+" icon in the top right and select "New repository"
3. Name your repository (e.g., `event-planning-app`)
4. Choose "Public" or "Private"
5. **DO NOT** initialize with README, .gitignore, or license (we already have these)
6. Click "Create repository"

### Step 2: Push Your Code to GitHub

```bash
# Add all files
git add .

# Create first commit
git commit -m "Initial commit: Event Planning Application"

# Add GitHub as remote (replace YOUR_USERNAME and REPO_NAME)
git remote add origin https://github.com/YOUR_USERNAME/event-planning-app.git

# Push to GitHub
git push -u origin main
```

### Step 3: GitHub Actions Will Run Automatically

- Once you push, GitHub Actions will automatically:
  - Build your project
  - Run all tests
  - Create build artifacts
- View the progress under the "Actions" tab in your repository

### Step 4: Get Your Repository Link

Your repository link will be:
```
https://github.com/YOUR_USERNAME/event-planning-app
```

Include this link in your assignment report!

---

## Option 2: Azure DevOps Setup

### Step 1: Create Azure DevOps Project

1. Go to [Azure DevOps](https://dev.azure.com)
2. Sign in with your Microsoft account
3. Click "New Project"
4. Enter project name: `event-planning-app`
5. Choose visibility (Private or Public)
6. Click "Create"

### Step 2: Create a Git Repository in Azure DevOps

1. In your project, go to "Repos" → "Files"
2. Copy the repository URL shown on the page

### Step 3: Push Your Code to Azure DevOps

```bash
# Add all files
git add .

# Create first commit
git commit -m "Initial commit: Event Planning Application"

# Add Azure DevOps as remote
git remote add origin YOUR_AZURE_DEVOPS_REPO_URL

# Push to Azure DevOps
git push -u origin main
```

### Step 4: Set Up Azure Pipelines

1. Go to "Pipelines" in your Azure DevOps project
2. Click "New Pipeline"
3. Select "Azure Repos Git"
4. Select your repository
5. Choose "Existing Azure Pipelines YAML file"
6. Select `/azure-pipelines.yml`
7. Click "Run"

The pipeline will:
- Build your project using Gradle
- Run all unit tests
- Publish test results
- Create build artifacts

### Step 5: Get Your Repository Link

Your repository link will be:
```
https://dev.azure.com/YOUR_ORGANIZATION/event-planning-app/_git/event-planning-app
```

---

## Verifying CI/CD Works

### GitHub Actions
- Go to your repository
- Click "Actions" tab
- You should see your workflow running/completed
- Green checkmark ✓ means success

### Azure Pipelines
- Go to "Pipelines" in your project
- Click on the pipeline run
- You should see all steps completed
- Green checkmarks mean success

---

## What the CI/CD Pipeline Does

Both GitHub Actions and Azure Pipelines will:

1. **Checkout Code** - Get the latest code
2. **Set Up Java 17** - Install required Java version
3. **Cache Dependencies** - Speed up builds by caching Gradle files
4. **Build Project** - Compile Kotlin and Scala code
5. **Run Tests** - Execute all 53 unit tests
6. **Publish Results** - Show test results and create artifacts

---

## Troubleshooting

### Build Fails
- Check Java version (needs Java 17)
- Make sure all tests pass locally first: `./gradlew test`

### Push Fails
- Make sure you've set the correct remote URL
- Check your GitHub/Azure DevOps credentials

### Tests Fail in CI
- Run `./gradlew clean build` locally to verify
- Check test reports in the CI logs

---

## For Your Assignment Report

Include this information in **Section 1** of your report:

**Git Repository Link:** `https://github.com/YOUR_USERNAME/event-planning-app`

**CI/CD Status:** All builds passing ✓

Take a screenshot of:
- Your repository page
- Successful CI/CD pipeline run (green checkmarks)
- Test results showing all 53 tests passing

Include these screenshots in your report to show your project is properly set up with version control and automated testing.
