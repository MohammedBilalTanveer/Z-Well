package com.example.zwell;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {
    EditText etNewUsername, etNewPassword;
    Button btnSignup;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    private static final String TAG = "SignupActivity";
    // Regex for valid username: alphanumeric, dots, underscores
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._]+$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etNewUsername = findViewById(R.id.etNewUsername);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnSignup = findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(v -> {
            String username = etNewUsername.getText().toString().trim();
            String password = etNewPassword.getText().toString().trim();

            // Validate inputs
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!USERNAME_PATTERN.matcher(username).matches()) {
                Toast.makeText(this, "Username can only contain letters, numbers, dots, or underscores", Toast.LENGTH_LONG).show();
                return;
            }
            if (username.length() < 3) {
                Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = username + "@zwell.app";
            Log.d(TAG, "Attempting signup with email: " + email);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                db.collection("users").document(user.getUid())
                                        .set(new HashMap<String, Object>() {{
                                            put("username", username);
                                        }})
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Failed to save username: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                                startActivity(new Intent(this, HomeActivity.class));
                                finish();
                            }
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(this, "Signup failed: " + errorMsg, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Signup error: " + errorMsg);
                        }
                    });
        });
    }
}