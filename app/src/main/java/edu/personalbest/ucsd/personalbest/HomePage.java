package edu.personalbest.ucsd.personalbest;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import edu.personalbest.ucsd.personalbest.Calendar.ICalendar;
import edu.personalbest.ucsd.personalbest.Fitness.FitnessService;
import edu.personalbest.ucsd.personalbest.Fitness.SharedFitnessFactory;
import edu.personalbest.ucsd.personalbest.Calendar.SharedCalendarFactory;
import edu.personalbest.ucsd.personalbest.Friend.UserFriend;

public class HomePage extends StepCount implements Observer{

    // Google Service Key
    private String fitnessServiceKey = "GOOGLE_FIT";
    private String testMode;

    // Activity information
    private static final String TAG = "StepCountActivity";

    // Fitness service
    private FitnessService fitnessService;

    // UI components
    private TextView textSteps;
    private TextView textDistance;

    // Step Counter
    private Handler update = new Handler();
    public GoalObserver updateGoal = new GoalObserver();

    // Provides all user data and calculations
    public UserData user;
    ICalendar calendar;

    public DrawerLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // UI components
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        FirebaseApp.initializeApp(this);
        // Choose test modes
        // SET this with roboletric and intent in unit testing
        try{ testMode = getIntent().getExtras().getString("mode", "development"); }
            catch(Exception e){testMode = "development";}

        // SET this with... uh not sure yet for espresso
        // testMode = "test";

        switch (testMode){
            case "development":
                SharedCalendarFactory.setDefault(SharedCalendarFactory.MOCK_CALENDAR);
                SharedFitnessFactory.setDefault(SharedFitnessFactory.GOOGLEFIT);
                break;
            case "test":
                SharedCalendarFactory.setDefault(SharedCalendarFactory.MOCK_CALENDAR);
                SharedFitnessFactory.setDefault(SharedFitnessFactory.MOCKFITNESS);
                break;
            case "real":
                SharedCalendarFactory.setDefault(SharedCalendarFactory.REAL_CALENDAR);
                SharedFitnessFactory.setDefault(SharedFitnessFactory.GOOGLEFIT);
        }

