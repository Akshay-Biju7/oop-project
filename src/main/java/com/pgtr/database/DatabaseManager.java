package com.pgtr.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Manages database connectivity via JDBC.
 * Primary target: Supabase Cloud PostgreSQL.
 * Fallback: Local SQLite database (pgtr_local.db) for offline demonstration and resilience.
 *
 * Security Notice:
 * In desktop client architectures, database credentials should ideally be injected via secure
 * environment variables (PGTR_DB_URL, PGTR_DB_USER, PGTR_DB_PASSWORD) or restricted PostgreSQL
 * user accounts with Row Level Security (RLS). Never expose Supabase service-role keys to client apps.
 */
public class DatabaseManager {

    private static DatabaseManager instance;

    private String dbType = "POSTGRESQL"; // "POSTGRESQL" or "SQLITE"
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private boolean connectedToSupabase = false;
    private String statusMessage = "Initializing...";

    private static final String CONFIG_FILE = "db.properties";

    private DatabaseManager() {
        loadConfiguration();
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Loads database credentials from Environment Variables first, then from optional db.properties.
     */
    private void loadConfiguration() {
        // 1. Check environment variables
        String envUrl = System.getenv("PGTR_DB_URL");
        String envUser = System.getenv("PGTR_DB_USER");
        String envPass = System.getenv("PGTR_DB_PASSWORD");

        // 2. Check local properties file
        Properties props = new Properties();
        File propFile = new File(CONFIG_FILE);
        if (propFile.exists()) {
            try (InputStream in = new FileInputStream(propFile)) {
                props.load(in);
            } catch (Exception ignored) {}
        }

        this.dbUrl = envUrl != null && !envUrl.trim().isEmpty() ? envUrl.trim() : props.getProperty("db.url");
        this.dbUser = envUser != null && !envUser.trim().isEmpty() ? envUser.trim() : props.getProperty("db.user");
        this.dbPassword = envPass != null ? envPass : props.getProperty("db.password");
    }

    /**
     * Attempts PostgreSQL connection first; if unavailable, falls back to SQLite.
     */
    public synchronized void initializeDatabase() {
        // Attempt PostgreSQL if URL is provided
        if (dbUrl != null && dbUrl.startsWith("jdbc:postgresql")) {
            try {
                Class.forName("org.postgresql.Driver");
                try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                    this.connectedToSupabase = true;
                    this.dbType = "POSTGRESQL";
                    this.statusMessage = "Connected to Supabase PostgreSQL (Cloud)";
                    System.out.println("[DatabaseManager] " + statusMessage);
                    initPostgresSchema(conn);
                    return;
                }
            } catch (Exception e) {
                System.err.println("[DatabaseManager] Supabase connection failed: " + e.getMessage());
                this.statusMessage = "Supabase unreachable. Falling back to local storage.";
            }
        }

        // Fallback to SQLite
        try {
            Class.forName("org.sqlite.JDBC");
            this.dbType = "SQLITE";
            this.dbUrl = "jdbc:sqlite:pgtr_local.db";
            this.dbUser = null;
            this.dbPassword = null;
            this.connectedToSupabase = false;
            this.statusMessage = "Connected: Local Storage (Offline Mode)";
            System.out.println("[DatabaseManager] " + statusMessage);

            try (Connection conn = DriverManager.getConnection(dbUrl)) {
                initSqliteSchema(conn);
            }
        } catch (Exception e) {
            System.err.println("[DatabaseManager] Fatal error initializing SQLite: " + e.getMessage());
            this.statusMessage = "Database Error: " + e.getMessage();
        }
    }

    /**
     * Gets a new JDBC Connection. Callers must close it via try-with-resources.
     */
    public Connection getConnection() throws SQLException {
        if ("POSTGRESQL".equalsIgnoreCase(dbType) && connectedToSupabase) {
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } else {
            return DriverManager.getConnection("jdbc:sqlite:pgtr_local.db");
        }
    }

