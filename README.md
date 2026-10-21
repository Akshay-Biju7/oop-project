# Vyntrix — Fitness & Personal Gain Tracking App
### B.Tech Computer Science & Engineering — Object-Oriented Programming (OOP) Desktop Application

![Java Version](https://img.shields.io/badge/Java-17%20LTS%20%7C%2021%20LTS%20%7C%2026-00E5FF?style=for-the-badge&logo=openjdk)
![Swing](https://img.shields.io/badge/GUI-Java%20Swing%20%2B%20FlatLaf-0A0E17?style=for-the-badge)
![Theme](https://img.shields.io/badge/Aesthetic-Cyber%20Cyan%20%26%20Gold-FFB800?style=for-the-badge)
![Database](https://img.shields.io/badge/Database-Supabase%20PostgreSQL%20%2B%20SQLite-3ECF8E?style=for-the-badge&logo=supabase)
![Build](https://img.shields.io/badge/Build-Maven%203.9-C71A36?style=for-the-badge&logo=apachemaven)
![IDE](https://img.shields.io/badge/IDE-Eclipse%20%2F%20IntelliJ%20%2F%20VSCode-2C2255?style=for-the-badge)

---

## 📌 Project Overview

**Vyntrix** is an enterprise-grade desktop fitness and athletic development application engineered for bodybuilders, athletes, and fitness enthusiasts. Built with a futuristic dark navy and cyber aesthetic accented with electric cyan (`#00E5FF`) and golden-yellow (`#FFB800`), Vyntrix unifies progressive overload tracking, specialized **Cut & Bulk workout splits**, personalized **Cut & Bulk nutrition engines**, anthropometric health screening, hydration, sleep recovery, milestone goals, and automated analytics.

The application strictly follows Object-Oriented Programming (OOP) principles, clean MVC architecture, DAO abstraction, JDBC prepared statements, and dual-mode database persistence with Supabase PostgreSQL and an embedded SQLite offline fallback.

---

## 🎨 App Branding & Visual Identity

- **Official Name**: **Vyntrix** (used consistently throughout titles, labels, dialogs, and reports).
- **Official Emblem**: Futuristic human silhouette enveloped in cyan, teal, and golden orbital rings.
- **Login Banner**: Sleek cyan emblem with the Vyntrix wordmark.
- **Color Palette**:
  - Primary Background: Dark Cyber Navy (`#0A0E17`) & Card Panels (`#111827`, `#1A2234`)
  - Accent Primary: Electric Cyan (`#00E5FF`)
  - Accent Secondary: Golden-Yellow (`#FFB800`)
  - Success / Active: Vibrant Mint (`#00E676`)
  - Text: Clean High-Contrast Whites (`#F3F4F6`) & Cool Grays (`#9CA3AF`)
- **Splash Screen**: Undecorated startup window displaying the official Vyntrix emblem, animated progress bar, status loading messages, and smooth transition to the Login screen.
- **Application Window Icon**: High-resolution Vyntrix orbital emblem configured across all application frames and dialogs.

---

## 🚀 Core Modules & Features

### 1. 🔐 Redesigned Login & Authentication
- **Clean Aesthetic**: Removed legacy sidebar titles ("Personal Training Tracking System" / "Gym Workout Logger") in favor of a sleek, centered commercial-grade presentation.
- **Top Branding**: Centered high-resolution Vyntrix logo banner.
- **Headings**: *"Welcome to Vyntrix"* with subtitle *"Track your fitness. Build your future."*
- **Show / Hide Password**: Password field with a toggle eye icon button for user convenience and security.
- **Instant Demo**: One-click **Quick Demo Account** button for swift evaluation and viva demonstrations.
- **Security**: Passwords salted and hashed with **jBCrypt** (12 log rounds); full email and input validation.

### 2. 📊 Dynamic Command Dashboard with Daily Reminders
- **Real-Time Saved Metrics**: Pulls live data from the database (no hardcoded mockups).
- **Metric Highlights**: Current vs Target Weight, Calories In vs Burned, Protein progress, Water consumption, Sleep duration, Weekly workouts, and Current workout streak.
- **Integrated Daily Reminders & Alerts Widget**: High-visibility reminder cards embedded directly into the central dashboard with scheduled times, motivational alerts, quick dismissal, and one-click reminder creation.
- **Interactive Visualizations**: JFreeChart weekly workout frequency chart, longitudinal weight progression vs target curve, recent workouts, and recent meal logs.

### 3. 🏋️ Gym Workouts — Cut & Bulk Modes + Goals & Trophy Case
- **Dual Training Modes**:
  - **Cut Mode**: High-density training splits tailored for calorie deficit conditions (higher rep ranges, 60s rest periods, and integrated cardio).
  - **Bulk Mode**: Progressive overload compound lifts designed for maximum hypertrophy and mechanical tension (heavy compound sets, 120s–150s rest, minimal cardio).
- **Weekly Workout Planner (Monday to Sunday)**:
  - Displays day name, target muscle group, exercise list, sets, repetitions, rest duration (seconds), optional cardio, workout notes, and completion toggles.
  - **Independent Saved Plans**: Custom edits for Cut and Bulk are saved separately in the database without overwriting each other.
  - **Completed Workout Sync**: Checking off a day's workout logs it into actual workout history (`workouts` table), updating total volume, streaks, and dashboard stats.
  - **Reset to Defaults**: One-click reset to restore evidence-based sample splits for either mode.
- **Integrated Workout Timers**: Live stopwatch timer (Start/Pause/Resume/Reset) and customizable rest interval countdown timer (30s, 60s, 90s, 120s).
- **🎯 Gym Milestone Goals & Trophy Case**:
  - Embedded directly below the workout history with a distinct header and generous visual spacing.
  - **Active Gym Goals**: Set targets (e.g. "Bench Press 100 kg"), starting vs target values, and progress percentage bars.
  - **🏆 Trophy Case & Badges**: Unlockable achievement badges (*First Step, Iron Consistency, Beast Mode, PR Smasher, Macro Master, Goal Crusher, Hydration Hero, Sleep Champion*) with real-time unlock detection!

### 4. 🥗 Nutrition — Cut & Bulk Modes
- **Dual Nutrition Modes**:
  - **Cut Nutrition**: Structured around a target calorie deficit with high protein density to preserve lean muscle tissue.
  - **Bulk Nutrition**: Structured around a clean caloric surplus with balanced complex carbohydrates and fats for progressive strength.
  - Can be toggled independently of the workout mode.
- **Daily Meal Planner**:
  - Structured meal timeline: **Breakfast**, **Mid-Morning Snack**, **Lunch**, **Evening Snack**, **Dinner**, and **Post-Workout Meal**.
  - Default evidence-based foods: Oats, whole eggs, milk, chicken breast, white rice, salmon, paneer, mixed vegetables, bananas, and whey protein.
  - Per-item macro breakdown: Grams of Protein, Carbs, Fat, and total Calories.
- **Log as Consumed**:
  - Differentiates planned meals from actually consumed meals.
  - Clicking **"Log as Consumed Today ✓"** writes real consumption to the `meals` database table and instantly updates the daily dashboard progress bars.
- **Personalized Targets Calculator**:
  - Interactive calculator using the **Mifflin-St Jeor formula** (Age, Gender, Height, Weight, Activity Level, and Goal).
  - Automatically calculates BMR and TDEE, recommending a controlled calorie deficit (-450 kcal) for Cut or surplus (+400 kcal) for Bulk.
  - Includes medical disclaimers emphasizing that nutritional estimates are not medical advice.
  - **Copy Yesterday**: One-click duplicate of the previous day's nutrition log.

### 5. 💧 Water, Sleep & Habit Tracking
- **Hydration Logging**: Quick-add buttons (+250ml glass, +500ml shaker, +750ml bottle, custom amount) with real-time target progress indicator.
- **Sleep & Recovery**: Bedtime, wake time, duration calculation, and perceived sleep quality scoring.

### 6. 📅 Weekly Workout Planner
- Full-page Monday to Sunday training calendar displaying target muscle splits, rest intervals, exercises, and completion tracking.

### 7. 👤 Athlete Profile & Health Hub (Centralized)
- Streamlines the main sidebar navigation by centralizing physical metrics, body measurements, and analytics into three tabbed views:
  - **👤 Athlete Profile & Credentials**: Personal bio details, Mifflin-St Jeor formula recalculations, password update, and cloud database setup.
  - **📏 Health Metrics & Anthropometry**:
    - Circumference tracking: Weight, Height, Waist, Chest, Arms, Thighs, Hips, and Neck.
    - **BMI Screening**: Formulated with official WHO categories and clinical disclaimer.
    - **Recomposition Comparison Tool**: Compare any two dates to observe real physical adaptations (+/- cm and kg).
  - **📈 Progress Reports & Analytics**:
    - Longitudinal **JFreeChart** trends (weekly workout frequencies, calorie trends, weight progression, volume overload).
    - Standard RFC 4180 CSV exports for workout sessions and nutrition logs.
    - Printable progress report generator.

---

## 🏛️ Object-Oriented Architecture (OOP)

The project adheres to clean OOP principles (Encapsulation, Inheritance, Polymorphism, Abstraction), modular packaging, and strict separation of concerns:

```
src/main/java/com/pgtr/
├── model/                     # Domain Entities, OOP Base Classes, Enums
│   ├── IdentifiableEntity.java   # Abstract base class (Abstraction & Inheritance)
│   ├── User.java                 # User profile + BMR/TDEE/BMI scientific formulas
│   ├── Workout.java              # Workout session entity + volume calculations
│   ├── Exercise.java             # Exercise item (sets × reps × weight kg)
│   ├── WorkoutPlan.java          # Weekly planner routine + Cut/Bulk mode & rest times
│   ├── NutritionPlanItem.java    # Planned meal entity with Cut/Bulk mode
│   ├── Meal.java                 # Actual logged meal consumption
│   ├── FoodItem.java             # Food database item
│   ├── BodyMeasurement.java      # Circumferences & BMI calculations
│   ├── WaterLog.java             # Timestamped hydration log
│   ├── SleepLog.java             # Sleep duration and quality
│   ├── FitnessGoal.java          # Milestone goals with completion percentages
│   ├── Reminder.java             # In-app notifications
│   ├── Achievement.java          # Badge catalog definition
│   └── UserAchievement.java      # Earned user badge relationship
│
├── dao/                       # Data Access Interfaces (Polymorphism & Abstraction)
│   ├── UserDAO.java
│   ├── WorkoutDAO.java
│   ├── MealDAO.java
│   ├── NutritionPlanDAO.java     # Cut & Bulk meal plan persistence
│   ├── HealthDAO.java
│   ├── WaterDAO.java
│   ├── SleepDAO.java
│   ├── GoalDAO.java
│   ├── PlannerDAO.java           # Cut & Bulk weekly workout plan persistence
│   ├── ReminderDAO.java
│   └── AchievementDAO.java
│
├── dao/impl/                  # JDBC Data Access Implementations (Prepared Statements)
│   ├── UserDAOImpl.java
│   ├── WorkoutDAOImpl.java
│   ├── MealDAOImpl.java
│   ├── NutritionPlanDAOImpl.java
│   ├── HealthDAOImpl.java
│   ├── WaterDAOImpl.java
│   ├── SleepDAOImpl.java
│   ├── GoalDAOImpl.java
│   ├── PlannerDAOImpl.java
│   ├── ReminderDAOImpl.java
│   └── AchievementDAOImpl.java
│
├── service/                   # Business Logic Layer & Domain Services
│   ├── UserSession.java          # Thread-safe Singleton session manager
│   ├── AuthService.java          # BCrypt password validation & registration
│   ├── WorkoutService.java       # Streaks, volume aggregates, PR auto-detection
│   ├── NutritionService.java     # Macro aggregation & Cut/Bulk target calculations
│   ├── HealthService.java        # Anthropometric differences & BMI screening
│   ├── HabitService.java         # Water and sleep logic
│   ├── GoalService.java          # Goal lifecycle & progress math
│   ├── AchievementService.java   # Dynamic badge unlock triggers
│   ├── PlannerService.java       # Cut & Bulk workout scheduling
│   ├── ReminderService.java      # Alert notifications
│   └── ReportService.java        # JFreeChart generators & CSV/Print reports
│
├── database/                  # Database Layer
│   └── DatabaseManager.java      # Supabase PostgreSQL + SQLite local fallback with auto-migrations
│
├── util/                      # Security, Math & Design Tokens
│   ├── Theme.java                # Dark Cyber Navy, Electric Cyan, Gold, App Icons
│   ├── PasswordHasher.java       # jBCrypt password encryption
│   ├── ValidationUtil.java       # Input sanitization and validation
│   ├── DateUtil.java             # Date formatting and week boundaries
│   └── CsvExporter.java          # RFC 4180 CSV builder
│
├── view/                      # Presentation Layer (Java Swing MVC)
│   ├── SplashScreen.java         # Futuristic animated loading screen
│   ├── LoginFrame.java           # Centered Vyntrix login & registration
│   ├── MainFrame.java            # Main window container (Sidebar, Header, CardLayout)
│   ├── DashboardPanel.java       # Central metric summary & charts
│   ├── WorkoutPanel.java         # Cut/Bulk workout mode cards, planner & timers
│   ├── NutritionPanel.java       # Cut/Bulk meal planner, "Log as Consumed", target calculator
│   ├── HealthPanel.java          # Measurements & comparison delta tool
│   ├── HabitsPanel.java          # Hydration and sleep logs
│   ├── GoalsPanel.java           # Milestones & trophy case
│   ├── PlannerPanel.java         # Full-page weekly calendar scheduler
│   ├── ReportsPanel.java         # Chart analytics and export tools
│   └── SettingsPanel.java        # Profile editor & live database connection tester
│
├── view/components/           # Reusable Custom Swing Components
│   ├── ModernCard.java           # Charcoal rounded panel with custom border
│   ├── StatCard.java             # Key performance indicator cards
│   ├── NeonButton.java           # Cyan, Gold, Mint, and Ghost styled buttons
│   ├── ModernProgressBar.java    # Sleek rounded gradient progress bars
│   ├── StopwatchTimerPanel.java  # Live workout stopwatch & rest timer
│   └── DarkTable.java            # Customized dark JTable with alternating row colors
│
└── main/                      # Application Entry Points
    ├── Main.java                 # Desktop bootstrap (Splash -> Login -> MainFrame)
    └── SystemVerification.java   # Comprehensive 15-point automated verification suite
```

---

## 🗄️ Database Architecture

The schema supports both **Supabase PostgreSQL** and **SQLite** (local resilient fallback). All tables enforce foreign key integrity with `ON DELETE CASCADE` on `user_id`.

### Core Tables:
1. `users`: Credentials, bio targets, height, weight, activity level.
2. `workouts`: Recorded sessions with duration, calories, and notes.
3. `workout_exercises`: Individual sets, reps, weight (kg), and volume.
4. `workout_plans`: Weekly Monday–Sunday planner supporting `plan_mode` (`CUT` or `BULK`), `rest_seconds`, `cardio`, and `is_completed`.
5. `nutrition_plans`: Weekly planned meal items supporting `plan_mode` (`CUT` or `BULK`), calories, protein, carbs, fat, and timing.
6. `food_items`: Nutrition reference database.
7. `meals`: Actual logged food consumption records.
8. `body_measurements`: Historical anthropometric circumferences and BMI.
9. `water_logs`: Hydration records.
10. `sleep_logs`: Sleep duration and subjective quality scores.
11. `fitness_goals`: Milestone targets and progress states.
12. `reminders`: Alert schedules.
13. `achievements` & `user_achievements`: Gamified badge catalog and earned milestones.

---

## 🛠️ How to Run & Build

### Prerequisites
- **JDK 17 LTS, 21 LTS, or 26** installed.
- **Maven 3.8+** (or use the included `.tools/maven/bin/mvn.cmd`).

### 1. Build with Maven
Open PowerShell in the project directory:
```powershell
cmd.exe /c ".tools\maven\bin\mvn.cmd clean compile"
```

### 2. Run Automated System Verification (15 / 15 Tests)
```powershell
cmd.exe /c ".tools\maven\bin\mvn.cmd test-compile"
java -cp "target\test-classes;target\classes;lib\*" com.pgtr.main.SystemVerification
```
*Executes all 15 test suites verifying user creation, authentication, Cut/Bulk workout routines, Cut/Bulk nutrition engines, log-as-consumed flows, body measurements, streak math, achievements, CSV reports, and logo asset resolution.*

### 3. Launch the Application
```powershell
java -cp "target\classes;lib\*" com.pgtr.main.Main
```

### 4. Package into Executable JAR
```powershell
cmd.exe /c ".tools\maven\bin\mvn.cmd package -DskipTests=true"
java -jar target\personal-gain-tracker-1.0.0-jar-with-dependencies.jar
```

---

## ☁️ Supabase Cloud Configuration (Optional)

The application runs instantly out of the box using the embedded SQLite database. To connect your cloud Supabase database:
1. Execute `sql/supabase_schema.sql` and `sql/seed_data.sql` in your Supabase SQL Editor.
2. Configure credentials in any of these three ways:
   - **In-App**: Click **Settings** in the sidebar, input JDBC URL and password, click **Connect & Test Supabase**.
   - **Environment Variables**:
     ```bash
     PGTR_DB_URL=jdbc:postgresql://db.YOUR_PROJECT_REF.supabase.co:5432/postgres?sslmode=require
     PGTR_DB_USER=postgres
     PGTR_DB_PASSWORD=YOUR_PASSWORD
     ```
   - **`db.properties`**: Create `db.properties` in project root with your credentials.

---

## 👨‍💻 B.Tech Viva & Demonstration Guide

1. **Splash Screen & Branding**: Observe the animated startup screen with the official Vyntrix orbital emblem and dark cyber aesthetic.
2. **Login Page**: Note the clean layout, centered logo, "Welcome to Vyntrix" heading, and test the **Show/Hide password toggle**. Click **Quick Demo Account** for instant one-click login.
3. **Gym Workouts — Cut & Bulk**:
   - Toggle between **Cut Mode** and **Bulk Mode** using the top selection cards.
   - Inspect Monday through Sunday: notice Cut mode has higher rep counts, shorter rest times (60s), and cardio; Bulk mode has compound sets and longer rest intervals (120s–150s).
   - Click **+ Add / Edit Day** to customize an exercise or change rest time.
   - Check the **Completed** box on Monday's workout: notice it automatically prompts to log into actual workout history and updates the workout streak.
   - Test the live stopwatch and rest countdown timers.
4. **Nutrition — Cut & Bulk**:
   - Toggle between **Cut Nutrition** (calorie deficit) and **Bulk Nutrition** (calorie surplus).
   - Review meals across Breakfast, Mid-Morning, Lunch, Evening Snack, Dinner, and Post-Workout.
   - Click **"Log as Consumed Today ✓"** to demonstrate that planned meals become actual food logs, updating the daily dashboard calories and protein progress bars.
   - Open **Personalize Targets ⚙️**: enter age, weight, and activity level to demonstrate automatic BMR and TDEE calculation with the medical disclaimer.
5. **Dashboard & Health Metrics**:
   - View live stats: weight vs target, calories consumed vs burned, water and sleep indicators.
   - Log an anthropometric measurement in Health Metrics and review the Date-to-Date Comparison delta tool.
6. **Goals, Badges & Reports**:
   - View dynamic badges unlocked in the Trophy Case (*First Step*, *Macro Master*).
   - Export CSV reports and view JFreeChart progress trends.

---
*Vyntrix — Developed for B.Tech Computer Science and Engineering OOP Course Project.*

