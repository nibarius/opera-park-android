# Opera Park Android - Future Improvements & TODOs

This document outlines planned technical debt cleanups, architecture modernizations, and library upgrades for the project following the Jetpack Compose & Material 3 migration.

---

## 1. Edge-to-Edge Modernization (`enableEdgeToEdge()`)
- **Status:** Completed
- **Context:** `ParkActivity`, `ManageCarsActivity`, and `SettingsActivity` currently call `WindowCompat.setDecorFitsSystemWindows(window, false)`.
- **Action:** Replace with `enableEdgeToEdge()` from `androidx.activity` (Activity 1.8+ API).
- **Files Involved:**
  - `app/src/main/java/se/barsk/park/mainui/ParkActivity.kt`
  - `app/src/main/java/se/barsk/park/managecars/ManageCarsActivity.kt`
  - `app/src/main/java/se/barsk/park/settings/SettingsActivity.kt`

---

## 2. Replace Legacy Joda-Time with `java.time`
- **Status:** Completed
- **Context:** The app uses `net.danlew:android.joda` (`JodaTimeAndroid`). Standard Android natively supports `java.time` (`Instant`, `ZonedDateTime`, `ZoneOffset`).
- **Action:**
  - Refactor `TimeUtils.kt` to use `java.time.Instant`, `java.time.ZonedDateTime`, and `java.time.ZoneOffset`.
  - Remove `JodaTimeAndroid.init(this)` from `ParkApp.kt`.
  - Remove `android-joda` dependency from `gradle/libs.versions.toml` and `app/build.gradle`.
- **Files Involved:**
  - `app/src/main/java/se/barsk/park/utils/TimeUtils.kt`
  - `app/src/main/java/se/barsk/park/ParkApp.kt`
  - `gradle/libs.versions.toml`
  - `app/build.gradle`

---

## 3. Replace Legacy Fuel HTTP Library
- **Status:** Completed
- **Context:** The networking layer uses `com.github.kittinunf.fuel` (`fuel-android`, `fuel-json`), which is archived and no longer maintained.
- **Action:**
  - Migrate `NetworkManager.kt` to use **Ktor Client** or **Retrofit/OkHttp**.
  - Remove `fuel-android`, `fuel-json`, and `result` dependencies from `libs.versions.toml` and `app/build.gradle`.
- **Files Involved:**
  - `app/src/main/java/se/barsk/park/network/NetworkManager.kt`
  - `gradle/libs.versions.toml`
  - `app/build.gradle`

---

## 4. Migrate `SharedPreferences` to Preferences `DataStore`
- **Status:** Planned
- **Context:** `SharedPrefs.kt` uses synchronous Android `SharedPreferences`.
- **Action:**
  - Add `androidx.datastore:datastore-preferences` to `libs.versions.toml` and `app/build.gradle`.
  - Migrate preference storage to DataStore, exposing settings via Kotlin `Flow`.
- **Files Involved:**
  - `app/src/main/java/se/barsk/park/storage/SharedPrefs.kt`
  - `app/src/main/java/se/barsk/park/storage/StorageManager.kt`
  - `gradle/libs.versions.toml`
  - `app/build.gradle`

---

## 5. Single-Activity Architecture with Compose Navigation (Optional)
- **Status:** Backlog / Optional
- **Context:** The app currently uses 3 separate activities (`ParkActivity`, `ManageCarsActivity`, `SettingsActivity`).
- **Action:**
  - Consolidate screens into a single `MainActivity` using Compose `NavHost` (`navigation-compose` is already present in `libs.versions.toml`).
  - Add transition animations between screens.
