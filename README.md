# Smart Roots App

> **A modern, AI-powered hydroponics assistant — beautifully composed, thoughtfully architected, and built for extensibility.**

---

## 🌱 Table of Contents

1. [Project Overview](#project-overview)
2. [Key Features](#key-features)
     - [Home Features Overview](#home-features-overview)
     - [Notes System](#notes-system)
     - [Device Image/Camera Features](#device-imagecamera-features)
     - [Tent Management](#tent-management)
     - [Haptic Feedback](#haptic-feedback)
     - [PDF & Export](#pdf--export)
3. [Architecture](#architecture)
    - [Core Layers](#core-layers)
    - [UI & Theming](#ui--theming)
    - [AI & Contextual Assistance](#ai--contextual-assistance)
    - [Networking & Device Control](#networking--device-control)
     - [Repository & Service Layer Structure](#repository--service-layer-structure)
     - [ViewModel Design](#viewmodel-design)
     - [Navigation & Composables](#navigation--composables)
4. [How It Works](#how-it-works)
5. [Why This Approach?](#why-this-approach)
6. [Room for Expansion](#room-for-expansion)
7. [Next Steps: Multiplatform Vision](#next-steps-multiplatform-vision)
8. [License](#license)
9. [Reference: Explore the Codebase](#reference-explore-the-codebase)

---

## 📜 Project Overview

Smart Roots App is the next-generation smart agriculture companion, fusing **real-time IoT controls**, **AI assistant guidance**, and **beautiful analytics** into a single composable application for hydroponic systems. Its mission is to empower growers to monitor, maintain, and optimize their setup — with actionable intelligence and zero jargon.

---

## ✨ Key Features

### Home Features Overview

- **Live Sensor Dashboards:** Realtime views of environmental variables such as humidity, EC, pH, light intensity, and more.
- **Device Control (Toggles):** Instantly control tent actuators, including lights, pumps, extractors, and fans. Modular API design supports easy addition of new device types.
- **Notifications:** Supports in-app and system notifications for alerts and commands needing user attention.
- **Adaptive Feature Listing:** Feature list is dynamically loaded; localization, permissions, and device context supported.

### Notes System

- **Rich Notes:** Create, view, and export notes with title, description, and images (camera/gallery with Base64 conversion).
- **Timestamps:** Every note is timestamped and stored in Firebase Realtime Database.
- **Export:** PDF generation and export for all notes, with image and metadata.
- **Harvest Tracker (built on Notes):** Track harvests and cycles, including planting dates and completion.

### Device Image/Camera Features

- **Live Tent Images:** View the latest tent image (by MAC address) from the tent or remote source.
- **AI Disease Detection via Camera:** Capture photo and run TensorFlow Lite model for instant plant disease analysis. Flexible for future more advanced models.
- **Age Detection:** Scans user face via camera, applies dynamic UI adaptation for accessibility (font size, simplified navigation), with strong privacy controls.

### Tent Management

- **Secure Connect:** Join/leave management for hydroponics tent, with password dialogs, tent metadata (location, type, org).
- **Multiple Tents:** Manages multiple tents and devices within single user experience.

### Haptic Feedback

- **Tactile Experience:** Operations (tap/confirm) can give real physical feedback for critical actions, bringing a pro/accessible mobile UX.

### PDF & Export

- **Single-click Export:** Generate PDF records from plant notes/history and save to device.
- **Future-Proof:** Design for CSV/other export formats.

---

## 🏗️ Architecture

### Core Layers

- **UI/UX:** Built on **Jetpack Compose**, using atomic and highly reusable `Composable` components for every screen.
- **ViewModels:** Lifecycle-aware, modular, and simple. Each major feature (sensor dashboard, chat, notes, tents) gets its own state and dependency-injected ViewModel.
- **AI Subsystems:** Plugged-in via a clear interface, supporting Google Gemini APIs for chat, and TFLite models for on-device inference.
- **Networking:** **Ktor** and **Retrofit**. Most controls flow through RESTful endpoints (`https://smart-roots-server.onrender.com`), enabling real-time, scalable communication.
- **Local Storage:** Notes use Firebase Realtime Database; further expandability for Room DB/local-first support.

### UI & Theming

- Frenetically branded for modern agriculture — bold typography powered by custom League Spartan font, and a dynamic light/dark color palette.
- UI elements (shape, spacing, input widgets) centralized for atomic reuse and consistent style.
- Theming and branding **never hardcoded**: everything flows from centralized theme configuration.

### AI & Contextual Assistance

- “Fred” Chatbot is **bound to strict system prompt** — plant/hydroponic only.
- Disease detection leverages TFLite for instant, privacy-preserving plant analysis.
- Age-based UI adapts on-the-fly for accessibility.
- All AI flows are wrapped with robust error handling and deterministic output for clarity.

### Networking & Device Control

- **No static IPs** — all device communication routes through the Smart Roots backend.
- Device toggles and sensor readings use secure, modular API endpoints.
- Tent connection, sensor/actuator updates, and images run in clean, injectable repositories/services.

#### Repository & Service Layer Structure

- Device and data access are via a `Repository` and Retrofit-powered `Service` (Pattern). Types of repositories/services:
  - `SensorRepository` + `SensorService`: For all tent sensor readings, both over local and cloud connection.
  - `ComponentRepository` + `ComponentService`: For toggling and controlling hardware devices (pump, fan, light, pH/EC controls, etc.)
  - `ImageRepository` + `ImageService`: For retrieving live tent images.
  - `TentRepository` + `TentService`: Tent management and tent metadata.
- Repositories abstract details away from ViewModels/screens, making code testable and portable!

#### ViewModel Design

- Each major screen (Sensors, Tent, Notes, Chat, Camera) has its own ViewModel.
- Handles business logic, network calls, and state for the Composables.
- Reactive updates (LiveData, StateFlow) power UI.

#### Navigation & Composables

- Navigation handled via Compose’s navigation-composable.
- All major features (Chat, Note, Image, Dashboard, Tent) are directly linked in bottom navigation for speed.
- Home Dashboard shows dynamic "features" depending on tent/device capabilities and user config.

---

## 🔍 How It Works

1. **User Logs In:** Enters Smart Roots and connects to their hydroponic tent/system.
2. **Live Data:** Sensor dashboard painted in real time, using Compose graphs and cards.
3. **Device Management:** With a tap, users toggle devices (pump, light, fans) via cloud calls.
4. **AI Interaction:** User can chat with “Fred” for help on plant care, or snap a plant photo to get health guidance.
5. **Harvest Tracker/Notes:** Simple forms log planting/harvest events, with camera and PDF export.
6. **Tent Management:** Manages connection, view details, add/remove tent.
7. **UI Adaptation:** Face scan (with consent!) adapts sizes for different age groups.

---

## 🌀 Why This Approach?

- **Modularity:** Every feature is its own atomic, composable unit, ready for on/off toggling or replacement.
- **Injectability (DI):** Uses Koin for dependency injection. All services, APIs, and ViewModels are easily swappable.
- **Extensible API:** New devices or AI tools can be added with almost no UI refactor.
- **Accessibility:** Designed for all ages; haptics, UI resize, context-aware navigation.

---

## 🚀 Room for Expansion

- 🔗 **Notification Integration:** Harvest Tracker could push smart reminders (“Your lettuce is ready to harvest!”).
- 🌍 **Localization:** Expand beyond English and South African culture.
- 🧠 **Smarter Plant Models:** Use more advanced AI/ML models for disease/nutrient/deficiency detection — and allow user-curated knowledge.
- 💾 **Cloud Sync:** Seamless backup, user profiles, and cross-device sync.
- 🛡️ **Security Options:** MFA/2FA and improved data encryption.
- 🔗 **Open Hardware APIs:** Partner with 3rd-party device makers.

---

## 🔮 Next Steps: Multiplatform Vision

**Smart Roots App is architected for multiplatform.**  
Major logic, business rules, and state management are decoupled from Android specifics.  
Ready to evolve to iOS and web dashboards using Jetpack Compose Multiplatform and KMM principles.

---

## 📃 License

Smart Roots App is licensed under the [GNU GPL v3.0](https://choosealicense.com/licenses/gpl-3.0/).

---

## 🏗️ Reference: Explore the Codebase

Due to space, not all details may be reflected here.  
See the source and code search for further exploration:  
- [Search features in the Smart Roots App codebase on GitHub.](https://github.com/Code-Syndicate-SH/Smart-Roots-App/search?q=feature+screen+viewmodel+service+api)

---

> *Built by Code-Syndicate-SH: Empowering smarter farmers, everywhere.*
