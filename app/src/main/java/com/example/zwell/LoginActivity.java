package com.example.zwell;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText etUsername, etPassword;
    Button btnLogin;
    TextView tvSignup;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignupRedirect);

        btnLogin.setOnClickListener(v -> {
            // TODO: replace with real auth
            getSharedPreferences("auth", MODE_PRIVATE)
                    .edit()
                    .putBoolean("logged", true)
                    .putString("username", etUsername.getText().toString())
                    .apply();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        tvSignup.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    }
}
