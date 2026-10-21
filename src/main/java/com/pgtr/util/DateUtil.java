package com.pgtr.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Common date & time manipulation and formatting helpers.
 */
public class DateUtil {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    public static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    public static String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DATE_FORMATTER);
    }

    public static String formatTime(LocalTime time) {
        if (time == null) return "N/A";
        return time.format(TIME_FORMATTER);
    }

    /**
     * Robustly parses various time strings (e.g. "08:00 AM", "01:30 PM", "1:30 pm", "13:30", "08:00")
     * into a LocalTime object, accurately preserving morning vs evening.
     */
    public static LocalTime parseTime(String text) {
        if (text == null || text.trim().isEmpty()) {
            return LocalTime.now();
        }
        String clean = text.trim();

        // 1. Try standard 12-hour AM/PM patterns
        String[] patterns12 = {"hh:mm a", "h:mm a", "hh:mma", "h:mma"};
        for (String pattern : patterns12) {
            try {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern, java.util.Locale.ENGLISH);
                return LocalTime.parse(clean.toUpperCase(), dtf);
            } catch (Exception ignored) {}
        }

        // 2. Try 24-hour patterns
        String[] patterns24 = {"HH:mm", "H:mm", "HH:mm:ss", "H:mm:ss"};
        for (String pattern : patterns24) {
            try {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern, java.util.Locale.ENGLISH);
                return LocalTime.parse(clean, dtf);
            } catch (Exception ignored) {}
        }

        // 3. Manual digit extraction fallback for non-standard inputs
        try {
            String upper = clean.toUpperCase();
            boolean isPm = upper.contains("PM");
            boolean isAm = upper.contains("AM");
            String digits = upper.replaceAll("[^0-9:]", "").trim();
            String[] parts = digits.split(":");
            if (parts.length >= 2) {
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                if (isPm && hour < 12) hour += 12;
                if (isAm && hour == 12) hour = 0;
                return LocalTime.of(Math.min(23, Math.max(0, hour)), Math.min(59, Math.max(0, minute)));
            }
        } catch (Exception ignored) {}

        return LocalTime.now();
    }

    public static LocalDate getStartOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static LocalDate getEndOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    public static List<LocalDate> getCurrentWeekDates() {
        LocalDate monday = getStartOfWeek(LocalDate.now());
        List<LocalDate> week = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            week.add(monday.plusDays(i));
        }
        return week;
    }
}
