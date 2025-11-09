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
    private static final int DATABASE_VERSION = 3;

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

    // Used when a specific date is selected in a calendar view
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
                USER_PHONE + " TEXT UNIQUE, " +
                USER_EMAIL + " TEXT UNIQUE, " +
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICINES);
        onCreate(db);
    }

    // ===== Utility Methods =====
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

    // ===== User Management =====
    public boolean registerUser(String f, String l, String dob,
                                String phone, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(USER_FNAME, f);
        cv.put(USER_LNAME, l);
        cv.put(USER_DOB, dob);
        cv.put(USER_PHONE, phone);
        cv.put(USER_EMAIL, email);
        cv.put(USER_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, cv);
        return result != -1;
    }

    public boolean userExists(String phone, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE phone=? OR email=?",
                new String[]{phone, email});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    public boolean login(String id, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS +
                        " WHERE (phone=? OR email=?) AND password=?",
                new String[]{id, id, password});
        boolean valid = c.moveToFirst();
        c.close();
        return valid;
    }

    public Cursor getUser(String idOrEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT firstName, lastName, dob, phone, email FROM users WHERE phone=? OR email=?",
                new String[]{idOrEmail, idOrEmail});
    }

    // ===== Medicines =====
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
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_MEDICINES + " WHERE name=?",
                new String[]{name});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    // ===== Medicine Queries =====
    public List<String> getMedicinesForToday() {
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
            String remaining = getNextRemainingTime(times);

            // skip anything that has no remaining time today
            if (remaining.startsWith("All") || remaining.startsWith("00:00:00"))
                continue;

            list.add(name + " (" + dose + ") - " + remaining);
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
            list.add(name + " (" + dosage + ") - " + getNextRemainingTime(times));
        }
        c.close();
        return list;
    }

    private String getNextRemainingTime(String times) {
        if (times == null || times.isEmpty())
            return "No times set";

        try {
            Date now = new Date();
            long nowMs = now.getTime();

            SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
            String todayStr = (selectedDateGlobal != null)
                    ? selectedDateGlobal
                    : new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            for (String t : times.split(",")) {
                Date full = sdfFull.parse(todayStr + " " + t.trim());
                if (full == null) continue;
                if (full.getTime() > nowMs) {
                    long diff = full.getTime() - nowMs;
                    long hrs = diff / (1000 * 60 * 60);
                    long mins = (diff / (1000 * 60)) % 60;
                    long secs = (diff / 1000) % 60;
                    return String.format(Locale.getDefault(), "Remaining: %02d:%02d:%02d", hrs, mins, secs);
                }
            }
            return "All times passed";
        } catch (Exception e) {
            return "Time error";
        }
    }

    public List<String> getUpcomingMedicines() {
        String today = todayDate();
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name, dosage FROM " + TABLE_MEDICINES + " WHERE startDate > ?", new String[]{today});
        while (c.moveToNext()) {
            list.add(c.getString(0) + " (" + c.getString(1) + ")");
        }
        c.close();
        return list;
    }

    public List<String> getMissedMedicines() {
        String today = todayDate();
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name, dosage FROM " + TABLE_MEDICINES +
                        " WHERE endDate IS NOT NULL AND endDate <> '' AND endDate < ?",
                new String[]{today});
        while (c.moveToNext()) {
            list.add(c.getString(0) + " (" + c.getString(1) + ")");
        }
        c.close();
        return list;
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

    // ===== Dashboard Counts =====
    public int getAllMedicinesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_MEDICINES, null);
        int count = c.moveToFirst() ? c.getInt(0) : 0;
        c.close();
        return count;
    }

    public int getLowStockCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_MEDICINES + " WHERE currentStock <= lowStockAlert", null);
        int count = c.moveToFirst() ? c.getInt(0) : 0;
        c.close();
        return count;
    }

    // ===== Medicine Object Lists =====
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
                        " WHERE startDate > ?", new String[]{today});
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
}