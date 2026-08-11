# Photo Gallery (Offline Android App)

A native offline photo gallery app for Android — browse photos grouped into
albums, view full-screen with pinch-to-zoom and swipe, delete, share, and
view photo info. No internet access required to run the app.

## How to build the APK using GitHub (free, no PC needed)

1. Create a free GitHub account at github.com if you don't have one.
2. Create a **new repository** (e.g. "PhotoGallery"), public, no README.
3. Upload every file/folder from this project into the repo, keeping the
   folder structure exactly as it is (including the hidden `.github` folder
   with the `workflows/build.yml` file inside it — this is what tells GitHub
   to build the APK for you automatically).
4. Once uploaded, go to the **Actions** tab of your repo. A workflow called
   "Build APK" should start running automatically (takes ~3-5 minutes).
5. When it finishes (green checkmark), click into that workflow run, scroll
   to **Artifacts**, and download `PhotoGallery-debug-apk`. It's a zip
   containing `app-debug.apk`.
6. Transfer that .apk to your phone (or download it directly from your
   phone's browser), open it, and allow "install from unknown sources" if
   prompted. The app will install like any other app.

## Notes

- The app requests photo access permission on first launch.
- Everything runs 100% offline — no network calls anywhere in the app.
