package com.example.medicinetrackerandreminder;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.medicinetrackerandreminder.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying and editing the medicine list.
 * Includes Current Stock + Low Stock Alert + Edit/Delete actions.
 */
public class MedAdapter extends RecyclerView.Adapter<MedAdapter.ViewHolder> {

    private List<Medicine> medicines;
    private List<Medicine> allMedicines;
    private final Context context;

    // ✅  Remove those illegal lines that executed at class level

    public MedAdapter(List<Medicine> medicines, Context context) {
        this.medicines = medicines;
        this.allMedicines = new ArrayList<>(medicines);
        this.context = context;
    }

    public void setMedicines(List<Medicine> newList) {
        this.medicines = newList;
        this.allMedicines = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        query = query.toLowerCase();
        List<Medicine> filtered = new ArrayList<>();
        for (Medicine m : allMedicines) {
            if (m.getName().toLowerCase().contains(query)
                    || m.getDosage().toLowerCase().contains(query)
                    || m.getNotes().toLowerCase().contains(query)
                    || m.getType().toLowerCase().contains(query)) {
                filtered.add(m);
            }
        }
        medicines = filtered;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        Medicine med = medicines.get(pos);
        holder.tvMedName.setText(med.getName());
        holder.tvDosage.setText("Dosage: " + med.getDosage());
        holder.tvType.setText("Type: " + med.getType());
        holder.tvTimes.setText("Times: " +
                (med.getTimes().isEmpty() ? "Not set" : med.getTimes()));
        holder.tvNotes.setText(med.getNotes().isEmpty() ? "No notes" : med.getNotes());
        holder.tvStock.setText("Current Stock: " + med.getStock());

        DatabaseHelper db = new DatabaseHelper(context);
        android.database.Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT lowStockAlert FROM medicines WHERE name=?",
                new String[]{med.getName()});
        if (c.moveToFirst()) {
            holder.tvLowStock.setText("Low Stock Alert: " + c.getInt(0));
        }
        c.close();

        // --- Delete button (per user now) ---
        holder.btnDelete.setOnClickListener(v ->
                new AlertDialog.Builder(context)
                        .setTitle("Delete Medicine")
                        .setMessage("Are you sure you want to delete this medicine?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            long uid = SessionManager.get();
                            boolean deleted = db.deleteMedicineByName(med.getName(), uid);
                            if (deleted) {
                                medicines.remove(pos);
                                notifyItemRemoved(pos);
                                Toast.makeText(context, "Medicine deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show()
        );

        // --- Edit button ---
        holder.btnEdit.setOnClickListener(v -> showEditDialog(med, pos));
    }

    private void showEditDialog(Medicine med, int pos) {
        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_edit_medicine, null);

        EditText etName = dialogView.findViewById(R.id.etEditName);
        EditText etDosage = dialogView.findViewById(R.id.etEditDosage);
        EditText etType = dialogView.findViewById(R.id.etEditType);
        EditText etTimes = dialogView.findViewById(R.id.etEditTimes);
        EditText etNotes = dialogView.findViewById(R.id.etEditNotes);
        EditText etStock = dialogView.findViewById(R.id.etEditStock);
        EditText etLowStock = dialogView.findViewById(R.id.etEditLowStock);

        etName.setText(med.getName());
        etDosage.setText(med.getDosage());
        etType.setText(med.getType());
        etTimes.setText(med.getTimes());
        etNotes.setText(med.getNotes());
        etStock.setText(String.valueOf(med.getStock()));

        DatabaseHelper db = new DatabaseHelper(context);
        android.database.Cursor c = db.getReadableDatabase().rawQuery(
                "SELECT lowStockAlert FROM medicines WHERE name=?",
                new String[]{med.getName()});
        if (c.moveToFirst()) etLowStock.setText(String.valueOf(c.getInt(0)));
        c.close();

        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Edit Stocks / Notes")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    try {
                        int stock = Integer.parseInt(etStock.getText().toString());
                        int lowstk = Integer.parseInt(etLowStock.getText().toString());

                        android.content.ContentValues cv = new android.content.ContentValues();
                        cv.put("notes", etNotes.getText().toString());
                        cv.put("currentStock", stock);
                        cv.put("lowStockAlert", lowstk);

                        long uid = SessionManager.get();
                        int updated = db.getWritableDatabase()
                                .update("medicines", cv,
                                        "name=? AND userId=?",
                                        new String[]{med.getName(), String.valueOf(uid)});

                        if (updated > 0) {
                            Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show();
                            medicines.set(pos, new Medicine(
                                    med.getName(),
                                    med.getDosage(),
                                    etNotes.getText().toString(),
                                    stock,
                                    med.getType(),
                                    med.getTimes()));
                            notifyItemChanged(pos);
                        } else {
                            Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return medicines == null ? 0 : medicines.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedName, tvDosage, tvNotes, tvStock, tvType, tvTimes, tvLowStock;
        Button btnDelete, btnEdit;

        ViewHolder(View itemView) {
            super(itemView);
            tvMedName = itemView.findViewById(R.id.tvMedName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            tvStock = itemView.findViewById(R.id.tvStock);
            tvType = itemView.findViewById(R.id.tvType);
            tvTimes = itemView.findViewById(R.id.tvTimes);
            tvLowStock = itemView.findViewById(R.id.tvLowStock);
            btnDelete = itemView.findViewById(R.id.btnDeleteMed);
            btnEdit = itemView.findViewById(R.id.btnEditMed);
        }
    }
}