package com.example.zwell;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_home);

        // Top AppBar
        MaterialToolbar bar = findViewById(R.id.topAppBar);
        bar.setNavigationOnClickListener(v ->
                Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show()
        );
        bar.setOnMenuItemClickListener(this::onMenuClick);

        // Feature cards → activities
        setCardClick(R.id.cardEmotion, EmotionToArtActivity.class);
        setCardClick(R.id.cardWhatIf, WhatIfActivity.class);
        setCardClick(R.id.cardStory, StoryActivity.class);
        setCardClick(R.id.cardMusic, MusicActivity.class);
        // Alert card opens Usage Access so the app can monitor for mindful nudges
        findViewById(R.id.cardAlert).setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        );
        setCardClick(R.id.cardVR, VRWorldActivity.class);

        // CTA buttons
        View btnLogin = findViewById(R.id.btnLogin);
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        }
        View btnSignup = findViewById(R.id.btnSignup);
        if (btnSignup != null) {
            btnSignup.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
        }

        // Chatbot FAB
        FloatingActionButton fab = findViewById(R.id.fabChat);
        if (fab != null) {
            fab.setOnClickListener(v -> startActivity(new Intent(this, ChatbotActivity.class)));
        }

        // Start service if usage access already allowed
        if (hasUsageAccess()) {
            startService(new Intent(this, UsageStatsService.class));
        }

        // Animate cards on first draw
        animateCards();
    }

    private void setCardClick(int id, Class<?> activity) {
        View v = findViewById(id);
        if (v != null) {
            v.setOnClickListener(x -> {
                startActivity(new Intent(this, activity));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    private boolean onMenuClick(@NonNull MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_settings) {
            startActivity(new Intent(this, AccountActivity.class));
        } else if (i == R.id.action_signout) {
            getSharedPreferences("auth", MODE_PRIVATE).edit().putBoolean("logged", false).apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        return true;
    }

    private boolean hasUsageAccess() {
        try {
            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    getPackageName()
            );
            return mode == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            return false;
        }
    }

    private void animateCards() {
        LinearLayout container = findViewById(R.id.cardContainer);
        if (container == null) return;
        final int count = container.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = container.getChildAt(i);
            child.setAlpha(0f);
            child.setTranslationY(24f);
            child.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(i * 70L)
                    .setDuration(220L)
                    .start();
        }
    }
}