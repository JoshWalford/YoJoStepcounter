package com.example.mystepcounter2.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavHostController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mystepcounter2.R;
import com.example.mystepcounter2.databinding.FragmentReportBinding;

public class ReportFragment extends Fragment {
    private FragmentReportBinding reportBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        reportBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_report,container,false);
        View view = reportBinding.getRoot();

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Alert");
        builder.setIcon(R.drawable.alert_fill_icon);
        builder.setMessage("Coming soon!! currently underdevelopment.\nPlease return to the home screen");
        builder.setNegativeButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NavController navController = NavHostFragment.findNavController(ReportFragment.this);
                navController.navigate(R.id.action_reportFragment_to_homeFragment);
            }
        });
        AlertDialog dialog = builder.show();
        dialog.show();
        return view;
    }
}