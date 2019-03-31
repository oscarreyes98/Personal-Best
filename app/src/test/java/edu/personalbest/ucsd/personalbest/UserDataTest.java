package edu.personalbest.ucsd.personalbest;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import static org.junit.Assert.assertEquals;


@RunWith(RobolectricTestRunner.class)
public class UserDataTest {
    private String THIS_USER = "This User";
    private String OTHER_USER = "Other User";
    private float HEIGHT = 50;
    private float STRIDE_LENGTH = (float) (HEIGHT * 0.413 / 63360);

    private Activity activity;
    private SharedPreferences thisSharedPreferences;
    private SharedPreferences.Editor thisEditor;
    private SharedPreferences otherSharedPreferences;
    private UserData thisUser;
    private UserData otherUser;

    @Before
    public void setup() {

        // create objects
        activity =  Robolectric.setupActivity(Activity.class);
        thisUser = new UserData(THIS_USER, activity, true);
        otherUser = new UserData(OTHER_USER, activity, true);

        // test tools
        thisSharedPreferences = activity.getSharedPreferences(THIS_USER, Context.MODE_PRIVATE);
        thisUser.setHeight(HEIGHT);

        otherSharedPreferences = activity.getSharedPreferences(OTHER_USER, Context.MODE_PRIVATE);
        thisUser.setHeight(HEIGHT);

        thisEditor = thisSharedPreferences.edit();
    }

    @Test
    public void testTotalSteps(){
        // set previous steps
        thisUser.setDailyTotalSteps(5000);
        // test get steps
        assertEquals(5000, thisUser.getTotalSteps());
    }

    @Test
    public void testPlannedSteps(){
        // test set planned steps
        thisUser.setDailyPlannedSteps(10);
        assertEquals(10, thisSharedPreferences.getLong("intentSteps", 0));

        // test set planned steps to override old steps
        thisUser.setDailyPlannedSteps(20);
        assertEquals(20, thisSharedPreferences.getLong("intentSteps", 0));

        // test increment planned steps
        thisUser.incrementDailyPlannedSteps(50);
        assertEquals(70, thisSharedPreferences.getLong("intentSteps", 0));
    }

    @Test
    public void testWalkTime(){
        // test set planned steps
        thisUser.setPlannedWalkTime(10);
        assertEquals(10, thisSharedPreferences.getLong("intentTime", 0));

        // test set planned steps to override old steps
        thisUser.setPlannedWalkTime(20);
        assertEquals(20, thisSharedPreferences.getLong("intentTime", 0));

        // test increment planned steps
        thisUser.incrementPlannedWalkTime(50);
        assertEquals(70, thisSharedPreferences.getLong("intentTime", 0));
    }


    @Test
    public void testCalculateDistance(){
        // test whole distance
        thisUser.setDailyTotalSteps(5000);
        assertEquals(5000 * STRIDE_LENGTH, thisUser.getPreviousDistance(), 1e-5);

        // test new distance
        assertEquals(100 * STRIDE_LENGTH, thisUser.getNewDistance(100), 1e-5);
    }

    @Test
    public void testCalculateSpeed(){
        long newSteps = 5312;
        long seconds = 10000;
        assertEquals(newSteps * STRIDE_LENGTH * 3600000 / seconds, thisUser.getWalkSpeed(newSteps, seconds), 1e-5);
    }

    @Test
    public void testUserSpecification(){
        thisUser.setDailyTotalSteps(1);
        otherUser.setDailyTotalSteps(2);
        assertEquals(1, thisSharedPreferences.getLong("totalSteps", 0));
        assertEquals(1, thisUser.getTotalSteps());
        assertEquals(2, otherSharedPreferences.getLong("totalSteps", 0));
        assertEquals(2, otherUser.getTotalSteps());
    }

    @Test
    public void testSetHeight(){
        float height = (float)30;
        float stride_length =  (float) (height * 0.413 / 63360);
        thisUser.setHeight(height);

        assertEquals(thisSharedPreferences.getFloat("height", 0), height, 1e-5);
        assertEquals(thisSharedPreferences.getFloat("stride_length", 0), stride_length, 1e-5);
    }

    @Test
    public void testCtors(){
        // create objects
        String NEW_USER = "New User";
        thisSharedPreferences = activity.getSharedPreferences(NEW_USER, Context.MODE_PRIVATE);
        thisEditor = thisSharedPreferences.edit();
        thisEditor.putLong("totalSteps", 123).apply();
        thisEditor.putFloat("stride_length", (float)234).apply();
        thisEditor.putLong("intentSteps",345).apply();
        thisEditor.putLong("goal",456).apply();
        thisEditor.putLong("intentTime", 567).apply();


        thisUser = new UserData(NEW_USER, activity, true);


        // test loading
        assertEquals(123, thisUser.getTotalSteps());
        assertEquals(234, thisUser.getStrideLength(), 1e-5);
        assertEquals(345, thisUser.getPlannedSteps());
        assertEquals(456, thisUser.getGoal());
        assertEquals(567, thisUser.getPlannedWalkTime());
    }

    @Test
    public void testCleanData(){
        // create objects
        String NEW_USER = "New User";
        thisSharedPreferences = activity.getSharedPreferences(NEW_USER, Context.MODE_PRIVATE);
        thisEditor = thisSharedPreferences.edit();
        thisEditor.putLong("totalSteps", 123).apply();
        thisEditor.putFloat("stride_length", (float)234).apply();
        thisEditor.putLong("intentSteps",345).apply();
        thisEditor.putLong("goal", 456).apply();
        thisEditor.putLong("goalNext", 567).apply();
        thisEditor.putLong("intentTime", 567).apply();
        thisUser = new UserData(NEW_USER, activity, true);
        // test clearing
        thisUser.clearDayData();
        assertEquals(0, thisUser.getTotalSteps());
        assertEquals(234, thisUser.getStrideLength(), 1e-5);
        assertEquals(0, thisUser.getPlannedSteps());
        assertEquals(567, thisUser.getGoal());
        assertEquals(0, thisUser.getPlannedWalkTime());
    }
}