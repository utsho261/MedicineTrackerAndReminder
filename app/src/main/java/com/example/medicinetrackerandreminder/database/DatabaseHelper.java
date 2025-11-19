package com.example.medicinetrackerandreminder.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.medicinetrackerandreminder.Medicine;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "medicine_tracker.db";
    private static final int DATABASE_VERSION = 4;

    // ===== USERS TABLE =====
    private static final String TABLE_USERS = "users";
    private static final String USER_ID = "id";
    private static final String USER_FNAME = "firstName";
    private static final String USER_LNAME = "lastName";
    private static final String USER_DOB = "dob";
    private static final String USER_PHONE = "phone";
    private static final String USER_EMAIL = "email";
    private static final String USER_PASSWORD = "password";

    // ===== MEDICINES TABLE =====
    private static final String TABLE_MEDICINES = "medicines";
    private static final String MED_ID = "id";
    private static final String MED_NAME = "name";
    private static final String MED_TYPE = "type";
    private static final String MED_DOSAGE = "dosage";
    private static final String MED_START = "startDate";
    private static final String MED_END = "endDate";
    private static final String MED_STOCK = "currentStock";
    private static final String MED_LOWSTOCK = "lowStockAlert";
    private static final String MED_FREQUENCY = "frequency";
    private static final String MED_TIMES = "times";
    private static final String MED_COLOR = "color";
    private static final String MED_NOTES = "notes";

    // ===== MEDICINE STATUS TABLE =====
    private static final String TABLE_STATUS = "medicine_status";
    private static final String STATUS_ID = "id";
    private static final String STATUS_MED_NAME = "name";
    private static final String STATUS_DATE = "date";
    private static final String STATUS_TIME = "time";
    private static final String STATUS_RESULT = "result";

    // Used by calendar
    public String selectedDateGlobal = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS = "CREATE TABLE " + TABLE_USERS + " (" +
                USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USER_FNAME + " TEXT, " +
                USER_LNAME + " TEXT, " +
                USER_DOB + " TEXT, " +
                USER_PHONE + " TEXT, " +
                USER_EMAIL + " TEXT, " +
                USER_PASSWORD + " TEXT)";
        db.execSQL(CREATE_USERS);

        String CREATE_MEDICINES = "CREATE TABLE " + TABLE_MEDICINES + " (" +
                MED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MED_NAME + " TEXT, " +
                MED_TYPE + " TEXT, " +
                MED_DOSAGE + " TEXT, " +
                MED_START + " TEXT, " +
                MED_END + " TEXT, " +
                MED_STOCK + " INTEGER, " +
                MED_LOWSTOCK + " INTEGER, " +
                MED_FREQUENCY + " TEXT, " +
                MED_TIMES + " TEXT, " +
                MED_COLOR + " INTEGER, " +
                MED_NOTES + " TEXT)";
        db.execSQL(CREATE_MEDICINES);

        String CREATE_STATUS = "CREATE TABLE " + TABLE_STATUS + " (" +
                STATUS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                STATUS_MED_NAME + " TEXT, " +
                STATUS_DATE + " TEXT, " +
                STATUS_TIME + " TEXT, " +
                STATUS_RESULT + " TEXT)";
        db.execSQL(CREATE_STATUS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICINES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATUS);
        onCreate(db);
    }

    // ===== Utility =====
    public static String formatDate(String input) {
        try {
            SimpleDateFormat src = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat dest = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return dest.format(src.parse(input));
        } catch (Exception e) {
            return input;
        }
    }

    public String todayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    // ===== Single-user helpers =====
    public long getFirstUser() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM users ORDER BY id LIMIT 1", null);
        long id = -1;
        if (c.moveToFirst()) id = c.getLong(0);
        c.close();
        return id;
    }

    public long upsertSingleUser(String f, String l, String dob, String phone, String email) {
        long first = getFirstUser();
        if (first == -1) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(USER_FNAME, f);
            cv.put(USER_LNAME, l);
            cv.put(USER_DOB, dob);
            cv.put(USER_PHONE, phone);
            cv.put(USER_EMAIL, email);
            return db.insert(TABLE_USERS, null, cv);
        } else {
            boolean ok = updateSingleUser(f, l, dob, phone, email);
            return ok ? first : -1;
        }
    }

    public boolean updateSingleUser(String f, String l, String dob, String phone, String email) {
        long first = getFirstUser();
        if (first == -1) {
            return upsertSingleUser(f, l, dob, phone, email) != -1;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(USER_FNAME, f);
        cv.put(USER_LNAME, l);
        cv.put(USER_DOB, dob);
        cv.put(USER_PHONE, phone);
        cv.put(USER_EMAIL, email);
        int rows = db.update(TABLE_USERS, cv, "id=?", new String[]{String.valueOf(first)});
        return rows > 0;
    }

    public Cursor getSingleUserCursor() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT firstName, lastName, dob, phone, email FROM users ORDER BY id LIMIT 1", null);
    }

    // ===== Medicines and Status (unchanged) =====
    public boolean addMedicine(String name, String type, String dosage,
                               String start, String end, int stock, int lowStock,
                               String freq, String times, int color, String notes) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(MED_NAME, name);
        cv.put(MED_TYPE, type);
        cv.put(MED_DOSAGE, dosage);
        cv.put(MED_START, start);
        cv.put(MED_END, end);
        cv.put(MED_STOCK, stock);
        cv.put(MED_LOWSTOCK, lowStock);
        cv.put(MED_FREQUENCY, freq);
        cv.put(MED_TIMES, times);
        cv.put(MED_COLOR, color);
        cv.put(MED_NOTES, notes);
        long result = db.insert(TABLE_MEDICINES, null, cv);
        return result != -1;
    }

    public boolean medicineExists(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT 1 FROM " + TABLE_MEDICINES + " WHERE name=?",
                new String[]{name});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    public void markMedicineTaken(String name, String time, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(STATUS_MED_NAME, name);
        cv.put(STATUS_DATE, date);
        cv.put(STATUS_TIME, time);
        cv.put(STATUS_RESULT, "taken");
        db.insert(TABLE_STATUS, null, cv);

        decreaseMedicineStock(name);
    }

    public void markMedicineMissed(String name, String time, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(STATUS_MED_NAME, name);
        cv.put(STATUS_DATE, date);
        cv.put(STATUS_TIME, time);
        cv.put(STATUS_RESULT, "missed");
        db.insert(TABLE_STATUS, null, cv);
    }

    public List<String> getTakenMedicinesForToday() {
        String today = todayDate();
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name, time FROM " + TABLE_STATUS + " WHERE date=? AND result=?",
                new String[]{today, "taken"});
        while (c.moveToNext()) {
            list.add(c.getString(0) + " (" + c.getString(1) + ")");
        }
        c.close();
        return list;
    }


    private String getInstanceStatus(String name, String time, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT result FROM " + TABLE_STATUS +
                        " WHERE name=? AND time=? AND date=? ORDER BY id DESC LIMIT 1",
                new String[]{name, time, date});
        String status = null;
        if (c.moveToFirst()) status = c.getString(0);
        c.close();
        return status; // taken | missed | null
    }

    public List<String> getMissedCardItemsForToday() {
        String today = todayDate();
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name, time FROM medicine_status WHERE date=? AND result=? ORDER BY time",
                new String[]{today, "missed"});
        while (c.moveToNext()) {
            list.add(c.getString(0) + " (" + c.getString(1) + ")");
        }
        c.close();
        return list;
    }

    public List<String> getMedicinesForTodayDetailed() {
        String today = todayDate();
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> list = new ArrayList<>();

        Cursor c = db.rawQuery(
                "SELECT name, dosage, times FROM " + TABLE_MEDICINES +
                        " WHERE startDate <= ? AND (endDate IS NULL OR endDate='' OR endDate >= ?)",
                new String[]{today, today});

        while (c.moveToNext()) {
            String name = c.getString(0);
            String dose = c.getString(1);
            String times = c.getString(2);

            if (times == null || times.trim().isEmpty()) continue;

            for (String t : times.split(",")) {
                String time = t.trim();
                String remaining = getRemainingForSingleTime(time);
                String status = getInstanceStatus(name, time, today);
                String prefix = "";

                if ("taken".equals(status)) prefix = "✔ ";
                else if ("missed".equals(status)) prefix = "✖ ";

                list.add(prefix + name + " (" + dose + ") @ " + time + " - " + remaining);
            }
        }
        c.close();
        return list;
    }

    public List<String> getMedicinesForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name, dosage, times FROM " + TABLE_MEDICINES +
                        " WHERE startDate <= ? AND (endDate IS NULL OR endDate='' OR endDate >= ?)",
                new String[]{date, date});
        while (c.moveToNext()) {
            String name = c.getString(0);
            String dosage = c.getString(1);
            String times = c.getString(2);
            list.add(name + " (" + dosage + ")");
        }
        c.close();
        return list;
    }

    private String getRemainingForSingleTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty())
            return "No time set";
        try {
            Date now = new Date();
            long nowMs = now.getTime();

            SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
            String todayStr = (selectedDateGlobal != null)
                    ? selectedDateGlobal
                    : new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            Date full = sdfFull.parse(todayStr + " " + timeStr);
            if (full == null) return "Time error";
            if (full.getTime() > nowMs) {
                long diff = full.getTime() - nowMs;
                long hrs = diff / (1000 * 60 * 60);
                long mins = (diff / (1000 * 60)) % 60;
                long secs = (diff / 1000) % 60;
                return String.format(Locale.getDefault(), "Remaining: %02d:%02d:%02d", hrs, mins, secs);
            }
            return "All times passed";
        } catch (Exception e) {
            return "Time error";
        }
    }

    public List<String> getLowStockMedicines() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name, dosage, currentStock FROM " + TABLE_MEDICINES +
                        " WHERE currentStock <= lowStockAlert", null);
        while (c.moveToNext()) {
            list.add(c.getString(0) + " (" + c.getString(1) + ") - Stock: " + c.getInt(2));
        }
        c.close();
        return list;
    }

    public int getLowStockCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_MEDICINES + " WHERE currentStock <= lowStockAlert", null);
        int count = c.moveToFirst() ? c.getInt(0) : 0;
        c.close();
        return count;
    }

    public boolean deleteMedicineByName(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_MEDICINES, "name=?", new String[]{name});
        return result > 0;
    }

    public List<Medicine> getAllMedicineObjects() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Medicine> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name, dosage, notes, currentStock, type, times FROM " + TABLE_MEDICINES, null);
        if (c.moveToFirst()) {
            do {
                list.add(new Medicine(
                        c.getString(0), c.getString(1), c.getString(2),
                        c.getInt(3), c.getString(4), c.getString(5)
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public List<Medicine> getMedicinesForTodayObjects() {
        return getMedicinesForDateObjects(todayDate());
    }

    public List<Medicine> getMedicinesForDateObjects(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Medicine> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name, dosage, notes, currentStock, type, times FROM " + TABLE_MEDICINES +
                        " WHERE startDate <= ? AND (endDate IS NULL OR endDate='' OR endDate >= ?)",
                new String[]{date, date});
        if (c.moveToFirst()) {
            do {
                list.add(new Medicine(
                        c.getString(0), c.getString(1), c.getString(2),
                        c.getInt(3), c.getString(4), c.getString(5)
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public List<Medicine> getUpcomingMedicinesObjects() {
        String today = todayDate();
        SQLiteDatabase db = this.getReadableDatabase();
        List<Medicine> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name, dosage, notes, currentStock, type, times FROM " + TABLE_MEDICINES +
                        " WHERE startDate > ?",
                new String[]{today});
        if (c.moveToFirst()) {
            do {
                list.add(new Medicine(
                        c.getString(0), c.getString(1), c.getString(2),
                        c.getInt(3), c.getString(4), c.getString(5)
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public List<Medicine> getMissedMedicinesObjects() {
        String today = todayDate();
        SQLiteDatabase db = this.getReadableDatabase();
        List<Medicine> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name, dosage, notes, currentStock, type, times FROM " + TABLE_MEDICINES +
                        " WHERE endDate IS NOT NULL AND endDate <> '' AND endDate < ?",
                new String[]{today});
        if (c.moveToFirst()) {
            do {
                list.add(new Medicine(
                        c.getString(0), c.getString(1), c.getString(2),
                        c.getInt(3), c.getString(4), c.getString(5)
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public List<Medicine> getLowStockMedicinesObjects() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Medicine> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name, dosage, notes, currentStock, type, times FROM " + TABLE_MEDICINES +
                        " WHERE currentStock <= lowStockAlert", null);
        if (c.moveToFirst()) {
            do {
                list.add(new Medicine(
                        c.getString(0), c.getString(1), c.getString(2),
                        c.getInt(3), c.getString(4), c.getString(5)
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public boolean isInstanceResolved(String name, String time, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT result FROM medicine_status WHERE name=? AND time=? AND date=? LIMIT 1",
                new String[]{name, time, date});
        boolean resolved = false;
        if (c.moveToFirst()) {
            String r = c.getString(0);
            resolved = "taken".equals(r) || "missed".equals(r);
        }
        c.close();
        return resolved;
    }

    public List<String> getActionableInstancesForToday() {
        String today = todayDate();
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> list = new ArrayList<>();

        Cursor c = db.rawQuery(
                "SELECT name, dosage, times FROM medicines " +
                        "WHERE startDate <= ? AND (endDate IS NULL OR endDate='' OR endDate >= ?)",
                new String[]{today, today});

        while (c.moveToNext()) {
            String name = c.getString(0);
            String dose = c.getString(1);
            String times = c.getString(2);
            if (times == null || times.trim().isEmpty()) continue;

            for (String t : times.split(",")) {
                String time = t.trim();
                if (isInstanceResolved(name, time, today)) continue;

                String remaining = getRemainingForSingleTime(time);
                list.add(name + " (" + dose + ") @ " + time + " - " + remaining);
            }
        }
        c.close();
        return list;
    }

    // Decrease stock by 1 for the given medicine, if possible
    private void decreaseMedicineStock(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery(
                "SELECT currentStock FROM " + TABLE_MEDICINES + " WHERE name=? LIMIT 1",
                new String[]{name});
        if (c.moveToFirst()) {
            int stock = c.getInt(0);
            if (stock > 0) {
                int newStock = stock - 1;
                ContentValues cv = new ContentValues();
                cv.put(MED_STOCK, newStock);
                db.update(TABLE_MEDICINES, cv, "name=?", new String[]{name});
            }
        }
        c.close();
    }
}
