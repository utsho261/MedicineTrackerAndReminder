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
    private static final int DATABASE_VERSION = 5;
    public String selectedDateGlobal = null;

    private static final String TABLE_USERS = "users";
    private static final String USER_ID = "id";
    private static final String USER_FNAME = "firstName";
    private static final String USER_LNAME = "lastName";
    private static final String USER_DOB = "dob";
    private static final String USER_PHONE = "phone";
    private static final String USER_EMAIL = "email";
    private static final String USER_PASSWORD = "password";

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

    private static final String TABLE_STATUS = "medicine_status";
    private static final String STATUS_ID = "id";
    private static final String STATUS_MED_NAME = "name";
    private static final String STATUS_DATE = "date";
    private static final String STATUS_TIME = "time";
    private static final String STATUS_RESULT = "result";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "firstName TEXT,lastName TEXT,dob TEXT," +
                "phone TEXT,email TEXT,password TEXT)");

        db.execSQL("CREATE TABLE medicines (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "userId INTEGER,name TEXT,type TEXT,dosage TEXT," +
                "startDate TEXT,endDate TEXT,currentStock INTEGER," +
                "lowStockAlert INTEGER,frequency TEXT,times TEXT," +
                "color INTEGER,notes TEXT)");

        db.execSQL("CREATE TABLE medicine_status (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "userId INTEGER,name TEXT,date TEXT,time TEXT,result TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS medicines");
        db.execSQL("DROP TABLE IF EXISTS medicine_status");
        onCreate(db);
    }

    // ---------- Utility helpers ----------
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

    // ---------- User management ----------
    public boolean registerUser(String f, String l, String dob, String phone, String email, String pass) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(USER_FNAME, f);
        cv.put(USER_LNAME, l);
        cv.put(USER_DOB, dob);
        cv.put(USER_PHONE, phone);
        cv.put(USER_EMAIL, email);
        cv.put(USER_PASSWORD, pass);
        return db.insert(TABLE_USERS, null, cv) != -1;
    }

    public boolean userExists(String phone, String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM users WHERE phone=? OR email=? LIMIT 1",
                new String[]{phone, email});
        boolean ex = c.moveToFirst();
        c.close();
        return ex;
    }

    public boolean login(String phoneEmail, String pass) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT id FROM users WHERE (phone=? OR email=?) AND password=? LIMIT 1",
                new String[]{phoneEmail, phoneEmail, pass});
        boolean ok = c.moveToFirst();
        c.close();
        return ok;
    }

    public long getUserId(String phoneEmail) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT id FROM users WHERE phone=? OR email=? LIMIT 1",
                new String[]{phoneEmail, phoneEmail});
        long id = c.moveToFirst() ? c.getLong(0) : -1;
        c.close();
        return id;
    }

    // ---------- Medicine management ----------
    public boolean addMedicine(String name, String type, String dosage,
                               String start, String end, int stock, int low,
                               String freq, String times, int color, String notes, long userId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("userId", userId);
        cv.put(MED_NAME, name);
        cv.put(MED_TYPE, type);
        cv.put(MED_DOSAGE, dosage);
        cv.put(MED_START, start);
        cv.put(MED_END, end);
        cv.put(MED_STOCK, stock);
        cv.put(MED_LOWSTOCK, low);
        cv.put(MED_FREQUENCY, freq);
        cv.put(MED_TIMES, times);
        cv.put(MED_COLOR, color);
        cv.put(MED_NOTES, notes);
        return db.insert(TABLE_MEDICINES, null, cv) != -1;
    }

    public boolean medicineExists(String name, long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT 1 FROM medicines WHERE name=? AND userId=? LIMIT 1",
                new String[]{name, String.valueOf(userId)});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    // ---------- Medicine status management ----------
    private void upsertMedicineStatus(String name, String time, String date, long userId, String result) {
        SQLiteDatabase db = getWritableDatabase();
        String normalizedTime = time.trim().toUpperCase(Locale.getDefault());
        Cursor c = db.rawQuery(
                "SELECT id FROM " + TABLE_STATUS +
                        " WHERE userId=? AND name=? AND UPPER(TRIM(time))=? AND date=? LIMIT 1",
                new String[]{String.valueOf(userId), name, normalizedTime, date});

        if (c.moveToFirst()) {
            long id = c.getLong(0);
            ContentValues cv = new ContentValues();
            cv.put(STATUS_RESULT, result);
            db.update(TABLE_STATUS, cv, "id=?", new String[]{String.valueOf(id)});
        } else {
            ContentValues cv = new ContentValues();
            cv.put("userId", userId);
            cv.put(STATUS_MED_NAME, name);
            cv.put(STATUS_DATE, date);
            cv.put(STATUS_TIME, normalizedTime);
            cv.put(STATUS_RESULT, result);
            db.insert(TABLE_STATUS, null, cv);
        }
        c.close();
        if ("taken".equals(result)) decreaseMedicineStock(name, userId);
    }

    public void markMedicineTaken(String name, String time, String date, long userId) {
        upsertMedicineStatus(name, time, date, userId, "taken");
    }

    public void markMedicineMissed(String name, String time, String date, long userId) {
        upsertMedicineStatus(name, time, date, userId, "missed");
    }

    private void decreaseMedicineStock(String name, long userId) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery(
                "SELECT currentStock FROM medicines WHERE name=? AND userId=? LIMIT 1",
                new String[]{name, String.valueOf(userId)});
        if (c.moveToFirst()) {
            int st = c.getInt(0);
            if (st > 0) {
                ContentValues cv = new ContentValues();
                cv.put(MED_STOCK, st - 1);
                db.update(TABLE_MEDICINES, cv, "name=? AND userId=?",
                        new String[]{name, String.valueOf(userId)});
            }
        }
        c.close();
    }

    // ---------- Reading helpers ----------
    public List<Medicine> getAllMedicineObjects(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        List<Medicine> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name,dosage,notes,currentStock,type,times FROM medicines WHERE userId=?",
                new String[]{String.valueOf(userId)});
        while (c.moveToNext()) {
            list.add(new Medicine(
                    c.getString(0), c.getString(1), c.getString(2),
                    c.getInt(3), c.getString(4), c.getString(5)));
        }
        c.close();
        return list;
    }

    public List<String> getMedicinesForTodayDetailed(long userId) {
        String today = todayDate();
        SQLiteDatabase db = getReadableDatabase();
        List<String> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name,dosage,times FROM medicines WHERE userId=? AND startDate<=? AND " +
                        "(endDate IS NULL OR endDate='' OR endDate>=?)",
                new String[]{String.valueOf(userId), today, today});
        while (c.moveToNext()) {
            String name = c.getString(0), dose = c.getString(1), times = c.getString(2);
            if (times == null || times.trim().isEmpty()) continue;
            for (String t : times.split(",")) {
                String time = t.trim();
                String status = getInstanceStatus(name, t.trim(), today, userId);
                String prefix = "";
                if ("taken".equals(status)) prefix = "✔ ";
                else if ("missed".equals(status)) prefix = "✖ ";
                list.add(prefix + name + " (" + dose + ") @ " + t.trim());
            }
        }
        c.close();
        return list;
    }

    private String getInstanceStatus(String name, String time, String date, long userId) {
        SQLiteDatabase db = getReadableDatabase();
        String normalized = time.trim().toUpperCase(Locale.getDefault());
        Cursor c = db.rawQuery(
                "SELECT result FROM medicine_status " +
                        "WHERE userId=? AND name=? AND UPPER(TRIM(time))=? AND date=? " +
                        "ORDER BY id DESC LIMIT 1",
                new String[]{String.valueOf(userId), name, normalized, date});
        String r = null;
        if (c.moveToFirst()) r = c.getString(0);
        c.close();
        return r;
    }

    public List<String> getMedicinesForDate(String date, long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name, dosage, times FROM medicines " +
                        "WHERE userId=? AND startDate<=? AND (endDate IS NULL OR endDate='' OR endDate>=?)",
                new String[]{String.valueOf(userId), date, date});
        while (c.moveToNext()) {
            String name = c.getString(0);
            String dosage = c.getString(1);
            list.add(name + " (" + dosage + ")");
        }
        c.close();
        return list;
    }

    public List<String> getTakenMedicinesForToday(long userId) {
        String today = todayDate();
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name, time FROM medicine_status " +
                        "WHERE userId=? AND date=? AND result=?",
                new String[]{String.valueOf(userId), today, "taken"});
        while (c.moveToNext()) {
            list.add(c.getString(0) + " (" + c.getString(1) + ")");
        }
        c.close();
        return list;
    }

    public List<String> getMissedCardItemsForToday(long userId) {
        String today = todayDate();
        SQLiteDatabase db = getReadableDatabase();
        List<String> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name, time FROM medicine_status " +
                        "WHERE userId=? AND date=? AND result=? ORDER BY time",
                new String[]{String.valueOf(userId), today, "missed"});
        while (c.moveToNext()) {
            list.add(c.getString(0) + " (" + c.getString(1) + ")");
        }
        c.close();
        return list;
    }

    public List<String> getLowStockMedicines(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        List<String> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name, dosage, currentStock FROM medicines " +
                        "WHERE userId=? AND currentStock <= lowStockAlert",
                new String[]{String.valueOf(userId)});
        while (c.moveToNext()) {
            list.add(c.getString(0) + " (" + c.getString(1) + ") - Stock: " + c.getInt(2));
        }
        c.close();
        return list;
    }

    public int getLowStockCount(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM medicines " +
                        "WHERE userId=? AND currentStock <= lowStockAlert",
                new String[]{String.valueOf(userId)});
        int count = c.moveToFirst() ? c.getInt(0) : 0;
        c.close();
        return count;
    }

    // ---------- Medicine retrieval for various screens ----------
    public List<Medicine> getMedicinesForTodayObjects(long userId) {
        return getMedicinesForDateObjects(todayDate(), userId);
    }

    public List<Medicine> getMedicinesForDateObjects(String date, long userId) {
        SQLiteDatabase db = getReadableDatabase();
        List<Medicine> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name,dosage,notes,currentStock,type,times FROM medicines " +
                        "WHERE userId=? AND startDate<=? AND (endDate IS NULL OR endDate='' OR endDate>=?)",
                new String[]{String.valueOf(userId), date, date});
        while (c.moveToNext()) {
            list.add(new Medicine(
                    c.getString(0), c.getString(1), c.getString(2),
                    c.getInt(3), c.getString(4), c.getString(5)));
        }
        c.close();
        return list;
    }

    public List<Medicine> getUpcomingMedicinesObjects(long userId) {
        String today = todayDate();
        SQLiteDatabase db = getReadableDatabase();
        List<Medicine> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name,dosage,notes,currentStock,type,times FROM medicines " +
                        "WHERE userId=? AND startDate>?",
                new String[]{String.valueOf(userId), today});
        while (c.moveToNext()) {
            list.add(new Medicine(
                    c.getString(0), c.getString(1), c.getString(2),
                    c.getInt(3), c.getString(4), c.getString(5)));
        }
        c.close();
        return list;
    }

    public List<Medicine> getMissedMedicinesObjects(long userId) {
        String today = todayDate();
        SQLiteDatabase db = getReadableDatabase();
        List<Medicine> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name,dosage,notes,currentStock,type,times FROM medicines " +
                        "WHERE userId=? AND endDate IS NOT NULL AND endDate<>'' AND endDate<?",
                new String[]{String.valueOf(userId), today});
        while (c.moveToNext()) {
            list.add(new Medicine(
                    c.getString(0), c.getString(1), c.getString(2),
                    c.getInt(3), c.getString(4), c.getString(5)));
        }
        c.close();
        return list;
    }

    public List<Medicine> getLowStockMedicinesObjects(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        List<Medicine> list = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT name,dosage,notes,currentStock,type,times FROM medicines " +
                        "WHERE userId=? AND currentStock<=lowStockAlert",
                new String[]{String.valueOf(userId)});
        while (c.moveToNext()) {
            list.add(new Medicine(
                    c.getString(0), c.getString(1), c.getString(2),
                    c.getInt(3), c.getString(4), c.getString(5)));
        }
        c.close();
        return list;
    }

    // ---------- Deletion and user info ----------
    public boolean deleteMedicineByName(String name, long userId) {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete("medicines", "name=? AND userId=?",
                new String[]{name, String.valueOf(userId)});
        return result > 0;
    }

    public Cursor getSingleUserCursor(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT firstName, lastName, dob, phone, email FROM users WHERE id=? LIMIT 1",
                new String[]{String.valueOf(userId)});
    }

}