package com.example.mystepcounter2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mystepcounter2.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateAccountActivity extends AppCompatActivity {

    private static final String TAG = "UserActivity";
    private FirebaseAuth mAuth;
    private EditText userNameEt, emailEt, passwordEt, conPassword;
    private Button createBtn;
    String name;
    String password;
    String email;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mAuth = FirebaseAuth.getInstance();
        userNameEt = findViewById(R.id.userName);
        emailEt = findViewById(R.id.userEmail);
        passwordEt = findViewById(R.id.userPassword);
        createBtn = findViewById(R.id.createBtn);

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = userNameEt.getText().toString().trim();
                email = emailEt.getText().toString().trim();
                password = passwordEt.getText().toString().trim();

                if (name.isEmpty()) {
                    userNameEt.setError("Username is empty.");
                }
                if (email.isEmpty()) {
                    emailEt.setError("Email is empty.");
                }
                if (password.isEmpty()) {
                    passwordEt.setError("Password cannot be empty.");
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                makeUsers();
                                Toast.makeText(CreateAccountActivity.this, "Signup Succesfull", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(CreateAccountActivity.this, Login.class));
                            } else {
                                Toast.makeText(CreateAccountActivity.this, "Sign Up Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }

    public void makeUsers() {

        // Get input values from EditText fields
        String username = userNameEt.getText().toString().trim();
        String email = emailEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();

        // Create a new User object
        user = new User();

        // Set user properties
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setStepCount(0);
        user.setActiveTime(0);
        user.setDistance(0.0);

        // Format the current date and set it
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date());
        user.setDate(formattedDate);

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference parentRef = database.getReference("myDatabase");
        DatabaseReference userRef = parentRef.child("users");

        // Add the new user to the database with a unique key
        userRef.push().setValue(user);

        // Create a query to sort users by username
        Query query = userRef.orderByChild(user.getUsername());

        // Add a listener to handle the sorted data
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Loop through the sorted data
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    Log.d(TAG, "User: " + user.getUsername());
                    // Handle the user data as needed
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }


    @Override
        protected void onStart () {
            super.onStart();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            updateUI(currentUser);
        }

        private void updateUI (FirebaseUser currentUser){
            if (currentUser != null) {
                Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
                intent.putExtra("email", currentUser.getEmail());
                startActivity(intent);
                finish();
            } else {

            }
        }
    }