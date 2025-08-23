package com.example.zwell;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ChatbotActivity extends AppCompatActivity {
    private AIService ai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        ai = new AIService(this); // constructor accepts Context

        EditText et = findViewById(R.id.etChatPrompt);
        Button send = findViewById(R.id.btnChatSend);
        TextView out = findViewById(R.id.tvChatOut);

        send.setOnClickListener(v -> {
            String prompt = "Be a friendly, supportive teen coach. " + et.getText().toString();
            out.setText("Thinking…");

            ai.generateText(prompt, new AIService.Callback() {
                @Override
                public void onResult(String text) {
                    // This runs on main thread thanks to AIService implementation
                    out.setText(text);
                }

                @Override
                public void onError(Exception e) {
                    out.setText("Error: " + e.getMessage());
                }
            });
        });
    }
}
