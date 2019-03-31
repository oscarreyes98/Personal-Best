package edu.personalbest.ucsd.personalbest;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;

import edu.personalbest.ucsd.personalbest.Calendar.ICalendar;
import edu.personalbest.ucsd.personalbest.Calendar.SharedCalendarFactory;
import edu.personalbest.ucsd.personalbest.Fitness.SharedFitnessFactory;

import static org.junit.Assert.assertEquals;


@RunWith(RobolectricTestRunner.class)
public class ProgressGraphTest {
    private String TEST_USER = "User";
    private UserHistoryGraphPage graphPage;

    @Before
    public void setup() {
    }

    @Test
    public void testLoadGraphInfo(){
        Activity activity = Robolectric.setupActivity(Activity.class);
        Intent intent0 = new Intent(activity, Activity.class);
        intent0.putExtra("mode", "test");
        HomePage homepage = Robolectric.buildActivity(HomePage.class, intent0).create().start().get();

        // set up environment
        UserData user = new UserData(TEST_USER, homepage, true);
        ICalendar calendar = SharedCalendarFactory.getSharedCalendar(SharedCalendarFactory.MOCK_CALENDAR, null);
        calendar.setDate(2000,1,1);

        // set up history
        user.setDailyTotalSteps(500);
        calendar.incrementDay();
        user.setHeight(60);
        user.setDailyTotalSteps(500);
        user.setDailyPlannedSteps(200);
        user.setGoalToday(6000);
        user.setPlannedWalkTime(100);
        calendar.incrementDay();

        // init graph
        Intent intent = new Intent(homepage, HomePage.class);
        intent.putExtra("userID", TEST_USER);
        graphPage = Robolectric.buildActivity(UserHistoryGraphPage.class, intent).create().start().get();

        // test graph history

        // steps
        float[][] monthStepData = new float[28][2];
        for(int i=0; i<28; i++)
            for(int j=0; j<2; j++)
                monthStepData[i][j] = 0;
        monthStepData[26][0] = 300;
        monthStepData[26][1] = 200;
        monthStepData[25][0] = 500;
        for(int i=0; i<28; i++)
            for(int j=0; j<2; j++)
                assertEquals(monthStepData[i][j], graphPage.monthStepData[i][j], 1e-2);

        // goals
        long[] goal = new long[28];
        for(int i=0; i<26; i++)
            goal[i] = 5000;
        goal[26] = 6000;
        goal[27] = 6000;
        for(int i=0; i<26; i++)
            assertEquals(goal[i], graphPage.goalData[i]);

        // distance and speed
        assertEquals(100/ (float)1000/(float)3600, graphPage.walkTime[26], 1e-2);
        assertEquals(500*user.getStrideLength(), graphPage.walkDist[26], 1e-2);
        assertEquals(user.getWalkSpeed(200, 100), graphPage.walkSpeeds[26], 1e-2);

    }
}