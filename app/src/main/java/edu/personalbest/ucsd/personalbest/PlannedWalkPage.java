package edu.personalbest.ucsd.personalbest;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import edu.personalbest.ucsd.personalbest.Calendar.ICalendar;
import edu.personalbest.ucsd.personalbest.Fitness.FitnessService;
import edu.personalbest.ucsd.personalbest.Fitness.SharedFitnessFactory;
import edu.personalbest.ucsd.personalbest.Calendar.SharedCalendarFactory;

public class PlannedWalkPage extends StepCount implements Observer {

    // Google Service Key
    public static final String FITNESS_SERVICE_KEY = "FITNESS_SERVICE_KEY";
    private String fitnessServiceKey = "GOOGLE_FIT";

    // Activity information
    private static final String TAG = "StepCountActivity";

    // Fitness Service
    private FitnessService fitnessService;
    private ICalendar calendar;

    // UI components
    private TextView textTotalSteps;
    private TextView textWalkSteps;
    private TextView textTotalDistance;
    private TextView textWalkDistance;
    private TextView textWalkSpeed;

    // Temporary data
    long time_start = 0;
    long time_end = 0;
    final int UPDATE_RATE = 1000;
    long steps = 0;
    long previousSteps;

    Timer timer;
    String testMode;

    // Provides all user data and calculations
    UserData user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // UI Components
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planned_walk_page);
        final Button btn_endWalk = findViewById(R.id.btn_walk_end);
        final TextView time_display = findViewById(R.id.time_display);
        textTotalSteps = findViewById(R.id.total_step_display);
        textWalkSteps = findViewById(R.id.walk_step_display);
        textTotalDistance = findViewById(R.id.total_dist_display);
        textWalkDistance = findViewById(R.id.walk_dist_display);
        textWalkSpeed = findViewById(R.id.walk_speed_display);

        // for safety
        calendar = SharedCalendarFactory.getSharedCalendar(SharedCalendarFactory.DEFAULT, this);
        String userName = getIntent().getExtras().getString("userID", "default");
        previousSteps = getIntent().getExtras().getLong("previousSteps", 0);
        testMode = getIntent().getExtras().getString("mode", "development");
        user = UserData.createUserData(userName, this, testMode.equals("test"));

        // Timer
        timer = new Timer();
        time_start = getCurrentTime();
        btn_endWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.cancel();
                time_end = getCurrentTime();
                displayResults();
            }
        });
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateTimer(time_display);
                    }
                });
            }
        }, 0, UPDATE_RATE);

        // Google API
        fitnessService = SharedFitnessFactory.getSharedService(SharedFitnessFactory.DEFAULT, this);
        fitnessService.setup(this);
        steps = user.getTotalSteps();
        setStepCount(steps);

        setupFABListeners();

    }

    /**
     * Mock step and time increment
     * Features not fully implemented
     * fab_add_steps increments current intentional step by 500
     * fab_change_time todo
     */
    private void setupFABListeners() {
        FloatingActionButton fab_add_steps = findViewById(R.id.fab_add_steps);
        fab_add_steps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fitnessService.increaseStepOffset((long)500);
            }
        });

        FloatingActionButton fab_change_time = findViewById(R.id.fab_change_time);
        fab_change_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.incrementDay();
            }
        });
    }

    @Override
    public void update(Observable observable, Object arg){
        if(observable instanceof FitnessService){
            if(arg instanceof String){
                updateAccount((String)arg);
            }
            else if(arg instanceof Long){
                setStepCount((long)arg);
            }
        }

        // If date changes during a walk, count these steps toward the second day
        if(observable instanceof ICalendar){
            previousSteps = previousSteps - steps;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // If authentication was required during google fit setup, this will be called after the user authenticates
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == fitnessService.getRequestCode()) {
                fitnessService.updateStepCount();
            }
        } else {
            Log.e(TAG, "ERROR, google fit result code: " + resultCode);
        }
        fitnessService.deleteObserver(this);
    }

    /**
     * Initiate the timer for walk stats
     */
    public void setStarTime(long time_start) {
        this.time_start = time_start;
    }

    /**
     * Display a summary of this walk when the user clicks on the stop button
     */
    private void displayResults() {

        // gather statistics
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        long plannedSteps = steps - previousSteps;
        long time = time_end - time_start;
        float traveled_distance = user.getNewDistance(steps);
        float travel_speed = user.getWalkSpeed(steps, time);
        String message = "Steps: %d\nTime: %s\nDistance: %.1f\nMPH: %.2f";
        String formatted_message = String.format(Locale.getDefault(), message, plannedSteps, generateFormattedTimeString(time), traveled_distance, travel_speed);
        builder.setTitle("Walk information");
        builder.setMessage(formatted_message);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // update planned steps
                user.incrementDailyPlannedSteps(steps - previousSteps);
                user.incrementPlannedWalkTime(time_end - time_start);

                // wait for display message to finish
                finish();
            }
        });

        // --- Magic below ---
        builder.setCancelable(false);
        // --- Magic above ---

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * get current time for time elapse and speed calculation
     */
    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    // thanks final Button btn_endWalk = findViewById(R.id.btn_walk_end);to: https://stackoverflow.com/a/9027379
    private String generateFormattedTimeString(long millis) {
        // TODO: Do not include hours (or even minutes?) if unnecessary
        return String.format(Locale.getDefault(), "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
            TimeUnit.MILLISECONDS.toSeconds(millis) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    /**
     * For time elapsed display
     */
    private void updateTimer(TextView time_display) {
        long time_difference = getCurrentTime() - time_start;
        time_display.setText(generateFormattedTimeString(time_difference));
    }

    /**
     * Called by FitnessService to update information
     * in the user and on display
     */
    public void setStepCount(long stepCount) {
        final long s = stepCount;
        final Activity a = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                steps = s;
                long newSteps = steps - previousSteps;
                long currTime = getCurrentTime();
                textWalkSteps.setText(String.valueOf(newSteps));
                textTotalSteps.setText(String.valueOf(steps));
                textWalkDistance.setText(String.format(Locale.getDefault(),
                        "(%.1f miles)", user.getNewDistance(newSteps)));
                textTotalDistance.setText(String.format(Locale.getDefault(),
                        "(%.1f miles)", user.getPreviousDistance()));
                textWalkSpeed.setText(String.format(Locale.getDefault(),
                        "%.2f MPH", user.getWalkSpeed(newSteps, currTime - time_start)));
            }
        });
    }

    /**
     * Implemented for interface
     * not used
     */
    @Override
    public void updateAccount(String userID){
        final String s = userID;
        final Activity a = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                user = UserData.createUserData(s, a, testMode.equals("test"));
                steps = user.getTotalSteps();
                setStepCount(steps);
            }
        });

    }
}
