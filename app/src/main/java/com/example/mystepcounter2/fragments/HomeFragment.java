package com.example.mystepcounter2.fragments;

import static android.content.Context.SENSOR_SERVICE;

import android.Manifest;
import android.app.Activity;
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

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mystepcounter2.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements SensorEventListener {

    Calendar calendar;
    Button startBtn, pauseBtn;
    ProgressBar progressBar;
    TextView setGoal, distance,time, setTarget;
    int stepCount = 0;
    private Sensor stepCounterSensor;
    private SensorManager sensorManager;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private BarChart barChart;
    private List<String> Week;
    private boolean isCounterSensorPresent;
    private boolean isTrackingStarted,isPaused,goalAchieved = false;
    //private CalendarView calendarView;
    private int stepCountTarget = 6000;
    private long startTime;
    private float stepLenghtInMeter = 0.762f;
    private static final int ACTIVITY_RECOGNITION_PERMISSION_CODE = 101;
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
    private void startTracking() {
        if (isPaused) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            startTime -= elapsedTime;
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
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        progressBar = view.findViewById(R.id.progressbar);
        setGoal = view.findViewById(R.id.setGoal);
        startBtn = view.findViewById(R.id.startBtn);
        pauseBtn = view.findViewById(R.id.pauseBtn);
        distance = view.findViewById(R.id.distance);
        setTarget = view.findViewById(R.id.setTarget);
        barChart = view.findViewById(R.id.barChart);
        time = view.findViewById(R.id.time);

        startTime = System.currentTimeMillis();

        sensorManager = (SensorManager) requireActivity().getSystemService(SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        //calendarView = view.findViewById(R.id.calendarView);
        calendar = Calendar.getInstance();
        progressBar.setMax(stepCountTarget);

        setTarget.setText("Target : " + stepCountTarget);
        stepCount = 0;
        isCounterSensorPresent = (stepCounterSensor != null);
        if (!isCounterSensorPresent) {
            setTarget.setText("Step Counter not Available.");
        }

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
        return  view;
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

            // Update step count text
            setGoal.setText(String.valueOf(currentStepCount));

            // Calculate steps taken since last update
            int stepsTaken = currentStepCount - stepCount;

            // Update progress bar with steps taken
            if (stepsTaken > 0) {
                progressBar.incrementProgressBy(stepsTaken);
                stepCount = currentStepCount;

                // Check if goal is achieved
                if (!goalAchieved && stepCount >= stepCountTarget) {
                    setGoal.setText("Step Goal Achieved");
                    goalAchieved = true;
                }
            }

            // Calculate and display distance
            float distanceInKms = stepCount * stepLenghtInMeter / 1000;
            distance.setText(String.format(Locale.getDefault(), "Distance: %.2f kms", distanceInKms));

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                String currentData = getCurrentDate();
                DatabaseReference userStepRef = mDatabase.child("user").child(userId).child("date").child(currentData);
                userStepRef.child("steps").setValue(stepCount);
            }
        }
    }

    private void populateChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();

        BarDataSet dataSet = new BarDataSet(entries, "Step Count");
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        Week = new ArrayList<>();
        Week.add("Sunday");
        Week.add("Monday");
        Week.add("Tuesday");
        Week.add("Wednesday");
        Week.add("Thursday");
        Week.add("Friday");
        Week.add("Saturday");

        XAxis xAxis = barChart.getXAxis(); // Get the XAxis instance
        xAxis.setValueFormatter(new IndexAxisValueFormatter(Week)); // Set custom value formatter

        dataSet.setColor(Color.BLUE);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        // Refresh the chart
        barChart.invalidate();
    }

    private void trackProgress(int progress) {
        progressBar.setProgress(progress);

        // Use Handler to delay the recursive call
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                trackProgress(progress + 1);
            }
        }, 1000);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}