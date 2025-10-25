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
        // Apply saved theme before view inflation
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        ThemeManager.apply(prefs.getBoolean("darkMode", false));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);
        etEmailPhone = findViewById(R.id.etLoginEmailPhone);
        etPassword   = findViewById(R.id.etLoginPassword);

        findViewById(R.id.btnSignIn).setOnClickListener(v -> login());
        findViewById(R.id.tvGoRegister).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }

    private void login() {
        String id   = etEmailPhone.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (id.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.length() < 6) {
            Toast.makeText(this, "Password must be at least 6", Toast.LENGTH_SHORT).show();
            return;
        }

        if (db.login(id, pass)) {
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

            // ✅ Send phone/email to dashboard
            Intent dash = new Intent(this, DashboardActivity.class);
            dash.putExtra("userId", id);
            startActivity(dash);
            finish();

        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }
}