package edu.personalbest.ucsd.personalbest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;


import java.util.Objects;


public class UserHistoryGraphPage extends AppCompatActivity {

    public CombinedChart monthHistory;
    public UserData user;
    public float[][] monthStepData;
    public int[] goalData;
    public float[] walkTime;
    public float[] walkDist = new float[28];
    public float[] walkSpeeds= new float[28];
    public String[] dateNames;

    TextView selectedGoal;
    TextView totalSteps;
    TextView date;
    TextView intentSteps;
    TextView walkDistance;
    TextView walkingTime;
    TextView avgSpeed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_history_graph);

        monthHistory = findViewById(R.id.stacked_user_history_graph);
        selectedGoal = findViewById(R.id.selected_goal);
        totalSteps = findViewById(R.id.selected_total_steps);
        date = findViewById(R.id.day_of_week_text);
        intentSteps = findViewById(R.id.Intentional_steps);
        walkDistance = findViewById(R.id.walk_dist);
        walkingTime = findViewById(R.id.walk_time);
        avgSpeed = findViewById(R.id.avg_walk_speed);

        String userName = Objects.requireNonNull(getIntent().getExtras()).getString("userID");
        user = new UserData(userName, this);
        loadMonthInfo();

        HistoryGraphCreator graphCreator = new HistoryGraphCreator(monthHistory, HistoryGraphCreator.USER);
        graphCreator.axisSetup(user.getDateOneMonthAgo())
                    .graphDataSetup(loadStepData(), loadGoalData())
                    .interactionSetup()
                    .legendSetup();

        monthHistory = graphCreator.getGraph();

        monthHistory.setOnChartValueSelectedListener(new OnChartValueSelectedListener(){
            @Override
            public void onValueSelected(Entry e, Highlight h ){
                int day = (int)(e.getX() - 0.5);
                String goalString, totalStepsString, dateString, intentStepsString, distanceString, timeString, speedString;

                goalString = "Goal: " + goalData[day];
                totalStepsString = "Total Steps: " + (long)(monthStepData[day][0]+ monthStepData[day][1]);
                dateString = "Date: " + dateNames[day];
                intentStepsString = "Intentional Steps: " + (long) monthStepData[day][1];
                distanceString = "Walk Distance: " + walkDist[day] + " miles";
                timeString = "Walk Time: " + walkTime[day] + " hours";
                speedString = "Average Walk Speed: " + walkSpeeds[day] + " mph";

                selectedGoal.setText(goalString);
                totalSteps.setText(totalStepsString);
                date.setText(dateString);
                intentSteps.setText(intentStepsString);
                walkDistance.setText(distanceString);
                walkingTime.setText(timeString);
                avgSpeed.setText(speedString);

            }

            @Override
            public void onNothingSelected() {
                //Nothing to do~
            }
        });

    }

    // Helper method for graph interaction
    // Displays this information
    private void loadMonthInfo(){
        monthStepData = user.loadMonthStepData();
        goalData = user.loadMonthGoalData();
        walkTime = user.loadMonthWalkTimeData();
        dateNames = user.loadMonthDates();

        for(int i = 0; i < 28; i++) {
            walkDist[i] = (monthStepData[i][0] + monthStepData[i][1]) * user.getStrideLength();
            if(walkTime[i] == 0){
                walkSpeeds[i] = 0;
            }
            else {
                walkSpeeds[i] = user.getWalkSpeed((long) (monthStepData[i][1]), walkTime[i]);
            }
        }
    }

    // Helper method to load previous month's step data from UserData
    private float[][] loadStepData(){
        return user.loadMonthStepData();
    }

    // Helper method to load previous month's goal data
    private int[] loadGoalData(){
        return user.loadMonthGoalData();
    }


}
