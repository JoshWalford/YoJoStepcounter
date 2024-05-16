package com.example.mystepcounter2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HelperClass {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public HelperClass() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void writeStepCount(String userId, String date, int steps) {
        String path = "users/" + userId + "date/" + date + "steps";

        mDatabase.child(path).setValue(steps);
    }
}
