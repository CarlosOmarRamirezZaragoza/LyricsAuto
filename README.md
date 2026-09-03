# LyricsAuto: Intelligent Synchronized Lyrics for Android Auto

## Overview
LyricsAuto is a high-performance pedagogical project designed to display real-time, synchronized lyrics (Karaoke-style) on Android Auto. This project explores the integration of media session tracking, external API consumption, and custom rendering on automotive surfaces while adhering to strict safety and architectural standards.

### 📜 Educational Purpose & Disclaimer
**FOR EDUCATIONAL PURPOSES ONLY.**
This software is developed to demonstrate technical capabilities within the Android Automotive ecosystem. 
*   **Responsibility**: The use of this software is at the sole responsibility of the user. The developer is **NOT liable** for any accidents, personal injuries, property damage, or any type of damage to third parties resulting from the use of this application.
*   **Google Safety Rules**: This project implements `distractionOptimized="true"` and follows Google's guidelines regarding UI refreshing limits in automotive environments to prevent driver distraction. **Always keep your eyes on the road.**

---

## 👨‍💻 Author Information
*   **Developer**: Omar Ramirez (CORZ)
*   **LinkedIn**: [omar-ramirez-6a51b7141](https://www.linkedin.com/in/omar-ramirez-6a51b7141/)
*   **Role**: Senior Android Developer / AI Architect

---

## 🛠 Tech Stack
*   **Kotlin**: Primary language with structured concurrency.
*   **Jetpack Compose**: Modern declarative UI for the mobile settings interface.
*   **Clean Architecture**: Separation of concerns into Data, Domain, and Presentation layers.
*   **Hilt**: Dependency injection for robust component management.
*   **Coroutines & Flow**: Reactive data streams for real-time synchronization.
*   **Android Car App Library (Android Auto)**: Template-based and Surface-based rendering for vehicles.
*   **Retrofit & OkHttp**: LRCLIB API integration for lyrics retrieval.
*   **MediaSessionManager**: Intelligent tracking of active media players (Spotify, YouTube Music).
*   **Room Database**: Local persistence for offline lyrics storage and caching.
*   **FTS4 (Full-Text Search)**: High-speed local search optimization for stored lyrics.

---

## 🧠 Implementation Logic

### 1. Media Tracking (The Listener)
The `LyricsNotificationListener` (inheriting from `NotificationListenerService`) acts as a bridge. It monitors active `MediaSession` tokens on the device. When music plays:
1.  It extracts metadata (Artist, Title, Album Art, Duration, Position).
2.  It cleans the metadata (removing noise like "- Remastered").
3.  It updates the `MusicStateRepository`.

### 2. State Management (Single Source of Truth)
The `MusicStateRepository` holds the global state using `StateFlow`. It synchronizes:
*   `currentLine`: The text currently being sung.
*   `currentPositionMs`: The absolute playback time.
*   `fullLyrics`: The complete set of timestamps and lines.
*   `currentArtwork`: The blurred album art used for the background.

### 3. Settings Interface
The mobile settings screen is built using **Jetpack Compose**, providing a modern and reactive user experience for toggling the service and checking permission statuses without relying on traditional XML layouts.

### 4. Custom Rendering (The Karaoke Effect)
Instead of standard templates, we use a `SurfaceCallback` to draw directly onto the car's screen using `Canvas`.
*   **Deterministic Snap Logic**: To ensure the lyrics never desync, we calculate the active word index based on character weights. Words are highlighted in **Yellow** exactly as the singer utters them.
*   **Auto-Scaling & Margins**: A 15% safe-zone margin is implemented to prevent text clipping. If a line is too long, the font size automatically shrinks to fit the width.
*   **Split Screen (Dashboard)**: By declaring the app as a `NAVIGATION` category and using `MapTemplate`, the app can occupy the primary slot in the Android Auto Dashboard (Coolwalk) alongside music players or maps.

### 5. Local Storage & Capacity Management
The app implements a **No-Backend** policy with local persistence to minimize data usage and ensure offline availability.
*   **Intelligent Caching**: Lyrics are stored in a Room database the first time they are downloaded. Subsequent plays of the same song use the local copy.
*   **Quota Enforcement**: To preserve device storage, the app strictly maintains a limit of **1000 songs** or **200MB** of lyrics data. It automatically purges the oldest records (LRU) when these limits are reached.
*   **Fast Search**: Uses SQLite's FTS4 extension to allow instantaneous searching through thousands of stored songs.
*   **Hybrid Management**: 
    *   **Mobile**: A scrollable list with **Swipe-to-Delete** functionality allows manual management of the library.
    *   **Automotive**: A dedicated delete button in the car interface allows removing the current song's lyrics on-the-go.

---

## 🚀 Setup & Execution Guide

### 1. Mobile & Android Auto Preparation (Required for both DHU and Real Car)
1.  Enable **Developer Options** on your Android phone (Settings > About Phone > Tap 'Build Number' 7 times).
2.  Install the `:mobile` module APK on your phone.
3.  Grant **Notification Access**: Go to Settings > Search "Notification Access" > Enable **LyricsAuto**.
4.  Disable **Battery Optimization**: Settings > Apps > LyricsAuto > Battery > **Unrestricted**.
5.  Prepare the **Android Auto** app on your phone:
    *   Open Android Auto settings.
    *   Scroll to the bottom to **'Version'** and tap it 10 times to enable **Developer Mode**.
    *   Open the three-dot menu (⋮) > **Developer Settings**.
    *   Enable **"Unknown Sources"** (Mandatory for debug apps).
    *   Enable **"Wireless Android Auto"** (Optional).

### 2. Option A: Usage on Desktop Head Unit (DHU Emulator)
1.  Open the **Android Auto** app on your phone > Menu (⋮) > **Start Head Unit Server**.
2.  Connect your phone to your PC via **USB Cable** or **WiFi** (ensure they are on the same network).
3.  Run the ADB Bridge command in your terminal:
    ```powershell
    adb forward tcp:5277 tcp:5277
    ```
4.  Launch the DHU on your PC:
    ```powershell
    cd "$env:LOCALAPPDATA\Android\Sdk\extras\google\auto"
    .\desktop-head-unit.exe
    ```

### 3. Option B: Usage on a Real Car
1.  Connect your phone to your car using a high-quality **USB cable** or via **Wireless Android Auto**.
2.  Ensure **"Unknown Sources"** is enabled in the phone's Android Auto Developer Settings as mentioned in step 1.5.
3.  Wait for the Android Auto interface to appear on the car's screen.
4.  Open the **LyricsAuto** app from the car's app list.
5.  Start playing music on **Spotify** or **YouTube Music** on your phone.
6.  Enjoy real-time lyrics on your car's display.

---

## ⚖️ License & Responsibility
This project is shared under the principle of academic freedom. Redistribution or modification must maintain the author's credits. The software is provided "as is", without warranty of any kind. The developer shall not be held responsible for any legal consequences, accidents, or damages to third parties arising from the use of this software.
