package edu.personalbest.ucsd.personalbest.Friend;

// Yes, here
public class UserFriend {
    private static final String TAG = UserFriend.class.getSimpleName();
    private String userID;
    private boolean isPending;
    // user ID of who requested the friendship
    private String friendshipRequester;

    public UserFriend(String userID, boolean isPending, String requester) {
        this.userID = userID;
        this.isPending = isPending;
        this.friendshipRequester = requester;
    }

    public String getFriendshipRequester() {
        return friendshipRequester;
    }

    public void setPending(boolean isPending) {
        this.isPending = isPending;
    }

    public String getUserID() {
        return userID;
    }

    public boolean isPending() {
        return isPending;
    }

    public static final String DELIM = "#";

    public String toString() {
        return userID + DELIM + Boolean.toString(isPending) + DELIM + friendshipRequester;
    }
}
