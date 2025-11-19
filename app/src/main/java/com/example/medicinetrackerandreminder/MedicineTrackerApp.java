package com.example.medicinetrackerandreminder;

import android.app.Application;
import android.content.SharedPreferences;

public class MedicineTrackerApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("darkMode", false);
        ThemeManager.apply(darkMode);
    }
}