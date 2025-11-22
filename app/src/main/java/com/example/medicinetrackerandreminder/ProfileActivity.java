package com.example.medicinetrackerandreminder;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medicinetrackerandreminder.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ProfileActivity extends AppCompatActivity {

    private SwitchMaterial swDark;
    private MaterialButton btnEdit, btnClear;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    long uid = SessionManager.get();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        ThemeManager.apply(prefs.getBoolean("darkMode", false));

        setContentView(R.layout.activity_profile);

        db = new DatabaseHelper(this);
        swDark = findViewById(R.id.swDark);
        btnEdit = findViewById(R.id.btnEditProfile);
        btnClear = findViewById(R.id.btnClear);

        // ---- Dark mode toggle ----
        boolean current = prefs.getBoolean("darkMode", false);
        swDark.setChecked(current);
        swDark.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked != current) {
                prefs.edit().putBoolean("darkMode", isChecked).apply();
                ThemeManager.apply(isChecked);
            }
        });

        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            prefs.edit().remove("loggedIn").remove("userId").apply();
            SessionManager.set(-1);
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        // Clear all data with confirmation
        btnClear.setOnClickListener(v -> showConfirmDialog());
        btnEdit.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        loadUserInfo();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                return true;
            } else if (id == R.id.nav_calendar) {
                startActivity(new Intent(this, CalendarActivity.class));
                return true;
            } else if (id == R.id.nav_meds) {
                startActivity(new Intent(this, MyMedsActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to clear all data?")
                .setPositiveButton("Yes", (DialogInterface dialog, int which) -> {
                    SQLiteDatabase database = db.getWritableDatabase();
                    database.delete("users", null, null);
                    database.delete("medicines", null, null);
                    database.delete("medicine_status", null, null);
                    Toast.makeText(this, "All data cleared!", Toast.LENGTH_SHORT).show();
                    recreate();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadUserInfo() {
        TextView tvName = findViewById(R.id.tvFullName);
        TextView tvEmail = findViewById(R.id.tvEmail);
        TextView tvPhone = findViewById(R.id.tvPhone);
        TextView tvDob = findViewById(R.id.tvDob);

        try (var c = db.getSingleUserCursor(uid)) {
            if (c != null && c.moveToFirst()) {
                tvName.setText(c.getString(0) + " " + c.getString(1));
                tvDob.setText(c.getString(2));
                tvPhone.setText(c.getString(3));
                tvEmail.setText(c.getString(4));
            } else {
                tvName.setText("No profile yet");
                tvEmail.setText("-");
                tvPhone.setText("-");
                tvDob.setText("-");
            }
        }
    }
}