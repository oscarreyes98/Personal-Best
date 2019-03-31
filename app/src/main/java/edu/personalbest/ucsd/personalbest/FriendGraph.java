package edu.personalbest.ucsd.personalbest;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.github.mikephil.charting.charts.CombinedChart;

import java.util.HashMap;
import java.util.Map;

import edu.personalbest.ucsd.personalbest.Calendar.ICalendar;
import edu.personalbest.ucsd.personalbest.Calendar.SharedCalendarFactory;

public class FriendGraph extends AppCompatActivity implements Consumer<Map<String,String>> {

    Map<String,String> step_data;
    CombinedChart friendGraph;
    float[][] stepData;
    int[] goalData;
    boolean requestAccepted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_graph);

        friendGraph = findViewById(R.id.stacked_friend_history_graph);

        //This is the friend id for which we will get graph history.
        String friendId = getIntent().getStringExtra("friendID");
        //Updating string so it's only the last part of the string, after the '@' sign.

        //Get data from Firebase using the id
        FirebaseManager.requestStepDataForUser(friendId, this);


        Toolbar myToolbar = findViewById(R.id.toolbar_friend_graph);
        myToolbar.setTitle("Activity for " + friendId);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        Log.d(this.getClass().getSimpleName(), "Our friend id for graph is: " + friendId);

    }


    public void createGraph(){

        HistoryGraphCreator graphCreator = new HistoryGraphCreator(friendGraph, HistoryGraphCreator.FRIEND);

        ICalendar cal = SharedCalendarFactory.getNewCalendar(SharedCalendarFactory.MOCK_CALENDAR, null);
        cal.incrementDay(-28);

        graphCreator.axisSetup(cal)
                .graphDataSetup(stepData, goalData)
                .interactionSetup()
                .legendSetup();

        friendGraph = graphCreator.getGraph();
        friendGraph.notifyDataSetChanged();
        friendGraph.invalidate();
    }

    public void parseData(){

        stepData = new float[28][2];
        goalData = new int[28];

        if(step_data.isEmpty()){
            for(int i = 0 ; i < 28 ; i++){
                goalData[i] = 5000;
            }
        } else{
            ICalendar cal = SharedCalendarFactory.getNewCalendar(SharedCalendarFactory.MOCK_CALENDAR, null);
            String dateWOSlashes, data;

            for(int i = 0 ; i < 28; i++){
                dateWOSlashes = cal.getDateWithoutSlash();
                if(step_data.containsKey(dateWOSlashes)){
                    data = step_data.get(dateWOSlashes);
                    stepData[27-i] = parseForSteps(data);
                    goalData[27-i] = parseForGoal(data);
                } else{
                    if(i != 0) {
                        goalData[27 - i] = goalData[27 - i + 1];
                    }
                }
                cal.incrementDay(-1);
            }
            for(int i = 1 ; i < 28 ; i++){
                if(goalData[i] == 0 && i > 0){
                    goalData[i] = goalData[i-1];
                }
            }
        }

    }

    private int parseForGoal(String str){
        String[] parsedStrings = str.split("#");
        return Integer.valueOf(parsedStrings[0]);
    }

    private float[] parseForSteps(String str){
        String[] parsedStrings = str.split("#");
        float[] steps = new float[2];
        steps[0] = Float.valueOf(parsedStrings[3]);
        steps[1] = Float.valueOf(parsedStrings[2]);
        return steps;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void accept(Map<String,String> arg) {
        requestAccepted = true;
        System.out.println("ACCEPTED");
        if(arg != null){
            step_data = arg;
        }
        else{
            step_data = new HashMap<>();
        }

        //Parse data retrieved for graphing
        parseData();

        //Create the graph using the builder
        createGraph();
    }

    @Override
    public void reject(){
        requestAccepted = false;
    }
}
