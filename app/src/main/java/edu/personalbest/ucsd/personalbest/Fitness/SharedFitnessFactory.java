package edu.personalbest.ucsd.personalbest.Fitness;

import java.util.Observer;

public class SharedFitnessFactory {

    private static GoogleFitAdapter sharedGoogleFit = new GoogleFitAdapter();
    private static MockFitnessService sharedMockFitness = new MockFitnessService();
    public static final int GOOGLEFIT = 0;
    public static final int MOCKFITNESS = 1;
    public static int DEFAULT = 0;

    public static void setDefault(int code){
        DEFAULT = code;
    }

    public static FitnessService getSharedService(int code, Observer o){
        if(code==GOOGLEFIT){
            if(o!=null) sharedGoogleFit.addObserver(o);
            return sharedGoogleFit;
        }
        if(code==MOCKFITNESS){
            if(o!=null) sharedMockFitness.addObserver(o);
            return sharedMockFitness;
        }
        return null;
    }


}
