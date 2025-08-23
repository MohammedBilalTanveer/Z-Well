package com.example.zwell;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class AccountActivity extends AppCompatActivity {
    EditText etChangeUsername, etChangePassword;
    Button btnUpdate, btnSignout;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_account);

        etChangeUsername = findViewById(R.id.etChangeUsername);
        etChangePassword = findViewById(R.id.etChangePassword);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnSignout = findViewById(R.id.btnSignout);

        btnUpdate.setOnClickListener(v -> {
            if (!etChangeUsername.getText().toString().isEmpty()) {
                getSharedPreferences("auth", MODE_PRIVATE)
                        .edit().putString("username", etChangeUsername.getText().toString()).apply();
            }
            if (!etChangePassword.getText().toString().isEmpty()) {
                getSharedPreferences("auth", MODE_PRIVATE)
                        .edit().putString("password", etChangePassword.getText().toString()).apply();
            }
        });

        btnSignout.setOnClickListener(v -> {
            getSharedPreferences("auth", MODE_PRIVATE).edit().putBoolean("logged", false).apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
