package com.hamzaiqbal.fotoeditorsmdproj;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class registrationActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Button btnsignup = findViewById(R.id.signupbutton);
        btnsignup.setOnClickListener(view -> {
            String name = ((EditText) findViewById(R.id.enterName)).getText().toString().trim();
            String email = ((EditText) findViewById(R.id.emailEnter)).getText().toString().trim();
            String password = ((EditText) findViewById(R.id.pwdEnter)).getText().toString().trim(); // Get password from input field


            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(getApplicationContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String userId = mAuth.getCurrentUser().getUid();
                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("email", email);


                        db.collection("users")
                                .document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getApplicationContext(), "User registered successfully!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(registrationActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> {
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("YourTag", "Error during network request", e);
                        });

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        TextView txtlogin = findViewById(R.id.loginbutton);
        txtlogin.setOnClickListener(view -> {
            Intent intent = new Intent(registrationActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}