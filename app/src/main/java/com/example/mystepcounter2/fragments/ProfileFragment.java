package com.example.mystepcounter2.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mystepcounter2.EditAccount;
import com.example.mystepcounter2.R;
import com.example.mystepcounter2.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding profileBinding;
    String[] item = {"On the home screen click the START button to start counting steps. To pause it press the PAUSE button. You can then resume counting using the START button"};
    String[] item1 = {"The built in sensor in you is used to count the steps. The accuracy of the steps wold be based on how good the sensor is in your phone."};
    String[] item2 = {"Upon shaking of you phone it may register as a step. This is not a bug. This is based on the in built sensor."};
    String[] item3 = {"You can pause the app to save battery when you are not walking."};
    String[] item4 = {"We help you set you goal according to your achieved steps to keep you motivated."};

    AutoCompleteTextView autoCompleteTextView;
    AutoCompleteTextView autoCompleteTextView1;
    AutoCompleteTextView autoCompleteTextView2;
    AutoCompleteTextView autoCompleteTextView3;
    AutoCompleteTextView autoCompleteTextView4;

    ArrayAdapter adapteritems;
    ArrayAdapter adapteritems1;
    ArrayAdapter adapteritems2;
    ArrayAdapter adapteritems3;
    ArrayAdapter adapteritems4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        profileBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_profile,container,false);
        View view = profileBinding.getRoot();

        Bundle args = getArguments();
        if (args != null) {
            String username = args.getString("username");

            profileBinding.userNameTxt.setText(username);
        }

        autoCompleteTextView = view.findViewById(R.id.auto_completeTxt);
        autoCompleteTextView1 = view.findViewById(R.id.auto_completeTxt1);
        autoCompleteTextView2 = view.findViewById(R.id.auto_completeTxt2);
        autoCompleteTextView3 = view.findViewById(R.id.auto_completeTxt3);
        autoCompleteTextView4 = view.findViewById(R.id.auto_completeTxt4);

        adapteritems = new ArrayAdapter<>(requireContext(), R.layout.list_item, item);
        adapteritems1 = new ArrayAdapter<>(requireContext(), R.layout.list_item, item1);
        adapteritems2 = new ArrayAdapter<>(requireContext(), R.layout.list_item, item2);
        adapteritems3 = new ArrayAdapter<>(requireContext(), R.layout.list_item, item3);
        adapteritems4 = new ArrayAdapter<>(requireContext(), R.layout.list_item, item4);

        autoCompleteTextView.setAdapter(adapteritems);
        autoCompleteTextView1.setAdapter(adapteritems1);
        autoCompleteTextView2.setAdapter(adapteritems2);
        autoCompleteTextView3.setAdapter(adapteritems3);
        autoCompleteTextView4.setAdapter(adapteritems4);


        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(requireContext(), "item" + item, Toast.LENGTH_SHORT).show();
            }
        });

        autoCompleteTextView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item1 = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(requireContext(), "item" + item1, Toast.LENGTH_SHORT).show();

            }
        });

        autoCompleteTextView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item2 = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(requireContext(), "item" + item2, Toast.LENGTH_SHORT).show();
            }
        });

        autoCompleteTextView3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item3 = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(requireContext(), "item" + item3, Toast.LENGTH_SHORT).show();
            }
        });

        autoCompleteTextView4.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item4 = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(requireContext(), "item" + item4, Toast.LENGTH_SHORT).show();
            }
        });

        profileBinding.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), EditAccount.class);
                startActivity(intent);
            }
        });
        return view;
    }

    public void getArguments(Bundle bundle) {
    }
}
