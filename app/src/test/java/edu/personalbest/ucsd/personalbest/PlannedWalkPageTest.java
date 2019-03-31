package edu.personalbest.ucsd.personalbest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.TextView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import edu.personalbest.ucsd.personalbest.Calendar.SharedCalendarFactory;
import edu.personalbest.ucsd.personalbest.Fitness.SharedFitnessFactory;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class PlannedWalkPageTest {
    private String TEST_USER = "User";
    private PlannedWalkPage plannedWalkPage;
    private TextView walk_step_display;
    private TextView walk_dist_display;
    private TextView walk_speed_display;
    private TextView total_step_display;
    private TextView total_dist_display;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor dataEditor;

    @Before
    public void setup(){
        SharedCalendarFactory.setDefault(SharedCalendarFactory.MOCK_CALENDAR);
        SharedFitnessFactory.setDefault(SharedFitnessFactory.MOCKFITNESS);

        // create objects
        Activity activity = Robolectric.setupActivity(Activity.class);
        Intent intent = new Intent(activity, Activity.class);
        intent.putExtra("userID", TEST_USER);
        intent.putExtra("previousSteps", 0);
        intent.putExtra("mode", "test");
        plannedWalkPage = Robolectric.buildActivity(PlannedWalkPage.class, intent).create().start().get();
        walk_step_display = plannedWalkPage.findViewById(R.id.walk_step_display);
        walk_dist_display = plannedWalkPage.findViewById(R.id.walk_dist_display);
        walk_speed_display = plannedWalkPage.findViewById(R.id.walk_speed_display);
        total_step_display = plannedWalkPage.findViewById(R.id.total_step_display);
        total_dist_display = plannedWalkPage.findViewById(R.id.total_dist_display);

        // test tools
        sharedPreferences = plannedWalkPage.getSharedPreferences(TEST_USER, Context.MODE_PRIVATE);
        dataEditor = sharedPreferences.edit();
        dataEditor.clear();
        dataEditor.putFloat("stride_length", (float)0.0004);
        plannedWalkPage.updateAccount(TEST_USER);
    } 

    @Test
    public void testWalkPage_TotalStepsAndDistance(){
        int expected = 199;
        plannedWalkPage.setStepCount(expected);
        assertEquals(Integer.toString(expected), total_step_display.getText().toString());
        float stride_length = sharedPreferences.getFloat("stride_length", (float)0);
        String milesDisplay = String.format("(%.1f miles)", expected * stride_length);
        // assertEquals(milesDisplay, total_dist_display.getText().toString());
    }

    @Test
    public void testWalkPage_PlannedStepsAndDistance(){
        int expected = 199;
        plannedWalkPage.setStepCount(expected);
        assertEquals(Integer.toString(expected), walk_step_display.getText().toString());
        float stride_length = sharedPreferences.getFloat("stride_length", (float)0);
        String milesDisplay = String.format("(%.1f miles)", expected * stride_length);
        assertEquals(milesDisplay, walk_dist_display.getText().toString());
    }

    @Test
    public void testWalkPage_PlannedMPH(){
        int stepCount = 6250;
        float stride_length = sharedPreferences.getFloat("stride_length", (float)0);
        String expected = String.format("%.2f", stepCount * stride_length);
        long mocked_walk_time = 3600000;
        long mock_time = System.currentTimeMillis() - mocked_walk_time;
        plannedWalkPage.setStarTime(mock_time);
        plannedWalkPage.setStepCount(stepCount);
        assertEquals(expected + " MPH", walk_speed_display.getText().toString());
    }
}