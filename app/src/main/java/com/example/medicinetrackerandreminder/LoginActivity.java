package com.example.medicinetrackerandreminder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medicinetrackerandreminder.database.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    EditText etEmailPhone, etPassword;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        ThemeManager.apply(prefs.getBoolean("darkMode", false));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);
        etEmailPhone = findViewById(R.id.etLoginEmailPhone);
        etPassword = findViewById(R.id.etLoginPassword);

        findViewById(R.id.btnSignIn).setOnClickListener(v -> login());
        findViewById(R.id.tvGoRegister).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    @Override protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("loggedIn", false)) {
            long uid = prefs.getLong("userId", -1);
            if (uid != -1) {
                SessionManager.set(uid);
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            }
        }
    }

    private void login() {
        String id = etEmailPhone.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (id.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (db.login(id, pass)) {
            long uid = db.getUserId(id);
            SessionManager.set(uid);

            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            prefs.edit().putBoolean("loggedIn", true)
                    .putLong("userId", uid).apply();

            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }
}