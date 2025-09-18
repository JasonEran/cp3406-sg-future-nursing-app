# Elderly Care Coordination Platform (CP3D406 Project)

[![Platform](https://img.shields.io/badge/platform-Android-A4C639?style=for-the-badge&logo=android)](https://www.android.com)
[![Kotlin Version](https://img.shields.io/badge/kotlin-1.9.20-7F52FF?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![UI Framework](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

A modern Android application designed to streamline caregiving coordination for elderly family members. This project is developed as part of the **CP3406 Mobile Computing** course at James Cook University.

## 📋 Table of Contents

1.  [**Project Overview**](#-project-overview)
2.  [**Core Features**](#-core-features)
3.  [**Architectural Design**](#-architectural-design)
4.  [**Technology Stack**](#-technology-stack)
5.  [**UI & UX Design**](#-ui--ux-design)
6.  [**Getting Started**](#-getting-started)
7.  [**Project Roadmap**](#-project-roadmap)

## 📌 Project Overview

The Elderly Care Coordination Platform tackles a significant real-world challenge: the logistical and emotional difficulty of coordinating care for an elderly loved one, particularly one with a condition like dementia. The application aims to replace fragmented communication channels (like group chats and phone calls) with a single, reliable **source of truth** for all care-related activities. It is designed for family caregivers, the elderly individual, and potentially healthcare professionals, ensuring everyone stays informed and synchronized in real-time.

## ✨ Core Features

* **👥 Multi-User Collaboration:** Built from the ground up for team-based care, featuring a shared activity feed for full transparency.
* **🔐 Role-Based Access Control:** Assign roles (e.g., 'Primary Caregiver') to manage data access and ensure privacy and security.
* **📅 Shared Care Plan:** A real-time, synchronized plan for medications, appointments, and daily routines, preventing missed events and confusion.
* **❗ High-Priority Reminders:** Utilizes robust system services to deliver critical alerts that cannot be easily missed, even when the device is in a low-power state.
* **📊 "Today's View" Dashboard:** A central dashboard providing an at-a-glance summary of the day's tasks—what's due, what's completed, and what's been missed.
* **🔒 Data Portability & Deletion:** Empowers users with the ability to export their data and permanently delete their account.

## 🏗️ Architectural Design

This project adopts a modern, offline-first architecture using the **MVVM (Model-View-ViewModel)** pattern. This separation of concerns ensures the application is scalable, testable, and maintainable.

* **UI Layer (View):** Built entirely with Jetpack Compose, responsible for observing state changes from the ViewModel and rendering the UI.
* **ViewModel Layer:** Holds and manages UI-related data in a lifecycle-conscious way. It exposes state to the UI and handles user interactions.
* **Data Layer (Model):** Manages all application data and business logic. It follows the "single source of truth" principle, using a **Repository Pattern** to abstract data sources.
* **Dependency Injection:** Hilt will be used to manage dependencies, reducing boilerplate and improving code structure.

## 🛠️ Technology Stack

* **Language:** Kotlin
* **UI:** Jetpack Compose & Material Design 3
* **Architecture:** MVVM, Repository Pattern, Offline-First
* **Database:** Room for local data persistence
* **Asynchronous Tasks:** WorkManager & AlarmManager for reliable background tasks
* **Data Storage:** Jetpack DataStore for user preferences
* **Coroutines & Flow:** For managing asynchronous operations

## 🎨 UI & UX Design

*(This section is ready for your UI mockups. Once coded, add screenshots here.)*

**Dashboard Screen:**
`![Dashboard Mockup](./docs/images/dashboard.png)`

**Care Plan Screen:**
`![Care Plan Mockup](./docs/images/care_plan.png)`

## 🚀 Getting Started

1.  Clone the repository:
    ```bash
    git clone [https://github.com/JasonEran/cp3406-sg-future-nursing-app.git](https://github.com/JasonEran/cp3406-sg-future-nursing-app.git)
    ```
2.  Open the project in the latest stable version of Android Studio.
3.  Build and run the application on an emulator or a physical device.

## 🗺️ Project Roadmap

| Milestone                       | Target Completion | Status      |
| ------------------------------- | ----------------- | ----------- |
| **Assessment 1: Part A** | End of Week 2     | ✅ Complete |
| **Assessment 1: Part B** | End of Week 3     | ⏳ In Progress |
| **Core Architecture Setup** | End of Week 4     | 🔲 Pending   |
| **Feature MVP Implementation** | End of Week 7     | 🔲 Pending   |
| **Testing & Final Polish** | End of Week 8     | 🔲 Pending   |
| **Assessment 2: Final Submission**| End of Week 9     | 🔲 Pending   |