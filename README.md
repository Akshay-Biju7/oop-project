# Personal Gain Tracking System (PGTR)
### B.Tech Computer Science & Engineering — Object-Oriented Programming (OOP) Desktop Application

![Java Version](https://img.shields.io/badge/Java-17%20LTS%20%7C%2021%20LTS-00E676?style=for-the-badge&logo=openjdk)
![Swing](https://img.shields.io/badge/GUI-Java%20Swing%20%2B%20FlatLaf-18181B?style=for-the-badge)
![Database](https://img.shields.io/badge/Database-Supabase%20PostgreSQL-3ECF8E?style=for-the-badge&logo=supabase)
![Build](https://img.shields.io/badge/Build-Maven%203.9-C71A36?style=for-the-badge&logo=apachemaven)
![IDE](https://img.shields.io/badge/IDE-Eclipse%20Compatible-2C2255?style=for-the-badge&logo=eclipseide)

---

## 📌 Project Overview

**Personal Gain Tracking System (PGTR)** is a complete, production-grade desktop fitness and personal development application designed for individual gym athletes, lifters, and health-conscious individuals. 

Built entirely in **Java Swing** with modern dark charcoal and electric lime UI aesthetics, **Core Java**, **JDBC**, and **Supabase PostgreSQL**, the system centralizes workout progressive overload tracking, nutrition macronutrients, anthropometric body measurements, hydration, sleep recovery, fitness milestones, and weekly routines in one desktop interface.

---

## 🚀 Key Modules & Features

### 1. 🏋️ Gym Workout Management
- **Workout Session Logger**: Record workout date, workout name, muscle group, duration, and calories burned.
- **Exercise Set & Rep Builder**: Add individual exercises with sets, repetitions, and weight lifted in kg.
- **Progressive Overload Calculation**: Automatic training volume calculation (`Sets × Reps × Weight in kg`).
- **Personal Record (PR) Tracking**: Auto-detects when an athlete lifts a new maximum weight for any exercise and awards the *PR Smasher* badge.
- **Integrated Timers**: Live session stopwatch (Start, Pause, Resume, Reset) plus between-set countdown rest timer (30s, 60s, 90s, 120s with audio/visual notification).
- **Search & Filter**: Search workout history by name/notes and filter by target muscle group (Chest, Back, Legs, Shoulders, Arms, Core, Cardio).

### 2. 🥗 Nutrition & Calorie Tracking
- **Meal Categorization**: Log meals across Breakfast, Lunch, Dinner, and Snacks.
- **Searchable Food Database**: Pre-populated with staple foods (Chicken Breast, Whole Eggs, Oats, White Rice, Whey Protein, Paneer, Salmon, Sweet Potato, Bananas, etc.).
- **Manual & Custom Foods**: Enter custom food products with estimated calories, protein, carbs, and fat per serving.
- **Daily Target Progress Bars**: Live progress tracking against user-defined daily calorie and protein goals.
- **Macronutrient Split Chart**: Interactive **JFreeChart** pie chart showing calorie percentage from Protein, Carbohydrates, and Healthy Fats.

### 3. 📏 Health & Body Measurements
- **Anthropometric Logging**: Track body weight, height, waist, chest, arms/biceps, thighs, hips, and neck.
- **Body Mass Index (BMI) Screening**: Calculated as $\text{Weight (kg)} / (\text{Height (m)})^2$ with World Health Organization classifications.
- **Clinical Notice**: Clearly labeled as a screening metric that does not differentiate muscle hypertrophy from adipose tissue.
- **Date-to-Date Recomposition Comparison Tool**: Select any two dates (e.g. Baseline vs Current) to view exact differences (+/- cm and kg) with color-coded badges.

### 4. 💧 Water Intake & Sleep Recovery
- **Hydration Tracker**: Quick-add buttons (+250ml glass, +500ml shaker, +750ml bottle, custom ml), daily target progress bar, and timestamped water logs.
- **Sleep & Recovery**: Bedtime, wake time, total sleep duration calculation, perceived sleep quality (Excellent, Good, Fair, Poor), and 7-day average recovery duration.

### 5. 🎯 Goal Setting & Gamified Achievements
- **Milestone Goals**: Create target goals (e.g. "Bench Press 100 kg", "Target Weight 75 kg") with start date, deadline, starting value, target value, and current value.
- **Progress Percentage**: Bidirectional percentage completion math for both increasing goals (muscle/strength) and decreasing goals (weight loss).
- **Trophy Case & Badges**:
  - 🏋️ **First Step**: Logged first gym workout.
  - 🔥 **Iron Consistency**: Maintained a 7-day workout streak.
  - 🥉 **Gym Regular**: Completed 10 recorded workouts.
  - 👑 **Beast Mode**: Crushed 50 gym workouts.
  - 🥩 **Macro Master**: Hit daily protein intake target.
  - ⚡ **PR Smasher**: Achieved a new personal weight record.
  - 🎯 **Goal Crusher**: Successfully completed a personal fitness goal.
  - 💧 **Hydration Hero**: Reached daily water intake target.
  - 🌙 **Sleep Champion**: Logged 7+ hours of quality sleep.

### 6. 📅 Weekly Workout Planner & Reminders
- **Monday – Sunday Routine**: Set scheduled workout splits, target muscle groups, planned exercises, and scheduled times.
- **In-App Reminder System**: Active alerts bell with badge count, popup reminder notifications, and reminder management.

### 7. 📈 Progress Analysis & Reports
- **Weekly Workout Frequency Bar Chart**: Real-time JFreeChart distribution of workouts across days of the week.
- **Calorie Intake Trend Chart**: Longitudinal 7-day, 30-day, or 90-day intake trend line.
- **Weight Progression Chart**: Historical body weight curve plotted against user's target weight threshold.
- **Training Volume Trend**: Progression of total lifted tonnage over consecutive workout sessions.
- **CSV Data Export**: Export workouts and nutrition logs into standard CSV files.
- **Printable Progress Report**: Formatted summary printable via native Java Print dialog.

---

## 🏛️ Object-Oriented Architecture (OOP)

The application strictly adheres to clean OOP principles and modern software design patterns:

```
src/main/java/com/pgtr/
├── model/                # Encapsulated entities, inheritance base classes, enums
│   ├── IdentifiableEntity.java   # Abstract base class (Abstraction & Inheritance)
│   ├── User.java                 # Profile model + BMR/TDEE/BMI scientific calculators
│   ├── Workout.java              # Gym session entity + volume aggregation methods
│   ├── Exercise.java             # Individual exercise + volume = sets * reps * kg
│   ├── Meal.java                 # Logged meal entry
│   ├── FoodItem.java             # Nutrition database item
│   ├── BodyMeasurement.java      # Circumferences + BMI calculations
│   ├── WaterLog.java             # Water intake entry
│   ├── SleepLog.java             # Sleep duration and quality
│   ├── FitnessGoal.java          # Milestone goals + progress percentage
│   ├── WorkoutPlan.java          # Weekly planner split
│   ├── Reminder.java             # Notification entity
│   ├── Achievement.java          # Badge catalog definition
│   └── UserAchievement.java      # Earned user badge relationship
│
├── dao/                  # Data Access Object Interfaces (Polymorphism & Contracts)
│   ├── UserDAO.java
│   ├── WorkoutDAO.java
│   ├── MealDAO.java
│   ├── HealthDAO.java
│   ├── WaterDAO.java
│   ├── SleepDAO.java
│   ├── GoalDAO.java
│   ├── PlannerDAO.java
│   ├── ReminderDAO.java
│   └── AchievementDAO.java
│
├── dao/impl/             # JDBC Implementation Layer with Prepared Statements
│   ├── UserDAOImpl.java
│   ├── WorkoutDAOImpl.java       # Atomic transactions (conn.commit / rollback)
│   ├── MealDAOImpl.java
│   ├── HealthDAOImpl.java
│   ├── WaterDAOImpl.java
│   ├── SleepDAOImpl.java
│   ├── GoalDAOImpl.java
│   ├── PlannerDAOImpl.java
│   ├── ReminderDAOImpl.java
│   └── AchievementDAOImpl.java
│
├── service/              # Business Logic, Calculations & Milestone Triggers
│   ├── UserSession.java          # Thread-safe Singleton storing logged-in user
│   ├── AuthService.java          # Authentication, BCrypt hash verification
│   ├── WorkoutService.java       # Streaks, volume, and PR checks
│   ├── NutritionService.java     # Macro aggregation & protein target triggers
│   ├── HealthService.java        # Anthropometric diff calculations & profile sync
│   ├── HabitService.java         # Water & sleep tracking triggers
│   ├── GoalService.java          # Goal completion lifecycle
│   ├── AchievementService.java   # Dynamic badge unlock evaluation
│   ├── PlannerService.java       # Routine scheduling
│   ├── ReminderService.java      # Notification queue
│   └── ReportService.java        # JFreeChart generators & text report builder
│
├── database/             # JDBC Database Connection Manager
│   └── DatabaseManager.java      # Supabase PostgreSQL + Local SQLite resilient fallback
│
├── util/                 # Security, Date & Math Utilities
│   ├── PasswordHasher.java       # BCrypt password hashing with salt (log rounds = 12)
│   ├── ValidationUtil.java       # Input bounds & regex validation
│   ├── DateUtil.java             # Formatting & temporal adjusters
│   ├── CsvExporter.java          # RFC 4180 compliant CSV generator
│   └── Theme.java                # Dark charcoal & neon lime design tokens
│
├── view/                 # Java Swing User Interface (MVC Presentation)
│   ├── LoginFrame.java           # Authentication screen (Sign In & Registration)
│   ├── MainFrame.java            # Root container (Sidebar, TopBar, CardLayout)
│   ├── DashboardPanel.java       # Personalized command center
│   ├── WorkoutPanel.java         # Workout logger, exercise builder, timers
│   ├── NutritionPanel.java       # Food database, meal logger, macro charts
│   ├── HealthPanel.java          # Measurements, BMI disclaimer, comparison tool
│   ├── HabitsPanel.java          # Water rings & sleep logs
│   ├── GoalsPanel.java           # Active goals & badges showcase
│   ├── PlannerPanel.java         # Weekly routine schedule
│   ├── ReportsPanel.java         # Longitudinal charts & exports
│   └── SettingsPanel.java        # Profile editor & live Supabase connection tester
│
├── view/components/      # Reusable Styled Modern UI Components
│   ├── ModernCard.java           # Rounded panel with charcoal background
│   ├── StatCard.java             # Metric card (icon, title, large value, diff)
│   ├── NeonButton.java           # Custom painted neon green & dark buttons
│   ├── ModernProgressBar.java    # Sleek rounded progress bar
│   ├── StopwatchTimerPanel.java  # Interactive workout & set rest timers
│   └── DarkTable.java            # Customized dark JTable with alternating rows
│
└── main/                 # Application Entry Points & Automated Verification
    ├── Main.java                 # Desktop app entry point
    └── SystemVerification.java   # Automated 12-step test suite
```

---

## 🗄️ Database Design (Supabase PostgreSQL)

The database schema is fully normalized and located in `sql/supabase_schema.sql` and `sql/seed_data.sql`.

### Relational Schema:
- **`users`**: User profiles, credentials hash, anthropometric baseline, daily targets.
- **`workouts`**: Workout sessions (`user_id` FK ON DELETE CASCADE).
- **`workout_exercises`**: Exercises per workout session (`workout_id` FK, `volume_kg`).
- **`food_items`**: Preset & custom food database with estimated macros.
- **`meals`**: Logged meal consumption records (`meal_type`, calories, macros).
- **`body_measurements`**: Historical measurements (weight, waist, chest, arms, thighs, BMI).
- **`water_logs`**: Timestamped water intake (ml).
- **`sleep_logs`**: Sleep/wake times, duration, quality score.
- **`fitness_goals`**: Personal milestone targets and completion status.
- **`workout_plans`**: Day-of-week routines (Monday to Sunday).
- **`reminders`**: In-app notifications with recurrence flags.
- **`achievements`**: Catalog of unlockable badges and tiers.
- **`user_achievements`**: Earned user badges with timestamp (`UNIQUE(user_id, achievement_code)`).

---

## 🛠️ Setup & Running Instructions

### Prerequisites:
1. **Java Development Kit (JDK)**: JDK 17 LTS or higher (JDK 21 or 26 recommended). Verify by typing:
   ```bash
   java -version
   javac -version
   ```
2. **Eclipse IDE**: Eclipse IDE for Java Developers (2022-09 or newer).

---

### Step 1: Open / Import into Eclipse

1. Open Eclipse and select your preferred workspace.
2. Click **File -> Import...**
3. Select **Maven -> Existing Maven Projects** and click **Next**.
4. Click **Browse...** and choose the `new pgtr` folder:
   `c:\Users\Akshay\Desktop\new pgtr`
5. Ensure `pom.xml` is checked, then click **Finish**.
6. Eclipse will automatically configure the project classpath, download dependencies, and set compiler compliance to Java 17.

*(Alternative: You can also import as **General -> Existing Projects into Workspace** since pre-configured `.project` and `.classpath` files are included).*

---

### Step 2: Supabase PostgreSQL Configuration

The application includes dual-mode database support:
- **Cloud Mode**: Connects directly to Supabase PostgreSQL via JDBC.
- **Local Fallback Mode**: If Supabase credentials are not provided or you are offline, the system automatically uses the embedded local database (`pgtr_local.db`) so the application **never crashes** and can always be demonstrated!

#### To configure your live Supabase project:
1. Log in to [Supabase](https://supabase.com) and create a new project.
2. In Supabase, go to the **SQL Editor** tab.
3. Open `sql/supabase_schema.sql` from this project, paste the content into Supabase SQL Editor, and click **Run**.
4. Open `sql/seed_data.sql`, paste the content into SQL Editor, and click **Run**.
5. Retrieve your project database connection details under **Project Settings -> Database**:
   - Host: `db.YOUR_PROJECT_REF.supabase.co`
   - Port: `5432`
   - Database: `postgres`
   - User: `postgres`
   - Password: `YOUR_DATABASE_PASSWORD`

6. Provide your credentials using **either** of the following methods:
   - **Method A (In-App Settings)**: Launch the app, click **Settings & DB** in the sidebar, enter your JDBC URL, username, and password, and click **Connect & Test Supabase**.
   - **Method B (Environment Variables)**: Set the following environment variables:
     ```bash
     PGTR_DB_URL=jdbc:postgresql://db.YOUR_PROJECT_REF.supabase.co:5432/postgres?sslmode=require
     PGTR_DB_USER=postgres
     PGTR_DB_PASSWORD=YOUR_DATABASE_PASSWORD
     ```
   - **Method C (`db.properties`)**: Copy `db.properties.example` to `db.properties` in the project root and fill in your values.

---

### Step 3: Run the Application

#### Option A: Run inside Eclipse
1. In Eclipse Package Explorer, navigate to:
   `src/main/java -> com.pgtr.main -> Main.java`
2. Right-click `Main.java` and select **Run As -> Java Application**.

#### Option B: Run from Command Line
```powershell
# Using Maven:
.\mvnw.cmd compile
java -cp "target\classes;lib\*" com.pgtr.main.Main

# Or run the generated fat JAR directly:
java -jar target\personal-gain-tracker-1.0.0-jar-with-dependencies.jar
```

---

### Step 4: Run Automated System Verification

An automated 12-point test suite is included to verify all database tables, CRUD operations, calculations, achievements, and report exports:

```powershell
java -cp "target\classes;lib\*" com.pgtr.main.SystemVerification
```

All 12 tests will execute and report a 100% pass status!

---

## 🔒 Security & Client Architecture Notice

- **Password Security**: Passwords are never stored in plaintext. Passwords are salted and hashed using **jBCrypt** with 12 log rounds.
- **Database Access Restrictions**: When connecting directly to Supabase via JDBC from a client application, use environment variables or local property files that are git-ignored. Never expose Supabase service-role keys in desktop application source code.
- **Offline Resilience**: Network failures or missing remote credentials trigger an automatic fallback to local storage without application disruption.

---

## 👨‍💻 Project Demonstration Walkthrough

When presenting this project for your course viva or evaluation:
1. **Welcome Screen**: Demonstrate **Sign In** and **Create Account** with field validation. Use the **Quick Demo Account** button for instant demonstration.
2. **Dashboard**: Highlight the dark charcoal & electric lime theme, real-time cards, daily calorie/protein progress bars, weekly workout activity chart, and streak counter.
3. **Workout Logger & Timer**: 
   - Start the workout stopwatch timer.
   - Start a 30s rest timer to show set intervals.
   - Click **+ Record Workout**, select exercises (Bench Press, Incline DB), enter sets/reps/weights, and demonstrate automatic training volume calculation (`sets × reps × kg`).
4. **Nutrition & Food Database**: Search for "Chicken Breast" or "Whole Egg", adjust serving quantity, log the meal, and show the updated Macronutrient Split Pie Chart.
5. **Body Recomposition**: Add a weight and measurement entry. Open the **Recomposition Comparison Tool** to show difference deltas between dates.
6. **Habits**: Click **+250ml** or **+500ml** to show the hydration progress bar fill in real time. Log sleep duration.
7. **Goals & Badges**: Open the **Trophy Case** to show dynamic badges unlocked based on actual database activity (e.g., *First Step*, *Sleep Champion*).
8. **Reports & Printing**: Filter by 30 days, view volume/weight trends, export workouts to CSV, and preview the formatted printable report.
9. **Settings**: Show user profile editing, scientific auto-recalculation of calories via Mifflin-St Jeor formula, and the live database connection indicator.

---
*Developed for B.Tech Computer Science and Engineering — OOP Course Project.*
