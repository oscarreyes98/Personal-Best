package edu.personalbest.ucsd.personalbest;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import edu.personalbest.ucsd.personalbest.Friend.UserFriend;
import edu.personalbest.ucsd.personalbest.Friend.UserFriendFactory;

public class UserFriendList extends Observable implements Serializable {
    private static final String TAG = UserFriend.class.getSimpleName();

    static final String ARG_FRIEND_ADD  = "add";
    static final String ARG_FRIEND_REMOVE = "remove";
    static final String ARG_FRIEND_ACCEPT_PENDING = "pending";

    private Map<String, UserFriend> friends = new HashMap<>();
    private String userID;

    public UserFriendList(String userID) {
        this.userID = userID;
    }

    boolean userIsRequesterForFriend(String friendID) {
        if (!isFriendsWith(friendID))
            return false;
        return friends.get(friendID).getFriendshipRequester().equals(userID);
    }

    boolean isFriendsWith(String friendID) {
        return friends.containsKey(friendID);
    }

    public boolean removeFriend(String friendID) {
        if (!isFriendsWith(friendID))
            return false;

        friends.remove(friendID);
        Object args = Utility.makeArgs(ARG_FRIEND_REMOVE, friendID);
        setChanged();
        notifyObservers(args);
        return true;
    }

    /**
     * Adds a friend, returning false if the friend does not exist or is already in user list
     * @param friendID friend to add
     * @return
     */
    public boolean addFriend(String friendID, boolean isPending, String userID) {
        return addFriend(UserFriendFactory.createUserFriend(friendID, isPending, userID));
    }

    public boolean addFriend(UserFriend userFriend) {
        String friendID = userFriend.getUserID();
        if (isFriendsWith(friendID) || friendID.equals(userID))
            return false;

        friends.put(friendID, userFriend);
        Object args = Utility.makeArgs(ARG_FRIEND_ADD, friendID);
        setChanged();
        notifyObservers(args);
        return true;
    }

    // Shorthand for addFriend(friendID, true, userID)
    public boolean addFriend(String friendID) {
        return addFriend(friendID, true, userID);
    }

    public boolean isFriendPending(String friendID) {
        if (!isFriendsWith(friendID))
            return false;
        return friends.get(friendID).isPending();
    }

    public ArrayList<UserFriend> getUserFriends() {
        String[] keys = friends.keySet().toArray(new String[friends.keySet().size()]);
        ArrayList<UserFriend> friend_list = new ArrayList<>();
        for (String key: keys)
            friend_list.add(friends.get(key));
        return friend_list;
    }

    public void clearFriendsList() {
        friends.clear();
        setChanged();
        notifyObservers();
    }

    /**
     * Delegates to removeFriend()
     * @param friendID friend's pending request to cancel
     */
    public void declinePendingFriendRequest(String friendID) {
        removeFriend(friendID);
    }

    public boolean acceptPendingFriendRequest(String friendID) {
        if (!isFriendsWith(friendID) || !isFriendPending(friendID))
            return false;

        friends.get(friendID).setPending(false);
        Object args = Utility.makeArgs(ARG_FRIEND_ACCEPT_PENDING, friendID);
        setChanged();
        notifyObservers(args);
        return true;
    }

    public static final String DELIM = "~~~";

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(userID);
        for (UserFriend userFriend : friends.values())
            sb.append(DELIM + userFriend.toString());

        return sb.toString();
    }
}