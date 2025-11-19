package com.example.medicinetrackerandreminder;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medicinetrackerandreminder.database.DatabaseHelper;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etFirst, etLast, etDob, etPhone, etEmail;
    private MaterialButton btnSave;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = new DatabaseHelper(this);

        etFirst = findViewById(R.id.etFirst);
        etLast  = findViewById(R.id.etLast);
        etDob   = findViewById(R.id.etDob);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);


        etDob.setFocusable(false);
        etDob.setOnClickListener(v -> openDobPicker());

        btnSave.setOnClickListener(v -> {
            String f = etFirst.getText().toString().trim();
            String l = etLast.getText().toString().trim();
            String dob = etDob.getText().toString().trim();
            String ph = etPhone.getText().toString().trim();
            String em = etEmail.getText().toString().trim();

            if (f.isEmpty() || l.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            db.upsertSingleUser(f, l, dob, ph, em);
            Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void openDobPicker() {
        Calendar now = Calendar.getInstance();
        int y = now.get(Calendar.YEAR);
        int m = now.get(Calendar.MONTH);
        int d = now.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dlg = new DatePickerDialog(
                this,
                (view, year, month, day) ->
                        etDob.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d",
                                day, month + 1, year)),
                y, m, d);
        dlg.getDatePicker().setMaxDate(System.currentTimeMillis());
        dlg.show();
    }
}