package edu.personalbest.ucsd.personalbest;


import android.content.Context;

import com.google.firebase.firestore.FirebaseFirestore;

import edu.personalbest.ucsd.personalbest.UserData;

public interface IFirebaseDatabase {
    String getChats();
    String[] getFriends();
    boolean isFriendPending(String friendID);
    void initialize(FirebaseFirestore db_instance, UserData user, Context context);
}
