# LumaGlass Widgets — Google Play Store & Monetization Launch Guide (40-Widget Release)

## 1. Ready-to-Upload Build & Assets
- **Signed Release App Bundle (upload this to Play Console):**
  `app/build/outputs/bundle/release/app-release.aab`
- **Upload Keystore (BACK THIS UP SAFELY — never lose or commit this):**
  `keystore/lumaglass-upload-key.jks` (credentials in `keystore.properties`, excluded by `.gitignore`)
- **Live Privacy Policy URL (already deployed on GitHub Pages):**
  `https://ayanava-99.github.io/LumaGlassWidgets/privacy-policy.html`
- **Play Store Graphics (`play-store-assets/`):**
  - `app-icon-512x512.png` (512×512 App Icon)
  - `feature-graphic-1024x500.png` (1024×500 Feature Graphic)
  - `screenshot-1-clocks.png`, `screenshot-2-productivity.png`, `screenshot-3-battery-photo.png`, `screenshot-4-editor.png` (1080×1920 Phone Screenshots)

---

## 2. Store Listing Copy (40 Widgets • 6 Categories • Copy & Paste into Play Console)
- **App Name (30 chars max):** `LumaGlass: Aesthetic Widgets`
- **Short Description (80 chars max):** `40 Liquid Glass & Material You clock, battery, focus & photo widgets.`
- **Full Description:**
  > Transform your Android home screen with **LumaGlass**—a lightweight, battery-friendly pack of **40 Liquid Glass and Material You widgets** across **6 curated categories**, built 100% offline with zero login.
  >
  > **Why LumaGlass?**
  > • **Frosted Liquid Glass Style:** Layered translucent glass cards, specular 1dp borders, custom textures (Crystal Frost, Matte Satin, Prism Edge, Soft Halo), and adaptive Light/Dark/Material You themes.
  > • **100% Offline & Private:** Zero accounts, zero cloud tracking, and zero background polling drain.
  > • **5 Studio App Themes:** Personalize the studio in Pixel Blue, Emerald Sage, Amethyst, Sunset Gold, or Monochrome.
  > • **Deep Per-Widget Customization:** Adjust transparency (0%–100%), corner radius, tint presets, custom hex colors, and typography directly before pinning.
  >
  > **40 Included Widgets Across 6 Categories:**
  > • **Minimal (7):** Minimal Pill Time, Today Date Tile, Ambient Greeting, Full Month Calendar, Focus Sprint Counter, Dual World Clock, Next Alarm Pill.
  > • **Abstract (7):** Aurora Orb Clock, Liquid Wave Horizon, Solar Horizon Arc, Bauhaus Geometric Dial, Lunar Phase Glass, Orbital Rings, Dot Matrix Time.
  > • **Clocks & Date (7):** Frosted Digital, Minimalist Flip, Analog Ring, Vertical Typo, Day Progress Bar, Golden Hour Window, Binary Matrix Clock.
  > • **Battery & Device (5):** Battery Ring, Storage & RAM Pill, Thermal & Voltage Pill, Device Status Card, System Uptime & Specs.
  > • **Productivity (10):** Exam/Event Countdown (GATE/UPSC/JEE/Trip), Habit Streak (+1 tap), Hydration Tracker, Interactive To-Do Checklist, Step Goal Counter, Sticky Note, Year Dot Grid, Weekly Rhythm Bar, Caffeine Counter, Daily Spend Tracker.
  > • **Aesthetic & Photo (4):** Polaroid Frame, Glass Photo Carousel, Daily Quote Card, Quick-Launch App Dock.

---

## 3. Activating Live Monetization (Earn Money via AdMob + ₹49 Pro IAP)
> **Important:** `gradle.properties` currently uses **Official Google Test AdMob IDs** so you can test safely without AdMob policy violations. **Swap to your real AdMob IDs before publishing to Production**, otherwise test ads earn ₹0.

### Step A: Google AdMob (Rewarded + Interstitial Ads)
1. Create a free account at [apps.admob.com](https://apps.admob.com), click **Apps → Add App** (Android → *"No, not listed on a store yet"*), and name it `LumaGlass Widgets`.
2. Create **2 Ad Units**:
   - **Rewarded Ad Unit** (name: `Widget_Rewarded_Unlock`)
   - **Interstitial Ad Unit** (name: `Post_Save_Interstitial`)
3. Open `gradle.properties` and replace the 3 test IDs with your live IDs:
   ```properties
   ADMOB_APP_ID=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
   ADMOB_REWARDED_ID=ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ
   ADMOB_INTERSTITIAL_ID=ca-app-pub-XXXXXXXXXXXXXXXX/WWWWWWWWWW
   ```
4. **Add `app-ads.txt` (Recommended by AdMob to prevent revenue loss):**
   Once AdMob gives you your `pub-XXXXXXXXXXXXXXXX` publisher ID, add `docs/app-ads.txt` containing:
   `google.com, pub-XXXXXXXXXXXXXXXX, DIRECT, f08c47fec0942fa0`
   and set `https://ayanava-99.github.io/LumaGlassWidgets/` as your Developer Website in Play Console.

### Step B: Google Play In-App Purchase (`lumaglass_pro_lifetime` at ₹49)
1. In **Google Play Console**, finish your **Identity Verification**, then go to **Settings → Payments profile → Create payments profile** (link your PAN + Indian bank account + complete **BillDesk KYC**).
2. Open your app → **Monetize with Play → Products → In-app products → Create product**:
   - **Product ID:** `lumaglass_pro_lifetime`
   - **Name:** `LumaGlass Pro Lifetime`
   - **Description:** `Unlocks all 40 widgets, removes all ads forever, and unlocks custom hex colors & fonts.`
   - **Price:** `₹49.00 INR`
   - Click **Save → Activate**.

### Step C: Rebuild Your Production `.aab`
After pasting your live AdMob IDs into `gradle.properties` (and incrementing `versionCode` in `app/build.gradle.kts` if replacing an existing Play Console build), run:
```powershell
.\gradlew.bat bundleRelease
```

---

## 4. Play Console App Content & Data Safety Cheat-Sheet
- **Privacy Policy URL:** `https://ayanava-99.github.io/LumaGlassWidgets/privacy-policy.html`
- **Ads:** **Yes, my app contains ads**
- **App Access:** **All functionality in my app is available without any access restrictions** (no login)
- **Target Audience:** **13–15, 16–17, 18 and over** (do NOT select under 13)
- **Data Safety Form:**
  - *Does your app collect or share any required user data types?* → **Yes** (due to Google AdMob SDK)
  - *Is all user data encrypted in transit?* → **Yes**
  - Select only what Google Mobile Ads SDK collects automatically:
    - **Device or other IDs** (Collected, Shared with AdMob, Advertising/Marketing & Analytics)
    - **App info and performance → Diagnostics** (Collected, Analytics)
    - **App activity → App interactions** (Collected, Advertising/Marketing & Analytics)
  - *(All widget settings, notes, countdowns, and photos remain 100% local on-device and are never collected.)*

---

## 5. Closed Testing (14-Day Rule) → Production
1. Go to **Testing → Closed testing → Alpha → Create new release** and upload `app/build/outputs/bundle/release/app-release.aab`.
2. Add **16–20 friends/family Gmail addresses** under **Testers** and share the opt-in link after Google reviews the closed test (~24–48 hrs).
3. Keep at least 12 testers opted in for **14 consecutive days**, then click **Apply for Production**.
