package edu.personalbest.ucsd.personalbest;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import android.view.View;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;


import com.google.firebase.Timestamp;

import java.util.List;

public class FriendChat extends AppCompatActivity implements Consumer<List<ChatMessage>> {

    private static final String TAG = FriendChat.class.getSimpleName();
    LinearLayout layout;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;
    private String friendID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_chat);

        //This is the friend id for which we will get chat history.
        friendID = getIntent().getStringExtra("friendID");

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_friend_chat);
        myToolbar.setTitle(friendID);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        //Updating string so it's only the last part of the string, after the '@' sign.
        Log.d(TAG, "Our friend id for chat is: " + friendID);

        layout = findViewById(R.id.layout1);
        sendButton = findViewById(R.id.sendButton);
        messageArea = findViewById(R.id.messageArea);
        scrollView = findViewById(R.id.scrollView);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageArea.getText().toString();
                messageArea.setText("");
                if (!message.equals("")) {
                    String msg = "Sending message to %s: %s";
                    Log.d(TAG, String.format(msg, friendID, message));
                    FirebaseManager.sendChatMessageToFriend(friendID, message);
                    addMessageBox(message, Timestamp.now(),  MessageType.MESSAGE_BY_USER);
                }
            }
        });

        FirebaseManager.requestFriendChats(friendID, this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    enum MessageType {
        MESSAGE_BY_USER,
        MESSAGE_BY_FRIEND
    }

    public void addMessageBox(String message, Timestamp timestamp, MessageType type){
        try {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View message_view = inflater.inflate(R.layout.chat_message, null);

            TextView message_timestamp = message_view.findViewById(R.id.message_timestamp);
            TextView message_text = message_view.findViewById(R.id.message_text);

            message_timestamp.setText(timestamp.toDate().toString());
            message_text.setText(message);

            switch (type) {
                case MESSAGE_BY_FRIEND:
                    //textView.setBackgroundResource(R.drawable.rounded_corner1);
                    //textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                    //message_text.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    break;
                case MESSAGE_BY_USER:
                    //textView.setBackgroundResource(R.drawable.rounded_corner2);
                    message_timestamp.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    message_text.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    break;
            }

            layout.addView(message_view);
            scrollView.fullScroll(View.FOCUS_DOWN);
        } catch (Exception e) {
            Log.wtf(TAG, "Error inflating chat message: " + e.getMessage());
        }
    }

    @Override
    public void accept(List<ChatMessage> arg) {
        for (ChatMessage message : arg) {
            boolean isFriendsMessage = message.getFrom().equals(friendID);
            MessageType type = isFriendsMessage ? MessageType.MESSAGE_BY_FRIEND : MessageType.MESSAGE_BY_USER;
            addMessageBox(message.getText(), message.getTimestamp(), type);
        }
    }

    @Override
    public void reject() {
        // no messages?
    }
}
