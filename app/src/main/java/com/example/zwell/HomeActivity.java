package com.example.zwell;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_home);

        MaterialToolbar bar = findViewById(R.id.topAppBar);
        bar.setOnMenuItemClickListener(this::onMenuClick);

        LinearLayout accountBtn = findViewById(R.id.accountBtn);
        accountBtn.setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));

        findViewById(R.id.cardEmotion).setOnClickListener(v -> startActivity(new Intent(this, EmotionToArtActivity.class)));
        findViewById(R.id.cardWhatIf).setOnClickListener(v -> startActivity(new Intent(this, WhatIfActivity.class)));
        findViewById(R.id.cardStory).setOnClickListener(v -> startActivity(new Intent(this, StoryActivity.class)));
        findViewById(R.id.cardMusic).setOnClickListener(v -> startActivity(new Intent(this, MusicActivity.class)));
        findViewById(R.id.cardVR).setOnClickListener(v -> startActivity(new Intent(this, VRWorldActivity.class)));

        Button mindfulCoach = findViewById(R.id.btnMindfulCoach);
        mindfulCoach.setOnClickListener(v -> startActivity(new Intent(this, MindfulDialogActivity.class)));

        Button usageBtn = findViewById(R.id.btnUsageAccess);
        usageBtn.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));

        FloatingActionButton fab = findViewById(R.id.fabChat);
        fab.setOnClickListener(v -> startActivity(new Intent(this, ChatbotActivity.class)));

        // If usage access already allowed, start service
        if (hasUsageAccess()) startService(new Intent(this, UsageStatsService.class));
    }

    private boolean onMenuClick(@NonNull MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_what_if) startActivity(new Intent(this, WhatIfActivity.class));
        else if (i == R.id.action_story) startActivity(new Intent(this, StoryActivity.class));
        else if (i == R.id.action_music) startActivity(new Intent(this, MusicActivity.class));
        else if (i == R.id.action_vr) startActivity(new Intent(this, VRWorldActivity.class));
        else if (i == R.id.action_art) startActivity(new Intent(this, EmotionToArtActivity.class));
        else if (i == R.id.action_signout) {
            getSharedPreferences("auth", MODE_PRIVATE).edit().putBoolean("logged", false).apply();
            startActivity(new Intent(this, LoginActivity.class)); finish();
        }
        return true;
    }

    private boolean hasUsageAccess() {
        try {
            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), getPackageName());
            return mode == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            return false;
        }
    }
}