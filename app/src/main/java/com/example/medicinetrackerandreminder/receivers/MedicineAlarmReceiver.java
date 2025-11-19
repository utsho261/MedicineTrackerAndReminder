package com.example.medicinetrackerandreminder.receivers;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.medicinetrackerandreminder.R;
import com.example.medicinetrackerandreminder.database.DatabaseHelper;

/**
 * Triggered by the AlarmManager for each scheduled medicine time.
 * Builds a reminder notification with “Take” and “Skip” actions.
 */
public class MedicineAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String medName = intent.getStringExtra("medName");
        String medTime = intent.getStringExtra("medTime");

        Log.d("MedicineAlarmReceiver", "Received alarm for " + medName + " at " + medTime);
        Toast.makeText(context, "Alarm for " + medName, Toast.LENGTH_SHORT).show();

        createNotification(context, medName, medTime);
    }

    /** Create reminder notification unless already resolved. */
    private void createNotification(Context context, String medName, String medTime) {
        String channelId = "medicine_channel";
        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // --- Notification Channel (Android 8.0+) ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Medicine Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Reminders for scheduled medicines");
            nm.createNotificationChannel(channel);
        }

        DatabaseHelper db = new DatabaseHelper(context);
        String todayDate = db.todayDate();

        // --- Skip if already marked taken or missed ---
        String result = getInstanceResult(db, medName, medTime, todayDate);
        if ("taken".equals(result) || "missed".equals(result)) {
            Log.d("MedicineAlarmReceiver", "Already " + result + ", no new notification.");
            cancelAlarmIfAny(context, medName, medTime); // make sure alarm also cleared
            return;
        }

        // --- Intent for Take action ---
        Intent takeIntent = new Intent(context, MedicineActionReceiver.class);
        takeIntent.setAction("ACTION_TAKE");
        takeIntent.putExtra("medName", medName);
        takeIntent.putExtra("medTime", medTime);
        takeIntent.putExtra("date", todayDate);
        PendingIntent piTake = PendingIntent.getBroadcast(
                context,
                (medName + medTime + "take").hashCode(),
                takeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // --- Intent for Skip action ---
        Intent skipIntent = new Intent(context, MedicineActionReceiver.class);
        skipIntent.setAction("ACTION_SKIP");
        skipIntent.putExtra("medName", medName);
        skipIntent.putExtra("medTime", medTime);
        skipIntent.putExtra("date", todayDate);
        PendingIntent piSkip = PendingIntent.getBroadcast(
                context,
                (medName + medTime + "skip").hashCode(),
                skipIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // --- Build notification ---
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_medication)
                .setContentTitle("Medicine Reminder")
                .setContentText("It’s time to take " + medName + " (" + medTime + ")")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_check, "Take", piTake)
                .addAction(R.drawable.ic_close, "Skip", piSkip);

        nm.notify((medName + medTime).hashCode(), builder.build());
        Log.d("MedicineAlarmReceiver", "Notification created for " + medName + " @ " + medTime);
    }

    /** Get "taken"/"missed"/null for this instance. */
    private String getInstanceResult(DatabaseHelper db, String name, String time, String date) {
        SQLiteDatabase rdb = db.getReadableDatabase();
        Cursor c = rdb.rawQuery(
                "SELECT result FROM medicine_status WHERE name=? AND time=? AND date=? " +
                        "ORDER BY id DESC LIMIT 1",
                new String[]{name, time, date});
        String result = null;
        if (c.moveToFirst()) result = c.getString(0);
        c.close();
        return result;
    }

    /** Cancel the exact alarm if any still scheduled (safety). */
    private void cancelAlarmIfAny(Context context, String medName, String medTime) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        Intent i = new Intent(context, MedicineAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                context, (medName + medTime).hashCode(),
                i, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pi != null) {
            am.cancel(pi);
            pi.cancel();
        }
    }
}