        layout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.menu_icon);

        // for safety
        calendar = SharedCalendarFactory.getSharedCalendar(SharedCalendarFactory.DEFAULT, this);
        //user = new UserData("default", this);
        user = UserData.createUserData("default", this, testMode.equals("test"));
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseManager.Initialize(user, db, this);

        // Google API
        fitnessService = SharedFitnessFactory.getSharedService(SharedFitnessFactory.DEFAULT, this);
        fitnessService.setActivity(this);
        fitnessService.login(this);

        // Texts and buttons
        textSteps = findViewById(R.id.step_total);
        textDistance = findViewById(R.id.dist_total);
        final Button btn_startWalk = findViewById(R.id.btn_walk_start);
        btn_startWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchActivity();
            }
        });
        final Button btn_changeGoal = findViewById(R.id.custom_goal);
        final HomePage homePageRef = this;
        btn_changeGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "this" in this context is a "View", not a "HomePage"
                updateGoal.customizeGoal( user, homePageRef, true);
            }
        });

        final NavigationView hamburger = findViewById(R.id.nav_view);
        hamburger.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.nav_friends_list: {
                                launchFriendsList();
                                break;
                            }
                            case R.id.nav_graph: {
                                launchGraph();
                                break;
                            }
                            case R.id.nav_logout: {
                                switchAccount();
                                break;
                            }
                        }
                        layout.closeDrawers();
                        return true;
                    }
                });

        Button btn_clearSharedPref = findViewById(R.id.clear);
        btn_clearSharedPref.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                calendar.clearMockedTime();
                fitnessService.resetStepsOffset();
                user.dataEditor.clear().apply();
                user.clearDayData();
                updateGoal.update(user, homePageRef);
                if(user.isFirstLogIn())
                    launchHeight();
            }
        });

        if(testMode.equals("real")){
            btn_clearSharedPref.setVisibility(View.GONE);
        }

        // init screen
        setStepCount(user.getTotalSteps());

        setupFABListeners();

        // Step Counter
        Thread thread = new Thread(new TrackNotPlannedSteps());
        thread.start();
    }

    /**
     * Mock step and time increment
     * Features not fully implemented
     * fab_add_steps increments total (nont intentional) step by 500
     * fab_change_time should increment the app time onto the next day
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                layout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


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

        if(testMode == "real"){
            fab_add_steps.hide();
            fab_change_time.hide();
        }
    }

    /**
     * Launches the planned walk page
     */
    public void launchActivity() {
        Intent intent = new Intent(this, PlannedWalkPage.class);
        intent.putExtra("userID",fitnessService.getUserID());
        intent.putExtra("previousSteps",user.getTotalSteps());
        intent.putExtra("mode",testMode);
        getIntent().getSerializableExtra("MyClass");
        startActivity(intent);
        updateGoal.update(user, this);
    }

    public void  launchFriendsList(){
        Intent intent = new Intent(this, FriendsList.class);
        intent.putExtra("userID", user.getUserID());
        startActivity(intent);
    }

    /**
     * Allows user to switch account
     */
    public void switchAccount(){
        fitnessService.logout();
        fitnessService.login(this);
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
        if(observable instanceof ICalendar){
            fitnessService.resetStepsOffset();
            updateGoal.newDayUpdate(this, user);
        }
    }

    @Override
    public void updateAccount(String userID){
        final String s = userID;
        final HomePage a = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView user_id = findViewById(R.id.user_id);
                user_id.setText(s);
                //user = new UserData(s, a);
                user = UserData.createUserData(s, a, testMode.equals("test"));

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseManager.Initialize(user, db, a);
                Log.d(TAG, "Loading user friendslist... " + FirebaseManager.getUserFriendList().getUserFriends().size());
                for (UserFriend userFriend : FirebaseManager.getUserFriendList().getUserFriends())
                    FirebaseManager.subscribeToNotificationsTopic(userFriend.getUserID());

                updateGoal.updateGoalDisplay(a, user);
                if (user.isFirstLogIn()) {
                    // set height
                    launchHeight();
                }
            }
        });
    }

    /**
     * Prompts for height
     */
    public void launchHeight() {
        Intent intent = new Intent(this, Height.class);
        intent.putExtra("userID",fitnessService.getUserID());
        startActivity(intent);
    }

    /**
     * Shows weekly summary graph
     */
    public void launchGraph(){
        Intent intent = new Intent(this, UserHistoryGraphPage.class);
        intent.putExtra("userID",fitnessService.getUserID());
        startActivity(intent);
    }

    /**
     * Called by FitnessService to update the step counts
     * in the user and on display
     */
    public void setStepCount(long stepCount) {
        final long s = stepCount;
        final HomePage a = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                user.setDailyTotalSteps(s);
                textSteps.setText(String.valueOf(user.getTotalSteps()));
                textDistance.setText(String.format(Locale.getDefault(),
                        "(%.1f miles)", user.getPreviousDistance()));
                updateGoal.update(user, a);
                }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//       If authentication was required during google fit setup, this will be called after the user authenticates
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == fitnessService.getRequestCode()) {
                fitnessService.updateStepCount();
            }
        } else {
            Log.e(TAG, "ERROR, google fit result code: " + resultCode);
            fitnessService.deleteObserver(this);
        }
    }

    /**
     * Checks for fitnessService update every second
     */
    HomePage page = this;
    private class TrackNotPlannedSteps implements Runnable {
        @Override
        public void run(){
            while(true) {
                try {
                    Thread.sleep(1000);
                    update.post(new Runnable() {
                        @Override
                        public void run() {
                            // MUST CHECK Calendar before FitnessService, to ensure data saving
                            if (calendar.checkNewDate()){   // THIS NOT ONLY RETURNS BOOLEAN
                                // It also notifies every listener
                                fitnessService.resetStepsOffset();
                            }
                            fitnessService.updateStepCount();
                        }
                    });
                } catch (Exception e) {Log.d(TAG, "exception thrown");}
            }
        }
    }
}
