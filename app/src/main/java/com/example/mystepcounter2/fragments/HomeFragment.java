package com.example.mystepcounter2.fragments;

import static android.content.ContentValues.TAG;
import static android.content.Context.SENSOR_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mystepcounter2.R;
import com.example.mystepcounter2.StepCounterViewModel;
import com.example.mystepcounter2.models.User;
import com.github.mikephil.charting.charts.BarChart;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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
    Button startBtn, pauseBtn;
    ProgressBar progressBar;
    TextView setGoal, distance, time, setTarget;
    int stepCount = 0;
    private Sensor stepCounterSensor;
    private SensorManager sensorManager;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private BarChart barChart;
    private StepCounterViewModel viewModel;
    private long postDelayed;

    private boolean isCounterSensorPresent;
    private boolean isTrackingStarted, isPaused, goalAchieved = false;
    private int stepCountTarget = 6000;
    private long startTime;
    private float stepLengthInMeter = 0.762f;
    private static final int ACTIVITY_RECOGNITION_PERMISSION_CODE = 101;
    private Handler handler = new Handler();

    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long elapsedTimeMillis = System.currentTimeMillis() - startTime;
            int totalSeconds = (int) (elapsedTimeMillis / 1000);
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            time.setText(String.format(Locale.getDefault(), "Time: %02d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 1000);
        }
    };

    // Variables to store state
    private int initialStepCount = 0;
    private int stepsSinceLastPause = 0;

    private void startTracking() {
        if (isPaused) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            startTime -= elapsedTime;
        } else {
            startTime = System.currentTimeMillis();
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
        viewModel = new ViewModelProvider(requireActivity()).get(StepCounterViewModel.class);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("myDatabase");

        scheduleMidnightReset();

        /*DatabaseReference parentReference = mDatabase.child("My Database");
        DatabaseReference userReference = parentReference.child("users");*/
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("stepCount", viewModel.getStepCount());
        outState.putLong("startTime",viewModel.getStartTime());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            int stepCount = savedInstanceState.getInt("stepCount");
            long startTime = savedInstanceState.getLong("startTime");
            viewModel.setStepCount(stepCount);
            viewModel.setStartTime(startTime);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        progressBar = view.findViewById(R.id.progressbar);
        setGoal = view.findViewById(R.id.setGoal);
        startBtn = view.findViewById(R.id.startBtn);
        pauseBtn = view.findViewById(R.id.pauseBtn);
        distance = view.findViewById(R.id.distance);
        setTarget = view.findViewById(R.id.setTarget);
        barChart = view.findViewById(R.id.barChart);
        time = view.findViewById(R.id.time);

        //startTime = System.currentTimeMillis();

        sensorManager = (SensorManager) requireActivity().getSystemService(SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        calendar = Calendar.getInstance();
        progressBar.setMax(stepCountTarget);

        setTarget.setText("Target : " + stepCountTarget);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE);
        stepCount = sharedPreferences.getInt("currentStepCount",0);
        setGoal.setText(String.valueOf(stepCount));
        progressBar.setProgress(stepCount);

        //stepCount = 0;
        if (stepCount > 0) {
            float distanceInKms = stepCount * stepLengthInMeter / 1000;
            distance.setText(String.format(Locale.getDefault(), "Distance: %.2f kms", distanceInKms));
        }
        /*isCounterSensorPresent = (stepCounterSensor != null);
        if (!isCounterSensorPresent) {
            setTarget.setText("Step Counter not Available.");
        }*/

        startBtn.setOnClickListener(view1 -> {
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

        pauseBtn.setOnClickListener(view1 -> {
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
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isCounterSensorPresent) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);

            if (isPaused) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                startTime += elapsedTime;
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTIVITY_RECOGNITION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getCurrentDate() {
        Calendar calendar1 = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(calendar1.getTime());
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int currentStepCount = (int) sensorEvent.values[0];

            if (!isPaused && isTrackingStarted) {
                if (initialStepCount == 0) {
                    initialStepCount = currentStepCount;
                }
                stepCount = currentStepCount - initialStepCount + stepsSinceLastPause;
                trackProgress(stepCount);
            }
        }
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
                    String[] daysOfWeek = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
                    for (String day : daysOfWeek) {
                        dayStepsMap.put(day,0);
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());

                    for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                        String date = dateSnapshot.getKey();
                        Integer stepCount = dateSnapshot.child("steps").getValue(Integer.class);

                        if(date != null && stepCount != null){
                            try {
                                Date parsedDate = sdf.parse(date);
                                if (parsedDate != null) {
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(parsedDate);
                                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                                    String dayName = daysOfWeek[(dayOfWeek - 2 + 7) % 7];
                                    dayStepsMap.put(dayName,dayStepsMap.get(dayName) + stepCount);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    ArrayList<BarEntry> entries = new ArrayList<>();
                    for (int i = 0; i < daysOfWeek.length; i++) {
                        entries.add(new BarEntry(i,dayStepsMap.get(daysOfWeek[i])));
                    }

                    BarDataSet dataSet = new BarDataSet(entries, "Step Count");
                    BarData barData = new BarData(dataSet);
                    barChart.setData(barData);

                    XAxis xAxis = barChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setDrawGridLines(false);
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(daysOfWeek));
                    xAxis.setGranularity(1f);
                    xAxis.setGranularityEnabled(true);

                    YAxis leftAxis = barChart.getAxisLeft();
                    leftAxis.setDrawGridLines(false);

                    YAxis rightAxis = barChart.getAxisRight();
                    rightAxis.setDrawGridLines(false);
                    rightAxis.setEnabled(false);

                    dataSet.setColor(Color.BLUE);
                    barChart.getDescription().setEnabled(false);
                    barChart.getLegend().setEnabled(false);

                    barData.setBarWidth(0.9f);

                    barChart.invalidate();

                    barChart.animateY(1000);
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

    public void fetchUser() {
        Intent intent = getActivity().getIntent();

        if (intent != null) {
            String email = intent.getStringExtra("email");

            if (email != null && !email.isEmpty()) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference parentRef = database.getReference("myDatabase").child("users");

                Query query = parentRef.orderByChild("email").equalTo(email);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                User user = userSnapshot.getValue(User.class);
                                Log.d(TAG, "User found: " + user.getUsername());
                                Toast.makeText(requireContext(), "User found: " + user.getUsername(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d(TAG, "User not found with email: " + email);
                            Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w(TAG, "fetchUser:onCancelled", error.toException());
                    }
                });
            } else {
                Log.d(TAG, "Email not found in intent");
                Toast.makeText(requireContext(), "Email not found in intent", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "Intent is null");
            Toast.makeText(requireContext(), "Intent is null", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No-op
    }

    private void scheduleMidnightReset() {
        Calendar calendar1 = Calendar.getInstance();

        long currentTimeInMillis = calendar1.getTimeInMillis();
        calendar1.set(Calendar.HOUR_OF_DAY, 24);
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.SECOND, 0);
        long midnightTimeInMillis = calendar1.getTimeInMillis();

        long delay = midnightTimeInMillis - currentTimeInMillis;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                resetDailyStep();
                scheduleMidnightReset();
            }
        }, delay);
    }

    private void resetDailyStep() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("totalSteps", 0);
        editor.apply();
        stepCount = 0;
        progressBar.setProgress(0);
        setGoal.setText("0");
    }

    private void trackProgress(int currentStepCount) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        setGoal.setText(String.valueOf(currentStepCount));

        /*int previousTotalSteps = sharedPreferences.getInt("totalSteps", 0);
        int stepsTaken = currentStepCount;*/

       // setGoal.setText(String.valueOf(stepsTaken));

        if (currentStepCount > 0) {
            progressBar.setProgress(currentStepCount);

            if (!goalAchieved && currentStepCount >= stepCountTarget) {
                setGoal.setText("Step Goal Achieved");
                goalAchieved = true;
            }
        }

        float distanceInKms = currentStepCount * stepLengthInMeter / 1000;
        distance.setText(String.format(Locale.getDefault(), "Distance: %.2f kms", distanceInKms));

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String currentDate = getCurrentDate();
            DatabaseReference userStepRef = mDatabase.child("myDatabase").child("users").child(userId).child("steps").child(currentDate);
            userStepRef.child("steps").setValue(currentStepCount);
        }

        editor.putInt("totalSteps", currentStepCount);
        editor.apply();
    }
}
