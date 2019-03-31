package edu.personalbest.ucsd.personalbest;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;


import java.util.Calendar;


import edu.personalbest.ucsd.personalbest.Calendar.ICalendar;
import edu.personalbest.ucsd.personalbest.Fitness.FitnessService;
import edu.personalbest.ucsd.personalbest.Fitness.SharedFitnessFactory;
import edu.personalbest.ucsd.personalbest.Calendar.SharedCalendarFactory;

//Do encouragement stuff and updating goal stuff
public class GoalObserver {
    public boolean reachedGoal = false;
    private final int NEW_GOAL_ADDITION = 500;
    private static final String TAG = "GoalObserver";
    private FitnessService fitnessService;
    ICalendar calendar;

    AlertDialog dialog;


    public GoalObserver(){
        calendar = SharedCalendarFactory.getSharedCalendar(SharedCalendarFactory.DEFAULT, null);
    }

    public void update(UserData user, HomePage homePage) {
        long goal = user.getGoal();
        if (!reachedGoal && user.getTotalSteps() >= goal) {
            reachedGoal = true;
            goalMet(homePage, user);//WILL probably need to change where this is called.
        }

        fitnessService = SharedFitnessFactory.getSharedService(SharedFitnessFactory.DEFAULT, null);
        if (fitnessService.getUserID() != null) {
            subGoalMet(homePage);
        }

    }


    public void newDayUpdate(HomePage homePage, UserData user) {
        reachedGoal = false;
        updateGoalDisplay(homePage, user);
    }

    public void changeGoal(final UserData user, final HomePage homePage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(homePage);
        builder.setTitle("Would you like to increase your goal?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                long goal = user.getGoal();
                user.setGoalTomorrow(goal+NEW_GOAL_ADDITION);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // no new goal!
            }
        });
        builder.setNeutralButton("Customize", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                customizeGoal(user, homePage, false);
            }
        });

        // --- Magic below ---
        builder.setCancelable(false);
        // --- Magic above ---

        dialog = builder.create();
        dialog.show();
    }

    public void customizeGoal(final UserData user, final HomePage homePage, final boolean update) {
        AlertDialog.Builder builder = new AlertDialog.Builder(homePage);
        builder.setTitle("Customize Goal");
        builder.setMessage("Set Goal To: ");

        // Set up the input
        final EditText input = new EditText(homePage);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        //input new goal somehow
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final String num = input.getText().toString();
                if (!(num.length() == 0)) {
                    long newGoal = Integer.parseInt(num);
                    user.setGoalToday(newGoal);
                    updateGoalDisplay(homePage, user);
                }
            }
        });

        // --- Magic below ---
        builder.setCancelable(false);
        // --- Magic above ---

        dialog = builder.create();
        dialog.show();
    }

    public void updateGoalDisplay(HomePage homePage, UserData user) {
        TextView goalDisplay = homePage.findViewById(R.id.step_goal);
        long goal = user.getGoal();
        goalDisplay.setText("Goal: " + goal + " steps");
    }

    //Shows encouragement for reaching general step goal
    private void goalMet(final HomePage homePage, final UserData user){
        AlertDialog.Builder builder = new AlertDialog.Builder(homePage);

        long goal = user.getGoal();
        builder.setTitle("Congrats, you've reach your goal of " + goal + " steps!");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                changeGoal(user, homePage);
            }
        });

        // --- Magic below ---
        builder.setCancelable(false);
        // --- Magic above ---

        dialog = builder.create();
        dialog.show();
    }

    //Shows encouragement for reaching sub-goal
    private void subGoalMet(HomePage page){
        final HomePage p = page;
        final UserData user = p.user;
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); // SHOULD USE THE SHARED CALENDAR
        ICalendar dateCal = SharedCalendarFactory.getNewCalendar(SharedCalendarFactory.MOCK_CALENDAR, null);
        dateCal.setDate(user.getDate());

        float[][] steps = user.loadWeekStepData();
        long todayS = (long)steps[6][0] + (long)steps[6][1];
        long yesterdayS = (long)steps[5][0] + (long)steps[5][1];
        long dayBeforeS = (long)steps[4][0] + (long)steps[4][1];

        //////// YESTERDAY CASE //////
        dateCal.incrementDay(-1);
        String yesterday = dateCal.getDate();
        if(!p.user.gotEncouragement(yesterday) ) {
            //encouragement should be displayed
            if (yesterdayS > (dayBeforeS + 499)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(page);
                Log.d(TAG,"in yesterday encouragement if statement");
                if (((yesterdayS - dayBeforeS) % 500) == 0) {
                    builder.setTitle("Congrats, you've walked " + (yesterdayS - dayBeforeS) + " more steps than yesterday");
                } else {
                    int diff = (int) (yesterdayS - dayBeforeS) / (int) 500;
                    builder.setTitle("Congrats, you've walked over " + (diff * 500) + " more steps than yesterday");
                }
                p.user.setEncouragement(yesterday,true);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.show();
            }

        }

        //////// TODAY CASE ////////////
        if(currentHour >= 20 && currentHour < 24) {
            Log.d(TAG, "in today encouragement if statement");
            AlertDialog.Builder builder = new AlertDialog.Builder(page);
            if (!p.user.gotEncouragement(p.user.getDate())) {
                //encouragement should be displayed
                if (todayS > (yesterdayS + 499)) {
                    if (((todayS - yesterdayS) % 500) == 0) {
                        builder.setTitle("Congrats, you've walked " + (todayS - yesterdayS) + " more steps than yesterday");
                    } else {
                        int diff = (int) (todayS - yesterdayS) / (int) 500;
                        builder.setTitle("Congrats, you've walked over " + (diff * 500) + " more steps than yesterday");
                    }

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    p.user.setEncouragement(p.user.getDate(), true);
                    builder.setCancelable(false);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        }

    }

}
