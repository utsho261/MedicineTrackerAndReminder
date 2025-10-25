package com.example.medicinetrackerandreminder;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    public static void apply(boolean darkMode) {
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}