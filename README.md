# Stack Casino

**by [Plain Studio](https://github.com/nullnullnullnullnullnullnullnullnullnul)**

> A fully-featured Android casino simulator with a real crypto economy on Polygon. Configure your own house wallet, play with real USDC, and verify every result with Provably Fair cryptography, all without risking real money, since the operator controls both sides of the system.

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Min SDK](https://img.shields.io/badge/Min%20SDK-31-blue)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-orange)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

---

## About

Stack Casino is a native Android application that simulates a fully operational online casino backed by real cryptocurrency transactions on the Polygon network. The operator loads a private key as the house wallet either generating a new one or importing an existing one, and players deposit and withdraw real USDC. Since the operator controls both the house wallet and their own player wallet, no money is ever truly lost. It functions as a self-contained sandbox for testing game logic, crypto flows, and Provably Fair algorithms in a real-world environment.

---

## Features

### Games
| Game | Description |
|------|-------------|
| **Roulette** | European roulette with multiple simultaneous bet types: straight, dozens, color, odd/even |
| **Blackjack** | Classic blackjack with full action set: hit, stand, double down, split |
| **Crash** | Ascending multiplier with real-time 120 FPS animation and Cash Out at any moment |
| **Mines** | 5×5 grid with configurable mine count (1–24), incremental multiplier and Cash Out |

### Crypto Wallet
- USDC deposits on Polygon via unique per-user deposit address
- Withdrawals signed from the house wallet via Firebase Cloud Functions
- Real-time deposit detection via Alchemy webhook
- Full transaction history with status tracking

### AI Assistant
- In-app Gemini-powered chat assistant scoped exclusively to casino rules
- Explains how to play each game, available bets, payout tables and allowed actions
- Persistent conversation history within the active session

### News Feed
- Live casino and crypto news via NewsAPI
- Filtered query: `(casino OR gambling OR blackjack OR roulette) AND (crypto OR blockchain OR polygon OR USDC)`
- Offline-first with Room caching, reactive search and article detail view

### Security
- Provably Fair RNG using HMAC-SHA256 in native C++ layer
- House wallet private key encrypted with Android Keystore (EncryptedSharedPreferences)
- BiometricPrompt authentication required to view or modify the private key
- JNI architecture keeps all game logic in the native layer, Kotlin acts as a thin client

---

## Tech Stack

### Android Client
| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose, Material Design 3 |
| Architecture | MVVM + Repository Pattern |
| Dependency Injection | Hilt |
| Local Database | Room (Single Source of Truth) |
| Networking | Retrofit |
| Image Loading | Glide |
| AI | Gemini SDK for Android |
| Game Engine | C++ via JNI |
| RNG | HMAC-SHA256 (Provably Fair) |
| Key Storage | Android Keystore + EncryptedSharedPreferences |
| Biometrics | BiometricPrompt API |
| Splash | Android Splash Screen API |

### Backend & Services
| Service | Purpose |
|---------|---------|
| Firebase Auth | Google Sign-In authentication and session persistence |
| Firebase Firestore | Remote data sync (users, wallets, rounds, bets, transactions) |
| Firebase Cloud Functions | Webhook receiver for deposits, withdrawal signing |
| Firebase Cloud Messaging | Push notifications for deposits, withdrawals and security alerts |
| Alchemy | Blockchain indexing and deposit detection on Polygon |
| NewsAPI | Casino and crypto news feed |
| Polygon Network | USDC deposits and withdrawals |

---

## Architecture

Stack Casino follows a strict **MVVM + Repository Pattern** with an **Offline-first** data strategy.

```
UI (Jetpack Compose)
    │  observes StateFlow via collectAsStateWithLifecycle
    ▼
ViewModel
    │  requests data from
    ▼
Repository
    ├── Room (Single Source of Truth for UI)
    └── Retrofit / Firebase SDK (updates Room after remote fetch)

Game Logic
    Kotlin ViewModel ──JNI──► C++ Game Engine
                               ├── HMAC-SHA256 RNG
                               ├── Roulette logic
                               ├── Blackjack logic
                               ├── Crash logic
                               └── Mines logic
```

The UI never reads directly from Firestore or the network. All data flows through Room. Retrofit and Firebase SDK update Room in the background after successful remote calls.

---

## House Wallet Setup

The house wallet is the account that pays out player winnings and receives player bets at the app level.

1. Open the app and navigate to **Settings → House Wallet**
2. Authenticate with your device lock method (fingerprint, PIN, password or pattern)
3. Choose one of two options:
   - **Generate new wallet**: the app derives a fresh key pair locally and displays the public Polygon address
   - **Import existing wallet**: paste your private key and the app derives and displays the corresponding address for verification
4. Fund the house wallet address with USDC on Polygon
5. The app reads the on-chain balance directly from the Polygon RPC — it never leaves the device

> ! The private key is encrypted with Android Keystore and never transmitted to any server. Back it up externally before using it with real funds.

---

## Provably Fair

Every game round generates a **server seed** and a **client seed** before play begins. The outcome is derived from `HMAC-SHA256(server_seed, client_seed)`. At the end of each round both seeds are revealed and stored in the round history.

To verify any result independently:
1. Open the round detail from your history
2. Copy the `server_seed` and `client_seed`
3. Run `HMAC-SHA256(server_seed, client_seed)` in any online tool or script
4. Compare the output against the documented result derivation for each game type

This guarantees that neither the app nor the operator could have predicted or manipulated the outcome after the bet was placed.

---

## Prerequisites

- Android Studio Hedgehog or later
- Android device or emulator running API 26+
- A Firebase project with Auth (Google Sign-In), Firestore and Cloud Functions enabled
- A NewsAPI.org API key (free tier)
- An Alchemy account with a Polygon webhook configured

---

## Environment Setup

1. Clone the repository:
```bash
git clone https://github.com/plainstudio/stack-casino.git
cd stack-casino
```

2. Add your `google-services.json` from your Firebase project to the `app/` directory.

3. Create a `local.properties` file in the root and add:
```properties
NEWS_API_KEY=your_newsapi_key_here
ALCHEMY_WEBHOOK_SECRET=your_alchemy_webhook_secret_here
```

4. Deploy the Firebase Cloud Functions from the `functions/` directory:
```bash
cd functions
npm install
firebase deploy --only functions
```

5. Configure your Alchemy webhook to point to the deployed `onDepositConfirmed` Cloud Function URL.

6. Build and run the project from Android Studio.

---

## Screenshots

> Figma prototype and screenshots coming soon.

---

## Branch Strategy

This project follows a feature-branch workflow:

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready code only |
| `develop` | Integration branch |
| `feature/*` | One branch per feature (e.g. `feature/auth`, `feature/crash`, `feature/wallet`) |

All feature branches are merged into `develop` via pull request before being merged to `main`.

---

## License

```
MIT License

Copyright (c) 2026 Plain Studio

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```
