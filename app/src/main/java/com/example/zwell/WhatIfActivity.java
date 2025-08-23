package com.example.zwell;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class WhatIfActivity extends AppCompatActivity {
    AIService ai;
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_what_if);
        ai = new AIService(this);

        EditText et = findViewById(R.id.etWhatIf);
        Button btn = findViewById(R.id.btnGenerate);
        TextView out = findViewById(R.id.tvResult);

        btn.setOnClickListener(v -> {
            btn.setEnabled(false);
            out.setText(R.string.generating_alternatives);
            ai.generateWhatIf(et.getText().toString().trim(), new AIService.Callback() {
                @Override public void onResult(String text) { runOnUiThread(() -> { out.setText(text); btn.setEnabled(true); }); }

                @Override public void onError(Exception e) { runOnUiThread(() -> { out.setText("Error: "+e.getMessage()); btn.setEnabled(true); }); }
            });
        });
    }
}
