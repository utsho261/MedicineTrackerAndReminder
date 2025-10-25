package com.example.medicinetrackerandreminder;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medicinetrackerandreminder.database.DatabaseHelper;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {
    EditText etFirstName, etLastName, etDob, etPhone, etEmail, etPassword;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etDob = findViewById(R.id.etDob);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        etDob.setOnClickListener(v -> showDatePicker());

        findViewById(R.id.btnCreateAccount).setOnClickListener(v -> validateForm());
        findViewById(R.id.tvGoLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(this,
                (view, year, month, day) -> etDob.setText(day + "/" + (month + 1) + "/" + year),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dp.show();
    }

    private void validateForm() {
        String first = etFirstName.getText().toString().trim();
        String last = etLastName.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (first.isEmpty() || last.isEmpty() || dob.isEmpty() || phone.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!first.matches("^[A-Za-z]+$") || !last.matches("^[A-Za-z]+$")) {
            Toast.makeText(this, "Name must only have letters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!phone.matches("^(013|014|015|016|017|018|019)\\d{8}$")) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.length() < 6) {
            Toast.makeText(this, "Password at least 6 chars", Toast.LENGTH_SHORT).show();
            return;
        }
        if (db.userExists(phone, email)) {
            Toast.makeText(this, "User already exists!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = db.registerUser(first, last, dob, phone, email, pass);

        if (success) {
            Toast.makeText(this, "Account created! Please Login", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Registration failed!", Toast.LENGTH_SHORT).show();
        }
    }
}