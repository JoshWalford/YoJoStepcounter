package com.example.mystepcounter2.fragments;

import static android.content.ContentValues.TAG;
import static android.content.Context.SENSOR_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.mystepcounter2.R;
import com.example.mystepcounter2.models.StepCounterViewModel;
import com.example.mystepcounter2.databinding.FragmentHomeBinding;
import com.example.mystepcounter2.models.User;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment implements SensorEventListener {
    User user;
    Calendar calendar;
    private long startTime;
    private Sensor stepCounterSensor;
    private SensorManager sensorManager;
    private FirebaseAuth mAuth;
    private Runnable timerRunnable;
    private DatabaseReference mDatabase;
    private FragmentHomeBinding binding;
    private StepCounterViewModel stepCounterViewModel;
    private final Handler handler = new Handler();
    private final Handler timerHandler = new Handler();

    private int initialStepCount = 0;
    private int stepsSinceLastPause = 0;
    private boolean isCounterSensorPresent;
    private boolean isTrackingStarted = false, isPaused = false, goalAchieved = false;
    private final int stepCountTarget = 6000;
    private static final int ACTIVITY_RECOGNITION_PERMISSION_CODE = 101;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stepCounterViewModel = new ViewModelProvider(this).get(StepCounterViewModel.class);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("myDatabase");
        scheduleMidnightReset();

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("stepCount", stepCounterViewModel.getStepCount());
        outState.putLong("startTime", stepCounterViewModel.getStartTime());
        outState.putFloat("distance", stepCounterViewModel.getStepLengthInMeter());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            stepCounterViewModel.setStepCount(savedInstanceState.getInt("stepCount"));
            stepCounterViewModel.setStartTime(savedInstanceState.getLong("startTime"));
            stepCounterViewModel.setStepLengthInMeter(savedInstanceState.getFloat("stepLenghtInMeters"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        View view = binding.getRoot();

        sensorManager = (SensorManager) requireActivity().getSystemService(SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        calendar = Calendar.getInstance();
        binding.progressbar.setMax(stepCountTarget);
        binding.setTarget.setText("Target : " + stepCountTarget);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE);
        stepCounterViewModel.setStepCount(sharedPreferences.getInt("currentStepCount", 0));
        binding.setGoal.setText(String.valueOf(stepCounterViewModel.getStepCount())); //edit made here
        binding.progressbar.setProgress(stepCounterViewModel.getStepCount()); //edit made here

        if (stepCounterViewModel.getStepCount() > 0) { //edit made here
            float distanceInKms = stepCounterViewModel.getStepCount() * stepCounterViewModel.getStepLengthInMeter() / 1000; //edit made here X2
            binding.distance.setText(String.format(Locale.getDefault(), "Distance: %.2f kms", distanceInKms));
        }

        binding.startBtn.setOnClickListener(v -> {
            if (!isTrackingStarted || isPaused) {
                startTracking();
            } else {
                Toast.makeText(requireContext(), "Tracking is already started", Toast.LENGTH_SHORT).show();
            }

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) requireContext(), new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        ACTIVITY_RECOGNITION_PERMISSION_CODE);
            }
        });
        binding.pauseBtn.setOnClickListener(v -> {

            if (isTrackingStarted && !isPaused) {
                pauseTracking();
            } else {
                Toast.makeText(requireContext(), "Tracking has not started or you have already paused.", Toast.LENGTH_SHORT).show();
            }
        });

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            fetchUser();
            populateChart();
        } else {
            Log.d(TAG, "User is not authenticated");
            Toast.makeText(requireContext(), "User is not authenticated", Toast.LENGTH_SHORT).show();
        }
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) return;

                long elapsedTimeMillis = System.currentTimeMillis() - stepCounterViewModel.getStartTime(); //edit made here
                int totalMinutes = (int) (elapsedTimeMillis / (1000 * 60));
                int totalSeconds = (int) (elapsedTimeMillis / 1000) % 60;
                binding.time.setText(String.format(Locale.getDefault(), "Time: %02d:%02d", totalMinutes, totalSeconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        return view;
    }

    private void startTracking() {
        if (isPaused) {
            long elapsedTime = System.currentTimeMillis() - stepCounterViewModel.getStartTime(); //edit made here
            startTime -= elapsedTime;
            stepCounterViewModel.setStartTime(startTime);
        } else {
            startTime = System.currentTimeMillis();
            stepCounterViewModel.setStartTime(startTime);
            initialStepCount = stepsSinceLastPause = 0;
        }

        goalAchieved = false;
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        timerHandler.postDelayed(timerRunnable, 0);
        isTrackingStarted = true;
        isPaused = false;
    }

    private void pauseTracking() {
        sensorManager.unregisterListener(this);
        timerHandler.removeCallbacks(timerRunnable);
        isPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isCounterSensorPresent) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);

            if (isPaused) {
                long elapsedTime = System.currentTimeMillis() - stepCounterViewModel.getStartTime(); //edit made here
                startTime += elapsedTime; //edit made here
                stepCounterViewModel.setStartTime(startTime);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isCounterSensorPresent) {
            sensorManager.unregisterListener(this, stepCounterSensor);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int currentStepCount = (int) sensorEvent.values[0];

            if (!isPaused && isTrackingStarted) {
                if (initialStepCount == 0) {
                    initialStepCount = currentStepCount;
                }
                stepCounterViewModel.setStepCount(currentStepCount - initialStepCount + stepsSinceLastPause);
                trackProgress(stepCounterViewModel.getStepCount()); //edit made here
            }
        }
    }

    private void trackProgress(int currentStepCount) {
        if (!isAdded()) return;
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        binding.setGoal.setText(String.valueOf(currentStepCount));

        if (currentStepCount > 0) {
            binding.progressbar.setProgress(currentStepCount);

            if (!goalAchieved && currentStepCount >= stepCountTarget) {
                binding.setGoal.setText("Step Goal Achieved");
                goalAchieved = true;
            }
        }

        float distanceInKms = currentStepCount * stepCounterViewModel.getStepLengthInMeter() / 1000; //edit made here
        binding.distance.setText(String.format(Locale.getDefault(), "Distance: %.2f kms", distanceInKms));

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String currentDate = getCurrentDate();
            DatabaseReference userStepRef = mDatabase.child("myDatabase").child("users").child(userId).child("steps").child(currentDate);
            userStepRef.setValue(currentStepCount);
        }
        editor.putInt("totalSteps", currentStepCount);
        editor.apply();
    }

    public void fetchUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            mDatabase.child("myDatabase").child("user").child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    user = snapshot.getValue(User.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "fetchUser:onCancelled", error.toException());
                }
            });
        }
    }

    private String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    private void scheduleMidnightReset() {
        Calendar calendar1 = Calendar.getInstance();

        long currentTimeInMillis = calendar1.getTimeInMillis();
        calendar1.set(Calendar.HOUR_OF_DAY, 24);
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.SECOND, 0);
        long midnightTimeInMillis = calendar1.getTimeInMillis();

        long delay = midnightTimeInMillis - currentTimeInMillis;
        handler.postDelayed(() -> {
            resetDailyStep();
            scheduleMidnightReset();
        }, delay);
    }

    private void resetDailyStep() {
        stepCounterViewModel.setStepCount(0);
        stepsSinceLastPause = 0;
        if (!isAdded()) return;

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("currentStepCount", 0);
        editor.apply();
        binding.progressbar.setProgress(0);
        binding.setGoal.setText("0");
        binding.distance.setText("Distance: 0.00kms");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void populateChart() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference stepsRef = mDatabase.child("myDatabase").child("users").child(userId).child("steps");
            stepsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Map<String, Integer> dayStepsMap = new HashMap<>();
                    String[] daysOfWeek = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                    for (String day : daysOfWeek) {
                        dayStepsMap.put(day, 0);
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                        String date = dateSnapshot.getKey();
                        Integer stepCount = dateSnapshot.getValue(Integer.class); // Adjusted to fetch Integer directly

                        if (date != null && stepCount != null) {
                            try {
                                Date parsedDate = sdf.parse(date);
                                if (parsedDate != null) {
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(parsedDate);
                                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                                    String dayName = daysOfWeek[(dayOfWeek - 2 + 7) % 7];
                                    dayStepsMap.put(dayName, dayStepsMap.get(dayName) + stepCount);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    ArrayList<BarEntry> entries = new ArrayList<>();
                    for (int i = 0; i < daysOfWeek.length; i++) {
                        entries.add(new BarEntry(i, dayStepsMap.get(daysOfWeek[i])));
                    }
                    BarDataSet dataSet = new BarDataSet(entries, "Step Count");
                    BarData barData = new BarData(dataSet);
                    binding.barChart.setData(barData);

                    XAxis xAxis = binding.barChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setDrawGridLines(false);
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(daysOfWeek));
                    xAxis.setTextSize(10f);
                    xAxis.setGranularity(1f);
                    xAxis.setGranularityEnabled(true);

                    YAxis leftAxis = binding.barChart.getAxisLeft();
                    leftAxis.setDrawGridLines(false);

                    YAxis rightAxis = binding.barChart.getAxisRight();
                    rightAxis.setDrawGridLines(false);
                    rightAxis.setEnabled(false);

                    dataSet.setColor(Color.BLUE);
                    binding.barChart.getDescription().setEnabled(false);
                    binding.barChart.getLegend().setEnabled(false);

                    barData.setBarWidth(0.9f);
                    binding.barChart.invalidate();
                    binding.barChart.animateY(1000);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, "populateChart:onCancelled", error.toException());
                }
            });
        } else {
            Log.d(TAG, "User not authenticated");
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTIVITY_RECOGNITION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
