package edu.personalbest.ucsd.personalbest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.annotation.Nullable;

//manages Firebase Firestore interaction
public class FirebaseDatabase extends Observable {

    private FirebaseFirestore db_instance = null;
    private String userID = null;

    private final static String TAG = FirebaseDatabase.class.getSimpleName();

    /**
     * The "user" collection reference
     */
    // private CollectionReference chats;
    private CollectionReference friends;
    private DocumentReference userDocument;

    private static final String USERDATA_COLLECTION_KEY = "user_data";
    // This is userID?
    //private static final String FRIEND_DOCUMENT_KEY = "user_friends";
    private static final String FRIEND_KEY = "friends";

    private static final String CHAT_FROM_KEY = "from";
    private static final String CHAT_TEXT_KEY = "text";
    private static final String CHAT_TIMESTAMP_KEY = "timestamp";

    private final static String CHAT_DATA_KEY = "chat_data";
    private final static String FRIEND_CHAT_ID_KEY = "chat_id";
    private final static String FRIEND_PENDING_KEY = "is_pending";
    private final static String FRIEND_REQUESTER_KEY = "requester";

    private final static String MESSAGES_KEY = "messages";
    private static final String STEP_DATA_KEY = "steps_data";

    private final static String ERR_DELETE_CHAT_HISTORY_FAIL = "Unable to delete chat history between %s & %s: ";
    private final static String ERR_DELETE_CHAT_HISTORY_SUCCESS = "Successfully deleted chat history between %s & %s: ";
    private final static String ERR_CANT_REMOVE_CHAT_HISTORY = "Unable to remove chat history between %s & %s: ";


    private Context context = null;

    public void initialize(final FirebaseFirestore db_instance, final UserData user, Context context) {
        this.context = context;
        String unmodifiedUserID = user.getUserID();
        this.userID = unmodifiedUserID;
        this.db_instance = db_instance;

        Log.d(TAG, "Initializing Firebase with userID: " + this.userID);

        Log.d(TAG, "userID: " + userID + "; " + unmodifiedUserID);
        userDocument = db_instance
                .collection(USERDATA_COLLECTION_KEY)
                .document(this.userID);

        loadUserData();
        initFriendsAndItsListener();
        Log.d(TAG, "FirebaseDatabase initialized");
    }
    private void updateUserObject() {
        Map<String, String> data = new HashMap<>();
        data.put("key_test", "val_test");
        userDocument.set(data);
    }


