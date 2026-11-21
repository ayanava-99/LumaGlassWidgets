# LumaGlass Widgets — Aesthetic Liquid Glass Android App

A native Android app built with **Kotlin**, **Jetpack Compose (Material 3)**, and **Jetpack Glance (`1.1.1`)**, targeting **Android 16 (API 36)** with **Min SDK 29 (Android 10)**.

## Architecture Highlights
- **100% Offline & Zero Login:** All widget styles, countdowns, habit counters, sticky notes, and downscaled photos are stored locally in `Preferences DataStore` (`LumaPreferencesRepository.kt`) and `filesDir/widget_photos/`.
- **16 Starter Pack Widgets (10 Free + 6 Rewarded/Pro):**
  - **Clocks & Date (5):** Frosted Glass Digital Clock, Minimalist Flip Clock, Analog Ring Clock, Vertical Typography Clock, Day-Progress Bar.
  - **Battery & Device (3):** Circular Glass Battery Ring, Storage + RAM Pill, Quick-Glance Wi-Fi & Brightness Status Card.
  - **Productivity & India-Focused (4):** Exam/Event Countdown (GATE/UPSC/JEE/Trip), Daily Habit Streak Counter (`+1 Today` tap callback), Quick Sticky Note, Year Progress Dot Grid.
  - **Aesthetic & Photo (4):** Polaroid Photo Frame, Rounded Glass Photo Carousel, Daily Minimalist Quote Card, Quick-Launch App Dock.
- **OOM-Safe Responsive Glance Engine:** Pre-decodes & downscales user photos and pre-renders Canvas glass surfaces & rings **before** `provideContent` inside `BaseLumaGlanceWidget.provideGlance` using `SizeMode.Responsive` (`2x2`, `4x2`, `4x4`).
- **Monetization Scaffolding:**
  - `AdMobManager.kt`: Uses official Google Test Ad Unit IDs (`ca-app-pub-3940256099942544/5224354917` Rewarded, `ca-app-pub-3940256099942544/1033173712` Interstitial).
  - `BillingManager.kt`: Google Play Billing v7.1.1 scaffold for product ID `lumaglass_pro_lifetime` (₹49 one-time lifetime unlock).

## Quick Setup in Android Studio
1. Open this folder (`LumaGlassWidgets`) in Android Studio (Ladybug / Meerkat or newer with Android SDK 36 installed).
2. Let Gradle sync (`gradle-8.10.2`, AGP `8.7.3`, Kotlin `2.0.21`).
3. Pair your Android phone via Wireless Debugging and press **Run** (`app`).
4. Before releasing to Google Play Production:
   - Replace the test AdMob App ID in `app/src/main/AndroidManifest.xml` and test Ad Unit IDs in `AdMobManager.kt` with your live AdMob IDs.
   - Create in-app product `lumaglass_pro_lifetime` in Google Play Console.
   - Host `docs/privacy-policy.html` on GitHub Pages for your Play Store listing.
