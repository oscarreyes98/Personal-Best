package edu.personalbest.ucsd.personalbest;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Prompts user for height on first time login
 * Includes a set up page and updates height and stride length in user
 */
public class Height extends AppCompatActivity{
    // Google Service Key
    public static final String FITNESS_SERVICE_KEY = "FITNESS_SERVICE_KEY";
    private String fitnessServiceKey = "GOOGLE_FIT";
    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = System.identityHashCode(this) & 0xFFFF;

    // Activity information
    private static final String TAG = "StepCountActivity";

    // Storage managers
    UserData userData;

    // UI components
    private EditText heightIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // UI Components
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height);
        final Button btnDone = findViewById(R.id.button);
        heightIn = (EditText) findViewById(R.id.heightIn);
        userData = new UserData(getIntent().getExtras().getString("userID"), this);

        //on Done button
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Ensure there is input
                if(heightIn.getText().toString().isEmpty()){
                    heightIn.setError("Must input a height");
                }
                else {

                    //Set height to user variable instead
                    double height = Double.valueOf(heightIn.getText().toString());
                    // UserInformation.setUserHeight(height);
                    userData.setHeight(height);
                    Log.d(TAG, "Height is: " + height);
                    finish();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == Activity.RESULT_OK){
            if(requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE){
               // accessGoogleFit();
            }
        }
    }
}