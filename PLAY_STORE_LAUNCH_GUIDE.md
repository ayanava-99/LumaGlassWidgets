# LumaGlass Widgets — Google Play Store & Monetization Launch Guide

## 1. Software Installed on Your PC
- **Java Development Kit (OpenJDK 17):** `%LOCALAPPDATA%\Programs\jdk-17-root\jdk-17.0.21.1+2` (`JAVA_HOME` configured)
- **Android SDK (API 36 / Platform-Tools / Build-Tools):** `%LOCALAPPDATA%\Android\Sdk` (`ANDROID_HOME` configured)
- **Gradle Wrapper (`8.10.2`):** Bootstrapped in `gradle/wrapper/gradle-wrapper.jar`

*(Optional IDE):* To install **Android Studio** for visual editing & emulator testing, run in PowerShell:
```powershell
winget install --id Google.AndroidStudio --exact --accept-package-agreements --accept-source-agreements
```

---

## 2. Ready-to-Upload Build Artifacts & Keystore
- **Signed Release App Bundle (upload this to Play Console):**
  `app/build/outputs/bundle/release/app-release.aab`
- **Debug APK (install directly on your Android phone via USB/ADB):**
  `app/build/outputs/apk/debug/app-debug.apk`
- **Upload Keystore (BACK THIS UP SAFELY — never lose or commit this):**
  `keystore/lumaglass-upload-key.jks` (credentials in `keystore.properties`, already excluded by `.gitignore`)
- **Play Store Graphics (`play-store-assets/`):**
  - `app-icon-512x512.png` (512×512 App Icon)
  - `feature-graphic-1024x500.png` (1024×500 Feature Graphic)
  - `screenshot-1-clocks.png`, `screenshot-2-productivity.png`, `screenshot-3-battery-photo.png`, `screenshot-4-editor.png` (1080×1920 Phone Screenshots)

---

## 3. Hosting Your Privacy Policy (Required for Play Store & AdMob)
1. Push `docs/privacy-policy.html` to a GitHub repository.
2. Enable **GitHub Pages** (`Settings → Pages → Deploy from branch → /docs`).
3. Copy your public URL (e.g., `https://<username>.github.io/<repo>/privacy-policy.html`) into Google Play Console → **App Content → Privacy Policy**.

---

## 4. Activating Live Monetization (AdMob + ₹49 Pro IAP)
Currently `gradle.properties` uses **Official Google Test AdMob IDs** so you never violate AdMob policy during testing.

### Step A: Google AdMob
1. Sign up at [apps.admob.com](https://apps.admob.com), add **LumaGlass Widgets (Android)**, and create:
   - One **Rewarded** Ad Unit (`Rewarded Unlock`)
   - One **Interstitial** Ad Unit (`Post-Save Gentle Interstitial`)
2. Open `gradle.properties` and replace only these 3 lines:
   ```properties
   ADMOB_APP_ID=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
   ADMOB_REWARDED_ID=ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ
   ADMOB_INTERSTITIAL_ID=ca-app-pub-XXXXXXXXXXXXXXXX/WWWWWWWWWW
   ```

### Step B: Google Play In-App Purchase (`lumaglass_pro_lifetime` at ₹49)
1. In **Google Play Console**, complete your **Payments Profile** (PAN + Bank Account + BillDesk KYC for Indian merchants).
2. Open your app → **Monetize with Play → Products → In-app products → Create product**:
   - **Product ID:** `lumaglass_pro_lifetime`
   - **Name:** `LumaGlass Pro Lifetime`
   - **Price:** `₹49` (default price in INR; Play Console auto-converts or lets you set global prices)
   - **Status:** Click **Activate**.

### Step C: Rebuild Your Production `.aab`
After updating `gradle.properties` with your live AdMob IDs (and bumping `versionCode` in `app/build.gradle.kts` if re-uploading), run:
```powershell
.\gradlew.bat bundleRelease
```

---

## 5. Google Play Console Upload Checklist
1. Create app in Play Console (`LumaGlass: Aesthetic Widgets`, Default language: English (India / US), App, Free).
2. Complete **App Content** declarations (Privacy Policy URL, Ads: **Yes, contains ads**, Target Audience: 13+, Data Safety: declare AdMob device/ad telemetry and no personal server data collection).
3. Upload the screenshots & graphics from `play-store-assets/` under **Store Listing**.
4. Upload `app/build/outputs/bundle/release/app-release.aab` to **Testing → Closed Testing** (opt in 16–20 testers for 14 continuous days as required for new personal accounts), then apply for **Production**.
