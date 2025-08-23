package com.example.zwell;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UsageStatsService extends Service {
    private static final String CHANNEL_ID = "MindfulCoachChannel";
    private ScheduledExecutorService scheduler;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::checkUsage, 0, 5, TimeUnit.MINUTES);
    }

    private void checkUsage() {
        try {
            UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long now = System.currentTimeMillis();
            UsageEvents events = usm.queryEvents(now - 10*60*1000L, now);

            boolean sawSocial = false, sawStudy = false;
            UsageEvents.Event e = new UsageEvents.Event();
            while (events.hasNextEvent()) {
                events.getNextEvent(e);
                if (e.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                    String pkg = e.getPackageName() == null ? "" : e.getPackageName().toLowerCase();
                    if (pkg.contains("instagram") || pkg.contains("facebook") || pkg.contains("youtube")) sawSocial = true;
                    if (pkg.contains("pdf") || pkg.contains("docs") || pkg.contains("gallery")) sawStudy = true;
                }
            }

            if (sawSocial) notifyCoach("Too much social media?", "Try 3 deep breaths.");
            else if (sawStudy) notifyCoach("Long study session?", "Take a 5-min mindful break.");

        } catch (Exception ignored) {}
    }

    private void notifyCoach(String title, String text) {
        Intent intent = new Intent(this, MindfulDialogActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pending = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(pending)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int) System.currentTimeMillis(), b.build());
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel(CHANNEL_ID, "Mindful Coach", NotificationManager.IMPORTANCE_HIGH);
            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(c);
        }
    }

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }
    @Override public void onDestroy() { if (scheduler != null) scheduler.shutdownNow(); super.onDestroy(); }
}
