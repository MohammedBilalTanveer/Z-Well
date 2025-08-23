package com.example.zwell;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class EmotionToArtActivity extends AppCompatActivity {
    AIService ai;
    WebView web;

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_emotion_to_art);
        ai = new AIService(this);

        EditText et = findViewById(R.id.etEmotion);
        Button btn = findViewById(R.id.btnRender);
        web = findViewById(R.id.webSvg);

        WebSettings ws = web.getSettings();
        ws.setJavaScriptEnabled(true);

        btn.setOnClickListener(v -> {
            btn.setEnabled(false);
            String emotion = et.getText().toString().trim();
            ai.generateEmotionArt(emotion, new AIService.Callback() {
                @Override public void onResult(String svg) {
                    runOnUiThread(() -> {
                        String html = "<html><body style='margin:0;background:#111'>" + svg + "</body></html>";
                        web.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
                        btn.setEnabled(true);
                    });
                }

                @Override public void onError(Exception e) {
                    runOnUiThread(() -> { web.loadData("Error: "+e.getMessage(),"text/plain","utf-8"); btn.setEnabled(true); });
                }
            });
        });
    }
}