package com.example.zwell;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private EditText etChatPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Top AppBar
        MaterialToolbar bar = findViewById(R.id.includeTopBar); // <-- fixed
        if (bar != null) {
            bar.setNavigationOnClickListener(v ->
                    Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show()
            );
            bar.setOnMenuItemClickListener(this::onMenuClick);
        }

        // Feature cards
        setCardClick(R.id.cardEmotion, EmotionToArtActivity.class);
        setCardClick(R.id.cardWhatIf, WhatIfActivity.class);
        setCardClick(R.id.cardStory, StoryActivity.class);
        setCardClick(R.id.cardMusic, MusicActivity.class);
        setCardClick(R.id.cardVR, VRWorldActivity.class);

        // Mindful Alerts
        MaterialCardView cardAlert = findViewById(R.id.cardAlert); // <-- fixed
        Button btnGrantAccess = findViewById(R.id.btnGrantAccess);
        boolean hasAccess = hasUsageAccess();
        if (cardAlert != null) {
            if (hasAccess) {
                cardAlert.setVisibility(View.GONE);
            } else {
                cardAlert.setOnClickListener(v ->
                        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                );
                if (btnGrantAccess != null) {
                    btnGrantAccess.setOnClickListener(v ->
                            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    );
                }
            }
        }

        // Chatbot input
        etChatPrompt = findViewById(R.id.etChatPrompt);
        ImageButton btnSendPrompt = findViewById(R.id.btnSendPrompt);
        if (btnSendPrompt != null && etChatPrompt != null) {
            btnSendPrompt.setOnClickListener(v -> {
                String prompt = etChatPrompt.getText().toString().trim();
                if (!prompt.isEmpty()) {
                    Intent intent = new Intent(this, ChatbotActivity.class);
                    intent.putExtra("prompt", prompt);
                    startActivity(intent);
                    etChatPrompt.setText("");
                } else {
                    Toast.makeText(this, "Enter a prompt", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Start service if allowed
        if (hasAccess) {
            startService(new Intent(this, UsageStatsService.class));
        }

        animateCards();
    }

    private void setCardClick(int id, Class<?> activity) {
        View v = findViewById(id);
        if (v != null) {
            v.setOnClickListener(x -> {
                AnimatorSet set = new AnimatorSet();
                ObjectAnimator sx = ObjectAnimator.ofFloat(v, "scaleX", 1f, 0.95f, 1f);
                ObjectAnimator sy = ObjectAnimator.ofFloat(v, "scaleY", 1f, 0.95f, 1f);
                set.playTogether(sx, sy);
                set.setDuration(150);
                set.start();

                startActivity(new Intent(this, activity));
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            });
        }
    }

    private boolean onMenuClick(@NonNull MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_settings) {
            startActivity(new Intent(this, AccountActivity.class));
            return true;
        } else if (i == R.id.action_signout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
            return true;
        }
        return false;
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
        int count = container.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = container.getChildAt(i);
            if (child.getVisibility() == View.GONE) continue;
            child.setAlpha(0f);
            child.setTranslationY(50f);
            child.setScaleX(0.9f);
            child.setScaleY(0.9f);
            child.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(i * 100L)
                    .setDuration(300L)
                    .start();
        }
    }
}
