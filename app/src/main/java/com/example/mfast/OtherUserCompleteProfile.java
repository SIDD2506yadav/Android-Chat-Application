package com.example.mfast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class OtherUserCompleteProfile extends AppCompatActivity {
    private ImageView coverPicture;
    private CircleImageView profilePicture,goToChat;
    private TextView name,location,birthday,profession,friends,status;
    private Button accept,reject;
    private String otherUserId,currentUserId;
    private DatabaseReference userReference,friendReference,requestReference,databaseReference,messageReference,lastMessageReference;
    private FirebaseAuth mAuth;
    private LinearLayout linearLayout ;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_complete_profile);
        Intent intent = getIntent();
        otherUserId = intent.getExtras().getString("OtherUserId").toString();
        declareVariables();
        loadDetails();
        loadFriends();

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(OtherUserCompleteProfile.this,FriendsListActivity.class);
                intent1.putExtra("UserId",otherUserId);
                startActivity(intent1);
            }
        });
    }
    private void declareVariables(){
        profilePicture = findViewById(R.id.other_user_picture);
        goToChat = findViewById(R.id.other_user_send_message);
        name = findViewById(R.id.other_user_name);
        location = findViewById(R.id.other_user_location);
        birthday = findViewById(R.id.other_user_birthday);
        profession = findViewById(R.id.other_user_profession);
        friends = findViewById(R.id.other_user_friends);
        status = findViewById(R.id.other_user_status);
        accept = findViewById(R.id.other_user_request_accept);
        reject = findViewById(R.id.other_user_request_cancel);
        linearLayout = findViewById(R.id.other_user_friends_layout);
        coverPicture = findViewById(R.id.other_user_cover_photo);
        recyclerView = findViewById(R.id.other_user_friends_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userReference = FirebaseDatabase.getInstance().getReference("Users");
        requestReference = FirebaseDatabase.getInstance().getReference("Requests");
        friendReference = FirebaseDatabase.getInstance().getReference("Friends");
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        lastMessageReference = FirebaseDatabase.getInstance().getReference("LastMessage");
        messageReference = FirebaseDatabase.getInstance().getReference("Messages");
    }
    private void loadDetails(){
        userReference.child(otherUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("ProfilePicture")){
                    String sProfile = snapshot.child("ProfilePicture").getValue().toString();
                    Picasso.get().load(sProfile).placeholder(R.drawable.user_profile_holder).into(profilePicture);
                }
                if(snapshot.hasChild("CoverPicture")){
                    String cover_picture = snapshot.child("CoverPicture").getValue().toString();
                    Picasso.get().load(cover_picture).into(coverPicture);
                }
                String sName = snapshot.child("Name").getValue().toString();
                name.setText(sName);
                String sProfession = snapshot.child("Profession").getValue().toString();
                if(sProfession.length() != 0){
                    profession.setVisibility(View.VISIBLE);
                    profession.setText(sProfession);
                }
                else{
                    profession.setVisibility(View.GONE);
                }
                String sStatus = snapshot.child("Status").getValue().toString();
                if(sStatus.length() != 0){
                    status.setVisibility(View.VISIBLE);
                    status.setText(sStatus);
                }
                else{
                    status.setVisibility(View.GONE);
                }
                String sBirthday = snapshot.child("Dob").getValue().toString();
                if(sStatus.length() != 0){
                    birthday.setVisibility(View.VISIBLE);
                    birthday.setText(sBirthday);
                }
                else{
                    birthday.setVisibility(View.GONE);
                }
                String sLocation = snapshot.child("Location").getValue().toString();
                if(sStatus.length() != 0){
                    location.setVisibility(View.VISIBLE);
                    location.setText(sLocation);
                }
                else{
                    location.setVisibility(View.GONE);
                }
                String sFriends = snapshot.child("Friends").getValue().toString();
                friends.setText(sFriends);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        requestReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child(currentUserId).child(otherUserId).exists()){
                        String status = snapshot.child(currentUserId).child(otherUserId).child("Status").getValue().toString();
                        if(status.equals("Received")){
                            goToChat.setVisibility(View.GONE);
                            accept.setVisibility(View.VISIBLE);
                            reject.setVisibility(View.VISIBLE);
                            accept.setText("Accept Friend Request");
                            reject.setText("Cancel Friend Request");

                            accept.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    acceptFriendRequest(otherUserId);
                                }
                            });
                            reject.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    cancelFriendRequest(otherUserId);
                                }
                            });
                        }
                        else{
                            goToChat.setVisibility(View.GONE);
                            accept.setVisibility(View.GONE);
                            reject.setVisibility(View.VISIBLE);
                            reject.setText("Cancel Sent Request");
                            reject.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    cancelFriendRequest(otherUserId);
                                }
                            });
                        }
                    }
                    else{
                        friendReference.addValueEventListener(new ValueEventListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    if(snapshot.child(currentUserId).child(otherUserId).exists()){
                                        goToChat.setVisibility(View.VISIBLE);
                                        accept.setVisibility(View.GONE);
                                        reject.setVisibility(View.VISIBLE);
                                        reject.setText("Already Friend");

                                        reject.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                removeFriend(otherUserId);
                                            }
                                        });
                                        goToChat.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(OtherUserCompleteProfile.this,ChatActivity.class);
                                                intent.putExtra("OtherUserId",otherUserId);
                                                startActivity(intent);
                                            }
                                        });

                                    }
                                    else{
                                        goToChat.setVisibility(View.GONE);
                                        accept.setVisibility(View.GONE);
                                        reject.setVisibility(View.VISIBLE);
                                        reject.setText("Send Friend Request");

                                        reject.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                sendFriendRequest(otherUserId);
                                            }
                                        });
                                    }
                                }
                                else{
                                    goToChat.setVisibility(View.GONE);
                                    accept.setVisibility(View.GONE);
                                    reject.setVisibility(View.VISIBLE);
                                    reject.setText("Send Friend Request");
                                    reject.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            sendFriendRequest(otherUserId);
                                        }
                                    });
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
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                if(snapshot.child(currentUserId).child(otherUserId).exists()){
                                    goToChat.setVisibility(View.VISIBLE);
                                    accept.setVisibility(View.GONE);
                                    reject.setVisibility(View.VISIBLE);
                                    reject.setText("Already Friend");
                                    reject.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            removeFriend(otherUserId);
                                        }
                                    });

                                    goToChat.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(OtherUserCompleteProfile.this,ChatActivity.class);
                                            intent.putExtra("OtherUserId",otherUserId);
                                            startActivity(intent);
                                        }
                                    });


                                }
                                else{
                                    goToChat.setVisibility(View.GONE);
                                    accept.setVisibility(View.GONE);
                                    reject.setVisibility(View.VISIBLE);
                                    reject.setText("Send Friend Request");
                                    reject.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            sendFriendRequest(otherUserId);
                                        }
                                    });
                                }
                            }
                            else{
                                goToChat.setVisibility(View.GONE);
                                accept.setVisibility(View.GONE);
                                reject.setVisibility(View.VISIBLE);
                                reject.setText("Send Friend Request");
                                reject.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        sendFriendRequest(otherUserId);
                                    }
                                });
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
    private void loadFriends(){
        Query query = friendReference.child(otherUserId).orderByChild("Friends");

        FirebaseRecyclerOptions<SearchUsers> options =
                new FirebaseRecyclerOptions.Builder<SearchUsers>()
                        .setQuery(query,SearchUsers.class)
                        .build();

        FirebaseRecyclerAdapter<SearchUsers,OtherUserCompleteProfile.FindFriendsViewHolder> adapter =
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
                        if(otherUserId.equals(currentUserId)){
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
                                        if(snapshot.child(currentUserId).child(otherUserId).exists()){
                                            String status = snapshot.child(currentUserId).child(otherUserId).child("Status").getValue().toString();
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
                                                        if(snapshot.child(currentUserId).child(otherUserId).exists()){
                                                            holder.acceptReject.setVisibility(View.GONE);
                                                            holder.send.setVisibility(View.VISIBLE);
                                                            holder.sendRequest.setText("Send Message");
                                                            holder.requestStatus.setText("Already friends,send message");

                                                            holder.sendRequest.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    Intent intent = new Intent(OtherUserCompleteProfile.this,ChatActivity.class);
                                                                    intent.putExtra("OtherUserId",otherUserId);
                                                                    startActivity(intent);
                                                                }
                                                            });
                                                        }
                                                        else{
                                                            if(!currentUserId.equals(otherUserId)){
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
                                                        if(!currentUserId.equals(otherUserId)){
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
                                                    if(snapshot.child(currentUserId).child(otherUserId).exists()){
                                                        holder.acceptReject.setVisibility(View.GONE);
                                                        holder.send.setVisibility(View.VISIBLE);
                                                        holder.sendRequest.setText("Send Message");
                                                        holder.requestStatus.setText("Already friends,send message");

                                                        holder.sendRequest.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                Intent intent = new Intent(OtherUserCompleteProfile.this,ChatActivity.class);
                                                                intent.putExtra("OtherUserId",otherUserId);
                                                                startActivity(intent);
                                                            }
                                                        });
                                                    }
                                                    else{
                                                        if(!currentUserId.equals(otherUserId)){
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
                                                    if(!currentUserId.equals(otherUserId)){
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
                                if(otherUserId.equals(currentUserId)){
                                    startActivity(new Intent(OtherUserCompleteProfile.this,userProfile.class));
                                }
                                else{
                                    Intent intent = new Intent(OtherUserCompleteProfile.this,OtherUserCompleteProfile.class);
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
                        OtherUserCompleteProfile.FindFriendsViewHolder viewHolder = new OtherUserCompleteProfile.FindFriendsViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
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
    private void sendFriendRequest(final String otherUserId){
        requestReference.child(currentUserId).child(otherUserId).child("Status").setValue("Sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            requestReference.child(otherUserId).child(currentUserId).child("Status").setValue("Received")
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
        requestReference.child(currentUserId).child(otherUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    requestReference.child(otherUserId).child(currentUserId)
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
        friendReference.child(currentUserId).child(otherUserId).child("Friends")
                .setValue("True").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    friendReference.child(otherUserId).child(currentUserId).child("Friends")
                            .setValue("True").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                requestReference.child(currentUserId).child(otherUserId)
                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            requestReference.child(otherUserId).child(currentUserId)
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

        friendReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long cFriends = snapshot.getChildrenCount();
                databaseReference.child(currentUserId).child("Friends").setValue(String.valueOf(String.valueOf(cFriends)));
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
    private void removeFriend(final String otherUserId){
        AlertDialog.Builder builder = new AlertDialog.Builder(OtherUserCompleteProfile.this);
        builder.setMessage("Do you want to unfriend "+name.getText().toString()+"?");
        builder.setTitle("Alert!");
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        friendReference.child(currentUserId).child(otherUserId)
                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    friendReference.child(otherUserId).child(currentUserId)
                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                lastMessageReference.child(currentUserId).child(otherUserId).removeValue();
                                                lastMessageReference.child(otherUserId).child(currentUserId).removeValue();
                                                messageReference.child(currentUserId).child(otherUserId).removeValue();
                                                messageReference.child(otherUserId).child(currentUserId).removeValue();
                                                friendReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        Long cFriends = snapshot.getChildrenCount();
                                                        databaseReference.child(currentUserId).child("Friends").setValue(String.valueOf(String.valueOf(cFriends)));
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
                                                Toast.makeText(OtherUserCompleteProfile.this,
                                                        name.getText().toString()+" removed from your friend list", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
        builder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}