package com.example.mystepcounter2;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

public class StepCounterViewModel extends ViewModel {
    private int stepCount;
    private long startTime;

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
