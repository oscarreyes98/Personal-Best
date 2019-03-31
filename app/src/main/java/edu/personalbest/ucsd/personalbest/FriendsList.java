package edu.personalbest.ucsd.personalbest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Observable;
import java.util.Observer;

import edu.personalbest.ucsd.personalbest.Fitness.FitnessService;
import edu.personalbest.ucsd.personalbest.Fitness.SharedFitnessFactory;

public class FriendsList extends AppCompatActivity implements Observer {

    private static final String TAG = FriendsList.class.getSimpleName();

    UserData user;
    FriendsAdapter friendsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView rvContacts = findViewById(R.id.rvContacts);

        // Initialize contacts
        FitnessService fitnessService = SharedFitnessFactory.getSharedService(SharedFitnessFactory.DEFAULT, null);
        String UserID = fitnessService.getUserID();
        Log.d(TAG, "Getting user list");

        user = UserData.createUserData(UserID, this);
        FirebaseManager.addObserver(this);

        final EditText editText_email = findViewById(R.id.add_friend_email);
        Button btn_addFriend = findViewById(R.id.btn_add_friend);
        btn_addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editText_email.getText().toString();
                if (email.equals(""))
                    return;
                // case is very important
                email = email.toLowerCase();
                Log.d(TAG, "Adding friend!");
                FirebaseManager.addFriend(email, true, user.getUserID());
                editText_email.setText("");
            }
        });


        // Create friendsAdapter passing in the sample user data
        friendsAdapter = new FriendsAdapter(FirebaseManager.getUserFriendList(), this);

        // Attach the friendsAdapter to the recyclerview to populate items
        rvContacts.setAdapter(friendsAdapter);

        // Set layout manager to position the items
        //rvContacts.setLayoutManager(new LinearLayoutManager(this));

        // Create adapter passing in the sample user data

        // Attach the adapter to the recyclerview to populate items
        //rvContacts.setAdapter(Globals.adapter);

        // Set layout manager to position the items
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.btn_add_friends) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof UserFriendList) {
            UserFriendList userFriendList = (UserFriendList)o;
            Log.d(TAG, "Updating friends list..." + userFriendList.toString());
            friendsAdapter.updateFriendsList(userFriendList);
        }
    }
}
