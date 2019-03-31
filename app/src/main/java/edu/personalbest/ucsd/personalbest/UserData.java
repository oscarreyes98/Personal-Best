package edu.personalbest.ucsd.personalbest;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.Observer;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.Observable;
import java.util.Observer;
import edu.personalbest.ucsd.personalbest.Calendar.ICalendar;
import edu.personalbest.ucsd.personalbest.Calendar.SharedCalendarFactory;

/**
 * To ensure different instances of a UserData can share the same variables
 * We keep everything in the sharedPreference
 * Note that previously we used a static week array to achieve this,
 * but now we want multiple users to co-exist in our code.
 */
public class UserData implements Observer, Serializable {

    /**
     * SharedPreferences Fields:
     * totalSteps = total steps of the day
     * intentSteps = planned walk steps of the day
     * intentTime = planned walk time of the day
     * incidentalSteps = unplanned steps of the day
     * goal = goal of today
     * goalNext = goal of the future
     * stride_length = my stride length
     * height = my height (unused)
     * encouragement = encouragement received
     * day = dayOfWeek today
     * date = date today
     */

    // For data saving and retrieval
    private final String userID;
    public final SharedPreferences sharedPreferences;
    public final SharedPreferences.Editor dataEditor;
    private final String TAG = "UserData";
    private boolean test = false;           // Don't use firebase in unit tests

    // For dates
    private ICalendar calendar;
    public UserData(String userID, Activity activity){
        this(userID, activity, false);
    }

    public UserData(String userID, Activity activity, boolean test){
        /* initialize once */
        this.userID = userID;
        sharedPreferences = activity.getSharedPreferences(userID, Context.MODE_PRIVATE);
        dataEditor = sharedPreferences.edit();
        calendar = SharedCalendarFactory.getSharedCalendar(SharedCalendarFactory.DEFAULT, this);

        if(!test)
            FirebaseManager.Initialize(this, FirebaseFirestore.getInstance(), activity);

        /* Sync from data */
        setDayOfWeek(calendar.getDayOfWeek() - 1);
        setDate(calendar.getDate());

        if(userID.equals("Test User"))
            setHeight(80);
    }

    public String getUserID() {
        return userID;
    }

    private final String STR_LAST_FIREBASE_UPLOAD = "last_firebase_upload";
    // Upload frequency for step data -- one hour
    private final long STEP_DATA_UPLOAD_DELAY = 1000 * 60 * 60;

    public void storeDayData(String date){
        DayData toStore = new DayData(getTotalSteps(), getPlannedSteps(), getGoal(), getPlannedWalkTime(), gotEncouragement(date));
        String data = toStore.toString();
        dataEditor.putString(date, data).apply();

        if(test)
            return;

        long now = System.currentTimeMillis();
        long last_upload = sharedPreferences.getLong(STR_LAST_FIREBASE_UPLOAD, 0);
        if (now > (last_upload + STEP_DATA_UPLOAD_DELAY)) {
            Log.d("UserData", "Uploading day data - last upload: " + last_upload);
            dataEditor.putLong(STR_LAST_FIREBASE_UPLOAD, now);
            FirebaseManager.storeDayData(date, data);// it is dead
        } else {
            Log.d("UserData", "Not uploading Step Data");
        }
    }

    public void clearDayData() {
        dataEditor.putLong("totalSteps", 0).apply();
        dataEditor.putLong("intentSteps", 0).apply();
        dataEditor.putLong("intentTime", 0).apply();
        dataEditor.putLong("incidentalSteps", 0).apply();
        dataEditor.putLong("goal", getGoalNext()).apply();
        dataEditor.putBoolean("encouragement", false).apply();
    }

    @Override
    public void update(Observable observable, Object arg) {
        if (observable instanceof ICalendar) {
            String date = getDate();
            String newDate = (String) arg;
            if( !newDate.equals(date) ) {
                Log.d(TAG, "New Day; Updating");
                storeDayData(date);
                clearDayData();
                setDayOfWeek(calendar.getDayOfWeek() - 1);
                setDate(calendar.getDate());
            }
        }
    }

    public long getTotalSteps() {
        return sharedPreferences.getLong("totalSteps", 0);

    }

