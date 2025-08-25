package com.example.zwell;

import android.app.Notification;
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
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UsageStatsService extends Service {
    private static final String TAG = "UsageStatsService";
    private static final String CHANNEL_ID = "MindfulCoachChannel";
    private static final int FOREGROUND_NOTIFICATION_ID = 1001;
    private ScheduledExecutorService scheduler;
    private long lastSocialNotify = 0;
    private long lastStudyNotify = 0;

    // Full package names for social media and study apps
    private final Set<String> socialPackages = new HashSet<>(Arrays.asList(
            "com.instagram.android", // Instagram
            "com.facebook.katana", // Facebook
            "com.google.android.youtube", // YouTube
            "com.twitter.android", // Twitter/X
            "org.telegram.messenger" // Telegram
    ));

    private final Set<String> studyPackages = new HashSet<>(Arrays.asList(
            "com.google.android.apps.photos", // Google Photos
            "com.android.gallery3d", // Default Gallery
            "com.sec.android.gallery3d", // Samsung Gallery
            "com.adobe.reader", // Adobe Acrobat
            "com.google.android.apps.pdfviewer", // Google PDF Viewer
            "com.google.android.apps.docs" // Google Docs
    ));

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            createChannel();
            Notification notification = createForegroundNotification();
            startForeground(FOREGROUND_NOTIFICATION_ID, notification);
            // Schedule usage checks every 5 minutes
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(this::checkUsage, 0, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start foreground service: " + e.getMessage(), e);
            stopSelf(); // Stop service if foreground fails
        }
    }

    private Notification createForegroundNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Zwell Monitoring")
                .setContentText("Tracking app usage for mindful alerts")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void checkUsage() {
        try {
            UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            if (usm == null) {
                Log.e(TAG, "UsageStatsManager not available");
                return;
            }

            long now = System.currentTimeMillis();
            long windowStart = now - TimeUnit.MINUTES.toMillis(30); // 30-minute window
            UsageEvents events = usm.queryEvents(windowStart, now);
            if (events == null) {
                Log.e(TAG, "UsageEvents is null");
                return;
            }

            Map<String, Long> appUsageTimes = new HashMap<>();
            String currentPkg = null;
            long currentStart = 0;
            UsageEvents.Event event = new UsageEvents.Event();

            // Aggregate usage time for each app
            while (events.hasNextEvent()) {
                events.getNextEvent(event);
                String pkg = event.getPackageName();
                long time = event.getTimeStamp();
                int eventType = event.getEventType();

                if (eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    if (currentPkg != null) {
                        long duration = time - currentStart;
                        appUsageTimes.merge(currentPkg, duration, Long::sum);
                    }
                    currentPkg = pkg;
                    currentStart = time;
                } else if (eventType == UsageEvents.Event.ACTIVITY_PAUSED && currentPkg != null && currentPkg.equals(pkg)) {
                    long duration = time - currentStart;
                    appUsageTimes.merge(currentPkg, duration, Long::sum);
                    currentPkg = null;
                }
            }

            // Account for app still in foreground
            if (currentPkg != null) {
                long duration = now - currentStart;
                appUsageTimes.merge(currentPkg, duration, Long::sum);
            }

            // Calculate total time for social and study apps
            long socialTime = 0;
            long studyTime = 0;
            for (Map.Entry<String, Long> entry : appUsageTimes.entrySet()) {
                String pkg = entry.getKey();
                long duration = entry.getValue();
                if (socialPackages.contains(pkg)) {
                    socialTime += duration;
                }
                if (studyPackages.contains(pkg)) {
                    studyTime += duration;
                }
            }

            // Thresholds and cooldown
            long socialThreshold = TimeUnit.MINUTES.toMillis(15); // 15 minutes
            long studyThreshold = TimeUnit.MINUTES.toMillis(20); // 20 minutes
            long notifyCooldown = TimeUnit.MINUTES.toMillis(15); // 15-minute cooldown

            if (socialTime >= socialThreshold && now - lastSocialNotify >= notifyCooldown) {
                notifyCoach("You're consuming a lot of social media", "Consider taking a break to recharge and focus on something else.");
                lastSocialNotify = now;
            }

            if (studyTime >= studyThreshold && now - lastStudyNotify >= notifyCooldown) {
                notifyCoach("You've been studying for a while", "Take a 5-minute break to rest your eyes and mind.");
                lastStudyNotify = now;
            }

        } catch (SecurityException e) {
            Log.e(TAG, "Usage stats permission denied: " + e.getMessage(), e);
            // Optionally notify user to grant permission
            notifyCoach("Permission Required", "Please grant Usage Access to enable mindful alerts.");
        } catch (Exception e) {
            Log.e(TAG, "Error checking usage stats: " + e.getMessage(), e);
        }
    }

    private void notifyCoach(String title, String text) {
        try {
            Intent intent = new Intent(this, MindfulDialogActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pending = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setAutoCancel(true)
                    .setContentIntent(pending)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.notify((int) System.currentTimeMillis(), builder.build());
            } else {
                Log.e(TAG, "NotificationManager is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending notification: " + e.getMessage(), e);
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Mindful Coach",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Notifications for mindful usage alerts");
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (nm != null) {
                    nm.createNotificationChannel(channel);
                } else {
                    Log.e(TAG, "NotificationManager is null during channel creation");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating notification channel: " + e.getMessage(), e);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        try {
            if (scheduler != null) {
                scheduler.shutdownNow();
            }
            stopForeground(true);
        } catch (Exception e) {
            Log.e(TAG, "Error destroying service: " + e.getMessage(), e);
        }
        super.onDestroy();
    }
}
