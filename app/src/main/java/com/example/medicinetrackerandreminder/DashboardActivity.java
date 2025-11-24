package com.example.medicinetrackerandreminder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.medicinetrackerandreminder.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    TextView tvProgress, tvUpcoming, tvMissed, tvLowStock, tvDate;
    LinearLayout scheduleContainer, emptyScheduleView;
    DatabaseHelper db;
    long uid = SessionManager.get();

    private final android.os.Handler handler = new android.os.Handler();
    private final Runnable refreshTask = new Runnable() {
        @Override
        public void run() {
            refresh();
            handler.postDelayed(this, 60_000);
        }
    };

    // ---------- Lifecycle ----------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }

        db = new DatabaseHelper(this);
        bindViews();
        setupTopButtons();
        setupBottomNavigation();
        refresh();
    }

    @SuppressLint({"UnregisteredReceiver", "UnspecifiedRegisterReceiverFlag"})
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(dashboardRefreshReceiver,
                new IntentFilter("com.example.medicinetrackerandreminder.REFRESH_DASHBOARD"));
        refresh();
        handler.postDelayed(refreshTask, 60_000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(dashboardRefreshReceiver);
        handler.removeCallbacks(refreshTask);
    }

    // ---------- UI Initialization ----------
    private void bindViews() {
        tvProgress = findViewById(R.id.tvProgress);
        tvUpcoming = findViewById(R.id.tvUpcoming);
        tvMissed = findViewById(R.id.tvMissed);
        tvLowStock = findViewById(R.id.tvLowStock);
        tvDate = findViewById(R.id.tvDate);
        scheduleContainer = findViewById(R.id.scheduleContainer);
        emptyScheduleView = findViewById(R.id.emptyScheduleView);

        String today = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(new Date());
        tvDate.setText(today);
    }

    // ---------- Top Buttons and Summary Cards ----------
    private void setupTopButtons() {
        findViewById(R.id.btnCalendar)
                .setOnClickListener(v -> startActivity(new Intent(this, CalendarActivity.class)));
        findViewById(R.id.btnAddMedicine)
                .setOnClickListener(v -> startActivity(new Intent(this, AddMedicineActivity.class)));
        findViewById(R.id.btnProfile)
                .setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        findViewById(R.id.btnAddFirstMedicine)
                .setOnClickListener(v -> startActivity(new Intent(this, AddMedicineActivity.class)));

        findViewById(R.id.cardProgress).setOnClickListener(v -> {
            List<String> taken = db.getTakenMedicinesForToday(uid);
            showListDialog("✅ Medicines Taken Today", taken);
        });

        findViewById(R.id.cardUpcoming).setOnClickListener(v -> {
            List<String> all = db.getMedicinesForTodayDetailed(uid);
            List<String> upcoming = new ArrayList<>();
            for (String s : all) if (!s.startsWith("✔") && !s.startsWith("✖")) upcoming.add(s);
            showListDialog("🕒 Upcoming Medicines", upcoming);
        });

        findViewById(R.id.cardMissed).setOnClickListener(v -> {
            List<String> missed = db.getMissedCardItemsForToday(uid);
            showListDialog("❌ Missed Medicines", missed);
        });

        findViewById(R.id.cardLowStock).setOnClickListener(v -> {
            List<String> low = db.getLowStockMedicines(uid);
            showListDialog("📦 Low Stock Medicines", low);
        });
    }

    // ---------- Bottom Navigation ----------
    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_home);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_calendar) {
                startActivity(new Intent(this, CalendarActivity.class));
            } else if (id == R.id.nav_meds) {
                startActivity(new Intent(this, MyMedsActivity.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
            }
            overridePendingTransition(0, 0);
            return true;
        });
    }

    // ---------- Dashboard Refresh ----------
    private void refresh() {
        updateCounters();
        List<String> todayList = db.getMedicinesForTodayDetailed(uid);
        scheduleContainer.removeAllViews();

        if (todayList == null || todayList.isEmpty()) {
            emptyScheduleView.setVisibility(android.view.View.VISIBLE);
            scheduleContainer.setVisibility(android.view.View.GONE);
            return;
        }

        emptyScheduleView.setVisibility(android.view.View.GONE);
        scheduleContainer.setVisibility(android.view.View.VISIBLE);

        for (String row : todayList) {
            String line = row.replace("✔", "").replace("✖", "").trim();
            String medName = line;
            String medTime = "";
            if (line.contains("@")) {
                String[] parts = line.split("@");
                medName = parts[0].trim();
                medTime = parts[1].trim();
            }

            String status;
            int statusColor = ContextCompat.getColor(this, R.color.gray);
            if (row.startsWith("✔")) {
                status = "✅ Taken";
                statusColor = ContextCompat.getColor(this, R.color.teal_primary);
            } else if (row.startsWith("✖")) {
                status = "❌ Missed";
                statusColor = ContextCompat.getColor(this, android.R.color.holo_red_light);
            } else {
                status = "Remaining time: " + calculateRemainingTime(medTime);
            }

            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setPadding(24, 24, 24, 24);
            item.setBackgroundResource(R.drawable.bg_white_rounded);

            TextView nameTv = new TextView(this);
            nameTv.setText(medName + " : " + medTime);
            nameTv.setTextSize(16);
            nameTv.setTextColor(ContextCompat.getColor(this, R.color.black));
            nameTv.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView remainTv = new TextView(this);
            remainTv.setText(status);
            remainTv.setTextSize(13);
            remainTv.setTextColor(statusColor);

            item.addView(nameTv);
            item.addView(remainTv);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 8, 0, 8);
            item.setLayoutParams(lp);

            scheduleContainer.addView(item);
        }
    }

    // ---------- Time Calculation ----------
    private String calculateRemainingTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return "No time set";
        try {
            SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
            SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date now = new Date();
            String todayStr = dateFmt.format(now);
            Date target = fullFormat.parse(todayStr + " " + timeStr);
            if (target == null) return "Time error";

            long diffMs = target.getTime() - now.getTime();
            if (diffMs <= 0) return "⏰ All times passed";

            long hours = diffMs / (1000 * 60 * 60);
            long minutes = (diffMs / (1000 * 60)) % 60;
            long seconds = (diffMs / 1000) % 60;
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } catch (Exception e) {
            return "Time error";
        }
    }

    // ---------- Counter Update ----------
    private void updateCounters() {
        String today = db.todayDate();
        SQLiteDatabase database = db.getReadableDatabase();
        int taken = numericCount(database,
                "SELECT COUNT(*) FROM medicine_status WHERE date=? AND result='taken'", today);
        int missed = numericCount(database,
                "SELECT COUNT(*) FROM medicine_status WHERE date=? AND result='missed'", today);
        int totalToday = db.getMedicinesForTodayDetailed(uid).size();
        int upcoming = Math.max(totalToday - taken - missed, 0);

        tvProgress.setText(String.valueOf(taken));
        tvUpcoming.setText(String.valueOf(upcoming));
        tvMissed.setText(String.valueOf(missed));
        tvLowStock.setText(String.valueOf(db.getLowStockCount(uid)));
    }

    private int numericCount(SQLiteDatabase db, String sql, String arg) {
        Cursor c = db.rawQuery(sql, new String[]{arg});
        int n = c.moveToFirst() ? c.getInt(0) : 0;
        c.close();
        return n;
    }

    // ---------- Dialog Utilities ----------
    private void showListDialog(String title, List<String> list) {
        if (list == null || list.isEmpty()) {
            Toast.makeText(this, "No items found", Toast.LENGTH_SHORT).show();
            return;
        }
        CharSequence[] arr = list.toArray(new CharSequence[0]);
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(arr, null)
                .setPositiveButton("OK", null)
                .show();
    }

    // ---------- Permission Handling ----------
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Permission required for reminders", Toast.LENGTH_LONG).show();
        }
    }

    // ---------- Broadcast Receiver ----------
    private final android.content.BroadcastReceiver dashboardRefreshReceiver =
            new android.content.BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if ("com.example.medicinetrackerandreminder.REFRESH_DASHBOARD"
                            .equals(intent.getAction())) {
                        refresh();
                    }
                }
            };
}