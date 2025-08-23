package com.example.zwell;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class VRWorldActivity extends AppCompatActivity {
    AIService ai;
    CalmWorldView world;
    TextView tvAffirm;

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_vrworld);
        ai = new AIService(this);

        world = findViewById(R.id.worldView);
        EditText et = findViewById(R.id.etMoodWorld);
        Button gen = findViewById(R.id.btnGenerateWorld);
        Button breathe = findViewById(R.id.btnBreathe);
        tvAffirm = findViewById(R.id.tvAffirm);

        gen.setOnClickListener(v -> {
            gen.setEnabled(false);
            ai.generateVRWorld(et.getText().toString().trim(), new AIService.Callback() {
                @Override public void onResult(String text) {
                    runOnUiThread(() -> {
                        try {
                            JSONObject o = new JSONObject(text);
                            int ct = Color.parseColor(o.getString("top"));
                            int cb = Color.parseColor(o.getString("bottom"));
                            float fog = (float)o.getDouble("fog");
                            int particles = o.getInt("particles");
                            world.configure(ct, cb, fog, particles);
                            tvAffirm.setText(o.optString("affirmation", "You are okay."));
                        } catch (Exception e) {
                            tvAffirm.setText("Parse error. Response:\n" + text);
                        }
                        gen.setEnabled(true);
                    });
                }

                @Override public void onError(Exception e) { runOnUiThread(() -> { tvAffirm.setText("Error: "+e.getMessage()); gen.setEnabled(true); }); }
            });
        });

        breathe.setOnClickListener(v -> new Thread(() -> {
            try {
                for (int i=0;i<3;i++) {
                    for (float b=1.0f;b<=1.4f;b+=0.02f){
                        float finalB = b;
                        world.post(() -> world.setBrightness(finalB)); Thread.sleep(80); }
                    Thread.sleep(400);
                    for (float b=1.4f;b>=1.0f;b-=0.02f){
                        float finalB = b;
                        world.post(() -> world.setBrightness(finalB)); Thread.sleep(80); }
                }
            } catch (InterruptedException ignored) {}
        }).start());
    }
}
