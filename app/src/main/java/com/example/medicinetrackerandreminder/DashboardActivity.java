package com.example.medicinetrackerandreminder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import com.example.medicinetrackerandreminder.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    TextView tvProgress, tvUpcoming, tvMissed, tvLowStock, tvDate;
    LinearLayout scheduleContainer, emptyScheduleView;
    DatabaseHelper db;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        db = new DatabaseHelper(this);
        userId = getIntent().getStringExtra("userId");

        // === Initialize Views ===
        tvProgress = findViewById(R.id.tvProgress);
        tvUpcoming = findViewById(R.id.tvUpcoming);
        tvMissed   = findViewById(R.id.tvMissed);
        tvLowStock = findViewById(R.id.tvLowStock);
        tvDate     = findViewById(R.id.tvDate);
        scheduleContainer = findViewById(R.id.scheduleContainer);
        emptyScheduleView = findViewById(R.id.emptyScheduleView);

        // === Show Today’s Date ===
        String today = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(new Date());
        tvDate.setText(today);

        // === Top Buttons ===
        ImageButton btnCalendar = findViewById(R.id.btnCalendar);
        btnCalendar.setOnClickListener(v -> startActivity(new Intent(this, CalendarActivity.class)));

        ImageButton btnAdd = findViewById(R.id.btnAddMedicine);
        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, AddMedicineActivity.class)));

        ImageButton btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v -> {
            Intent i = new Intent(this, ProfileActivity.class);
            i.putExtra("userId", userId);
            startActivity(i);
        });

        findViewById(R.id.btnAddFirstMedicine)
                .setOnClickListener(v -> startActivity(new Intent(this, AddMedicineActivity.class)));

        // === Card click listeners ===
        findViewById(R.id.cardProgress)
                .setOnClickListener(v -> Toast.makeText(this, "Progress card clicked", Toast.LENGTH_SHORT).show());

        // ✅ Upcoming / Missed / Low Stock show alert lists
        findViewById(R.id.cardUpcoming)
                .setOnClickListener(v -> showListDialog("Upcoming Medicines", db.getUpcomingMedicines()));

        findViewById(R.id.cardMissed)
                .setOnClickListener(v -> showListDialog("Missed Medicines", db.getMissedMedicines()));

        findViewById(R.id.cardLowStock)
                .setOnClickListener(v -> showListDialog("Low‑Stock Medicines", db.getLowStockMedicines()));

        findViewById(R.id.cardProgress)
                .setOnClickListener(v -> {
                    List<String> todayMeds = db.getMedicinesForToday();
                    if (todayMeds.isEmpty()) {
                        Toast.makeText(this, "No medicines scheduled for today.", Toast.LENGTH_SHORT).show();
                    } else {
                        showListDialog("Today's Medicines", todayMeds);
                    }
                });



        refresh();

        // === Bottom Navigation ===
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_home);
        nav.setOnItemSelectedListener(item -> {
            int i = item.getItemId();
            if (i == R.id.nav_calendar) {
                startActivity(new Intent(this, CalendarActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            if (i == R.id.nav_meds) {
                startActivity(new Intent(this, MyMedsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            if (i == R.id.nav_profile) {
                Intent p = new Intent(this, ProfileActivity.class);
                p.putExtra("userId", userId);
                startActivity(p);
                overridePendingTransition(0, 0);
                return true;
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    /** Refresh dashboard stats and today’s schedule. */
    private void refresh() {
        int total = db.getAllMedicinesCount();
        int low   = db.getLowStockCount();

        tvProgress.setText("0/" + total);
        tvUpcoming.setText(String.valueOf(db.getUpcomingMedicines().size()));
        tvMissed.setText(String.valueOf(db.getMissedMedicines().size()));
        tvLowStock.setText(String.valueOf(low));

        // === Show Today’s Schedule ===
        List<String> todayList = db.getMedicinesForToday();
        if (todayList.isEmpty()) {
            emptyScheduleView.setVisibility(View.VISIBLE);
            scheduleContainer.setVisibility(View.GONE);
        } else {
            emptyScheduleView.setVisibility(View.GONE);
            scheduleContainer.setVisibility(View.VISIBLE);
            scheduleContainer.removeAllViews();
            for (String med : todayList) {
                TextView t = new TextView(this);
                t.setText(med);
                t.setTextSize(14f);
                t.setPadding(12, 6, 12, 6);
                scheduleContainer.addView(t);
            }
        }
    }

    private final android.os.Handler handler = new android.os.Handler();
    private final Runnable refreshTask = new Runnable() {
        @Override public void run() {
            refresh();
            handler.postDelayed(this, 60_000); // refresh every 1 minute
        }
    };

    @Override protected void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshTask);
    }

    /** 🩺 Show list of items in an AlertDialog **/
    private void showListDialog(String title, List<String> list) {
        if (list == null || list.isEmpty()) {
            Toast.makeText(this, "No items found", Toast.LENGTH_SHORT).show();
            return;
        }

        CharSequence[] arr = list.toArray(new CharSequence[0]);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(title);
        dialog.setItems(arr, null);
        dialog.setPositiveButton("OK", null);
        dialog.show();
    }
}