package com.example.mystepcounter2.models;

import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StepCounterViewModel extends ViewModel {
    private final MutableLiveData<Integer> stepCount = new MutableLiveData<>();
    private final MutableLiveData<Long> startTime = new MutableLiveData<>();
    private float stepLengthInMeter = 0.762f;
    public StepCounterViewModel() {
        stepCount.setValue(0);
        startTime.setValue(0L);
        Log.i("StepCounterViewModel","ViewModel is created");
    }
    @Override
    protected void onCleared() {
        super.onCleared();
        Log.i("StepCounterViewModel","ViewModel is Destroyed");
    }
    public int getStepCount() {
        return stepCount.getValue();
    }
    public void setStepCount(int stepCount) {
        this.stepCount.setValue(stepCount);
    }
    public long getStartTime() {
        return startTime.getValue();
    }

    public void setStartTime(long startTime) {
        this.startTime.setValue(startTime);
    }
    public float getStepLengthInMeter() {
        return stepLengthInMeter;
    }
    public void setStepLengthInMeter(float stepLengthInMeter) {
        this.stepLengthInMeter = stepLengthInMeter;
    }
}
