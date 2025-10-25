package com.example.medicinetrackerandreminder;

public class Medicine {
    private String name;
    private String dosage;
    private String notes;
    private int stock;
    private String type;
    private String times;

    public Medicine(String name, String dosage, String notes, int stock, String type, String times){
        this.name = name;
        this.dosage = dosage;
        this.notes = notes;
        this.stock = stock;
        this.type = type;
        this.times = times;
    }

    public String getName() { return name; }
    public String getDosage() { return dosage; }
    public String getNotes() { return notes; }
    public int getStock() { return stock; }
    public String getType() { return type; }
    public String getTimes() { return times; }
}