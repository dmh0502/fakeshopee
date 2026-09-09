# 🚀 FakeShopee - Offline-First E-Commerce & Digital Wallet

A production-ready Android application built with **Jetpack Compose**, **Material 3**, **Clean Architecture**, and **MVI**. The app features a tech product store, cart management, digital wallet, and paginated transaction ledger with robust **Offline-First** capabilities.

---

## 🏗️ Architecture & Tech Stack

| Domain | Technology / Tool |
| :--- | :--- |
| **Language** | Kotlin 100% |
| **UI Framework** | Jetpack Compose + Material 3 |
| **Architecture** | Clean Architecture + MVI (Unidirectional Data Flow) |
| **Async / Reactive** | Kotlin Coroutines + Flow (`StateFlow`, `SharedFlow`) |
| **Dependency Injection** | Hilt |
| **Local Database** | Room Database (SSOT - Single Source of Truth) |
| **Networking** | Retrofit2 + Gson + OkHttp Interceptor |
| **Image Loading** | Coil (`AsyncImage`) |
| **Pagination** | Paging 3 + `RemoteMediator` |
| **Background Sync** | WorkManager (`PeriodicWorkRequestBuilder`) |
| **Testing** | JUnit4, MockK, Turbine, Coroutines Test |

---

## 📐 Clean Architecture & SOLID Compliance

The project strictly separates concerns into 3 core decoupled layers:

```
                          ┌───────────────────────────┐
                          │    Presentation Layer     │
                          │ Compose UI + MVI ViewModel│
                          └─────────────┬─────────────┘
                                        │
                                        ▼
                          ┌───────────────────────────┐
                          │       Domain Layer        │
                          │   UseCases + Repositories │
                          └─────────────▲─────────────┘
                                        │
                                        │
                          ┌─────────────┴─────────────┐
                          │        Data Layer         │
                          │ Room DB + Retrofit + Cache│
                          └───────────────────────────┘
```

- **Single Responsibility Principle (SRP):** Ui Screens only render UI; ViewModels manage state; UseCases encapsulate pure business logic; DAOs handle SQL queries.
- **Open/Closed Principle (OCP):** New data sources or UseCases can be added without modifying existing UI or domain code.
- **Dependency Inversion Principle (DIP):** Domain layer depends on abstractions (`ProductRepository` interface). Implementation details (`ProductRepositoryImpl`, Room, Retrofit) are injected via Hilt.

---

## 📱 Features

### 🛒 1. Store & Catalog (Offline-First)
- **Single Source of Truth (SSOT):** UI observes Room DB `Flow`. API fetches automatically update Room DB.
- **Offline Indicator:** Sticky warning banner when offline; pre-populated database via `SeedData` ensures zero-day offline availability.
- **Search & Filtering:** Instant keyword search, category filtering (Mobile, Laptops, Audio, Wearables, Accessories), and price/rating sorting.

### 💳 2. Product Detail & Cart Management
- **Rich Product Detail:** Image carousel, hardware specifications Bento grid, and customer reviews list with interactive review submission.
- **Cart Calculations:** Real-time subtotal calculation, coupon discount handling (`SHOPEE20` for -20%), estimated tax (8%), and free shipping.
- **Persistence:** Cart state persisted seamlessly in Room DB across device restarts.

### 💰 3. FakeShopee Pay Wallet & History (Paging 3)
- **Digital Wallet:** Available balance tracking, monthly spend insights, and Quick Top Up preset options.
- **Atomic Checkout:** Auto debit balance, apply coupon discounts, clear cart, and append ledger transaction atomically.
- **Paging 3 + RemoteMediator:** Smooth paginated transaction history synced from remote API to local cache with append/refresh states.

---

## ⚙️ How to Build & Run

### Prerequisites
- **Android Studio** (Ladybug 2024.2.1+ or Electric Eel+)
- **JDK 17**
- **Android Emulator or Physical Device** (Android 8.0 / API 26+)

### Installation
1. **Clone the repository:**
   ```bash
   git clone https://github.com/dmh0502/fakeshopee.git
   cd fakeshopee
   ```
2. **Open in Android Studio:**
   - Select **Open** and navigate to the cloned `android` folder.
   - Wait for Gradle Sync to complete.

3. **Run the App:**
   - Click **Run 'app'** (`Shift` + `F10`).

4. **Run Unit Tests:**
   ```bash
   ./gradlew test
   ```

---

## 🧪 Unit Testing

The repository contains 24 comprehensive unit tests passing with 100% success rate:
- **`ProductRepositoryTest`:** Tests SSOT stream observation and network failure fallback without corrupting Room DB cache.
- **`ProcessCheckoutUseCaseTest`:** Validates coupon discount calculations, wallet balance checks, and atomic checkout logic.
- **`StoreViewModelTest`, `CartViewModelTest`, `WalletViewModelTest`, `HistoryViewModelTest`:** Asserts MVI `StateFlow` emissions and `SharedFlow` UI effects using **Turbine** & **MockK**.

---

## 👤 Author

- **GitHub:** [@dmh0502](https://github.com/dmh0502)
