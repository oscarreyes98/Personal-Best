package edu.personalbest.ucsd.personalbest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class UserFriendListTest {

    private UserFriendList userFriendList = null;
    private String userID = "r9munoz";
    private String testFriend1 = "hsshu";
    private String testFriend2 = "ajdeguzm";


    @Before
    public void setup() {
        userFriendList = new UserFriendList(userID);
    }

    @Test
    public void testAddFriend() {
        // addFriend must return true for a valid ID
        Assert.assertTrue(userFriendList.addFriend(testFriend1));
        Assert.assertTrue(userFriendList.isFriendsWith(testFriend1));
        Assert.assertFalse(userFriendList.isFriendsWith(testFriend2));

        // addFriend must return false for "self"
        Assert.assertFalse(userFriendList.addFriend(userID));
        Assert.assertFalse(userFriendList.isFriendsWith(userID));
        Assert.assertFalse(userFriendList.isFriendPending(userID));
    }

    @Test
    public void testAddFriendPending() {
        userFriendList.addFriend(testFriend1, true, userID);
        userFriendList.addFriend(testFriend2, false, userID);
        Assert.assertTrue(userFriendList.isFriendPending(testFriend1));
        Assert.assertFalse(userFriendList.isFriendPending(testFriend2));
    }


    @Test
    public void testRemoveFriend() {
        userFriendList.addFriend(testFriend1);
        userFriendList.removeFriend(testFriend1);

        Assert.assertFalse(userFriendList.isFriendsWith(testFriend1));
    }

    @Test
    public void testSetFriendPending() {
        //userFriendList.addFriend(testFriend1, true);
        userFriendList.addFriend(testFriend1, true, userID);

       // userFriendList.addFriend(testFriend2, false);
        userFriendList.addFriend(testFriend2, true, userID);


        // Toggle initial "pending" state
        userFriendList.acceptPendingFriendRequest(testFriend1);
        Assert.assertFalse(userFriendList.isFriendPending(testFriend1));
        Assert.assertTrue(userFriendList.isFriendPending(testFriend2));
    }

}
