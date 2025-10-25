package com.example.medicinetrackerandreminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.medicinetrackerandreminder.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class MedAdapter extends RecyclerView.Adapter<MedAdapter.ViewHolder> {
    private List<Medicine> medicines;
    private List<Medicine> allMedicines;
    private Context context;

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

    public void filter(String query){
        query = query.toLowerCase();
        List<Medicine> filtered = new ArrayList<>();
        for(Medicine m : allMedicines){
            if(m.getName().toLowerCase().contains(query) ||
                    m.getDosage().toLowerCase().contains(query) ||
                    m.getNotes().toLowerCase().contains(query) ||
                    m.getType().toLowerCase().contains(query)){
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
        holder.tvTimes.setText("Times: " + (med.getTimes().isEmpty() ? "Not set" : med.getTimes()));
        holder.tvNotes.setText(med.getNotes().isEmpty()? "No notes" : med.getNotes());
        holder.tvStock.setText("Stock: " + med.getStock());

        // Delete directly
        holder.btnDelete.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Delete Medicine")
                    .setMessage("Are you sure you want to delete this medicine?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        DatabaseHelper db = new DatabaseHelper(context);
                        boolean deleted = db.deleteMedicineByName(med.getName());
                        if (deleted) {
                            medicines.remove(pos);
                            notifyItemRemoved(pos);
                            Toast.makeText(context, "Medicine deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return medicines == null ? 0 : medicines.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedName, tvDosage, tvNotes, tvStock, tvType, tvTimes;
        Button btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvMedName = itemView.findViewById(R.id.tvMedName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvNotes  = itemView.findViewById(R.id.tvNotes);
            tvStock  = itemView.findViewById(R.id.tvStock);
            tvType   = itemView.findViewById(R.id.tvType);
            tvTimes  = itemView.findViewById(R.id.tvTimes);
            btnDelete = itemView.findViewById(R.id.btnDeleteMed);
        }
    }
}