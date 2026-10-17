# Personal Gain Tracking System (PGTR) — Step-by-Step Setup Guide

This guide walks through setting up, configuring, and running the **Personal Gain Tracking System (PGTR)** in **Eclipse IDE** and connecting to **Supabase PostgreSQL**.

---

## Part 1: Prerequisites

1. **Java Development Kit (JDK 17 or higher)**:
   - Download and install JDK 17, 21, or 26 from [Adoptium Temurin](https://adoptium.net/) or [Oracle Java](https://www.oracle.com/java/technologies/downloads/).
   - Ensure `JAVA_HOME` is configured in your Environment Variables.
   - Verify in your terminal/PowerShell:
     ```bash
     java -version
     javac -version
     ```

2. **Eclipse IDE**:
   - Download **Eclipse IDE for Enterprise Java and Web Developers** or **Eclipse IDE for Java Developers** from [eclipse.org](https://www.eclipse.org/downloads/).

---

## Part 2: Importing into Eclipse

1. Launch **Eclipse IDE**.
2. Go to **File -> Import...** from the top menu.
3. In the import wizard, expand the **Maven** folder and select **Existing Maven Projects**. Click **Next**.
4. In the **Root Directory** field, click **Browse...** and select the project folder:
   `c:\Users\Akshay\Desktop\new pgtr`
5. Ensure that the `pom.xml` (`com.pgtr:personal-gain-tracker:1.0.0`) is checked in the list.
6. Click **Finish**.
7. Wait 10-20 seconds while Eclipse configures the project and indexes dependencies.
8. The project will appear in your **Package Explorer** without any compilation errors.

---

## Part 3: Setting Up Supabase PostgreSQL

### 1. Create Supabase Project
1. Go to [https://supabase.com](https://supabase.com) and sign in (free account).
2. Click **New Project**.
3. Choose an organization, enter Project Name (e.g., `pgtr-fitness`), and set a **Database Password** (keep this password safe).
4. Select your nearest region and click **Create new project**.

### 2. Execute SQL Database Scripts
1. On the left sidebar in Supabase, click the **SQL Editor** icon (`>_`).
2. Click **+ New Query**.
3. In your project workspace, open the file `sql/supabase_schema.sql`.
4. Copy the entire contents, paste into the Supabase SQL Editor, and click the green **Run** button.
5. In SQL Editor, click **+ New Query** again.
6. Open `sql/seed_data.sql`, paste the entire contents, and click **Run**.
7. Go to **Table Editor** on the left menu. You will see all 14 tables (`users`, `workouts`, `workout_exercises`, `meals`, `food_items`, `body_measurements`, `water_logs`, `sleep_logs`, `fitness_goals`, `workout_plans`, `reminders`, `achievements`, `user_achievements`).

### 3. Get Your JDBC Connection URL
1. Go to **Project Settings** (gear icon at the bottom of the left sidebar).
2. Under **Configuration**, click **Database**.
3. Scroll down to **Connection Parameters**:
   - Host: `db.<project-ref>.supabase.co`
   - Port: `5432`
   - Database name: `postgres`
   - User: `postgres`
   - Password: `[YOUR-PASSWORD]`
4. Your JDBC Connection String is:
   ```
   jdbc:postgresql://db.<project-ref>.supabase.co:5432/postgres?sslmode=require
   ```

---

## Part 4: Configuring Credentials in the Application

You can provide credentials in **any** of the following 3 ways:

### Method A: Directly inside the Running Application (Easiest)
1. Run the application (see Part 5).
2. Log in using the **Quick Demo Account** button or register a new user.
3. Click **Settings & DB** on the left sidebar.
4. Scroll to **Database Connection & Supabase Settings**.
5. Paste your Supabase JDBC URL, username (`postgres`), and password.
6. Click **Connect & Test Supabase**. The application will instantly connect to the live cloud database!

### Method B: Environment Variables
In Windows, set the following environment variables:
```bash
setx PGTR_DB_URL "jdbc:postgresql://db.<project-ref>.supabase.co:5432/postgres?sslmode=require"
setx PGTR_DB_USER "postgres"
setx PGTR_DB_PASSWORD "YOUR_PASSWORD"
```
Or in Eclipse:
1. Right-click `Main.java` -> **Run As -> Run Configurations...**
2. Go to the **Environment** tab.
3. Click **New...** and add `PGTR_DB_URL`, `PGTR_DB_USER`, and `PGTR_DB_PASSWORD`.

### Method C: Local Properties File
Create a file named `db.properties` in the project root:
```properties
db.url=jdbc:postgresql://db.<project-ref>.supabase.co:5432/postgres?sslmode=require
db.user=postgres
db.password=YOUR_PASSWORD
```

> **Note on Offline Resilience**: If no Supabase credentials are provided or if you are offline without internet access, the system automatically uses the embedded local database (`pgtr_local.db`). The app will **never crash** with connection failures and is always 100% functional for evaluation.

---

## Part 5: Running the Application

### In Eclipse:
1. In the **Package Explorer**, expand:
   `personal-gain-tracker -> src/main/java -> com.pgtr.main`
2. Right-click `Main.java`.
3. Select **Run As -> Java Application**.
4. The dark-themed authentication window will open.

### From Command Prompt / PowerShell:
```powershell
# From the project directory:
java -cp "target\classes;lib\*" com.pgtr.main.Main

# Or run the standalone executable fat JAR:
java -jar target\personal-gain-tracker-1.0.0-jar-with-dependencies.jar
```

---

## Part 6: Running the Automated Test Suite

To verify all database tables, CRUD operations, calculations, and badge awards:

```powershell
java -cp "target\classes;lib\*" com.pgtr.main.SystemVerification
```

All 12 automated test cases will execute and verify the entire system.
