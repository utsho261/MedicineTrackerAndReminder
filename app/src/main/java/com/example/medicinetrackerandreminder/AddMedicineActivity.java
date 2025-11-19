package com.example.medicinetrackerandreminder;
import com.example.medicinetrackerandreminder.database.DatabaseHelper;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddMedicineActivity extends AppCompatActivity {

    private EditText etStartDate, etEndDate;
    private LinearLayout timesContainer;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        // User permission for notifications
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }

        db = new DatabaseHelper(this);

        // Dropdowns
        AutoCompleteTextView dropType = findViewById(R.id.dropMedicineType);
        AutoCompleteTextView dropFreq = findViewById(R.id.dropFrequency);

        dropType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                new String[]{
                        "Tablet", "Capsule", "Syrup/Liquid", "Injection",
                        "Drops", "Inhaler", "Cream/Ointment", "Patch", "Other"
                }));

        dropFreq.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                new String[]{"Once daily", "Twice daily", "Three times daily", "Weekly"}));

        // Date pickers
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        // Container for time inputs
        timesContainer = findViewById(R.id.timesContainer);

        // Buttons
        findViewById(R.id.btnAddTime).setOnClickListener(v -> addTime("08:00 AM"));
        findViewById(R.id.btnSubmit).setOnClickListener(v -> saveMedicine());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }

    // Show date picker
    private void showDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                target.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Add a time field dynamically
    private void addTime(String defaultTime) {
        View v = getLayoutInflater().inflate(R.layout.item_time_field, timesContainer, false);
        EditText et = v.findViewById(R.id.etTime);
        et.setText(defaultTime);
        et.setOnClickListener(vw -> pickTime(et));

        ImageView del = v.findViewById(R.id.btnRemoveTime);
        del.setOnClickListener(vw -> timesContainer.removeView(v));

        timesContainer.addView(v);
    }

    // Time picker dialog
    private void pickTime(EditText et) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, h, m) -> {
            String ampm = h >= 12 ? "PM" : "AM";
            int h12 = h % 12 == 0 ? 12 : h % 12;
            et.setText(String.format(Locale.getDefault(), "%02d:%02d %s", h12, m, ampm));
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
    }

    // Save medicine details
    private void saveMedicine() {
        String name = ((EditText) findViewById(R.id.etMedicineName)).getText().toString().trim();
        String type = ((AutoCompleteTextView) findViewById(R.id.dropMedicineType)).getText().toString().trim();
        String dosage = ((EditText) findViewById(R.id.etDosage)).getText().toString().trim();
        String startStr = etStartDate.getText().toString().trim();
        String endStr = etEndDate.getText().toString().trim();
        String start = DatabaseHelper.formatDate(startStr);
        String end = endStr.isEmpty() ? "" : DatabaseHelper.formatDate(endStr);
        String stockStr = ((EditText) findViewById(R.id.etCurrentStock)).getText().toString().trim();
        String lowStr = ((EditText) findViewById(R.id.etLowStock)).getText().toString().trim();
        String freq = ((AutoCompleteTextView) findViewById(R.id.dropFrequency)).getText().toString().trim();
        String notes = ((EditText) findViewById(R.id.etNotes)).getText().toString().trim();

        // === Basic validation ===
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter medicine name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (db.medicineExists(name)) {
            Toast.makeText(this, "Medicine with this name already exists!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dosage.isEmpty()) {
            Toast.makeText(this, "Please enter dosage", Toast.LENGTH_SHORT).show();
            return;
        }
        if (startStr.isEmpty()) {
            Toast.makeText(this, "Please select a start date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (freq.isEmpty()) {
            Toast.makeText(this, "Please select frequency", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate dates
        if (!endStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Calendar startDate = Calendar.getInstance();
                Calendar endDate = Calendar.getInstance();
                startDate.setTime(sdf.parse(startStr));
                endDate.setTime(sdf.parse(endStr));
                if (endDate.before(startDate)) {
                    Toast.makeText(this, "End date cannot be before start date", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Validate stock numbers
        int stock = 0, low = 0;
        try {
            stock = stockStr.isEmpty() ? 0 : Integer.parseInt(stockStr);
            low = lowStr.isEmpty() ? 0 : Integer.parseInt(lowStr);
            if (low > stock) {
                Toast.makeText(this, "Low stock alert cannot be greater than current stock", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Stock values must be numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        // Collect times
        List<String> times = new ArrayList<>();
        for (int i = 0; i < timesContainer.getChildCount(); i++) {
            EditText et = timesContainer.getChildAt(i).findViewById(R.id.etTime);
            if (!et.getText().toString().trim().isEmpty())
                times.add(et.getText().toString());
        }
        if (times.isEmpty()) {
            Toast.makeText(this, "Please add at least one time", Toast.LENGTH_SHORT).show();
            return;
        }

        // === Save medicine ===
        boolean ok = db.addMedicine(
                name, type, dosage, start, end,
                stock, low, freq, String.join(",", times), 0, notes // color = 0 (removed)
        );

        if (ok) {
            scheduleAlarms(name, times);
            Toast.makeText(this, "Medicine saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save medicine!", Toast.LENGTH_SHORT).show();
        }
    }

    // Schedule alarms for added medicine times
    private void scheduleAlarms(String medName, List<String> times) {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Calendar now = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();

        for (String t : times) {
            try {
                java.util.Date d = sdf.parse(t.trim());
                if (d == null) continue;
                cal.setTime(d);
                Calendar trigger = Calendar.getInstance();
                trigger.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
                trigger.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
                trigger.set(Calendar.SECOND, 0);
                if (trigger.before(now)) trigger.add(Calendar.DATE, 1);

                Intent i = new Intent(this,
                        com.example.medicinetrackerandreminder.receivers.MedicineAlarmReceiver.class);
                i.putExtra("medName", medName);
                i.putExtra("medTime", t);

                PendingIntent pi = PendingIntent.getBroadcast(
                        this,
                        (medName + t).hashCode(),
                        i,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.getTimeInMillis(), pi);
            } catch (Exception ignored) { }
        }
    }
}