package com.example.zwell;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountActivity extends AppCompatActivity {
    EditText etChangeUsername, etChangePassword;
    Button btnUpdate, btnSignout;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        etChangeUsername = findViewById(R.id.etChangeUsername);
        etChangePassword = findViewById(R.id.etChangePassword);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnSignout = findViewById(R.id.btnSignout);

        // Pre-fill current username
        if (currentUser != null && currentUser.getEmail() != null) {
            String currentUsername = currentUser.getEmail().split("@")[0];
            etChangeUsername.setText(currentUsername);
        }

        btnUpdate.setOnClickListener(v -> {
            String newUsername = etChangeUsername.getText().toString().trim();
            String newPassword = etChangePassword.getText().toString().trim();
            boolean hasUpdate = false;

            if (!newUsername.isEmpty() && currentUser != null && !newUsername.equals(currentUser.getEmail().split("@")[0])) {
                String newEmail = newUsername + "@zwell.app";
                // Re-authenticate user (Firebase requires recent login for sensitive operations)
                String currentEmail = currentUser.getEmail();
                String currentPassword = etChangePassword.getText().toString().trim(); // Ideally, prompt for current password separately
                if (currentPassword.isEmpty()) {
                    Toast.makeText(this, "Enter current password to update username", Toast.LENGTH_SHORT).show();
                    return;
                }
                AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, currentPassword);
                currentUser.reauthenticate(credential).addOnCompleteListener(reAuthTask -> {
                    if (reAuthTask.isSuccessful()) {
                        currentUser.verifyBeforeUpdateEmail(newEmail)
                                .addOnCompleteListener(emailTask -> {
                                    if (emailTask.isSuccessful()) {
                                        db.collection("users").document(currentUser.getUid())
                                                .update("username", newUsername)
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Failed to update username in DB: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                });
                                        Toast.makeText(this, "Verification email sent for new username", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String errorMsg = emailTask.getException() != null ? emailTask.getException().getMessage() : "Unknown error";
                                        Toast.makeText(this, "Failed to update username: " + errorMsg, Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Re-authentication failed: " + reAuthTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                hasUpdate = true;
            }

            if (!newPassword.isEmpty() && currentUser != null) {
                if (newPassword.length() < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                String currentEmail = currentUser.getEmail();
                String currentPassword = etChangePassword.getText().toString().trim(); // Reuse for simplicity; ideally separate prompt
                AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, currentPassword);
                currentUser.reauthenticate(credential).addOnCompleteListener(reAuthTask -> {
                    if (reAuthTask.isSuccessful()) {
                        currentUser.updatePassword(newPassword)
                                .addOnCompleteListener(passwordTask -> {
                                    if (passwordTask.isSuccessful()) {
                                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String errorMsg = passwordTask.getException() != null ? passwordTask.getException().getMessage() : "Unknown error";
                                        Toast.makeText(this, "Failed to update password: " + errorMsg, Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Re-authentication failed: " + reAuthTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                hasUpdate = true;
            }

            if (!hasUpdate) {
                Toast.makeText(this, "No changes to update", Toast.LENGTH_SHORT).show();
            }
        });

        btnSignout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
