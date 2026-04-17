---
description: Build and deploy the release APK to GitHub Pages
---

This workflow handles building the Android release APK and deploying it to GitHub Pages using GitHub Actions.

## Prerequisites

- The project must be hosted on GitHub.
- GitHub Pages must be enabled for the repository (it will be automatically set up by the action on first run).

## Steps

// turbo
1. Build the release APK locally (to verify)
```bash
./gradlew assembleRelease
```

2. Trigger the deployment
The deployment happens automatically when you push to the `main` branch. Alternatively, you can trigger it manually:

- Navigate to your repository on GitHub.
- Go to the **Actions** tab.
- Select **Build and Deploy APK to GH Pages**.
- Click **Run workflow**.

3. Access the APK
Once the workflow is finished, the APK will be available at:
`https://<your-username>.github.io/<your-repo-name>/`
