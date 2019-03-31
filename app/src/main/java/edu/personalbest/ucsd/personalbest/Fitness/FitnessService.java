package edu.personalbest.ucsd.personalbest.Fitness;

import android.app.Activity;


import java.util.Observable;

import edu.personalbest.ucsd.personalbest.StepCount;

public abstract class FitnessService extends Observable {
    abstract public int getRequestCode();
    abstract public void setup(Activity activity);
    abstract public void updateStepCount();
    abstract public void login(Activity activity);
    abstract public void logout();
    abstract public String getUserID();
    abstract public void increaseStepOffset(long steps);
    abstract public void resetStepsOffset();
    abstract public void setActivity(StepCount o);
}
