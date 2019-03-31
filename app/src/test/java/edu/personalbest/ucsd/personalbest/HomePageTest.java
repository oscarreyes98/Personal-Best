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
public class HomePageTest {
    private String TEST_USER = "User";
    private HomePage homepage;
    private TextView step_goal;
    private TextView step_total;
    private TextView dist_total;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor dataEditor;

    @Before
    public void setup() {
        SharedCalendarFactory.setDefault(SharedCalendarFactory.MOCK_CALENDAR);
        SharedFitnessFactory.setDefault(SharedFitnessFactory.MOCKFITNESS);

        // create object
        Activity activity = Robolectric.setupActivity(Activity.class);
        Intent intent = new Intent(activity, Activity.class);
        intent.putExtra("mode", "test");
        homepage = Robolectric.buildActivity(HomePage.class, intent).create().start().get();

        step_goal = homepage.findViewById(R.id.step_goal);
        step_total = homepage.findViewById(R.id.step_total);
        dist_total = homepage.findViewById(R.id.dist_total);

        // test tools
        sharedPreferences = homepage.getSharedPreferences(TEST_USER, Context.MODE_PRIVATE);
        dataEditor = sharedPreferences.edit();
        dataEditor.clear();
        dataEditor.putFloat("stride_length", (float)0.0004);
        homepage.updateAccount(TEST_USER);
    }

    @Test
    public void testHomePage_StepsDisplay() {
        homepage.setStepCount(199);
        assertEquals("199", step_total.getText().toString());
    }

    @Test
    public void testHomePageGoalDisplay() {
    }

    @Test
    public void testHomePage_MilesDisplay() {
        homepage.setStepCount(299);
        float stride_length = sharedPreferences.getFloat("stride_length", (float)0);
        String milesDisplay = String.format("(%.1f miles)", 299 * stride_length);
        assertEquals(milesDisplay, dist_total.getText().toString());
    }

    @Test
    public void testHomePage_StepsStorage() {
        homepage.setStepCount(399);
        assertEquals(399, sharedPreferences.getLong("totalSteps", -1));
    }

}