    public long getPlannedSteps() {
        return sharedPreferences.getLong("intentSteps", 0);
    }

    public long getGoal() {
        return sharedPreferences.getLong("goal", getGoalNext());
    }

    public long getPlannedWalkTime() {
        return sharedPreferences.getLong("intentTime", 0);
    }

    public float getStrideLength() {
        return sharedPreferences.getFloat("stride_length", 0);
    }

    public float getHeight() {
        return sharedPreferences.getFloat("height", 0);
    }

    public String getDate() {
        return sharedPreferences.getString("date", "0/0/0");
    }

    public int getDayOfWeek() {
        return sharedPreferences.getInt("dayOfWeek", 1);
    }

    public void setDate(String date) {
        dataEditor.putString("date", date).apply();
    }

    public void setDayOfWeek(int dayOfWeek) {
        dataEditor.putInt("dayOfWeek", dayOfWeek).apply();
    }

    public boolean gotEncouragement(String date) {
        if (date.equals(getDate()))
            return sharedPreferences.getBoolean("encouragement", false);
        DayData day = new DayData(date, getGoalNow());
        return day.gotEncouragement;
    }

    public void setEncouragement(String date, boolean didSet){
        if (date.equals(getDate()))
            dataEditor.putBoolean("encouragement", didSet).apply();
        else {
            DayData day = new DayData(date, getGoalNow());
            day.setEncouragement(didSet);
            dataEditor.putString(date, day.toString()).apply();
        }
    }

    public void setIncidentalSteps() {
        long total = getTotalSteps();
        long planned = getPlannedSteps();
        dataEditor.putLong("incidentalSteps", total - planned).apply();
    }

    /**
     * Setters
     */

    public void setDailyTotalSteps(long steps) {
        if (steps > getTotalSteps()) {
            dataEditor.putLong("totalSteps", steps).apply();
            setIncidentalSteps();
        }
    }

    public void setDailyPlannedSteps(long steps) {
        if (steps > getPlannedSteps()) {
            dataEditor.putLong("intentSteps", steps).apply();
            setIncidentalSteps();
        }
    }

    public void incrementDailyPlannedSteps(long steps) {
        dataEditor.putLong("intentSteps", steps + getPlannedSteps()).apply();
        setIncidentalSteps();
    }

    public void setPlannedWalkTime(long milliseconds) {
        if (milliseconds > getPlannedWalkTime())
            dataEditor.putLong("intentTime", milliseconds).apply();
    }

    public void incrementPlannedWalkTime(long milliseconds) {
        dataEditor.putLong("intentTime", milliseconds + getPlannedWalkTime()).apply();
    }

    /**
     * Stride length related calculations
     */

    public float getPreviousDistance() {
        return getTotalSteps() * getStrideLength();
    }

    public float getNewDistance(long newSteps) {
        return getStrideLength() * newSteps;
    }

    public float getWalkSpeed(long newSteps, long milliseconds) {
        return (float) newSteps * getStrideLength() * 3600000 / milliseconds;
    }

    public float getWalkSpeed(long newSteps, float hours) {
        return ((float) newSteps * getStrideLength()) / hours;
    }

    /**
     * Height and stride length settings
     */
    public void setHeight(double h) {
        float height = (float) h;
        float stride_length = (float) (height * 0.413 / 63360);
        dataEditor.putFloat("height", height).apply();
        dataEditor.putFloat("stride_length", stride_length).apply();
    }

