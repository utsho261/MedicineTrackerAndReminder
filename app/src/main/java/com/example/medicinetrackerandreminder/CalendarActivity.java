package com.example.medicinetrackerandreminder;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;


import androidx.appcompat.app.AppCompatActivity;

import com.example.medicinetrackerandreminder.database.DatabaseHelper;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.List;

public class CalendarActivity extends AppCompatActivity {
    DatabaseHelper db;
    MaterialCalendarView calendarView;
    TextView tvSelectedDate,tvInstruction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        db = new DatabaseHelper(this);
        calendarView = findViewById(R.id.calendarView);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvInstruction = findViewById(R.id.tvInstruction);

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(date.getYear(), date.getMonth(), date.getDay());
            String formattedDate = df.format(cal.getTime());

            tvSelectedDate.setText(getString(R.string.selected_date, formattedDate));

            List<String> meds = db.getMedicinesForDate(formattedDate);
            if (meds.isEmpty()) {
                new AlertDialog.Builder(this)
                        .setTitle("No Medications")
                        .setMessage("No medications today")
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Medicines for " + formattedDate)
                        .setItems(meds.toArray(new String[0]), null)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });


    }
}