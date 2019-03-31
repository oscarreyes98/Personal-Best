package edu.personalbest.ucsd.personalbest.Fitness;

import android.app.Activity;
import android.widget.Toast;

import edu.personalbest.ucsd.personalbest.StepCount;

public class MockFitnessService extends FitnessService {
    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;
    private String UserID = "Test User";
    private long total;
    private StepCount activity;

    public MockFitnessService(){
        total = 0;
    }

    public MockFitnessService(StepCount o) {
        if(o!=null)
            this.addObserver(o);
        this.activity = o;
    }

    @Override
    public void notifyObservers(Object arg) {
        super.notifyObservers(arg);
        System.out.println("notifying "+arg.toString());
    }

    @Override
    public int getRequestCode() {
        // Don't know what it's doing. Kept because it's checked by other codes.
        return GOOGLE_FIT_PERMISSIONS_REQUEST_CODE;
    }

    @Override
    public void setup(Activity activity) {
        setChanged();
        notifyObservers(getUserID());
    }

    @Override
    public void updateStepCount() {
        setChanged();
        notifyObservers(total);
    }

    @Override
    public void login(Activity activity) {
        // Do nothing for now
        setup(activity);
    }

    @Override
    public void logout() {
        Toast.makeText(activity, "Switching account not implemented for test mode", Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getUserID() {
        return UserID;
    }

    @Override
    public void increaseStepOffset(long steps) {
        System.out.println("Mock steps + "+steps);
        total = total + steps;
        setChanged();
        notifyObservers(total);
    }

    @Override
    public void resetStepsOffset() {
        System.out.println("Resetting mocked steps");
        total = 0;
        setChanged();
        notifyObservers(total);
    }

    @Override
    public void setActivity(StepCount o) {
        this.activity = o;
    }
}