    /**
     * Updates database configuration dynamically (e.g. from Settings Dialog).
     */
    public boolean updateCredentials(String url, String user, String password) {
        this.dbUrl = url != null ? url.trim() : "";
        this.dbUser = user != null ? user.trim() : "";
        this.dbPassword = password != null ? password : "";

        // Save to db.properties
        try {
            Properties props = new Properties();
            props.setProperty("db.url", this.dbUrl);
            props.setProperty("db.user", this.dbUser);
            props.setProperty("db.password", this.dbPassword != null ? this.dbPassword : "");
            try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
                props.store(out, "Personal Gain Tracking System - Database Config");
            }
        } catch (Exception ignored) {}

        initializeDatabase();
        return connectedToSupabase;
    }

    private void initPostgresSchema(Connection conn) {
        try (Statement st = conn.createStatement()) {
            // Check if schema file exists and execute or create basic tables
            File schemaFile = new File("sql/supabase_schema.sql");
            if (schemaFile.exists()) {
                String sql = Files.readString(Paths.get(schemaFile.getPath()));
                for (String query : sql.split(";")) {
                    String trimmed = query.trim();
                    if (!trimmed.isEmpty()) {
                        try {
                            st.execute(trimmed);
                        } catch (SQLException ignored) {}
                    }
                }
            }
            seedInitialData(conn);
        } catch (Exception e) {
            System.err.println("[DatabaseManager] Postgres schema init warning: " + e.getMessage());
        }
    }

    private void initSqliteSchema(Connection conn) {
        String[] sqliteDDL = {
            "CREATE TABLE IF NOT EXISTS users (" +
                "id TEXT PRIMARY KEY, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password_hash TEXT NOT NULL, " +
                "full_name TEXT NOT NULL, " +
                "age INTEGER, " +
                "height_cm REAL, " +
                "current_weight_kg REAL, " +
                "target_weight_kg REAL, " +
                "fitness_goal TEXT DEFAULT 'MUSCLE_GAIN', " +
                "activity_level TEXT DEFAULT 'MODERATELY_ACTIVE', " +
                "daily_calorie_target INTEGER DEFAULT 2400, " +
                "daily_protein_target INTEGER DEFAULT 150, " +
                "daily_water_target INTEGER DEFAULT 3000, " +
                "daily_sleep_target REAL DEFAULT 8.0, " +
                "created_at TEXT, " +
                "updated_at TEXT)",

            "CREATE TABLE IF NOT EXISTS workouts (" +
                "id TEXT PRIMARY KEY, " +
                "user_id TEXT NOT NULL, " +
                "workout_date TEXT NOT NULL, " +
                "workout_name TEXT NOT NULL, " +
                "muscle_group TEXT NOT NULL, " +
                "duration_minutes INTEGER DEFAULT 45, " +
                "calories_burned INTEGER DEFAULT 0, " +
                "notes TEXT, " +
                "created_at TEXT, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)",

            "CREATE TABLE IF NOT EXISTS workout_exercises (" +
                "id TEXT PRIMARY KEY, " +
                "workout_id TEXT NOT NULL, " +
                "user_id TEXT NOT NULL, " +
                "exercise_name TEXT NOT NULL, " +
                "exercise_type TEXT DEFAULT 'Strength', " +
                "sets INTEGER DEFAULT 1, " +
                "reps INTEGER DEFAULT 1, " +
                "weight_kg REAL DEFAULT 0.0, " +
                "volume_kg REAL DEFAULT 0.0, " +
                "notes TEXT, " +
                "FOREIGN KEY (workout_id) REFERENCES workouts(id) ON DELETE CASCADE)",

            "CREATE TABLE IF NOT EXISTS food_items (" +
                "id TEXT PRIMARY KEY, " +
                "food_name TEXT NOT NULL, " +
                "category TEXT DEFAULT 'General', " +
                "serving_unit TEXT DEFAULT '100g', " +
                "calories REAL DEFAULT 0, " +
                "protein_g REAL DEFAULT 0, " +
                "carbs_g REAL DEFAULT 0, " +
                "fat_g REAL DEFAULT 0, " +
                "is_custom INTEGER DEFAULT 0, " +
                "user_id TEXT)",

            "CREATE TABLE IF NOT EXISTS meals (" +
                "id TEXT PRIMARY KEY, " +
                "user_id TEXT NOT NULL, " +
                "meal_date TEXT NOT NULL, " +
                "meal_time TEXT, " +
                "meal_type TEXT NOT NULL, " +
                "food_name TEXT NOT NULL, " +
                "serving_quantity REAL DEFAULT 1.0, " +
                "calories REAL DEFAULT 0, " +
                "protein_g REAL DEFAULT 0, " +
                "carbs_g REAL DEFAULT 0, " +
                "fat_g REAL DEFAULT 0, " +
                "notes TEXT, " +
                "created_at TEXT, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)",

            "CREATE TABLE IF NOT EXISTS body_measurements (" +
                "id TEXT PRIMARY KEY, " +
                "user_id TEXT NOT NULL, " +
                "log_date TEXT NOT NULL, " +
                "weight_kg REAL NOT NULL, " +
                "height_cm REAL NOT NULL, " +
                "bmi REAL NOT NULL, " +
                "waist_cm REAL DEFAULT 0, " +
                "chest_cm REAL DEFAULT 0, " +
                "arms_cm REAL DEFAULT 0, " +
                "thighs_cm REAL DEFAULT 0, " +
                "hips_cm REAL DEFAULT 0, " +
                "neck_cm REAL DEFAULT 0, " +
                "notes TEXT, " +
                "created_at TEXT, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)",

            "CREATE TABLE IF NOT EXISTS water_logs (" +
                "id TEXT PRIMARY KEY, " +
                "user_id TEXT NOT NULL, " +
                "log_date TEXT NOT NULL, " +
                "amount_ml INTEGER NOT NULL, " +
                "logged_at TEXT, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)",

            "CREATE TABLE IF NOT EXISTS sleep_logs (" +
                "id TEXT PRIMARY KEY, " +
                "user_id TEXT NOT NULL, " +
                "sleep_date TEXT NOT NULL, " +
                "sleep_time TEXT, " +
                "wake_time TEXT, " +
                "duration_hours REAL NOT NULL, " +
                "sleep_quality TEXT DEFAULT 'Good', " +
                "notes TEXT, " +
                "created_at TEXT, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)",

            "CREATE TABLE IF NOT EXISTS fitness_goals (" +
                "id TEXT PRIMARY KEY, " +
                "user_id TEXT NOT NULL, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "category TEXT NOT NULL, " +
                "starting_value REAL NOT NULL, " +
                "target_value REAL NOT NULL, " +
                "current_value REAL NOT NULL, " +
                "unit TEXT NOT NULL, " +
                "start_date TEXT NOT NULL, " +
                "deadline TEXT NOT NULL, " +
                "status TEXT DEFAULT 'IN_PROGRESS', " +
                "created_at TEXT, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)",

            "CREATE TABLE IF NOT EXISTS workout_plans (" +
                "id TEXT PRIMARY KEY, " +
                "user_id TEXT NOT NULL, " +
                "day_of_week TEXT NOT NULL, " +
                "workout_name TEXT NOT NULL, " +
                "muscle_group TEXT NOT NULL, " +
                "planned_exercises TEXT, " +
                "scheduled_time TEXT, " +
                "notes TEXT, " +
                "is_active INTEGER DEFAULT 1, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)",

            "CREATE TABLE IF NOT EXISTS reminders (" +
                "id TEXT PRIMARY KEY, " +
                "user_id TEXT NOT NULL, " +
                "reminder_type TEXT NOT NULL, " +
                "title TEXT NOT NULL, " +
                "message TEXT, " +
                "reminder_time TEXT NOT NULL, " +
                "is_recurring INTEGER DEFAULT 1, " +
                "is_dismissed INTEGER DEFAULT 0, " +
                "created_at TEXT, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)",

            "CREATE TABLE IF NOT EXISTS achievements (" +
                "code TEXT PRIMARY KEY, " +
                "title TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "icon_name TEXT NOT NULL, " +
                "badge_tier TEXT DEFAULT 'BRONZE')",

            "CREATE TABLE IF NOT EXISTS user_achievements (" +
                "id TEXT PRIMARY KEY, " +
                "user_id TEXT NOT NULL, " +
                "achievement_code TEXT NOT NULL, " +
                "earned_at TEXT, " +
                "UNIQUE(user_id, achievement_code), " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (achievement_code) REFERENCES achievements(code) ON DELETE CASCADE)"
        };

        try (Statement st = conn.createStatement()) {
            for (String sql : sqliteDDL) {
                st.execute(sql);
            }
            seedInitialData(conn);
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] SQLite schema error: " + e.getMessage());
        }
    }

    private void seedInitialData(Connection conn) {
        try (Statement st = conn.createStatement()) {
            // Check if achievements already exist
            boolean hasAchievements = false;
            try (var rs = st.executeQuery("SELECT COUNT(*) FROM achievements")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    hasAchievements = true;
                }
            } catch (Exception ignored) {}

            if (!hasAchievements) {
                String[] achievementsSeed = {
                    "INSERT INTO achievements (code, title, description, icon_name, badge_tier) VALUES ('FIRST_WORKOUT', 'First Step', 'Recorded your very first gym workout!', 'dumbbell', 'BRONZE')",
                    "INSERT INTO achievements (code, title, description, icon_name, badge_tier) VALUES ('STREAK_7_DAYS', 'Iron Consistency', 'Maintained a 7-day workout streak!', 'flame', 'SILVER')",
                    "INSERT INTO achievements (code, title, description, icon_name, badge_tier) VALUES ('WORKOUTS_10', 'Gym Regular', 'Completed 10 total recorded workouts!', 'medal', 'SILVER')",
                    "INSERT INTO achievements (code, title, description, icon_name, badge_tier) VALUES ('WORKOUTS_50', 'Beast Mode', 'Crushed 50 gym workouts like a champion!', 'trophy', 'GOLD')",
                    "INSERT INTO achievements (code, title, description, icon_name, badge_tier) VALUES ('PROTEIN_TARGET', 'Macro Master', 'Hit or exceeded your daily protein goal!', 'egg', 'BRONZE')",
                    "INSERT INTO achievements (code, title, description, icon_name, badge_tier) VALUES ('NEW_PR', 'PR Smasher', 'Achieved a new personal record in weight lifted!', 'award', 'GOLD')",
                    "INSERT INTO achievements (code, title, description, icon_name, badge_tier) VALUES ('GOAL_COMPLETED', 'Goal Crusher', 'Successfully completed a personal fitness goal!', 'star', 'GOLD')",
                    "INSERT INTO achievements (code, title, description, icon_name, badge_tier) VALUES ('HYDRATION_HERO', 'Hydration Hero', 'Met your daily water intake target!', 'droplet', 'BRONZE')",
                    "INSERT INTO achievements (code, title, description, icon_name, badge_tier) VALUES ('SLEEP_CHAMPION', 'Sleep Champion', 'Logged 7+ hours of quality sleep for recovery!', 'moon', 'BRONZE')"
                };
                for (String sql : achievementsSeed) {
                    try { st.execute(sql); } catch (Exception ignored) {}
                }
            }

            // Check food library
            boolean hasFood = false;
            try (var rs = st.executeQuery("SELECT COUNT(*) FROM food_items")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    hasFood = true;
                }
            } catch (Exception ignored) {}

            if (!hasFood) {
                String[] foods = {
                    "INSERT INTO food_items (id, food_name, category, serving_unit, calories, protein_g, carbs_g, fat_g, is_custom, user_id) VALUES ('f_egg', 'Whole Egg (Boiled)', 'Proteins', '1 egg (50g)', 78.0, 6.3, 0.6, 5.3, 0, NULL)",
                    "INSERT INTO food_items (id, food_name, category, serving_unit, calories, protein_g, carbs_g, fat_g, is_custom, user_id) VALUES ('f_chicken', 'Chicken Breast (Cooked)', 'Proteins', '100g', 165.0, 31.0, 0.0, 3.6, 0, NULL)",
                    "INSERT INTO food_items (id, food_name, category, serving_unit, calories, protein_g, carbs_g, fat_g, is_custom, user_id) VALUES ('f_paneer', 'Paneer (Cottage Cheese)', 'Dairy & Protein', '100g', 265.0, 18.3, 1.2, 20.8, 0, NULL)",
                    "INSERT INTO food_items (id, food_name, category, serving_unit, calories, protein_g, carbs_g, fat_g, is_custom, user_id) VALUES ('f_whey', 'Whey Protein Powder', 'Supplements', '1 scoop (30g)', 120.0, 24.0, 2.0, 1.5, 0, NULL)",
                    "INSERT INTO food_items (id, food_name, category, serving_unit, calories, protein_g, carbs_g, fat_g, is_custom, user_id) VALUES ('f_rice', 'White Basmati Rice', 'Carbohydrates', '100g cooked', 130.0, 2.7, 28.2, 0.3, 0, NULL)",
                    "INSERT INTO food_items (id, food_name, category, serving_unit, calories, protein_g, carbs_g, fat_g, is_custom, user_id) VALUES ('f_oats', 'Rolled Oats', 'Carbohydrates', '100g', 389.0, 16.9, 66.3, 6.9, 0, NULL)",
                    "INSERT INTO food_items (id, food_name, category, serving_unit, calories, protein_g, carbs_g, fat_g, is_custom, user_id) VALUES ('f_roti', 'Wheat Roti / Chapati', 'Carbohydrates', '1 medium (40g)', 104.0, 3.1, 22.0, 0.5, 0, NULL)",
                    "INSERT INTO food_items (id, food_name, category, serving_unit, calories, protein_g, carbs_g, fat_g, is_custom, user_id) VALUES ('f_banana', 'Banana', 'Fruits', '1 medium (118g)', 105.0, 1.3, 27.0, 0.3, 0, NULL)",
                    "INSERT INTO food_items (id, food_name, category, serving_unit, calories, protein_g, carbs_g, fat_g, is_custom, user_id) VALUES ('f_peanut_butter', 'Peanut Butter', 'Healthy Fats', '2 tbsp (32g)', 188.0, 8.0, 6.0, 16.0, 0, NULL)",
                    "INSERT INTO food_items (id, food_name, category, serving_unit, calories, protein_g, carbs_g, fat_g, is_custom, user_id) VALUES ('f_milk', 'Whole Milk', 'Dairy & Protein', '1 cup (240ml)', 149.0, 7.7, 11.7, 7.9, 0, NULL)",
                    "INSERT INTO food_items (id, food_name, category, serving_unit, calories, protein_g, carbs_g, fat_g, is_custom, user_id) VALUES ('f_dal', 'Moong Dal (Cooked)', 'Plant Protein', '1 bowl (150g)', 147.0, 9.8, 24.0, 0.8, 0, NULL)",
                    "INSERT INTO food_items (id, food_name, category, serving_unit, calories, protein_g, carbs_g, fat_g, is_custom, user_id) VALUES ('f_salmon', 'Baked Salmon Fillet', 'Fish & Seafood', '100g', 208.0, 20.4, 0.0, 13.4, 0, NULL)"
                };
                for (String sql : foods) {
                    try { st.execute(sql); } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            System.err.println("[DatabaseManager] Seeding error: " + e.getMessage());
        }
    }

    // --- Getters & Status ---

    public boolean isConnectedToSupabase() {
        return connectedToSupabase;
    }

    public String getDbType() {
        return dbType;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
}
