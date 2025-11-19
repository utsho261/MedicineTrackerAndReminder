package com.example.medicinetrackerandreminder;

import android.Manifest;
import android.content.Intent;
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

/**
 * Dashboard page showing today's schedule and summary stats.
 * Simplified (no Take / Missed buttons) but all cards clickable.
 */
public class DashboardActivity extends AppCompatActivity {

    TextView tvProgress, tvUpcoming, tvMissed, tvLowStock, tvDate;
    LinearLayout scheduleContainer, emptyScheduleView;
    DatabaseHelper db;

    private final android.os.Handler handler = new android.os.Handler();
    private final Runnable refreshTask = new Runnable() {
        @Override
        public void run() {
            refresh();
            handler.postDelayed(this, 60_000); // refresh every minute
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Request POST_NOTIFICATIONS for Android 13+
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

    private void setupTopButtons() {
        findViewById(R.id.btnCalendar).setOnClickListener(
                v -> startActivity(new Intent(this, CalendarActivity.class)));
        findViewById(R.id.btnAddMedicine).setOnClickListener(
                v -> startActivity(new Intent(this, AddMedicineActivity.class)));
        findViewById(R.id.btnProfile).setOnClickListener(
                v -> startActivity(new Intent(this, ProfileActivity.class)));
        findViewById(R.id.btnAddFirstMedicine)
                .setOnClickListener(v ->
                        startActivity(new Intent(this, AddMedicineActivity.class)));

        // === Statistic card click listeners ===
        findViewById(R.id.cardProgress).setOnClickListener(v -> {
            List<String> takenToday = db.getTakenMedicinesForToday();
            if (takenToday == null || takenToday.isEmpty()) {
                Toast.makeText(this, "No medicines taken today.", Toast.LENGTH_SHORT).show();
            } else {
                showListDialog("Today's Progress (Taken)", takenToday);
            }
        });

        findViewById(R.id.cardUpcoming).setOnClickListener(v -> {
            List<String> upcomingList = getUpcomingMedicinesWithStatus();
            if (upcomingList == null || upcomingList.isEmpty()) {
                Toast.makeText(this, "No upcoming medicines.", Toast.LENGTH_SHORT).show();
            } else {
                showListDialog("Upcoming Medicines", upcomingList);
            }
        });

        findViewById(R.id.cardMissed).setOnClickListener(v ->
                showListDialog("Missed Medicines (Today)", db.getMissedCardItemsForToday()));

        findViewById(R.id.cardLowStock).setOnClickListener(v ->
                showListDialog("Low‑Stock Medicines", db.getLowStockMedicines()));
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_home);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_calendar) {
                startActivity(new Intent(this, CalendarActivity.class));
                overridePendingTransition(0, 0);
            } else if (id == R.id.nav_meds) {
                startActivity(new Intent(this, MyMedsActivity.class));
                overridePendingTransition(0, 0);
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        handler.postDelayed(refreshTask, 60_000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshTask);
    }

    // ======================================================================
    private void refresh() {
        updateCounters();

        List<String> todayList = db.getActionableInstancesForToday();
        scheduleContainer.removeAllViews();

        if (todayList == null || todayList.isEmpty()) {
            emptyScheduleView.setVisibility(android.view.View.VISIBLE);
            scheduleContainer.setVisibility(android.view.View.GONE);
            return;
        }

        emptyScheduleView.setVisibility(android.view.View.GONE);
        scheduleContainer.setVisibility(android.view.View.VISIBLE);

        for (String row : todayList) {

            // --- Parent Card ---
            androidx.cardview.widget.CardView card = new androidx.cardview.widget.CardView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 8, 0, 8);
            card.setLayoutParams(lp);
            card.setRadius(16f);
            card.setCardElevation(3f);
            card.setUseCompatPadding(true);

            // --- Inside the card ---
            LinearLayout inner = new LinearLayout(this);
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setPadding(24, 20, 24, 20);

            // medicine name + dosage highlighted
            TextView tvMedName = new TextView(this);
            tvMedName.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Medium);
            tvMedName.setTextColor(ContextCompat.getColor(this, R.color.black));
            tvMedName.setText(row.split("-")[0].trim());

            // remaining time styled lighter
            TextView tvRemain = new TextView(this);
            tvRemain.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Body2);
            tvRemain.setTextColor(ContextCompat.getColor(this, R.color.gray));
            if (row.contains("Remaining"))
                tvRemain.setText(row.substring(row.indexOf("Remaining")));
            else
                tvRemain.setText("Time info unavailable");

            inner.addView(tvMedName);
            inner.addView(tvRemain);
            card.addView(inner);
            scheduleContainer.addView(card);
        }
    }


    // --- Counter update logic ---
    private void updateCounters() {
        String today = db.todayDate();
        SQLiteDatabase database = db.getReadableDatabase();

        int taken = numericCount(database,
                "SELECT COUNT(*) FROM medicine_status WHERE date=? AND result='taken'", today);
        int missed = numericCount(database,
                "SELECT COUNT(*) FROM medicine_status WHERE date=? AND result='missed'", today);
        int totalToday = db.getMedicinesForTodayDetailed().size();
        int upcoming = Math.max(totalToday - taken - missed, 0);

        tvProgress.setText(String.valueOf(taken));
        tvUpcoming.setText(String.valueOf(upcoming));
        tvMissed.setText(String.valueOf(missed));
        tvLowStock.setText(String.valueOf(db.getLowStockCount()));
    }

    private int numericCount(SQLiteDatabase db, String sql, String arg) {
        Cursor c = db.rawQuery(sql, new String[]{arg});
        int n = c.moveToFirst() ? c.getInt(0) : 0;
        c.close();
        return n;
    }

    // --- Used by "Upcoming" card to display today's medicines with color‑coded status ---
    private List<String> getUpcomingMedicinesWithStatus() {
        List<String> result = new ArrayList<>();
        List<String> todayList = db.getMedicinesForTodayDetailed();
        List<String> takenList = db.getTakenMedicinesForToday();
        for (String medicine : todayList) {
            if (takenList.contains(medicine))
                result.add(medicine + " (Taken)");
            else if (medicine.contains("All times passed"))
                result.add(medicine + " (Missed)");
            else result.add(medicine + " (Upcoming)");
        }
        return result;
    }

    // --- Dialog used by statistic cards ---
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

    // --- Runtime permission result ---
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
}