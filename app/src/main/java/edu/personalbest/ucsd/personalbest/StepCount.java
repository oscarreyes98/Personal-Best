package edu.personalbest.ucsd.personalbest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.Observer;

import edu.personalbest.ucsd.personalbest.Fitness.FitnessService;


public abstract class StepCount extends AppCompatActivity implements Observer {

    public void setStepCount(long stepCount) {};
    public void updateAccount(String userID) {};
}
