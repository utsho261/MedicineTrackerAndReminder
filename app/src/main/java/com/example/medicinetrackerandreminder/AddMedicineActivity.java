package com.example.medicinetrackerandreminder;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medicinetrackerandreminder.database.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddMedicineActivity extends AppCompatActivity {

    private EditText etStartDate, etEndDate;
    private LinearLayout timesContainer;
    private GridLayout colorGrid;
    private int selectedColor = Color.RED; // default
    private View selectedColorView;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        db = new DatabaseHelper(this);

        // Medicine type dropdown
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

        // Times container
        timesContainer = findViewById(R.id.timesContainer);

        // Color grid
        colorGrid = findViewById(R.id.colorGrid);
        setupColors();

        // Buttons
        findViewById(R.id.btnAddTime).setOnClickListener(v -> addTime("08:00 AM"));
        findViewById(R.id.btnSubmit).setOnClickListener(v -> saveMedicine());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }

    private void showDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                target.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Setup multiple color choices in a nice grid
    private void setupColors() {
        int[] colors = {
                Color.RED, Color.parseColor("#FF5722"), Color.parseColor("#FF9800"),
                Color.YELLOW, Color.parseColor("#8BC34A"), Color.GREEN,
                Color.CYAN, Color.BLUE, Color.parseColor("#3F51B5"),
                Color.MAGENTA, Color.parseColor("#9C27B0"), Color.parseColor("#E91E63")
        };

        for (int c : colors) {
            View circle = new View(this);
            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width = 90;
            p.height = 90;
            p.setMargins(12, 12, 12, 12);
            circle.setLayoutParams(p);
            circle.setBackgroundResource(R.drawable.color_circle);
            circle.getBackground().setTint(c);
            circle.setOnClickListener(v -> {
                if (selectedColorView != null) {
                    selectedColorView.setBackgroundResource(R.drawable.color_circle);
                    selectedColorView.getBackground().setTint(((ColorDrawable) selectedColorView.getBackground()).getColor());
                }
                selectedColor = c;
                v.setBackgroundResource(R.drawable.color_circle_selected);
                v.getBackground().setTint(c);
                selectedColorView = v;
            });
            colorGrid.addView(circle);
        }
    }

    private void addTime(String defaultTime) {
        View v = getLayoutInflater().inflate(R.layout.item_time_field, timesContainer, false);
        EditText et = v.findViewById(R.id.etTime);
        et.setText(defaultTime);

        et.setOnClickListener(vw -> pickTime(et));

        ImageView del = v.findViewById(R.id.btnRemoveTime);
        del.setOnClickListener(vw -> timesContainer.removeView(v));

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

        // ==== VALIDATION ====
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

        // Date validation: End >= Start
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

        // ==== Collect times ====
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

        boolean ok = db.addMedicine(
                name, type, dosage, start, end, stock, low,
                freq, String.join(",", times), selectedColor, notes
        );

        if (ok) {
            Toast.makeText(this, "Medicine saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save medicine!", Toast.LENGTH_SHORT).show();
        }
    }
}