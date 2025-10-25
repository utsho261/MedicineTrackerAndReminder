package com.example.medicinetrackerandreminder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medicinetrackerandreminder.database.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ProfileActivity extends AppCompatActivity {

    private SwitchMaterial swPush, swDark, swSound;
    private MaterialButton btnClear, btnSignOut;
    private DatabaseHelper db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Apply saved theme before showing UI
        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        ThemeManager.apply(prefs.getBoolean("darkMode", false));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = new DatabaseHelper(this);

        // ===== Switches =====
        swPush  = findViewById(R.id.swPush);
        swDark  = findViewById(R.id.swDark);
        swSound = findViewById(R.id.swSound);

        // Dark mode switch
        swDark.setChecked(prefs.getBoolean("darkMode", false));
        swDark.setOnCheckedChangeListener((b, checked) -> {
            prefs.edit().putBoolean("darkMode", checked).apply();
            ThemeManager.apply(checked);
        });

        // Other simple demo listeners
        CompoundButton.OnCheckedChangeListener listener =
                (btn, on) -> Toast.makeText(this,
                        btn.getText() + (on ? " enabled" : " disabled"),
                        Toast.LENGTH_SHORT).show();

        swPush.setOnCheckedChangeListener(listener);
        swSound.setOnCheckedChangeListener(listener);

        // ===== Buttons =====
        btnClear  = findViewById(R.id.btnClear);
        btnSignOut= findViewById(R.id.btnSignOut);

        btnClear.setOnClickListener(v ->
                Toast.makeText(this, "All data cleared", Toast.LENGTH_SHORT).show());

        btnSignOut.setOnClickListener(v -> {
            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        loadUserInfo();
    }

    private void loadUserInfo() {
        String id = getIntent().getStringExtra("userId");

        TextView tvName  = findViewById(R.id.tvFullName);
        TextView tvEmail = findViewById(R.id.tvEmail);
        TextView tvPhone = findViewById(R.id.tvPhone);
        TextView tvDob   = findViewById(R.id.tvDob);

        if (id == null || id.isEmpty()) {
            tvName.setText("Unknown user");
            tvEmail.setText("-");
            tvPhone.setText("-");
            tvDob.setText("-");
            return;
        }

        Cursor c = db.getUser(id);
        if (c != null && c.moveToFirst()) {
            tvName.setText(c.getString(0) + " " + c.getString(1));
            tvDob.setText(c.getString(2));
            tvPhone.setText(c.getString(3));
            tvEmail.setText(c.getString(4));
        } else {
            tvName.setText("User not found");
            tvEmail.setText(id);
            tvPhone.setText("-");
            tvDob.setText("-");
        }
        if (c != null) c.close();
    }
}