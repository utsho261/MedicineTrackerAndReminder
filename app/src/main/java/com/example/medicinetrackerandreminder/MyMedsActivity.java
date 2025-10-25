package com.example.medicinetrackerandreminder;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicinetrackerandreminder.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MyMedsActivity extends AppCompatActivity {
    DatabaseHelper db;
    RecyclerView recyclerView;
    MedAdapter adapter;
    EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_meds);

        db = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerViewMeds);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MedAdapter(db.getAllMedicineObjects(), this);
        recyclerView.setAdapter(adapter);

        etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                adapter.filter(s.toString());
            }
        });

        findViewById(R.id.btnAddMedicine).setOnClickListener(v ->
                startActivity(new Intent(this, AddMedicineActivity.class)));

        findViewById(R.id.btnAddFirstMed).setOnClickListener(v ->
                startActivity(new Intent(this, AddMedicineActivity.class)));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(MyMedsActivity.this, DashboardActivity.class));
                return true;
            } else if (id == R.id.nav_calendar) {
                startActivity(new Intent(MyMedsActivity.this, CalendarActivity.class));
                return true;
            } else if (id == R.id.nav_meds) {
                return true;
            }
            return false;
        });
        bottomNav.setSelectedItemId(R.id.nav_meds);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.setMedicines(db.getAllMedicineObjects());
    }
}