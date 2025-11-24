package com.example.medicinetrackerandreminder.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.example.medicinetrackerandreminder.database.DatabaseHelper;
import com.example.medicinetrackerandreminder.SessionManager;

/**
 * Handles user actions from the medicine reminder notification.
 * When "Take" or "Skip" is pressed, record it and close the notification.
 */
public class MedicineActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String medName = intent.getStringExtra("medName");
        String medTime = intent.getStringExtra("medTime");
        String date = intent.getStringExtra("date");
        String action = intent.getAction();

        DatabaseHelper db = new DatabaseHelper(context);
        long uid = SessionManager.get();

        if ("ACTION_TAKE".equals(action)) {
            db.markMedicineTaken(medName, medTime, date, uid);
            Toast.makeText(context, "Marked " + medName + " as taken", Toast.LENGTH_SHORT).show();
        } else if ("ACTION_SKIP".equals(action)) {
            db.markMedicineMissed(medName, medTime, date, uid);
            Toast.makeText(context, medName + " marked as missed", Toast.LENGTH_SHORT).show();
        }

        // === Hide the notification ===
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            int id = (medName + medTime).hashCode();
            nm.cancel(id);
        }

        // === Notify Dashboard to refresh ===
        Intent refreshIntent = new Intent("com.example.medicinetrackerandreminder.REFRESH_DASHBOARD");
        context.sendBroadcast(refreshIntent);
    }
}