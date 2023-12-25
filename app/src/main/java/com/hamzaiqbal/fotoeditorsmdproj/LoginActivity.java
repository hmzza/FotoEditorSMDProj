package com.hamzaiqbal.fotoeditorsmdproj;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        EditText emailEditText = findViewById(R.id.enteremail);
        EditText passwordEditText = findViewById(R.id.enterpassword); // <-- Use getEmail3 for the password field
        Button btnLogin = findViewById(R.id.loginbutton);

        btnLogin.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Authentication successful.", Toast.LENGTH_SHORT).show();
                            if (user != null) {
                                String uid = user.getUid();
                                getUserDetails(uid);
                            }// Finish LoginActivity to prevent going back after successful login
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        // Inside your LoginActivity.java
        Button forgotPwdButton = findViewById(R.id.forgotPwd);
        forgotPwdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPassword.class);
                startActivity(intent);
            }
        });

        TextView txtSignUp = findViewById(R.id.signUp);
        txtSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, registrationActivity.class);
            startActivity(intent);
        });


    }

    private void getUserDetails(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                System.out.println("hy " +name);

                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task3 -> {
                    if(task3.isSuccessful()){
                        String token = task3.getResult();
                        userRef.update("fcmtoken",token);
                        Log.i("my token", token);

                        if (name != null) {
                            // Pass user name to WelcomeActivity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("USER_NAME", name);
                            intent.putExtra("FCM_TOKEN", token);
                            intent.putExtra("UID", documentSnapshot.getId());
                            startActivity(intent);
                            finish(); // Finish LoginActivity to prevent going back after successful login
                        } else {
                            // Handle the case where 'name' is null in the database
                            // You might want to set a default name or show an error message
                        }

                    }
                });


            } else {
                // Handle the case where the user data does not exist in the database
                // You might want to set a default name or show an error message
            }
        }).addOnFailureListener(e -> {
            // Handle error
            Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

}
