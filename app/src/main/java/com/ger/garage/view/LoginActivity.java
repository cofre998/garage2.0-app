package com.ger.garage.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import com.ger.garage.R;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText emailId, password;
    private Button btnLogin;
    private TextView haveAccount;

    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFirebaseAuth = FirebaseAuth.getInstance();

        emailId = findViewById(R.id.userName);
        password = findViewById(R.id.password);
        btnLogin = findViewById(R.id.login);
        haveAccount = findViewById(R.id.haveAccount);

        btnLogin.setOnClickListener(v -> {

            startActivity(new Intent(LoginActivity.this, UserHomeActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            String email = emailId.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (email.isEmpty()) {
                emailId.setError("Enter email");
                return;
            }

            if (pass.isEmpty()) {
                password.setError("Enter password");
                return;
            }

            mFirebaseAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            String uid = mFirebaseAuth.getCurrentUser().getUid();

                            FirebaseFirestore.getInstance()
                                    .collection("garage")
                                    .document("userInformation")
                                    .collection("users")
                                    .document(uid)
                                    .get()
                                    .addOnSuccessListener(document -> {

                                        if (document.exists()) {

                                            String userType = document.getString("userType");

                                            Intent intent;

                                            if ("admin".equals(userType)) {

                                                intent = new Intent(this, AdminHomeActivity.class);
                                                intent.putExtra("role", "admin");

                                            } else if ("mechanic".equals(userType)) {

                                                intent = new Intent(this, AdminHomeActivity.class);
                                                intent.putExtra("role", "mechanic");

                                            } else {

                                                intent = new Intent(this, UserHomeActivity.class);
                                            }

                                            startActivity(intent);
                                            finish();

                                        } else {
                                            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            Toast.makeText(this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        haveAccount.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }
}