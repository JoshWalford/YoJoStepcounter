package com.example.mystepcounter2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.mystepcounter2.databinding.ActivityUserBinding;
import com.example.mystepcounter2.models.User;
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
    private ActivityUserBinding userBinding;
    String name;
    String password;
    String email;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userBinding = DataBindingUtil.setContentView(this,R.layout.activity_user);

        mAuth = FirebaseAuth.getInstance();
        userBinding.createBtn.setOnClickListener(v -> {
            name = userBinding.userName.getText().toString().trim();
            email = userBinding.userEmail.getText().toString().trim();
            password = userBinding.userPassword.getText().toString().trim();

            if (name.isEmpty()) {
                userBinding.userName.setError("Username is empty.");
            }
            if (email.isEmpty()) {
                userBinding.userEmail.setError("Email is empty.");
            }
            if (password.isEmpty()) {
                userBinding.userPassword.setError("Password cannot be empty.");
            } else {
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        makeUsers();
                        Toast.makeText(CreateAccountActivity.this, "Signup Succesfull", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CreateAccountActivity.this, Login.class));
                    } else {
                        Toast.makeText(CreateAccountActivity.this, "Sign Up Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        userBinding.loginRedirectText.setOnClickListener(v -> {
            Intent intent = new Intent(CreateAccountActivity.this, Login.class);
            startActivity(intent);
        });
    }

    public void makeUsers() {
        // Get input values from EditText fields
        String username = userBinding.userName.getText().toString().trim();
        String email = userBinding.userEmail.getText().toString().trim();
        String password = userBinding.userPassword.getText().toString().trim();
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
            }
        }
    }