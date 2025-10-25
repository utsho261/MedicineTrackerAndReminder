package com.example.medicinetrackerandreminder;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicinetrackerandreminder.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {

    DatabaseHelper db;
    RecyclerView recyclerView;
    TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        db = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tvTitle = findViewById(R.id.tvTitle);

        String mode = getIntent().getStringExtra("mode");
        List<Medicine> meds;

        switch(mode){
            case "progress":
                tvTitle.setText("Today's Progress");
                meds = db.getMedicinesForTodayObjects();
                break;
            case "upcoming":
                tvTitle.setText("Upcoming Medicines");
                meds = db.getUpcomingMedicinesObjects();
                break;
            case "missed":
                tvTitle.setText("Missed Medicines");
                meds = db.getMissedMedicinesObjects();
                break;
            case "lowstock":
                tvTitle.setText("Low Stock Medicines");
                meds = db.getLowStockMedicinesObjects();
                break;
            default:
                meds = new ArrayList<>();
        }

        MedAdapter adapter = new MedAdapter(meds, this);
        recyclerView.setAdapter(adapter);
    }
}