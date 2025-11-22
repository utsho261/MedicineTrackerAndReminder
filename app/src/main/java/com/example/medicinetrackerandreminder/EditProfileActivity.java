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

/**
 * Edit profile screen
 * - auto‑loads user info from registration
 * - allows user to modify and save
 */
public class EditProfileActivity extends AppCompatActivity {

    private EditText etFirst, etLast, etDob, etPhone, etEmail;
    private MaterialButton btnSave;
    private DatabaseHelper db;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = new DatabaseHelper(this);
        userId = SessionManager.get(); // current logged‑in user ID

        etFirst = findViewById(R.id.etFirst);
        etLast  = findViewById(R.id.etLast);
        etDob   = findViewById(R.id.etDob);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);

        etDob.setFocusable(false);
        etDob.setOnClickListener(v -> openDobPicker());

        // === Auto‑fill user info when activity opens ===
        loadCurrentUser();

        // === Save updates ===
        btnSave.setOnClickListener(v -> saveChanges());
    }

    /** Fetch current user's data from DB and display in fields. */
    private void loadCurrentUser() {
        try (var cursor = db.getReadableDatabase().rawQuery(
                "SELECT firstName, lastName, dob, phone, email FROM users WHERE id=? LIMIT 1",
                new String[]{String.valueOf(userId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                etFirst.setText(cursor.getString(0));
                etLast.setText(cursor.getString(1));
                etDob.setText(cursor.getString(2));
                etPhone.setText(cursor.getString(3));
                etEmail.setText(cursor.getString(4));
            } else {
                Toast.makeText(this, "No user found to edit", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Save updated info back to database. */
    private void saveChanges() {
        String f   = etFirst.getText().toString().trim();
        String l   = etLast.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String ph  = etPhone.getText().toString().trim();
        String em  = etEmail.getText().toString().trim();

        if (f.isEmpty() || l.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // update only this logged‑in user's record
        var dbWritable = db.getWritableDatabase();
        android.content.ContentValues cv = new android.content.ContentValues();
        cv.put("firstName", f);
        cv.put("lastName", l);
        cv.put("dob", dob);
        cv.put("phone", ph);
        cv.put("email", em);

        int rows = dbWritable.update("users", cv, "id=?", new String[]{String.valueOf(userId)});
        if (rows > 0) {
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }

    /** Opens date picker for DOB field. */
    private void openDobPicker() {
        Calendar now = Calendar.getInstance();
        int y = now.get(Calendar.YEAR);
        int m = now.get(Calendar.MONTH);
        int d = now.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dlg = new DatePickerDialog(
                this,
                (view, year, month, day) ->
                        etDob.setText(String.format(Locale.getDefault(),
                                "%02d/%02d/%04d", day, month + 1, year)),
                y, m, d
        );
        dlg.getDatePicker().setMaxDate(System.currentTimeMillis());
        dlg.show();
    }
}