package com.example.medicinetrackerandreminder;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

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

        calendarView.setOnDateChangedListener((widget,date,selected)->{
            String d=date.getDay()+"/"+date.getMonth()+"/"+date.getYear();
            tvSelectedDate.setText("Selected: "+d);
            List<String> meds=db.getMedicinesForDate(d);
            if(meds.isEmpty()) tvInstruction.setText("No medications today");
            else tvInstruction.setText(TextUtils.join("\n",meds));
        });
    }
}