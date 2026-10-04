<div align="center">

# 🎓 StudentHub (Student Manager)

**All-in-One Android Academic Planner, Attendance Guardian & Expense Tracker**  
*Engineered with Kotlin and Jetpack Compose to eliminate student chaos with automated lecture alarms, attendance tracking, custom canvas spending graphs, and offline persistence.*

[![Platform: Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Room Database](https://img.shields.io/badge/Room_DB-SQLite-0284C7?style=for-the-badge&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![License: MIT](https://img.shields.io/badge/License-MIT-F59E0B?style=for-the-badge)](LICENSE)

</div>

---

## 📖 Overview

University life requires balancing multiple priorities: tracking strict attendance minimums, managing class timetables, keeping track of daily college supplies, and managing a tight student budget.

**StudentHub (Student Manager)** is a native Android application built specifically to address daily student pain points. Powered by **Kotlin** and **Jetpack Compose (Material 3)**, it features automated lecture alarm scheduling, persistent offline Room databases, and custom Canvas-rendered expense analytics.

---

## 🚀 Key Modules & Capabilities

```
┌────────────────────────────────────────────────────────────────────────┐
│                   STUDENTHUB — ANDROID APPLICATION                     │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
   ┌─────────────────┬──────────────┴───────────────┬────────────────┐
   ▼                 ▼                              ▼                ▼
┌──────────────┐  ┌──────────────┐           ┌──────────────┐ ┌──────────────┐
│  ATTENDANCE  │  │  TIMETABLE   │           │   SPENDING   │ │  ESSENTIALS  │
│   GUARDIAN   │  │  AUTO-ALARMS │           │  ANALYTICS   │ │  CHECKLIST   │
├──────────────┤  ├──────────────┤           ├──────────────┤ ├──────────────┤
│ 75% Cutoff   │  │ AlarmManager │           │ Custom Canvas│ │ ID Card      │
│ Tap Logging  │  │ BootReceiver │           │ Category Pie │ │ Calculator   │
│ Safe Margin  │  │ Push Alerts  │           │ Monthly Trend│ │ Lab Manual   │
└──────────────┘  └──────────────┘           └──────────────┘ └──────────────┘
```

### 🛡️ 1. Attendance Guardian (`AttendanceScreen.kt`)
- **One-Tap Attendance Logging:** Quick "Present", "Absent", or "Cancelled" toggles for each subject.
- **Safety Margin Calculations:** Automatic computation of attendance percentage against university minimum requirements (75% and 85%).
- **Shortage Warnings:** Highlights subjects currently at risk with immediate alerts.

### 🕒 2. Timetable & Automated Lecture Alarms (`TimetableScreen.kt`)
- **Daily & Weekly Timetable Views:** View lectures with room numbers, instructors, and lecture periods.
- **Native Android AlarmManager (`ClassNotificationScheduler.kt`):** Automatically dispatches notifications 10–15 minutes before scheduled classes begin.
- **Reboot Resilience (`BootReceiver.kt`):** Seamlessly reschedules active class reminder alarms when the device restarts.

### 💰 3. Student Expense Tracker & Graph (`SpendingScreen.kt`, `SpendingGraph.kt`)
- **Custom Canvas Charts:** Procedurally rendered dynamic spending graphs visualizing day-by-day expenditure.
- **Category Allocation:** Group expenses into *Food & Canteen*, *Books & Stationery*, *Transport*, *Hostel*, and *Miscellaneous*.
- **Monthly Budget Guard:** Set allowance ceilings and monitor burn rate in real time.

### 🎒 4. College Essentials Checklist (`EssentialsScreen.kt`)
- **Morning Packing Checklist:** Never forget hall tickets, college ID badges, scientific calculators, or lab records.
- **Daily Reset:** Quick reset button to prepare your bag for the next academic day.

### ⚡ 5. 100% Offline-First Architecture (`StudentDatabase.kt`)
- **Zero Internet Requirement:** Powered by Android Room SQLite ORM for instant responsiveness and complete privacy.

---

## 🏛️ Architecture & File Structure

Built using the **MVVM (Model-View-ViewModel)** architectural pattern with modern Android conventions:

```
app/src/main/java/com/example/
├── MainActivity.kt                      # Single activity hosting StudentHubApp
├── ui/
│   ├── StudentHubApp.kt                 # Top-level scaffold & navigation host
│   ├── screens/
│   │   ├── DashboardScreen.kt           # Executive hub summary & quick actions
│   │   ├── AttendanceScreen.kt          # Attendance logger & percentage status
│   │   ├── TimetableScreen.kt           # Weekly schedule viewer
│   │   ├── SpendingScreen.kt            # Expense logging & budget dashboard
│   │   └── EssentialsScreen.kt          # Daily college bag checklist
│   ├── components/
│   │   ├── SpendingGraph.kt             # Custom Compose Canvas bar and curve chart
│   │   ├── DailyTimetableComponent.kt   # Day agenda widget
│   │   └── CommonDialogs.kt             # Material 3 dialog modals
│   ├── viewmodel/
│   │   └── StudentHubViewModel.kt       # State container coordinating database flows
│   └── theme/                           # Color, Typography, Material 3 Shape themes
├── data/
│   ├── model/Entities.kt                # Subject, AttendanceRecord, Expense entities
│   ├── local/StudentDatabase.kt         # Room Database configuration
│   ├── local/StudentHubDao.kt           # Room DAO queries with Coroutines Flow
│   └── repository/StudentHubRepository.kt
└── notification/
    ├── ClassNotificationScheduler.kt    # AlarmManager reminder scheduler
    ├── ClassReminderReceiver.kt         # Notification broadcast receiver
    └── BootReceiver.kt                  # Device reboot alarm restorer
```

---

## 🛠️ Tech Stack & Android Libraries

- **Language:** Kotlin 2.0+
- **UI Toolkit:** Jetpack Compose (Material Design 3)
- **Local Database:** Room Database (SQLite)
- **Background Tasks:** Android `AlarmManager` & `BroadcastReceiver`
- **Concurrency:** Kotlin Coroutines & `StateFlow`
- **Graphics:** Android Jetpack Compose Canvas API

---

## 🚀 Installation & Running

### Requirements
- **Android Studio:** Ladybug or Koala
- **JDK:** 17+
- **Min SDK:** 24 (Android 7.0+)
- **Target SDK:** 34 (Android 14+)

### Building via CLI

```bash
# Clone the repository
git clone https://github.com/shubhamkerure07/student-manager.git
cd student-manager

# Build debug APK
./gradlew assembleDebug

# Install on connected device or emulator
./gradlew installDebug
```

---

## 👨‍💻 Author

**Shubham Kerure**  
*Mechatronics Engineering Student @ Mangalore Institute of Technology & Engineering (MITE)*  
- **GitHub:** [@shubhamkerure07](https://github.com/shubhamkerure07)  
- **Portfolio:** [portfolio-lac-two-76.vercel.app](https://portfolio-lac-two-76.vercel.app/)  
- **LinkedIn:** [Shubham Kerure](https://www.linkedin.com/in/shubham-kerure-23350938b)  
- **Email:** [shubhamkerure13@gmail.com](mailto:shubhamkerure13@gmail.com)

---

<div align="center">
<sub>Designed & Built by Shubham Kerure • Licensed under MIT</sub>
</div>