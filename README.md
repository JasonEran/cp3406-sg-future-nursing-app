# Elderly Care Coordination Platform (CP3D406 Project)

[![Platform](https://img.shields.io/badge/platform-Android-A4C639?style=for-the-badge&logo=android)](https://www.android.com)
[![Kotlin Version](https://img.shields.io/badge/kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![UI Framework](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Backend](https://img.shields.io/badge/Backend-Firebase%20Auth%20%7C%20Firestore-FFCA28?style=for-the-badge&logo=firebase)](https://firebase.google.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

A future-ready Android application that unifies family caregivers, helpers, and clinical staff around a shared plan of care. Built for the **CP3406 Mobile Computing** capstone at James Cook University, it pairs elegant Jetpack Compose experiences with secure, cloud-backed coordination tools.

## 📋 Table of Contents
1. [Project Overview](#-project-overview)
2. [Feature Highlights](#-feature-highlights)
3. [Architecture](#-architecture)
4. [Technology Stack](#-technology-stack)
5. [Project Structure](#-project-structure)
6. [UI & UX Design](#-ui--ux-design)
7. [Getting Started](#-getting-started)
8. [Quality & Automation](#-quality--automation)
9. [Project Roadmap](#-project-roadmap)
10. [License](#-license)

## 🧭 Project Overview

Coordinating care for older adults, especially those living with dementia, demands clarity, accountability, and rapid communication. This platform replaces ad-hoc chat threads with a **single source of truth**: tasks, resources, reminders, and analytics live in one place, so every caregiver knows what is coming next and who is responsible.

Primary personas include:
- **Family caregivers** who need daily task scheduling, reminders, and insight into progress.
- **Helpers and community nurses** who execute tasks and update completion states on the go.
- **Administrators/clinicians** who curate care teams, track analytics, and ensure compliance.

## 🌟 Feature Highlights

- **👥 Care Team Workspaces:** Shared task board with real-time updates powered by Firestore, respecting user roles and care group membership.
- **🔐 Adaptive Access Control:** Firebase Auth + custom role resolution ensures only admins reach high-signal screens like analytics and team management.
- **📊 Actionable Analytics:** Compose-driven charts surface completion trends, helper throughput, and overdue work (admin-only view).
- **🔔 Smart Reminders:** WorkManager-driven high-priority notifications highlight the next most urgent task, even in Doze mode.
- **📰 Health News Digest:** NewsAPI.org integration with Retrofit and Room caching keeps caregivers informed about aged-care developments and works offline.
- **📚 Resource Centre:** Curated Firestore articles covering daily care, medication, and first-aid with detailed article pages.
- **🌐 Bilingual Experience:** English and Simplified Chinese strings with runtime language switching to support multicultural families.
- **📱 Responsive Compose UI:** Window size classes unlock adaptive layouts for tablets, foldables, and Chromebooks.

## 🧱 Architecture

Built on a modern, testable stack with clear separation of concerns:
- **Presentation:** Jetpack Compose screens backed by ViewModels, state hoisting, and navigation-compose.
- **Domain:** Use cases (`domain/usecase`) orchestrate task, auth, and profile flows for reuse across screens.
- **Data:** Repository pattern wrapping Firebase Auth, Firestore, Retrofit, and Room; flows stream live updates.
- **Dependency Injection:** Hilt modules wire Firebase, networking, database, and worker factories.
- **Background Work:** WorkManager coordinates scheduled reminders using injected repositories and Firebase context.

```text
app/
|-- src/main/java/com/example/sgfuturenursingapp
|   |-- di/              # Hilt modules for auth, network, DB, tasks, workers
|   |-- domain/          # Use cases encapsulating business rules
|   |-- network/         # Retrofit service + DTOs for News API
|   |-- ui/
|   |   |-- components/  # Reusable Compose widgets
|   |   |-- navigation/  # AppNavigation + route definitions
|   |   |-- screens/     # Auth, dashboard, analytics, resources, profile, etc.
|   |   \-- theme/       # Material 3 theming
|   \-- worker/          # NotificationWorker that surfaces priority tasks
```

## 🛠️ Technology Stack

| Layer | Tooling & Notes |
| --- | --- |
| **Language & UI** | Kotlin 2.0.21, Jetpack Compose BOM 2024.09.00, Material Design 3, Compose Window Size Class |
| **Cloud** | Firebase Authentication, Cloud Firestore, Firebase Crashlytics |
| **Data & Offline** | Room (news cache), Retrofit + OkHttp logging, Kotlinx Coroutines & Flow |
| **Dependency Injection** | Hilt with annotation processing (`kapt`) |
| **Background** | WorkManager high-priority tasks, Android notification APIs |
| **Tooling** | OWASP DependencyCheck, ktlint 12, MockK, JUnit, Coroutine test utilities |

## 📦 Project Structure

| Module | Purpose |
| --- | --- |
| `ui/screens/dashboard` | Today's overview, task list, swipe-to-dismiss with undo, and large-screen split layouts. |
| `ui/screens/admin` | Admin dashboard plus helper search and group membership management. |
| `ui/screens/analytics` | Charts for completion trends, helper stats, and access control logic. |
| `ui/screens/news` | Pull-to-refresh health news feed backed by Room caching. |
| `ui/screens/profile` | Language switching, notification toggles, and care group insights. |
| `ui/data` | Firestore repositories, Room database, News API repository. |
| `domain/usecase` | Focused business actions (task lifecycle, auth state observation, profile updates). |
| `worker` | WorkManager coroutine worker for high-priority task alerts. |

## 🎨 UI & UX Design

Material 3 design language with custom theming, stateful components, and motion. Screenshot hooks are ready - drop your latest exports into `docs/images` to bring the sections below to life.

**Dashboard Screen**  
![Dashboard Mockup](./docs/images/dashboard.png)

**Care Plan Screen**  
![Care Plan Mockup](./docs/images/care_plan.png)

> Tip: For production captures, align mockups with the adaptive layouts defined in `DashboardScreen` and `MainScreen` to highlight the large-screen experience.

## 🚀 Getting Started

**Prerequisites**
- Android Studio Koala (2024.1.1+) with the latest Compose tooling.
- JDK 17 (bundled with Android Studio).
- A Firebase project with Authentication and Firestore enabled.
- Optional: NewsAPI.org key for richer health news feeds.

**Clone & Open**
```bash
git clone https://github.com/JasonEran/cp3406-sg-future-nursing-app.git
cd cp3406-sg-future-nursing-app
```
Open the project in Android Studio and let Gradle sync complete.

**Configure Secrets & Build Config**
- Add your `google-services.json` to `app/`.
- Provide the following values via `gradle.properties` (project-level or user-level):
  - `NEWS_API_KEY` - obtain from [newsapi.org](https://newsapi.org/).
  - `ADMIN_DEMO_EMAIL` & `ADMIN_DEMO_PASSWORD` - optional demo credentials for admin login flows.
  - `NVD_API_KEY` - optional; unlocks faster CVE lookups for DependencyCheck.
- Run a first sync/build: `./gradlew assembleDebug`

**Run**
- Use the `app` configuration in Android Studio, targeting an emulator or device running Android 7.0 (API 24) or newer.
- To exercise WorkManager reminders quickly, trigger the `NotificationWorker` via `WorkManagerTestInitHelper` or schedule it through the profile settings switch.

## 🔍 Quality & Automation

- **Unit tests:** `./gradlew test` (covers dashboard, helper management, and domain use cases).
- **Static analysis:** `./gradlew ktlintCheck` keeps Kotlin style consistent.
- **Security scanning:** `./gradlew dependencyCheckAnalyze` flags known CVEs (fails build for CVSS >= 7).
- **Crash reporting:** Firebase Crashlytics is wired in; configure it before shipping test builds.

## 🗺️ Project Roadmap

| Milestone | Target Completion | Status |
| --- | --- | --- |
| **Assessment 1: Part A** | End of Week 2 | ✅ Complete |
| **Assessment 1: Part B** | End of Week 3 | 🚧 In Progress |
| **Core Architecture Setup** | End of Week 4 | ⏳ Pending |
| **Feature MVP Implementation** | End of Week 7 | ⏳ Pending |
| **Testing & Final Polish** | End of Week 8 | ⏳ Pending |
| **Assessment 2: Final Submission** | End of Week 9 | ⏳ Pending |

## 📜 License

Distributed under the [MIT License](LICENSE). See the license file for full details.
