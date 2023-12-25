package com.hamzaiqbal.fotoeditorsmdproj;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEditText = findViewById(R.id.enter_Email); // Make sure to use the correct ID
        resetPasswordButton = findViewById(R.id.resetPwd); // Use the correct ID for your reset password button

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = emailEditText.getText().toString();

                if (!userEmail.isEmpty()) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(userEmail)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Password reset email sent successfully.
                                        Toast.makeText(ForgotPassword.this, "Password reset email sent. Check your email.", Toast.LENGTH_LONG).show();
                                    } else {
                                        // Failed to send password reset email.
                                        Toast.makeText(ForgotPassword.this, "Failed to send password reset email. Check your email address.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                } else {
                    // Handle the case where the email field is empty.
                    Toast.makeText(ForgotPassword.this, "Please enter your email address.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}