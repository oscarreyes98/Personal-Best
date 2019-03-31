package edu.personalbest.ucsd.personalbest.Calendar;

import android.util.Log;

import java.util.Calendar;

public class MockCalendar extends ICalendar{

    private Calendar calendar;

    // always read from fields to ensure the calendar is frozen
    private int date = 0;
    private int month = 0;
    private int year = 0;
    public int day; // DAYS IN THIS OBJECT ARE INDEXED 1-7 == SUN-SAT
    public boolean mocking = false;

    public MockCalendar(){
        calendar = Calendar.getInstance();
        day = calendar.get(calendar.DAY_OF_WEEK);
        date = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);
        mocking = false;
    }

    public int getDayOfWeek() {
        return day;
    }

    public void incrementDay(){
        mocking = true;
        // calculate date
        calendar.set(year, month, date);
        calendar.add(Calendar.DATE,1);

        // update fields
        day = calendar.get(calendar.DAY_OF_WEEK);
        date = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);

        // notify observers
        setChanged();
        notifyObservers(getDate());
    }

    public void incrementDay(int amountOfDays){
        mocking = true;
        // calculate date
        calendar.set(year, month, date);
        calendar.add(Calendar.DATE,amountOfDays);

        // update fields
        day = calendar.get(calendar.DAY_OF_WEEK);
        date = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);

        // notify observers
        setChanged();
        notifyObservers(getDate());
    }

    @Override
    public void notifyObservers(Object arg) {
        super.notifyObservers(arg);
        Log.d("Notify","notifying "+arg.toString());
    }

    public void setDate(int y, int m, int d) {
        mocking = true;
        if( y == year && m == month && d == date)
            return;
        calendar.set(y, m, d);
        day = calendar.get(calendar.DAY_OF_WEEK);
        date = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);
        setChanged();
        notifyObservers(getDate());
    }

    public void setDate(String date) {
        mocking = true;
        String[] dmy = date.split("/",3);
        int d = Integer.valueOf(dmy[2]);
        int m = Integer.valueOf(dmy[1]);
        int y = Integer.valueOf(dmy[0]);
        setDate(y, m, d);
        setChanged();
        notifyObservers(getDate());
    }

    public boolean checkNewDate(){
        if(!mocking){
            Calendar cal = Calendar.getInstance();
            int newDay = cal.get(calendar.DAY_OF_WEEK);
            int newDate = cal.get(Calendar.DAY_OF_MONTH);
            int newMonth = cal.get(Calendar.MONTH);
            int newYear = cal.get(Calendar.YEAR);
            if(newDay!=day || newDate != date || newMonth != month || newYear != year){
                day = newDay; date = newDate; month = newMonth; year = newYear;
                setChanged();
                notifyObservers(getDate());
            }
        }
        if(hasChanged())
            return true;
        return false;
    }

    public String getDate(){
        String monthString = Integer.toString(month);
        String dayString = Integer.toString(date);
        if(month < 10){
            monthString = "0"+monthString;
        }
        if(date < 10){
            dayString = "0"+dayString;
        }

        return year+"/"+monthString+"/"+dayString;
    }

    public String getDateWithoutSlash(){
        String monthString = Integer.toString(month);
        String dayString = Integer.toString(date);
        if(month < 10){
            monthString = "0"+monthString;
        }
        if(date < 10){
            dayString = "0"+dayString;
        }
        return year+monthString+dayString;
    }


    public int[] getDateFields(){
        return new int[]{date,month,year};
    }

    public void clearMockedTime(){
        Log.d("Clear","Resetting mocked time");
        calendar = Calendar.getInstance();
        day = calendar.get(calendar.DAY_OF_WEEK);
        date = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);
        mocking = false;
        setChanged();
        notifyObservers(getDate());
    }
}