package edu.personalbest.ucsd.personalbest.Calendar;

import java.util.Calendar;

public class CalendarAdapter extends ICalendar {
    Calendar calendar;
    int today;

    public CalendarAdapter(){
        calendar = Calendar.getInstance();
        today = calendar.get(Calendar.DAY_OF_WEEK);
    }


    public int getDayOfWeek(){
        today =  calendar.get(Calendar.DAY_OF_WEEK);
        return today;
    }

    public void incrementDay(){
        // Not allowed for real Cal
    }

    public void incrementDay(int amount){
        // Not allowed for real Cal
    }

    public void setDate(int y, int m, int d){
        // Not allowed for real Cal
    }

    public void setDate(String date){
        // Not allowed for real Cal
    }

    public boolean checkNewDate(){
        if(today!= calendar.get(Calendar.DAY_OF_WEEK)){
            today =  calendar.get(Calendar.DAY_OF_WEEK);
            setChanged();
            notifyObservers(getDate());
            return true;
        }
        return false;
    }

    public String getDate(){
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        String monthString = Integer.toString(month);
        String dayString = Integer.toString(day);
        if(month < 10){
            monthString = "0"+monthString;
        }
        if(day < 10){
            dayString = "0"+dayString;
        }

        return year+"/"+monthString+"/"+dayString;
    }

    public String getDateWithoutSlash(){
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        String monthString = Integer.toString(month);
        String dayString = Integer.toString(day);
        if(month < 10){
            monthString = "0"+monthString;
        }
        if(day < 10){
            dayString = "0"+dayString;
        }

        return year+monthString+dayString;
    }

    public int[] getDateFields(){
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        return new int[]{day,month,year};
    }


    public void clearMockedTime(){}
}
