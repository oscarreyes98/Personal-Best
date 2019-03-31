package edu.personalbest.ucsd.personalbest.Calendar;

import java.util.Observable;

public abstract class ICalendar extends Observable {
    abstract public int getDayOfWeek();
    abstract public String getDate();
    abstract public String getDateWithoutSlash();
    abstract public void incrementDay();
    abstract public void incrementDay(int amount);
    abstract public void setDate(int year, int month, int date);
    abstract public void setDate(String date);
    abstract public boolean checkNewDate();
    abstract public void clearMockedTime();
    abstract public int[] getDateFields();
}