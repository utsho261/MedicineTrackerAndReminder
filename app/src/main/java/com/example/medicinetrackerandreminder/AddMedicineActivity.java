package com.example.medicinetrackerandreminder;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medicinetrackerandreminder.database.DatabaseHelper;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddMedicineActivity extends AppCompatActivity {

    private EditText etStartDate, etEndDate;
    private LinearLayout timesContainer;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
        }

        db = new DatabaseHelper(this);

        AutoCompleteTextView dropType = findViewById(R.id.dropMedicineType);
        AutoCompleteTextView dropFreq = findViewById(R.id.dropFrequency);

        dropType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                new String[]{"Tablet","Capsule","Syrup/Liquid","Injection",
                        "Drops","Inhaler","Cream/Ointment","Patch","Other"}));
        dropFreq.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                new String[]{"Once daily","Twice daily","Three times daily","Weekly"}));

        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        timesContainer = findViewById(R.id.timesContainer);
        findViewById(R.id.btnAddTime).setOnClickListener(v -> addTime("08:00 AM"));
        findViewById(R.id.btnSubmit).setOnClickListener(v -> saveMedicine());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }

    private void showDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, y, m, d) ->
                        target.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void addTime(String def) {
        View v = getLayoutInflater().inflate(R.layout.item_time_field, timesContainer, false);
        EditText et = v.findViewById(R.id.etTime);
        et.setText(def);
        et.setOnClickListener(vw -> pickTime(et));
        v.findViewById(R.id.btnRemoveTime).setOnClickListener(vw -> timesContainer.removeView(v));
        timesContainer.addView(v);
    }

    private void pickTime(EditText et) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, h, m) -> {
            String ampm = h >= 12 ? "PM" : "AM";
            int h12 = h % 12 == 0 ? 12 : h % 12;
            et.setText(String.format(Locale.getDefault(), "%02d:%02d %s", h12, m, ampm));
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
    }

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

        if (name.isEmpty()) { Toast.makeText(this, "Enter medicine name", Toast.LENGTH_SHORT).show(); return; }

        long uid = SessionManager.get();
        if (db.medicineExists(name, uid)) {
            Toast.makeText(this, "Medicine with this name already exists!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dosage.isEmpty() || startStr.isEmpty() || freq.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show(); return;
        }

        int stock = stockStr.isEmpty() ? 0 : Integer.parseInt(stockStr);
        int low = lowStr.isEmpty() ? 0 : Integer.parseInt(lowStr);

        List<String> times = new ArrayList<>();
        for (int i = 0; i < timesContainer.getChildCount(); i++) {
            EditText et = timesContainer.getChildAt(i).findViewById(R.id.etTime);
            if (!et.getText().toString().trim().isEmpty())
                times.add(et.getText().toString());
        }
        if (times.isEmpty()) { Toast.makeText(this,"Add at least one time",Toast.LENGTH_SHORT).show(); return; }

        boolean ok = db.addMedicine(name, type, dosage, start, end, stock, low,
                freq, String.join(",", times), 0, notes, uid);

        if (ok) {
            scheduleAlarms(name, times);
            Toast.makeText(this, "Medicine saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else Toast.makeText(this, "Failed to save medicine!", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleAlarms(String medName, List<String> times) {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Calendar now = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();

        for (String t : times) {
            try {
                Date d = sdf.parse(t.trim());
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
                        this, (medName + t).hashCode(), i,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.getTimeInMillis(), pi);
            } catch (Exception ignored) {}
        }
    }
}