    private void loadUserData() {
        updateUserObject();

        // Load user
        userDocument.collection("user")
        .addSnapshotListener(new EventListener<QuerySnapshot>() {

            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    String msg = "Unable to load user data for %s from Firebase database: %s";
                    Log.e(TAG, String.format(msg, userID, e.getMessage()));

                    // if user info cannot be loaded -> does not exist on database
                    // therefore -> create user info entry on database
                    initUserDataOnDatabase();
                    return;
                }

                Map<String, Boolean> remote_friends_list = new HashMap<>();
                List<DocumentSnapshot> friendDocuments = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot documentSnapshot : friendDocuments) {
                    String friendID = documentSnapshot.getId();
                    String chat_id = documentSnapshot.getString("chat_id");
                    Boolean is_pending = documentSnapshot.getBoolean(FRIEND_PENDING_KEY);
                    Log.d(TAG, "Friend info: " + documentSnapshot.getId() + "; " + chat_id);

                }

                setChanged();
                notifyObservers();
            }
        });
    }

    private String sanitizeDocumentKey(String key) {
        return key.replaceAll("[^a-zA-Z0-9]", "");
    }
    public static final String DAYDATA_DATE = "date";
    public static final String DAYDATA_DATA = "data";

    public void storeDayData(String date, String data) {
        date = sanitizeDocumentKey(date);

        Map<String, String> dbData = new HashMap<>();
        dbData.put(DAYDATA_DATE, date);
        dbData.put(DAYDATA_DATA, data);

        db_instance.collection(USERDATA_COLLECTION_KEY)
                .document(userID)
                .collection(STEP_DATA_KEY)
                .document(date)
                .set(dbData);
    }

    private final int DAYS_TO_LOAD = 28;

    public void requestStepDataForUser(String friendID, Consumer<Map<String,String>> c) {
        Log.d(TAG,"REQUESTING");
        db_instance.collection(USERDATA_COLLECTION_KEY)
            .document(friendID)
            .collection(STEP_DATA_KEY)
            .orderBy(DAYDATA_DATE)
            .limit(DAYS_TO_LOAD)
            .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                    System.out.println("SUCCESS");
                    List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();

                    StringBuilder sb = new StringBuilder();
                    sb.append("Snapshots: " + snapshots.size() + "; ");
                    Map<String, String> data = new HashMap<>();
                    for (DocumentSnapshot snapshot : snapshots) {
                        //snapshot.get()
                        String day_date = snapshot.getString(DAYDATA_DATE);
                        String day_data = snapshot.getString(DAYDATA_DATA);
                        data.put(day_date, day_data);
                        sb.append(snapshot.getId() + "; ");
                    }

                    Log.d(TAG, "Successful Firebase read");
                    c.accept(data);
                }
            })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("FAILURE");
                String msg = "Unable to retrieve step data for %s! %s";
                String formattedMessage = String.format(msg, friendID, e.getMessage());
                Log.d(TAG, formattedMessage);
                c.reject();
            }
        });
    }

    private void initUserDataOnDatabase() {
        Map<String, Object> data = new HashMap<>();
        data.put("test", true);
        db_instance.collection(USERDATA_COLLECTION_KEY)
            .document(userID)
                .update(data)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    String msg = "User %s created successfully on database!";
                    Log.d(TAG, String.format(msg, userID));
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    String msg = "Unable to create %s entry for user %s!";
                    Log.e(TAG, String.format(msg, USERDATA_COLLECTION_KEY, userID));
                }
            });
    }

    private void initFriendsAndItsListener() {
        friends = userDocument.collection(FRIEND_KEY);
        Log.d(TAG, "Initializing friends listener...");
        friends.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e)
            {
                Log.d(TAG, "Friend listener triggered...");
                if (e != null) {
                    Log.e(TAG, e.getLocalizedMessage());
                    return;
                }
                if (snapshots == null) {
                    Log.d(TAG, "Snapshots empty? " + snapshots.isEmpty());
                    return;
                }
                if (snapshots.isEmpty()) {
                    Log.d(TAG, "Clearing local friends list");
                    FirebaseManager.clearLocalFriendsList();
                } else {
                    for (DocumentChange change : snapshots.getDocumentChanges()) {
                        QueryDocumentSnapshot snapshot = change.getDocument();
                        String friendID = snapshot.getId();

                        switch(change.getType()) {
                            case ADDED:
                                String chat_id = snapshot.getString(FRIEND_CHAT_ID_KEY);
                                Boolean pending = snapshot.getBoolean(FRIEND_PENDING_KEY);
                                String requester = snapshot.getString(FRIEND_REQUESTER_KEY);
                                String msg = "New friend info: %s, [%s; %b; %s]";
                                Log.d(TAG, String.format(msg, friendID, chat_id, pending, requester));
                                FirebaseManager.addFriend(friendID, pending, requester);
                                break;

                            case REMOVED:
                                Log.d(TAG, "Friend removed: " + friendID);
                                FirebaseManager.removeFriend(friendID);
                                break;

                            case MODIFIED:
                                Log.d(TAG, "Friend modified: " + friendID);
                                FirebaseManager.acceptPendingFriendRequesst(friendID, false);
                                break;
                        }
                        Log.d(TAG, "Change: " + change.toString());
                    }
                }
            }
        });
    }

    public void requestFriendChats(String friendID, Consumer<List<ChatMessage>> c) {
        userDocument.collection(FRIEND_KEY)
            .document(friendID)
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    Exception e = task.getException();
                    if (e != null) {
                        String msg = "Error retrieving friend info for %s: %s";
                        Log.d(TAG, String.format(msg, friendID, e.getMessage()));
                        c.reject();
                        return;
                    }

                    DocumentSnapshot snapshot = task.getResult();
                    Boolean isPending = snapshot.getBoolean(FRIEND_PENDING_KEY);
                    String chat_id = snapshot.getString(FRIEND_CHAT_ID_KEY);
                    if (isPending == null || isPending) {
                        String msg = "Unable to chat with friend %s - friend is pending";
                        Log.d(TAG, String.format(msg, friendID));
                        c.reject();
                        return;
                    }
                    if (chat_id == null || chat_id.equals("")) {
                        String msg = "Unable to chat with friend %s - chat_id does not exist";
                        Log.d(TAG, String.format(msg, friendID));
                        c.reject();
                        return;
                    }

                    requestChats(chat_id, c);
                }
            });

    }

    private void requestChats(String chat_id, Consumer<List<ChatMessage>> c) {
        db_instance.collection(CHAT_DATA_KEY)
            .document(chat_id)
            .collection(MESSAGES_KEY)
            .orderBy(CHAT_TIMESTAMP_KEY, Query.Direction.ASCENDING)
            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        String msg = "Error retrieving chat info for %s: %s";
                        Log.d(TAG, String.format(msg, chat_id, e.getMessage()));
                        c.reject();
                        return;
                    }

                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        String msg = "Error retrieving chat info for %s: no documents";
                        Log.d(TAG, String.format(msg, chat_id));
                        c.reject();
                        return;
                    }

                    if (!queryDocumentSnapshots.getMetadata().hasPendingWrites()) {
                        List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();

                        // collect new messages
                        List<ChatMessage> newMessages = new ArrayList<>();
                        for (DocumentChange change : documentChanges) {
                            DocumentSnapshot document = change.getDocument();
                            String from = document.getString(CHAT_FROM_KEY);
                            String text = document.getString(CHAT_TEXT_KEY);
                            Timestamp timestamp = document.getTimestamp(CHAT_TIMESTAMP_KEY);
                            newMessages.add(new ChatMessage(from, text, timestamp));
                        }

                        // relay messages
                        c.accept(newMessages);
                    }
                }
            });
    }

    private void finalizeObserverConnection(Observer o, Object args, String message) {
        if (message != null)
            Log.d(TAG, message);
        if (o != null) {
            setChanged();
            notifyObservers(args);
            deleteObserver(o);
        }
    }

    public void sendChatToFriend(String friendID, String message) {
        userDocument.collection(FRIEND_KEY)
            .document(friendID)
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    Exception e = task.getException();
                    if (e != null) {
                        String msg = "Error sending message to %s: %s";
                        Log.d(TAG, String.format(msg, friendID, e.getMessage()));
                        return;
                    }
                    DocumentSnapshot document = task.getResult();
                    Boolean isPending = document.getBoolean(FRIEND_PENDING_KEY);
                    if (isPending == null || isPending) {
                        String msg = "Error sending message to %s: %s";
                        Log.d(TAG, String.format(msg, friendID, "Friend is pending"));
                        return;
                    }
                    String chat_id = document.getString(FRIEND_CHAT_ID_KEY);
                    sendChatToFriendByChatID(chat_id, message);
                }
            });
    }

    public void subscribeToNotificationsTopic(String friendID) {
        Log.d(TAG, "Subscribing to chat notifications for friend: " + friendID);
        userDocument.collection(FRIEND_KEY)
            .document(friendID)
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    Exception e = task.getException();
                    if (e != null) {
                        String msg = "Error loading chat_id for friend %s: %s";
                        Log.d(TAG, String.format(msg, friendID, e.getMessage()));
                        return;
                    }

                    DocumentSnapshot document = task.getResult();
                    Boolean isPending = document.getBoolean(FRIEND_PENDING_KEY);
                    if (isPending == null || isPending) {
                        String msg = "Error subscribing to chat_id for %s: %s";
                        Log.d(TAG, String.format(msg, friendID, "Friend is pending"));
                        return;
                    }
                    String chat_id = document.getString(FRIEND_CHAT_ID_KEY);

                    FirebaseMessaging.getInstance().subscribeToTopic(chat_id)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                String msg = "Subscription to topic " + chat_id;
                                if (task.isSuccessful()) {
                                    msg += " SUCCESSFUL";
                                } else {
                                    msg += " FAILED";
                                }
                                Log.d(TAG, msg);
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                }
            });
    }


    private void sendChatToFriendByChatID(String chat_id, String message) {
        Map<String, Object> newMessage = new HashMap<>();
        newMessage.put(CHAT_FROM_KEY, userID);
        newMessage.put(CHAT_TIMESTAMP_KEY, Timestamp.now());
        newMessage.put(CHAT_TEXT_KEY, message);

        Log.d(TAG, "Posting message to chat_id: " + chat_id);
        db_instance.collection(CHAT_DATA_KEY)
            .document(chat_id)
            .collection(MESSAGES_KEY)
                            /*
            .document()
            .set(newMessage)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "task: " + task.getResult().toString());
                    }
                });
            */
            .add(newMessage)
            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Log.d(TAG, "Successfully sent message!");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    String msg = "Unable to send message to friend: %s";
                    Log.d(TAG, String.format(msg, e.getMessage()));
                }
            });
    }


    private final String MSG_FRIEND_SET_CHAT_ID_SUCCESS = "Successfully set chat id for %s in friend list of %s: %s";
    private final String MSG_FRIEND_SET_CHAT_ID_FAIL = "Unable to set chat id for %s in friend list of %s: ";
    /**
     * ChatID is generated automatically by Firebase - this method simply creates the chat document
     * and sets the respective chat_id to both users
     * @param user1ID
     * @param user2ID
     * @param chatID
     */
    private void setChatIDForNewFriend(String user1ID, final String user2ID, String chatID) {
        Map<String, Object> chat_ref_doc_data = new HashMap<>();
        chat_ref_doc_data.put(FRIEND_CHAT_ID_KEY, chatID);
        //chat_ref_doc_data.put(FRIEND_PENDING_KEY, false);

        OnSuccessListener successListener = createGenericDocSuccessListener(
                String.format(MSG_FRIEND_SET_CHAT_ID_SUCCESS, user2ID, user1ID, chatID));

        OnFailureListener failureListener = createGenericDocFailListener(
                String.format(MSG_FRIEND_SET_CHAT_ID_FAIL, user2ID, user1ID));

        // set chat ID for user1ID
        db_instance.collection(USERDATA_COLLECTION_KEY)
            .document(user1ID)
            .collection(FRIEND_KEY)
            .document(user2ID)
            .update(chat_ref_doc_data)

            // handle success
            .addOnSuccessListener(successListener)
            // handle failure
            .addOnFailureListener(failureListener);
    }

    private void triggerAcceptFriendRequest(final String userID, String friendID) {
        Map<String, Object> data = new HashMap<>();
        data.put(FRIEND_PENDING_KEY, false);

        OnFailureListener failureListener = createGenericDocFailListener(
                String.format("Unable to accept friend request from %s: ", friendID));

        db_instance.collection(USERDATA_COLLECTION_KEY)
                .document(userID)
                .collection(FRIEND_KEY)
                .document(friendID)
                .update(data)
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Successfully accepted friend " + friendID);
            }
        }).addOnFailureListener(failureListener);
    }

    public void acceptPendingFriendRequest(final String friendID, boolean initChatID) {
        triggerAcceptFriendRequest(userID, friendID);
        triggerAcceptFriendRequest(friendID, userID);

        // TODO: check if there's already a relevant chat_id?
        if (initChatID)
            initChatIDForNewFriends(userID, friendID);
    }

    private void initChatIDForNewFriends(String userID, String friendID) {
        Map<String, Object> data = new HashMap<>();
        List<String> members = new ArrayList<>();
        members.add(userID);
        members.add(friendID);
        data.put("members", members);

        // create chat document for user and friend
        db_instance.collection(CHAT_DATA_KEY)
            .add(data)
            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {

                    String chat_id = documentReference.getId();
                    String msg = "Successfully accepted ";
                    Log.d(TAG, String.format(msg, chat_id));

                    setChatIDForNewFriend(userID, friendID, chat_id);
                    setChatIDForNewFriend(friendID, userID, chat_id);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            });
    }

    @NonNull
    private OnSuccessListener createGenericDocSuccessListener(final String message) {
        return new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Log.d(TAG, message);
            }
        };
    }

    @NonNull
    private OnFailureListener createGenericDocFailListener(final String message) {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, message + e.getMessage());
            }
        };
    }

    private final String MSG_FRIEND_ADD_SUCCESS = "Successfully added %s to user list of %s";
    private final String MSG_FRIEND_ADD_FAIL = "Unable to add %s to user list of %s: ";

    private void requestFriendAdd(final String userID, final String friendID,
                                  final String requester,
                                  OnSuccessListener successListener,
                                  OnFailureListener failureListener)
    {
        Map<String, Object> data = new HashMap<>();
        data.put(FRIEND_PENDING_KEY, true);
        data.put(FRIEND_REQUESTER_KEY, requester);
        db_instance.collection(USERDATA_COLLECTION_KEY)
                .document(userID)
                .collection(FRIEND_KEY)
                .document(friendID)
                .set(data)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    /**
     * * When we delete a friend and then re-add them, will we still have the message history?
     * @param userID user id requesting friend removal
     * @param friendID the frind to remove
     */
    private void processFriendAdd(final String userID, final String friendID) {
        // check if friend exists. If so, add each other to user list
        db_instance.collection(USERDATA_COLLECTION_KEY)
            .document(friendID)
            .get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    attemptAddFriendIfNotInFriendList(userID, friendID);
                } else {
                    FirebaseManager.removeFriend(friendID);
                    String msg = "User \"%s\" is not registered! :(";
                    Toast.makeText(context, String.format(msg, friendID), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, String.format("Friend with id %s does not exist!", friendID));
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Unable to add friend - ID does not exist: " + friendID);
            }
        });
    }

    private void attemptAddFriendIfNotInFriendList(final String userID, final String friendID) {
        userDocument.collection(FRIEND_KEY)
            .document(friendID)
            .get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (!documentSnapshot.exists()) {
                        Log.d(TAG, "Adding friend!");
                        addFriendsMutually(userID, friendID);
                    } else {
                        Log.d(TAG, "Unable to add friend - already in user list: " + friendID);
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "Attempting to add friend");
                }
            });
    }
    private void addFriendsMutually(final String userID, final String friendID) {
        // Friend exists! Add bidirectionally
        requestFriendAdd(userID, friendID, userID,
                createGenericDocSuccessListener(String.format(MSG_FRIEND_ADD_SUCCESS, friendID, userID)),
                createGenericDocFailListener(String.format(MSG_FRIEND_ADD_FAIL, friendID, userID)));

        requestFriendAdd(friendID, userID, userID,
                createGenericDocSuccessListener(String.format(MSG_FRIEND_ADD_SUCCESS, userID, friendID)),
                createGenericDocFailListener(String.format(MSG_FRIEND_ADD_FAIL, userID, friendID)));
    }

    public void addFriend(final String userID, final String friendID) {
        processFriendAdd(userID, friendID);
        //processFriendAdd(friendID, userID);
    }

    private final String MSG_FRIEND_REMOVE_SUCCESS = "Successfully removed %s friend list of %s";
    private final String MSG_FRIEND_REMOVE_FAIL = "Unable to remove %s from friend list of %s: ";

    /**
     * * When we delete a friend and then re-add them, will we still have the message history?
     * @param userID user id requesting friend removal
     * @param friendID the frind to remove
     */
    public void removeFriend(final String userID, final String friendID) {
        // Build reference
        DocumentReference friend_ref = db_instance
            .collection(USERDATA_COLLECTION_KEY)
            .document(userID)
            .collection(FRIEND_KEY)
            .document(friendID);

        OnSuccessListener successListener = createGenericDocSuccessListener(
                String.format(MSG_FRIEND_REMOVE_SUCCESS, friendID, userID));

        OnFailureListener failureListener = createGenericDocFailListener(
                String.format(MSG_FRIEND_REMOVE_FAIL, friendID, userID));

        // Query deletion
        friend_ref.delete()
            .addOnSuccessListener(successListener)
            .addOnFailureListener(failureListener);
    }

    private void removeFriendChatHistory(final String userID, final String friendID,
                                         final boolean queryMutualDeletions)
    {
        DocumentReference friend_ref = db_instance
            .collection(USERDATA_COLLECTION_KEY)
            .document(userID)
            .collection(FRIEND_KEY)
            .document(friendID);

        OnFailureListener failureListener = createGenericDocFailListener(
                String.format(ERR_CANT_REMOVE_CHAT_HISTORY, userID, friendID));

        // Get document info to delete chat history by ID
        friend_ref.get(Source.DEFAULT)
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    String chat_id = documentSnapshot.get(FRIEND_CHAT_ID_KEY, String.class);
                    if (chat_id == null)
                        return;

                    //delete chat history
                    deleteChatHistoryByID(chat_id, userID, friendID);


                    if (queryMutualDeletions) {
                        removeFriendsMutually(userID, friendID);
                    }
                }
            })
            .addOnFailureListener(failureListener);
    }

    private void deleteChatHistoryByID(final String chat_id, final String userID, final String friendID) {

        OnFailureListener failureListener = createGenericDocFailListener(
                String.format(ERR_DELETE_CHAT_HISTORY_FAIL, userID, friendID));

        OnSuccessListener successListener = createGenericDocSuccessListener(
                String.format(ERR_DELETE_CHAT_HISTORY_SUCCESS, userID, friendID));

        db_instance.collection(CHAT_DATA_KEY)
            .document(chat_id)
            .delete()
            .addOnSuccessListener(successListener)
            .addOnFailureListener(failureListener);
    }

    public void removeFriendsMutually(String userID, String friendID) {
        removeFriend(userID, friendID);
        removeFriend(friendID, userID);
    }
}
