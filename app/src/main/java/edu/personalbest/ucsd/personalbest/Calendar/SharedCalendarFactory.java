package edu.personalbest.ucsd.personalbest.Calendar;

import java.util.Observer;

public class SharedCalendarFactory {

    private static ICalendar sharedMockCalendar = new MockCalendar();
    private static ICalendar sharedRealCalendar = new CalendarAdapter();
    public static final int REAL_CALENDAR = 0;
    public static final int MOCK_CALENDAR = 1;
    public static int DEFAULT = 1;

    public static void setDefault(int code){
        DEFAULT = code;
    }

    public static ICalendar getNewCalendar(int code, Observer o){
        if(code==REAL_CALENDAR){
            ICalendar cal = new CalendarAdapter();
            if(o!=null) cal.addObserver(o);
            return cal;
        }
        else if(code==MOCK_CALENDAR){
            ICalendar cal = new MockCalendar();
            if(o!=null) cal.addObserver(o);
            return cal;
        }
        return null;
    }

    public static ICalendar getSharedCalendar(int code, Observer o){
        if(code==REAL_CALENDAR){
            if(o!=null) sharedRealCalendar.addObserver(o);
            return sharedRealCalendar;
        }
        else if(code==MOCK_CALENDAR){
            if(o!=null) sharedMockCalendar.addObserver(o);
            return sharedMockCalendar;
        }
        return null;
    }


}