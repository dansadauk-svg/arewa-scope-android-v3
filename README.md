# Arewa Scope Android App v1.1

WordPress WebView Android app for:

https://arewascope.com.ng/

## What changed in this upgrade

- Added native splash screen.
- Added improved app icon set.
- Added camera cutout/status-bar safe area protection.
- Added faster WebView settings and cache behavior.
- Added Firebase Cloud Messaging code for push notifications.
- Added Android 13+ notification permission request.
- Added notification tap handling so a notification can open a post URL inside the app.
- Kept file upload support, offline screen, external link handling, and back button support.

## Build APK Online With GitHub Actions

1. Upload/replace the files in your GitHub repository.
2. Go to **Actions**.
3. Open **Build Android APK**.
4. Click **Run workflow**.
5. Download the artifact named **ArewaScope-debug-apk**.
6. Extract it and install `app-debug.apk` on your Android phone.

## Firebase Push Notification Setup

The project contains a placeholder:

`app/google-services.json`

This placeholder allows GitHub Actions to build the APK immediately. Real notifications will not work until you replace it with your real Firebase file.

To activate notifications:

1. Open Firebase Console.
2. Create a Firebase project.
3. Add an Android app.
4. Use package name:

`com.arewascope.app`

5. Download the real `google-services.json`.
6. Replace:

`app/google-services.json`

7. Commit the change and run GitHub Actions again.

The app subscribes users to this FCM topic automatically:

`news`

You can send a test notification from Firebase Console to the `news` topic.

## WordPress Automatic Notifications

A WordPress plugin is included in the `/wordpress-plugin` folder. Upload it to your WordPress site to send a notification when a new post is published.

## Speed Note

The Android side now uses better WebView cache and hardware acceleration, but page speed still depends heavily on your WordPress theme, plugins, image sizes, caching plugin, hosting, and ads/scripts.
