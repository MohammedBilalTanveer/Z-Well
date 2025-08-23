package com.example.zwell;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {
    EditText etNewUsername, etNewPassword;
    Button btnSignup;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_signup);

        etNewUsername = findViewById(R.id.etNewUsername);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnSignup = findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(v -> {
            // TODO: secure storage
            getSharedPreferences("auth", MODE_PRIVATE).edit()
                    .putString("username", etNewUsername.getText().toString())
                    .putString("password", etNewPassword.getText().toString())
                    .putBoolean("logged", true)
                    .apply();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }
}