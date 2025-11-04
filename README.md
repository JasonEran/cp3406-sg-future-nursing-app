# Elderly Care Coordination Platform (CP3D406 Project)

[![Platform](https://img.shields.io/badge/platform-Android-A4C639?style=for-the-badge&logo=android)](https://www.android.com)
[![Kotlin Version](https://img.shields.io/badge/kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![UI Framework](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Backend](https://img.shields.io/badge/Backend-Firebase%20Auth%20%7C%20Firestore-FFCA28?style=for-the-badge&logo=firebase)](https://firebase.google.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

## Project Status

- Feature-complete handover build delivered for **CP3406 Mobile Computing** on **November 4, 2025**.
- All planned backlog items (dashboard revamp, analytics, resources hub, profile settings, CI hardening) are merged into `develop`.
- GitHub Actions pipeline now blocks merges unless lint, unit tests, security scanning, and debug APK builds succeed.

## Table of Contents
1. [Project Overview](#project-overview)
2. [Feature Highlights](#feature-highlights)
3. [Architecture](#architecture)
4. [Technology Stack](#technology-stack)
5. [Project Structure](#project-structure)
6. [UI and UX Notes](#ui-and-ux-notes)
7. [Getting Started](#getting-started)
8. [Quality and Automation](#quality-and-automation)
9. [Delivery Roadmap](#delivery-roadmap)
10. [Delivery Checklist](#delivery-checklist)
11. [License](#license)

## Project Overview

Coordinating dementia and chronic-care routines requires reliable task visibility, delegated responsibilities, and quick alerts. This application gives families, community helpers, and clinical staff a shared workspace that tracks task assignments, overdue actions, learning resources, and broadcast announcements in real time.

Primary personas:
- Family caregivers who need a simple today view and the ability to confirm work is finished.
- Helpers and community nurses who update task states on the go and receive reminder notifications.
- Administrators and clinicians who curate care teams, review analytics, and manage the resource catalogue.

## Feature Highlights

- **Care Team Workspaces:** `ui/screens/dashboard` presents role-aware task lists, priority chips, and swipe-to-complete interactions synchronized through Firestore streams.
- **Adaptive Access Control:** Firebase Authentication plus custom role lookups gate administrator-only surfaces such as analytics and helper management (`ui/screens/admin`, `ui/screens/analytics`).
- **Actionable Analytics:** Compose charts show completion trends, helper throughput, and overdue items, helping staff justify interventions.
- **Smart Reminders:** `worker/NotificationWorker` uses WorkManager to surface the next urgent task even when the app is backgrounded.
- **Health News and Resource Centre:** Retrofit + Room integration (`network`, `ui/screens/news`, `ui/screens/resources`) keeps a bilingual feed of curated articles cached for offline review.
- **Profile and Preferences:** `ui/screens/profile` centralises language toggles (English/Simplified Chinese), notification settings, and demo credentials for assessments.
- **Responsive Compose UI:** Window Size Classes and adaptive navigation patterns keep foldables, tablets, and Chromebooks usable without separate layouts.

## Architecture

The project follows a layered, testable structure with dependency injection everywhere a Firebase, network, or database primitive is needed.

- Presentation: Jetpack Compose + ViewModels with state hoisting, navigation-compose, and reusable UI components.
- Domain: `domain/usecase` folder holds focused interactors that orchestrate repository calls and enforce rules.
- Data: Repository interfaces wrap Firebase Auth, Firestore collections, Retrofit endpoints, and the Room cache while exposing Kotlin Flow streams.
- Dependency Injection: Hilt modules (under `di/`) provide Firebase, Retrofit, Room, workers, and coroutine dispatchers.
- Background Work: WorkManager coordinates scheduled reminders and leverages injected repositories to fetch the top priority task before firing notifications.

```
app/
|-- src/main/java/com/example/sgfuturenursingapp
|   |-- di/                  # Hilt modules
|   |-- domain/              # Use cases and business logic
|   |-- network/             # Retrofit services and DTOs
|   |-- ui/
|   |   |-- components/       # Reusable Compose widgets
|   |   |-- navigation/       # Route constants and NavHost
|   |   |-- screens/          # Auth, dashboard, analytics, news, profile, resources
|   |   \-- theme/            # Material 3 theme system
|   \-- worker/              # WorkManager workers
\-- build.gradle.kts        # Module configuration
```

## Technology Stack

| Layer | Tooling |
| --- | --- |
| Language & UI | Kotlin 2.0.21, Jetpack Compose BOM 2024.09.00, Material 3, Window Size Classes |
| Cloud | Firebase Authentication, Cloud Firestore, Firebase Crashlytics |
| Data & Offline | Retrofit, OkHttp logging, Room, Kotlinx Coroutines + Flow |
| Dependency Injection | Hilt (`kapt`) |
| Background | WorkManager with high-priority notifications |
| Tooling | ktlint 12, MockK, JUnit, Coroutine test utilities, OWASP DependencyCheck 12.1.x |

## Project Structure

| Module / Folder | Purpose |
| --- | --- |
| `ui/screens/dashboard` | Day overview, grouped task lists, swipe-to-complete with undo, tablet split pane layouts. |
| `ui/screens/admin` | Helper roster search, role assignment, care group membership management. |
| `ui/screens/analytics` | Charts for completion trends, helper velocity, overdue heat maps. |
| `ui/screens/news` | Pull-to-refresh health news feed that caches stories in Room for offline viewing. |
| `ui/screens/resources` | Curated care guides, medication tips, and first-aid content served from Firestore. |
| `ui/screens/profile` | Profile summary, notification toggles, language switching, demo account helpers. |
| `domain/usecase` | Business rules for task lifecycle, authentication, and profile updates. |
| `worker` | Notification worker that surfaces the next critical task using repository injections. |

## UI and UX Notes

- The Material 3 theme under `ui/theme` includes dynamic color hooks and typography suited for accessibility.
- Replace the placeholder images referenced in `docs/images/dashboard.png` and `docs/images/care_plan.png` with fresh emulator captures when presenting; the layout automatically adapts to tablets and foldables.
- Motion specs leverage `animateContentSize`, `AnimatedVisibility`, and low-emphasis transitions to keep updates legible for older adults.

## Getting Started

### Prerequisites

- Android Studio Koala (2024.1.1 or newer) with Compose tooling enabled.
- JDK 17 (bundled with Android Studio).
- Firebase project with Authentication and Firestore enabled.
- Optional API keys: NewsAPI.org (`NEWS_API_KEY`) and NVD (`NVD_API_KEY`) for faster DependencyCheck runs.

### Setup

1. Clone the repository and open it in Android Studio:
   ```
   git clone https://github.com/JasonEran/cp3406-sg-future-nursing-app.git
   cd cp3406-sg-future-nursing-app
   ```
2. Copy your `google-services.json` into the `app/` module.
3. Provide the following keys via `gradle.properties` (either project-level or global):
   - `NEWS_API_KEY=your_news_api_key`
   - `ADMIN_DEMO_EMAIL=demo@example.com`
   - `ADMIN_DEMO_PASSWORD=SuperSecret123`
   - `NVD_API_KEY=optional_but_recommended`
4. Run an initial sync: `./gradlew assembleDebug`

### Running the App

- Use the default `app` run configuration, targeting a device or emulator running Android 7.0 (API 24) or higher.
- To trigger notifications quickly during demos, toggle the reminder switch inside the Profile screen or invoke `NotificationWorker` through WorkManager test tooling.

### Testing and Tooling

- Unit tests: `./gradlew test`
- UI lint and composable checks: `./gradlew lint`
- Kotlin style: `./gradlew ktlintCheck`
- Dependency scanning: `./gradlew dependencyCheckAnalyze`
- Build artifacts: `./gradlew assembleDebug` (outputs `app/build/outputs/apk/debug/app-debug.apk`)

## Quality and Automation

The workflow defined in `.github/workflows/android-ci.yml` runs on each push to `develop` and every pull request.

| Job | What it does |
| --- | --- |
| `lint` | Runs `./gradlew lint` on JDK 21 to guard against Compose and XML regressions. |
| `unit_tests` | Executes JVM unit tests (`./gradlew test`) with caching enabled for faster feedback. |
| `security_scan` | Performs OWASP DependencyCheck (`./gradlew dependencyCheckAnalyze`) and uploads the HTML/JSON report artifacts. |
| `build` | Depends on the previous three jobs and assembles a debug APK that is attached as an artifact for testers. |

Use the uploaded dependency-check report (`dependency-check-report` artifact) to document the security posture during demonstrations, and provide the `app-debug` artifact when distributing interim builds without rebuilding locally.

## Delivery Roadmap

| Milestone | Target | Actual Completion | Status |
| --- | --- | --- | --- |
| Assessment 1: Part A | End of Week 2 | November 4, 2025 | Complete |
| Assessment 1: Part B | End of Week 3 | November 4, 2025 | Complete |
| Core Architecture Setup | End of Week 4 | November 4, 2025 | Complete |
| Feature MVP Implementation | End of Week 7 | November 4, 2025 | Complete |
| Testing and Final Polish | End of Week 8 | November 4, 2025 | Complete |
| Assessment 2: Final Submission | End of Week 9 | November 4, 2025 | Complete |

## Delivery Checklist

- [x] Dashboard, analytics, resources, profile, and admin screens validated on phone and tablet breakpoints.
- [x] WorkManager reminders verified through manual trigger and scheduled execution.
- [x] News and resource flows tested online/offline with Room cache fallback.
- [x] GitHub Actions workflow green (lint, unit tests, dependency scan, assemble).
- [x] README, license, and setup instructions updated for the final assessment handover (November 4, 2025).
- [x] Demo fixtures wired into skip-login flow so screenshots never show empty states.

## License

Distributed under the [MIT License](LICENSE). See the license file for complete terms.
