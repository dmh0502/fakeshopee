# 🚀 FakeShopee - Offline-First E-Commerce & Digital Wallet

A production-ready Android application built with **Jetpack Compose**, **Material 3**, **Clean Architecture**, and **MVI**. The app features a tech product store, cart management, digital wallet, and paginated transaction ledger with robust **Offline-First** capabilities.

---

## 🏗️ Tech Stack & Architecture

- **Language:** Kotlin 100%
- **UI Framework:** Jetpack Compose + Material 3
- **Architecture:** Clean Architecture + MVI (Unidirectional Data Flow)
- **Async / Reactive:** Kotlin Coroutines + Flow (`StateFlow`, `SharedFlow`)
- **Dependency Injection:** Hilt
- **Local Database:** Room Database (SSOT - Single Source of Truth)
- **Networking:** Retrofit2 + Gson + OkHttp
- **Image Loading:** Coil
- **Pagination:** Paging 3 + `RemoteMediator`
- **Background Sync:** WorkManager
- **Testing:** JUnit4, MockK, Turbine, Coroutines Test

---

## 📱 Features

1. **Store / Product List (Offline-First)**
   - Single Source of Truth via Room DB stream.
   - Network fallback & offline mode warning indicator.
   - Instant search, category filtering, and price sorting.
2. **Product Detail & Cart Management**
   - Image carousel, hardware specifications Bento grid, verified reviews.
   - Real-time cart calculations, coupon discount handling (`SHOPEE20`), tax calculation.
   - Cart state persistence in Room DB across restarts.
3. **Transaction History & Digital Wallet (Paging 3)**
   - FakeShopee Pay wallet top-up, balance management, and payment deduction.
   - Paginated ledger history using Paging 3 + `RemoteMediator`.

---

## ⚙️ How to Build & Run

1. **Clone the repository:**
   ```bash
   git clone <YOUR_GITHUB_REPO_URL>
   cd android
   ```
2. **Open in Android Studio:**
   - Open Android Studio.
   - Select **Open** and choose the `android` folder.
   - Allow Gradle to sync automatically.
3. **Run the App:**
   - Select an Android Emulator or connected physical device (Android 8.0 / API 26+).
   - Click **Run 'app'** (Shift + F10).
4. **Run Unit Tests:**
   ```bash
   ./gradlew test
   ```
