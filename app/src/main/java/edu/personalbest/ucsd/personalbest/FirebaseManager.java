package edu.personalbest.ucsd.personalbest;

import android.content.Context;
import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.List;
import java.util.Map;
import java.util.Observer;

public class FirebaseManager {
    private UserData data_local;
    private FirebaseDatabase data_remote;
    private static final String TAG = FirebaseManager.class.getSimpleName();

    private UserFriendList userFriendList;

    private static FirebaseManager instance = null;

    private FirebaseManager() {}

    private FirebaseManager(UserData data_local, FirebaseFirestore db_instance, Context context) {
        this.data_local = data_local;
        data_remote = new FirebaseDatabase();
        data_remote.initialize(db_instance, data_local, context);

        String userData = data_local.loadUserFriendListData();
        Log.d(TAG, "Loaded user data: " + userData);
        userFriendList = UserFriendListFactory.createUserFriendList(userData);

        instance = this;
    }

    private void cleanup() {
        if (userFriendList != null)
            userFriendList.deleteObservers();
    }

    public static void Initialize(UserData data_local, FirebaseFirestore db_instance, Context context) {
        if (instance != null)
            instance.cleanup();
        instance = new FirebaseManager(data_local, db_instance, context);
    }

    public static UserFriendList getUserFriendList() {
        if (instance == null)
            return null;
        return instance.userFriendList;
    }

    public static void addObserver(Observer o) {
        if (instance != null) {
            instance.userFriendList.addObserver(o);
        }
    }

    private Map<String, String> validateArgs(Object args) {
        Map<String, String> args_map = null;
        try {
            args_map = (Map<String, String>)args;
            return args_map;
        } catch (ClassCastException e) {
            return null;
        }
    }

    public static void addFriend(String userID, Boolean isPending, String requestingID) {
        if (instance.userFriendList.addFriend(userID, isPending, requestingID)) {
            instance.data_local.storeUserFriendListData(instance.userFriendList.toString());
            instance.data_remote.addFriend(instance.data_local.getUserID(), userID);
        }
    }

    public static void acceptPendingFriendRequesst(String friendID, boolean initChat) {
        if (instance.userFriendList.acceptPendingFriendRequest(friendID)) {
            instance.data_local.storeUserFriendListData(instance.userFriendList.toString());
            instance.data_remote.acceptPendingFriendRequest(friendID, initChat);
        }
        instance.data_remote.subscribeToNotificationsTopic(friendID);
    }

    public static void clearLocalFriendsList() {
        instance.userFriendList.clearFriendsList();
        instance.data_local.storeUserFriendListData(instance.userFriendList.toString());
    }

    public static void removeFriend(String friendID) {
        if (instance.userFriendList.removeFriend(friendID)) {
            instance.data_local.storeUserFriendListData(instance.userFriendList.toString());
            instance.data_remote.removeFriendsMutually(instance.data_local.getUserID(), friendID);
        }
    }

    public static void storeDayData(String date, String data) {
        Log.e(TAG, "Uploading day data!");
        instance.data_remote.storeDayData(date, data);
    }

    public static void requestStepDataForUser(String friendID, Consumer<Map<String,String>> c) {
        instance.data_remote.requestStepDataForUser(friendID, c);
    }

    public static void requestFriendChats(String friendID, Consumer<List<ChatMessage>> c) {
        instance.data_remote.requestFriendChats(friendID, c);
    }

    public static void sendChatMessageToFriend(String friendID, String message) {
        instance.data_remote.sendChatToFriend(friendID, message);
    }

    public static void subscribeToNotificationsTopic(String friendID) {
        instance.data_remote.subscribeToNotificationsTopic(friendID);
    }

}