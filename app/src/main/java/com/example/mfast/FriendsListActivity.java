package com.example.mfast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsListActivity extends AppCompatActivity {
    private String userId,currentUserId;
    private RecyclerView recyclerView ;
    private ImageView goBack,searchUser;
    private EditText enterUserName;
    private FirebaseAuth mAuth;
    private String currentUser,currentUserFriends,otherUserFriends;
    private int cNum,oNum;
    private DatabaseReference userReference,databaseReference,requestReference,friendReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        Intent intent = getIntent();
        userId = intent.getExtras().getString("UserId").toString();

        recyclerView = findViewById(R.id.recycler_view_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userReference = FirebaseDatabase.getInstance().getReference("Friends").child(userId);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        requestReference = FirebaseDatabase.getInstance().getReference("Requests");
        friendReference = FirebaseDatabase.getInstance().getReference("Friends");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid().toString();

        displayAllFriends();


    }
    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        TextView name,connections,requestStatus,requestType,profession;
        CircleImageView profilePicture;
        TextView rejectRequest,acceptRequest,sendRequest;
        LinearLayout acceptReject,send;
        RelativeLayout relativeLayout;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.other_user_profile_name);
            connections = itemView.findViewById(R.id.user_no_of_friends);
            requestStatus = itemView.findViewById(R.id.sent_you_request_text);
            profilePicture = itemView.findViewById(R.id.other_user_profile_picture_view);
            rejectRequest = itemView.findViewById(R.id.cancel_request_button);
            acceptRequest = itemView.findViewById(R.id.accept_request_button);
            sendRequest = itemView.findViewById(R.id.send_request_button);
            send = itemView.findViewById(R.id.send_user_request_layout);
            acceptReject = itemView.findViewById(R.id.accept_reject_layout);
            profession = itemView.findViewById(R.id.other_user_profession);
            relativeLayout = itemView.findViewById(R.id.other_user_view_layout);
        }
    }
    public void displayAllFriends(){

        Query query = userReference.orderByChild("Friends");

        FirebaseRecyclerOptions<SearchUsers> options =
                new FirebaseRecyclerOptions.Builder<SearchUsers>()
                        .setQuery(query,SearchUsers.class)
                        .build();

        FirebaseRecyclerAdapter<SearchUsers,FindFriendsViewHolder> adapter =
                new FirebaseRecyclerAdapter<SearchUsers, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final FindFriendsViewHolder holder, int position, @NonNull SearchUsers model) {
                        final String otherUserId = getRef(position).getKey();
                        databaseReference.child(otherUserId)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String sName = snapshot.child("Name").getValue().toString();
                                        holder.name.setText(sName);
                                        String sProfession = snapshot.child("Profession").getValue().toString();
                                        if(sProfession.length() != 0){
                                            holder.profession.setVisibility(View.VISIBLE);
                                            holder.profession.setText(sProfession);
                                        }
                                        else{
                                            holder.profession.setVisibility(View.GONE);
                                        }
                                        String sFriends = snapshot.child("Friends").getValue().toString();
                                        holder.connections.setText(sFriends + " connections");
                                        if(sFriends == null || sFriends.equals("0")){
                                            oNum = 0;
                                        }
                                        else{
                                            oNum = Integer.parseInt(sFriends) ;
                                        }
                                        if(snapshot.hasChild("ProfilePicture")){
                                            String sProfile = snapshot.child("ProfilePicture").getValue().toString();
                                            Picasso.get().load(sProfile).placeholder(R.drawable.user_profile_holder)
                                                    .into(holder.profilePicture);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                        if(otherUserId.equals(currentUser)){
                            holder.requestStatus.setText("Your account");
                            holder.send.setVisibility(View.GONE);
                            holder.acceptReject.setVisibility(View.GONE);
                        }
                        else{
                            requestReference.addValueEventListener(new ValueEventListener() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        if(snapshot.child(currentUser).child(otherUserId).exists()){
                                            String status = snapshot.child(currentUser).child(otherUserId).child("Status").getValue().toString();
                                            if(status.equals("Received")){
                                                holder.send.setVisibility(View.GONE);
                                                holder.acceptReject.setVisibility(View.VISIBLE);
                                                holder.requestStatus.setText("Requested to be your friend");

                                                holder.acceptReject.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        acceptFriendRequest(otherUserId);
                                                    }
                                                });

                                                holder.rejectRequest.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        cancelFriendRequest(otherUserId);
                                                    }
                                                });
                                            }
                                            else{
                                                holder.acceptReject.setVisibility(View.GONE);
                                                holder.send.setVisibility(View.VISIBLE);
                                                holder.sendRequest.setText("Cancel Request");
                                                holder.requestStatus.setText("Want to cancel sent request?");

                                                holder.sendRequest.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        cancelFriendRequest(otherUserId);
                                                    }
                                                });
                                            }
                                        }
                                        else{
                                            friendReference.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(snapshot.exists()){
                                                        if(snapshot.child(currentUser).child(otherUserId).exists()){
                                                            holder.acceptReject.setVisibility(View.GONE);
                                                            holder.send.setVisibility(View.VISIBLE);
                                                            holder.sendRequest.setText("Send Message");
                                                            holder.requestStatus.setText("Already friends,send message");

                                                            holder.sendRequest.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    Intent intent = new Intent(FriendsListActivity.this,ChatActivity.class);
                                                                    intent.putExtra("OtherUserId",otherUserId);
                                                                    startActivity(intent);
                                                                }
                                                            });
                                                        }
                                                        else{
                                                            if(!currentUser.equals(otherUserId)){
                                                                holder.acceptReject.setVisibility(View.GONE);
                                                                holder.send.setVisibility(View.VISIBLE);
                                                                holder.sendRequest.setText("Send Request");
                                                                holder.requestStatus.setText("Send friend request");

                                                                holder.sendRequest.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        sendFriendRequest(otherUserId);
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    }
                                                    else{
                                                        if(!currentUser.equals(otherUserId)){
                                                            holder.acceptReject.setVisibility(View.GONE);
                                                            holder.send.setVisibility(View.VISIBLE);
                                                            holder.sendRequest.setText("Send Request");
                                                            holder.requestStatus.setText("Send friend request");

                                                            holder.sendRequest.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    sendFriendRequest(otherUserId);
                                                                }
                                                            });
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    }
                                    else{
                                        friendReference.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.exists()){
                                                    if(snapshot.child(currentUser).child(otherUserId).exists()){
                                                        holder.acceptReject.setVisibility(View.GONE);
                                                        holder.send.setVisibility(View.VISIBLE);
                                                        holder.sendRequest.setText("Send Message");
                                                        holder.requestStatus.setText("Already friends,send message");

                                                        holder.sendRequest.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                Intent intent = new Intent(FriendsListActivity.this,ChatActivity.class);
                                                                intent.putExtra("OtherUserId",otherUserId);
                                                                startActivity(intent);
                                                            }
                                                        });
                                                    }
                                                    else{
                                                        if(!currentUser.equals(otherUserId)){
                                                            holder.acceptReject.setVisibility(View.GONE);
                                                            holder.send.setVisibility(View.VISIBLE);
                                                            holder.sendRequest.setText("Send Request");
                                                            holder.requestStatus.setText("Send friend request");

                                                            holder.sendRequest.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    sendFriendRequest(otherUserId);
                                                                }
                                                            });
                                                        }
                                                    }
                                                }
                                                else{
                                                    if(!currentUser.equals(otherUserId)){
                                                        holder.acceptReject.setVisibility(View.GONE);
                                                        holder.send.setVisibility(View.VISIBLE);
                                                        holder.sendRequest.setText("Send Request");
                                                        holder.requestStatus.setText("Send friend request");

                                                        holder.sendRequest.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                sendFriendRequest(otherUserId);
                                                            }
                                                        });
                                                    }
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(otherUserId.equals(currentUser)){
                                    startActivity(new Intent(FriendsListActivity.this,userProfile.class));
                                }
                                else{
                                    Intent intent = new Intent(FriendsListActivity.this,OtherUserCompleteProfile.class);
                                    intent.putExtra("OtherUserId",otherUserId);
                                    startActivity(intent);
                                }
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.other_user_outside_view,parent,false);
                        FriendsListActivity.FindFriendsViewHolder viewHolder = new FriendsListActivity.FindFriendsViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(adapter);

        adapter.startListening();
    }
    private void sendFriendRequest(final String otherUserId){
        requestReference.child(currentUser).child(otherUserId).child("Status").setValue("Sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            requestReference.child(otherUserId).child(currentUser).child("Status").setValue("Received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Snackbar.make(findViewById(android.R.id.content),"Friend request sent",Snackbar.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
    private void cancelFriendRequest(final String otherUserId){
        requestReference.child(currentUser).child(otherUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    requestReference.child(otherUserId).child(currentUser)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Snackbar.make(findViewById(android.R.id.content),"Request canceled",Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }
    public void acceptFriendRequest(final String otherUserId){
        friendReference.child(currentUser).child(otherUserId).child("Friends")
                .setValue("True").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    friendReference.child(otherUserId).child(currentUser).child("Friends")
                            .setValue("True").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                requestReference.child(currentUser).child(otherUserId)
                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            requestReference.child(otherUserId).child(currentUser)
                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        increaseNoOfFriends(otherUserId);
                                                        Snackbar.make(findViewById(android.R.id.content),"Request accepted",Snackbar.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }
    private void increaseNoOfFriends(final String otherUserId){

        friendReference.child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long cFriends = snapshot.getChildrenCount();
                databaseReference.child(currentUser).child("Friends").setValue(String.valueOf(String.valueOf(cFriends)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        friendReference.child(otherUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long cFriends = snapshot.getChildrenCount();
                databaseReference.child(otherUserId).child("Friends").setValue(String.valueOf(String.valueOf(cFriends)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //databaseReference.child(currentUser).child("Friends").setValue(String.valueOf(newCNum));
    }
}