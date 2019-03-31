package edu.personalbest.ucsd.personalbest;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import edu.personalbest.ucsd.personalbest.Friend.UserFriend;


public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private UserFriendList friendsListHash;
    private List<UserFriend> friendsList;
    private Context context;
    private final String TAG = "FriendsAdapter";


    public FriendsAdapter(UserFriendList list, Context context){
        this.context = context;
        updateFriendsList(list);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public Button chatButton;
        private Button btn_week_history;
        AlertDialog dialog;

        private final static String TAG = "FriendsAdapter";

        public ViewHolder(View friend) {
            super(friend);
            name = friend.findViewById(R.id.contact_name);
            chatButton = friend.findViewById(R.id.message_button);
            btn_week_history = friend.findViewById(R.id.btn_week_history);

            friend.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int p = getAdapterPosition();
                    System.out.println("LongClick: "+p);
                    String row = String.valueOf(p);
                    Log.d(TAG, row);

                    AlertDialog.Builder builder = new AlertDialog.Builder(friend.getContext());
                    builder.setTitle("Would you like to remove this friend?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //remove friend
                            String toRemove = friendsList.get(p).getUserID();
                            Log.d(TAG, "Removing: " + toRemove);
                            FirebaseManager.removeFriend(toRemove);
                            notifyItemRemoved(p);
                            notifyItemRangeChanged(p, friendsList.size());
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d(TAG, "Did not remove friend");
                        }
                    });

                    // --- Magic below ---
                    builder.setCancelable(false);
                    // --- Magic above ---

                    dialog = builder.create();
                    dialog.show();

                    return true;// returning true instead of false, works for me
                }
            });

            /*
            chatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int p = getLayoutPosition();
                    UserFriend toChat = friendsList.get(p);

                    Intent i = new Intent(context, FriendChat.class);
                    Log.d(TAG, "Starting friend chat!");
                    i.putExtra("friendID", toChat.getUserID());
                    context.startActivity(i);

                }
            });

            friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int p = getLayoutPosition();
                    UserFriend toGraph = friendsList.get(p);

                    Intent i = new Intent(context, FriendGraph.class);
                    Log.d(TAG, "Starting friend graph!");
                    i.putExtra("friendID", toGraph.getUserID());
                    context.startActivity(i);
                }
            });
            */
        }


    }


    public void updateFriendsList(UserFriendList userFriendList) {
        this.friendsListHash = userFriendList;
        this.friendsList = this.friendsListHash.getUserFriends();
        notifyDataSetChanged();
    }


    public void addFriend(String id){
        friendsListHash.addFriend(id);
        friendsList = friendsListHash.getUserFriends();
        notifyItemInserted(friendsList.size() - 1);
    }

    @Override
    public FriendsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View friendView = inflater.inflate(R.layout.friend_contact, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(friendView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(FriendsAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        //has to be changed to reflect our friend code
         UserFriend friend = friendsList.get(position);

         // Set item views based on your views and data model
         TextView textView = viewHolder.name;
         textView.setText(friend.getUserID());
         Button button = viewHolder.chatButton;
         Button btn_week_history = viewHolder.btn_week_history;

         String friendID = friend.getUserID();
         boolean isPending = friendsListHash.isFriendPending(friendID);
         if (isPending) {
             if (!friendsListHash.userIsRequesterForFriend(friendID)) {
                 button.setText("Accept");
                 button.setBackgroundColor(Color.GREEN);
                 btn_week_history.setVisibility(View.INVISIBLE);
                 button.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         Log.d(TAG, "Accepted Pending Friend Request From: " + friendID);
                         FirebaseManager.acceptPendingFriendRequesst(friendID, true);
                     }
                 });
             } else {
                 Log.d("FriendsAdapter", "User is requester for " + friendID);
                 button.setText("Pending");
                 btn_week_history.setVisibility(View.INVISIBLE);
                 button.setBackgroundColor(Color.GRAY);
                 button.setEnabled(false);
             }
         } else {
             button.setText("Chat");
             button.setBackgroundColor(Color.LTGRAY);
             button.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     // open "chat" view
                     Log.d(TAG, "Chat with " + friendID);
                     Intent i = new Intent(context, FriendChat.class);
                     i.putExtra("friendID", friendID);
                     context.startActivity(i);
                 }
             });

             btn_week_history.setVisibility(View.VISIBLE);
             btn_week_history.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     Intent i = new Intent(context, FriendGraph.class);
                     i.putExtra("friendID", friend.getUserID());
                     context.startActivity(i);
                 }
             });
         }

    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return friendsList.size();
    }


}

