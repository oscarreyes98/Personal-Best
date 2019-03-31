package edu.personalbest.ucsd.personalbest;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.personalbest.ucsd.personalbest.Friend.UserFriend;
import edu.personalbest.ucsd.personalbest.Friend.UserFriendFactory;

public class UserFriendListFactory {
    private static final String TAG = UserFriendListFactory.class.getSimpleName();
    private UserFriendListFactory() {

    }

    public static UserFriendList createUserFriendList(String data) {
        try {
            String msg_load = "Loading %s: %s";
            Log.d(TAG, String.format(msg_load, TAG, data));
            String[] params = data.split(UserFriendList.DELIM);
            String userID = params[0];
            List<UserFriend> friendsList = new ArrayList<>();
            final String base = "";
            for (int i = 1; i < params.length; ++i) {
                if (params[i] == null || base.equals(params[i]))
                    continue;

                try {
                    friendsList.add(UserFriendFactory.createUserFriend(params[i]));
                } catch (Exception e) {
                    String msg = "Error loading UserFriend at index %d: %s";
                    Log.d(TAG, String.format(msg, i, e.getMessage()));
                }
            }
            return createUserFriendList(userID, friendsList);
        } catch (Exception e) {
            String msg = "Error loading %s from string: %s";
            Log.d(TAG, String.format(msg, TAG, e.getMessage()));
            return null;
        }
    }

    public static UserFriendList createUserFriendList(String userID, List<UserFriend> friends) {
        UserFriendList userFriendList = new UserFriendList(userID);
        for (UserFriend userFriend : friends)
            userFriendList.addFriend(userFriend);
        String msg = "Successfully loaded %s! %s";
        Log.d(TAG, String.format(msg, TAG, userFriendList.toString()));
        return userFriendList;
    }
}
