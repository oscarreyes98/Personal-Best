package edu.personalbest.ucsd.personalbest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import static org.junit.Assert.assertEquals;

import android.content.SharedPreferences;


@RunWith(RobolectricTestRunner.class)
public class EncouragementTest {
    private HomePage homepage;
    private GoalObserver goalObserver;
    private UserData user;


    @Before
    public void setup() {
        homepage = Robolectric.setupActivity(HomePage.class);
        goalObserver = homepage.updateGoal;
        user = new UserData("default", homepage);

    }

    @Test
    public void testGoalObserver_GoalMet() {
        homepage.setStepCount(5000);
        goalObserver.update(user, homepage);
        assertEquals(true, goalObserver.reachedGoal);
    }

}