package com.example.mystepcounter2;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.mystepcounter2.databinding.ActivityEditAccountBinding;
import com.example.mystepcounter2.models.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditAccount extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private ActivityEditAccountBinding editAccountBinding;
    private FirebaseUser mUser;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editAccountBinding = DataBindingUtil.setContentView(this,R.layout.activity_edit_account);

        editAccountBinding.genderSpinner.setOnItemSelectedListener(this);

        String[] gender = new String[]{"Male","Female","Other"};
        String[] heights = new String[]{"150", "155", "160", "165", "170", "175", "180", "185", "190", "195", "200"};
        String[] weights = new String[]{"50", "55", "60", "65", "70", "75", "80", "85", "90", "95", "100"};

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,gender);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editAccountBinding.genderSpinner.setAdapter(genderAdapter);

        ArrayAdapter<String> heightAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,heights);
        heightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editAccountBinding.heightSpinner.setAdapter(heightAdapter);

        ArrayAdapter<String> weightAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,weights);
        weightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editAccountBinding.weightSpinner.setAdapter(weightAdapter);

    }

    public void makeUser() {
        String gender = String.valueOf(findViewById(R.id.genderSpinner));
        String height = String.valueOf(findViewById(R.id.heightSpinner));
        String weight = String.valueOf(findViewById(R.id.weightSpinner));

        user.setGender(gender);
        user.setHeight(Integer.parseInt(height));
        user.setWeight(Integer.parseInt(weight));

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference parentRef = database.getReference("myDatabase");
        DatabaseReference userRef = parentRef.child("users");

        userRef.push().setValue(user);
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}