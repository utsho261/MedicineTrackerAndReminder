package com.example.medicinetrackerandreminder;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medicinetrackerandreminder.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {
    DatabaseHelper db;
    MaterialCalendarView calendarView;
    TextView tvSelectedDate, tvInstruction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        db = new DatabaseHelper(this);
        calendarView = findViewById(R.id.calendarView);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvInstruction = findViewById(R.id.tvInstruction);

        // Set up navigation once
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(CalendarActivity.this, DashboardActivity.class));
                return true;
            } else if (id == R.id.nav_calendar) {
                return true;
            } else if (id == R.id.nav_meds) {
                startActivity(new Intent(CalendarActivity.this, MyMedsActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(CalendarActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            // Remainder of your date selection code here...
            Calendar cal = Calendar.getInstance();
            cal.set(date.getYear(), date.getMonth(), date.getDay());
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(cal.getTime());

            tvSelectedDate.setText(getString(R.string.selected_date, formattedDate));

            List<String> meds = db.getMedicinesForDate(formattedDate);

            if (meds.isEmpty()) {
                new AlertDialog.Builder(this)
                        .setTitle("No Medications")
                        .setMessage("No medications scheduled for this day.")
                        .setPositiveButton("OK", null)
                        .show();
                tvInstruction.setText("No medications today");
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Medicines for " + formattedDate)
                        .setItems(meds.toArray(new String[0]), null)
                        .setPositiveButton("OK", null)
                        .show();
                tvInstruction.setText(TextUtils.join("\n", meds));
            }
        });
    }

}