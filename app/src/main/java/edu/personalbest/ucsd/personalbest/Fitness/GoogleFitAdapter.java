package edu.personalbest.ucsd.personalbest.Fitness;


import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import edu.personalbest.ucsd.personalbest.StepCount;
import edu.personalbest.ucsd.personalbest.UserData;

import static com.google.android.gms.common.Scopes.PROFILE;


public class GoogleFitAdapter extends FitnessService {
    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;
    private final String TAG = "GoogleFitAdapter";
    private StepCount activity;
    private String lastUserID = "";
    private long stepsOffset = 0;
    private long total = 0;

    private GoogleSignInOptions gso;

    public GoogleFitAdapter() {
        stepsOffset = 0;
        total = 0;
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .build();
    }

    public void setActivity(StepCount o){
        this.activity = o;
    }

    public void setup(Activity activity) {
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(activity);
        /*
        if (!GoogleSignIn.hasPermissions(acct, fitnessOptions)) {
              GoogleSignIn.requestPermissions(activity, // your activity
                      GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                      acct, fitnessOptions);

        }*/
        List<Scope> scopes = fitnessOptions.getImpliedScopes();
        scopes.addAll(gso.getScopes());
        StringBuilder sb = new StringBuilder();
        sb.append("scopes: " + scopes.size() + ": ");
        for (Scope scope : scopes) {
            sb.append(scope.toString() + "; ");
        }
        /*Scope scope = new Scope(PROFILE);
        Log.d(TAG, "I got here!");*/
        Scope[] scope_array = scopes.toArray(new Scope[scopes.size()]);
        if (!GoogleSignIn.hasPermissions(acct, scope_array)) {
          GoogleSignIn.requestPermissions(activity, GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, acct, scope_array);
        }
        Log.d(TAG, sb.toString());
        setChanged();
        notifyObservers(getUserID());
    }

    @Override
    public void notifyObservers(Object arg) {
        super.notifyObservers(arg);
        System.out.println("notifying "+arg.toString());
    }
    

    public void login(Activity activity){
        setup(activity);
    }

    public void logout(){
        GoogleSignIn.getClient(activity, gso).signOut();
    }

    public String getUserID(){
        String id = "No User";
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(activity);
        if (acct != null) {
            id = acct.getEmail();
        }
        return id;
    }

    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */
    public void updateStepCount() {

        if(! lastUserID.equals(getUserID())){
            lastUserID = getUserID();
            setChanged();
            notifyObservers(getUserID());
        }

        // Log.d(TAG, getUserID());
        final GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity);

        if (lastSignedInAccount == null) {
            Log.d(TAG, "NO ACCOUNT");
            return;
        }
        Fitness.getHistoryClient(activity, lastSignedInAccount)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                // Log.d(TAG, dataSet.toString());
                                total =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                /* offset above */
                                setChanged();
                                notifyObservers(total+stepsOffset);
                                Log.d(TAG, lastSignedInAccount.toString());
                                Log.d(TAG, "Real steps:"+total);
                                Log.d(TAG, "Signed in account: " + lastSignedInAccount.getEmail());
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "There was a problem getting the step count.", e);
                            }
                        });
    }

    public void resetStepsOffset(){
        stepsOffset = 0;
        System.out.println("Resetting mocked steps");
        setChanged();
        notifyObservers(total + stepsOffset);
    }

    public void increaseStepOffset(long steps){
        System.out.println("Mock steps + "+steps);
        stepsOffset += steps;
        setChanged();
        notifyObservers(total + stepsOffset);
    }

    @Override
    public int getRequestCode() {
        return GOOGLE_FIT_PERMISSIONS_REQUEST_CODE;
    }
}
