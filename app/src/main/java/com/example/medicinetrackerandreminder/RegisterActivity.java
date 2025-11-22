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
        etLastName  = findViewById(R.id.etLastName);
        etDob       = findViewById(R.id.etDob);
        etPhone     = findViewById(R.id.etPhone);
        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);

        etDob.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.btnCreateAccount).setOnClickListener(v -> validateForm());
        findViewById(R.id.tvGoLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (v, year, mon, day) ->
                etDob.setText(String.format("%d/%d/%d", day, mon + 1, year)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void validateForm() {
        String f = etFirstName.getText().toString().trim();
        String l = etLastName.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String ph = etPhone.getText().toString().trim();
        String em = etEmail.getText().toString().trim();
        String pw = etPassword.getText().toString().trim();

        if (f.isEmpty() || l.isEmpty() || dob.isEmpty() || ph.isEmpty() || em.isEmpty() || pw.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(em).matches()) {
            Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (db.userExists(ph, em)) {
            Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = db.registerUser(f, l, dob, ph, em, pw);
        if (success) {
            Toast.makeText(this, "Registered! Please login", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }
}