# Elderly Care Coordination Platform (CP3406 Project)

[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com)
[![Language](https://img.shields.io/badge/language-Kotlin-blue.svg)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-orange.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A modern Android application designed to streamline caregiving coordination for elderly family members. This project is developed as part of the CP3406 Mobile Computing course at James Cook University.

## Table of Contents

1.  [Project Overview](#-project-overview)
2.  [Core Features](#-core-features)
3.  [Architectural Design](#-architectural-design)
4.  [Technology Stack](#-technology-stack)
5.  [UI & UX Design](#-ui--ux-design)
6.  [Getting Started](#-getting-started)
7.  [Project Roadmap](#-project-roadmap)

## Project Overview

[cite_start]The Elderly Care Coordination Platform tackles a significant real-world challenge: the logistical and emotional difficulty of coordinating care for an elderly loved one, particularly one with a condition like dementia[cite: 3]. The application aims to replace fragmented communication channels (like group chats and phone calls) with a single, reliable source of truth for all care-related activities. [cite_start]It is designed for family caregivers, the elderly individual, and potentially healthcare professionals, ensuring everyone stays informed and synchronized in real-time[cite: 4, 7].

## Core Features

The application is being developed with a focus on reliability, collaboration, and ease of use.

* [cite_start]**Multi-User Collaboration:** Built from the ground up for team-based care, featuring a shared activity feed for full transparency[cite: 10].
* [cite_start]**Role-Based Access Control:** Assign roles (e.g., 'Primary Caregiver') to manage data access and ensure privacy and security[cite: 16].
* [cite_start]**Shared Care Plan:** A real-time, synchronized plan for medications, appointments, and daily routines, preventing missed events and confusion[cite: 19].
* [cite_start]**High-Priority Reminders:** Utilizes robust system services to deliver critical alerts that cannot be easily missed, even when the device is in a low-power state[cite: 20, 24].
* [cite_start]**"Today's View" Dashboard:** A central dashboard providing an at-a-glance summary of the day's tasks—what's due, what's completed, and what's been missed[cite: 22].
* [cite_start]**Data Portability & Deletion:** Empowers users with the ability to export their data and permanently delete their account[cite: 17].

## Architectural Design

This project adopts a modern, offline-first architecture using the **MVVM (Model-View-ViewModel)** pattern. This separation of concerns ensures the application is scalable, testable, and maintainable.

* **UI Layer (View):** Built entirely with Jetpack Compose, responsible for observing state changes from the ViewModel and rendering the UI.
* **ViewModel Layer:** Holds and manages UI-related data in a lifecycle-conscious way. It exposes state to the UI and handles user interactions.
* **Data Layer (Model):** Manages all application data and business logic. It follows the "single source of truth" principle, using a **Repository Pattern** to abstract data sources (local database vs. network).
* **Dependency Injection:** Hilt will be used to manage dependencies, reducing boilerplate and improving code structure.

## Technology Stack

* **Kotlin:** The primary programming language.
* **Jetpack Compose:** For building the entire UI declaratively.
* **Material Design 3:** For UI components and design language.
* [cite_start]**Room Database:** For robust, offline-first data persistence[cite: 25].
* [cite_start]**WorkManager & AlarmManager:** For scheduling reliable, deferrable, and guaranteed background tasks and critical reminders[cite: 24].
* [cite_start]**Foreground Services:** For persistent, long-running operations like ongoing reminders[cite: 24].
* [cite_start]**Jetpack DataStore:** For securely storing simple key-value data like user preferences or auth tokens[cite: 25].
* **Coroutines & Flow:** For managing asynchronous operations throughout the app.

## UI & UX Design

*(This section is crucial for your Part B presentation. Once you've coded the mockups, add screenshots here.)*

**Dashboard Screen:**
![Dashboard Mockup](URL_TO_YOUR_DASHBOARD_SCREENSHOT)

**Care Plan Screen:**
![Care Plan Mockup](URL_TO_YOUR_CARE_PLAN_SCREENSHOT)

## Getting Started

1.  Clone the repository: `git clone https://github.com/JasonEran/cp3406-sg-future-nursing-app.git`
2.  Open the project in the latest stable version of Android Studio.
3.  Build and run the application on an emulator or a physical device.

## Project Roadmap

This timeline aligns with the course schedule for CP3406.

| Milestone                       | Target Completion | Status      |
| ------------------------------- | ----------------- | ----------- |
| **Assessment 1: Part A** | End of Week 2     | ✅ Complete |
| **Assessment 1: Part B** | End of Week 3     | ⏳ In Progress |
| **Core Architecture Setup** | End of Week 4     | 🔲 Pending   |
| **Feature MVP Implementation** | End of Week 7     | 🔲 Pending   |
| **Testing & Final Polish** | End of Week 8     | 🔲 Pending   |
| **Assessment 2: Final Submission** | End of Week 9     | 🔲 Pending   |