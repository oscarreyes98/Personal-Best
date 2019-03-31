package edu.personalbest.ucsd.personalbest.Friend;

import android.util.Log;

public class UserFriendFactory {
    private static final String TAG = UserFriendFactory.class.getSimpleName();
    private UserFriendFactory() {

    }
    public static UserFriend createUserFriend(String data) {
        try {
            String[] params = data.split(UserFriend.DELIM);
            String userID = params[0];
            Boolean isPending = Boolean.parseBoolean(params[1]);
            String requester = params[2];

            return createUserFriend(userID, isPending, requester);
        } catch (Exception e) {
            String msg = "Error loading %s from string: %s";
            Log.d(TAG, String.format(msg, TAG, e.getMessage()));
            return null;
        }
    }
    public static UserFriend createUserFriend(String userID, boolean isPending, String requester) {
        return new UserFriend(userID, isPending, requester);
    }
}
