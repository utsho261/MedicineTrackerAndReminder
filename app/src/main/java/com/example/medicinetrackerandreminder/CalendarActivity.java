package com.example.medicinetrackerandreminder;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
<<<<<<< HEAD
import androidx.appcompat.app.AlertDialog;

=======
>>>>>>> 6e0eac1e567fa77c22dd282e100fd17e0e462d85

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

<<<<<<< HEAD
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


=======
        calendarView.setOnDateChangedListener((widget,date,selected)->{
            String d=date.getDay()+"/"+date.getMonth()+"/"+date.getYear();
            tvSelectedDate.setText("Selected: "+d);
            List<String> meds=db.getMedicinesForDate(d);
            if(meds.isEmpty()) tvInstruction.setText("No medications today");
            else tvInstruction.setText(TextUtils.join("\n",meds));
        });
>>>>>>> 6e0eac1e567fa77c22dd282e100fd17e0e462d85
    }
}