    public boolean isFirstLogIn() {
        if(userID.equals("default") || userID.equals("No User"))
            return false;
        if (getStrideLength() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public DayData[] createWeekData() {
        return createWeekData(getDate());
    }

    public DayData[] createWeekData(String date) {
        storeDayData(date);
        DayData[] week = new DayData[7];
        ICalendar cal = SharedCalendarFactory.getNewCalendar(SharedCalendarFactory.MOCK_CALENDAR, null);
        cal.setDate(date);
        cal.incrementDay(-7);
        long lastGoal = new DayData(cal.getDate()).dayGoal;
        for (int i = 0; i < 7; i++) {
            cal.incrementDay();
            week[i] = new DayData(cal.getDate(), lastGoal);
            lastGoal = week[i].dayGoal;
        }
        return week;
    }

    public DayData[] createMonthData() {
        return createMonthData(getDate());
    }

    public DayData[] createMonthData(String date) {
        storeDayData(date);
        DayData[] month = new DayData[28];
        ICalendar cal = SharedCalendarFactory.getNewCalendar(SharedCalendarFactory.MOCK_CALENDAR, null);
        cal.setDate(date);
        cal.incrementDay(-28);
        long lastGoal = new DayData(cal.getDate()).dayGoal;
        for (int i = 0; i < 28; i++) {
            cal.incrementDay();
            month[i] = new DayData(cal.getDate(), lastGoal);
            lastGoal = month[i].dayGoal;
        }
        return month;
    }


    private String loadStringData(String key) {
        return sharedPreferences.getString(key, userID);
    }

    public String loadUserFriendListData() {
        return loadStringData("friends_list");
    }
/*
    public void addFriend(String userID, boolean isPending) {
        friendList.addFriend(userID, isPending);
        storeUserFriendListData();
    }

    /*
    public UserFriendList getFriendList() {
        return friendList;
    }

    public void acceptPendingFriend(String friendID) {
        friendList.acceptPendingFriendRequest(friendID);
        storeUserFriendListData();
    }

    public void removeFriend(String friendID) {
        friendList.removeFriend(friendID);
        storeUserFriendListData();
    }*/

    private void storeStringData(String key, String data) {
        dataEditor.putString(key, data).apply();
    }

    public void storeUserFriendListData(String data) {
        storeStringData("friends_list", data);
    }

    public float[][] loadMonthStepData(){
        float[][] weekStepData = new float[28][2];
        DayData[] month = createMonthData();
        for (int i = 0; i < 28; i++) {
            weekStepData[i][0] = month[i].getIncidentSteps();
            weekStepData[i][1] = month[i].getIntentSteps();
        }
        return weekStepData;
    }

    /**
     * Returns the week's unplanned and planned steps
     */
    public float[][] loadWeekStepData() {
        float[][] weekStepData = new float[7][2];
        DayData[] month = createWeekData();
        for (int i = 0; i < 7; i++) {
            weekStepData[i][0] = month[i].getIncidentSteps();
            weekStepData[i][1] = month[i].getIntentSteps();
        }
        return weekStepData;
    }

    /**
     * Returns the week's daily goals
     */
    public int[] loadGoalData() {
        int[] goalData = new int[7];
        DayData[] week = createWeekData();
        for (int i = 0; i < 7; i++) {
            goalData[i] = (int) week[i].getDayGoal();
        }
        return goalData;
    }

    public int[] loadMonthGoalData() {
        int[] goalData = new int[28];
        DayData[] month = createMonthData();
        for (int i = 0; i < 28; i++) {
            goalData[i] = (int) month[i].getDayGoal();
        }
        return goalData;
    }

    /**
     * Returns the week's daily intentional walking time
     */
    public long[] loadWalkTimeData() {
        long[] walkTimeData = new long[7];
        DayData[] week = createWeekData();
        for (int i = 0; i < 7; i++) {
            walkTimeData[i] = week[i].intentTime;
        }
        return walkTimeData;
    }


    /**
     * To set goals for the future
     */
    public float[] loadMonthWalkTimeData() {
        float[] walkTimeData = new float[28];
        DayData[] month = createMonthData();
        for (int i = 0; i < 28; i++) {
            walkTimeData[i] = (month[i].intentTime / (float) 1000) / (float) 3600;
        }
        return walkTimeData;
    }

    public String[] loadMonthDates() {
        String[] dates = new String[28];
        ICalendar cal = SharedCalendarFactory.getNewCalendar(SharedCalendarFactory.MOCK_CALENDAR, null);
        cal.setDate(this.getDate());
        cal.incrementDay(-28);
        for (int i = 0; i < 28; i++) {
            cal.incrementDay();
            int[] fields = cal.getDateFields();
            dates[i] = (fields[1] + 1) + "/" + fields[0] + "/" + fields[2];
        }
        return dates;
    }

    public ICalendar getDateOneMonthAgo () {
        ICalendar cal = SharedCalendarFactory.getNewCalendar(SharedCalendarFactory.MOCK_CALENDAR, null);
        cal.setDate(this.getDate());
        cal.incrementDay(-28);
        return cal;
    }

    public void setGoalToday ( long goal){
        dataEditor.putLong("goal", goal).apply();
        dataEditor.putLong("goalNext", goal).apply();
    }

    public void setGoalTomorrow ( long goal){
        dataEditor.putLong("goalNext", goal).apply();
    }

    public long getGoalNow () {
        return sharedPreferences.getLong("goal", getGoalNext());
    }

    public long getGoalNext () {
        return sharedPreferences.getLong("goalNext", 5000);
    }

    private class DayData {
        private long dayGoal = 5000;
        private long stepCount, intentSteps, incidentSteps, intentTime; // TIME IN MILLISECONDS
        private boolean gotEncouragement;
        private final String TAG = "DayData";

        /**
         * default constructor for no-record day
         */
        private DayData() {
            dayGoal = getGoalNext();
            stepCount = 0;
            intentSteps = 0;
            incidentSteps = 0;
            intentTime = 0;
            gotEncouragement = false;
        }

        /**
         * parser constructor for historical data in storage
         */
        private DayData(String date, long defaultGoal) {
            String toParse = sharedPreferences.getString(date, "");
            try {
                String[] parsed = toParse.split("#", 6);
                dayGoal = Long.parseLong(parsed[0]);
                stepCount = Long.parseLong(parsed[1]);
                intentSteps = Long.parseLong(parsed[2]);
                incidentSteps = Long.parseLong(parsed[3]);
                intentTime = Long.parseLong(parsed[4]);
                gotEncouragement = Boolean.parseBoolean(parsed[5]);
            } catch(Exception e){
                Log.d(TAG, "Invalid String Given for Parsing");
                dayGoal = defaultGoal;
                stepCount = 0;
                intentSteps = 0;
                incidentSteps = 0;
                intentTime = 0;
                gotEncouragement = false;
            }
        }

        private DayData(String toParse) {
            try {
                String[] parsed = toParse.split("#", 6);
                dayGoal = Long.parseLong(parsed[0]);
                stepCount = Long.parseLong(parsed[1]);
                intentSteps = Long.parseLong(parsed[2]);
                incidentSteps = Long.parseLong(parsed[3]);
                intentTime = Long.parseLong(parsed[4]);
                gotEncouragement = Boolean.parseBoolean(parsed[5]);
            } catch(Exception e){
                Log.d(TAG, "Invalid String Given for Parsing");
                dayGoal = 5000;
                stepCount = 0;
                intentSteps = 0;
                incidentSteps = 0;
                intentTime = 0;
                gotEncouragement = false;
            }
        }

        /**
         * parameter based constructor for creating new historical data
         */
        private DayData(long total_steps, long planned_steps, long goal, long time, boolean gotEncouragement) {
            this.dayGoal = goal;
            this.stepCount = total_steps;
            this.intentSteps = planned_steps;
            this.incidentSteps = total_steps - intentSteps;
            this.intentTime = time;
            this.gotEncouragement = gotEncouragement;
        }

        /**
         * Setters
         */
        public void setEncouragement(boolean didSet) {
            this.gotEncouragement = didSet;
        }

        private void setDayGoal(long goal) {
            this.dayGoal = goal;
        }

        private long getDayGoal() {
            return this.dayGoal;
        }

        private void setIntentSteps(long steps) {
            this.intentSteps = steps;
        }

        private long getIntentSteps() {
            return this.intentSteps;
        }

        private void setIncidentSteps(long steps) {
            this.incidentSteps = steps;
        }


        private long getIncidentSteps() {
            return this.incidentSteps;
        }

        private void setIntentTime(long time) {
            this.intentTime = time;
        }

        /**
         * Print the day as a string that is parsable by the ctor
         */
        public String toString() {
            return dayGoal + "#" + stepCount + "#" + intentSteps + "#" + incidentSteps + "#"
                    + intentTime + "#" + String.valueOf(gotEncouragement);
        }
    }

    public static UserData createUserData(String userID, Activity activity) {
        UserData ud = new UserData(userID, activity);
        return ud;
    }

    public static UserData createUserData(String userID, Activity activity, boolean test) {
        UserData ud = new UserData(userID, activity, test);
        return ud;
    }

}