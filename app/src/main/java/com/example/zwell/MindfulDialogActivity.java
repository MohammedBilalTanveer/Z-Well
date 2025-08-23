package com.example.zwell;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MindfulDialogActivity extends AppCompatActivity {
    private final String[] techniques = {
            "Close your eyes and inhale for 4s…",
            "Hold for 4s…",
            "Exhale for 6s…",
            "Repeat 3 times. Hydrate 💧"
    };
    private int idx = 0;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.dialog_mindful);

        TextView tip = findViewById(R.id.txtTip);
        Button next = findViewById(R.id.btnNext);
        tip.setText(techniques[idx]);

        next.setOnClickListener(v -> {
            idx = (idx + 1) % techniques.length;
            tip.setText(techniques[idx]);
        });
    }
}