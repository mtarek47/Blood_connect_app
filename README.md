

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Run the app on an emulator or physical device

## Privacy policy GitHub Pages deploy

- Add your privacy policy HTML file to the repository (example: `privacy-policy.html` in the project root).
- On push to `master`, GitHub Actions deploys only that privacy policy page to GitHub Pages.
- The deployed URL serves the page as the site root (`index.html`).
