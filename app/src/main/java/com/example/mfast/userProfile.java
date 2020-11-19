package com.example.mfast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class userProfile extends AppCompatActivity {
    private ImageView goBack,coverPicture;
    private Button editCoverPhoto;
    private CircleImageView profilePicture,logoutFromAccount,goToEditProfile,changePassword;
    private TextView name,location,bio,dob,friends,profession;
    private DatabaseReference databaseReference,friendReference;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private LinearLayout friendsLayout;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        declareVariables();
        loadDetails();
        loadFriends();

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(userProfile.this,HomeActivity.class));
            }
        });
        goToEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(userProfile.this,EditProfile.class);
                startActivity(intent);
            }
        });

        logoutFromAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(userProfile.this);
                builder.setMessage("Do you want to logout ? ");
                builder.setTitle("Alert!");
                builder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAuth.signOut();
                                Toast.makeText(userProfile.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(userProfile.this,MainActivity.class));
                                finish();
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
        });
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChangePasswordDialog();
            }
        });
        friendsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(userProfile.this,FriendsListActivity.class);
                intent.putExtra("UserId",currentUserId);
                startActivity(intent);
            }
        });
    }

    private void declareVariables(){
        goToEditProfile = findViewById(R.id.go_to_edit_profile);
        profilePicture = findViewById(R.id.user_profile_picture);
        name = findViewById(R.id.user_profile_name);
        location = findViewById(R.id.user_location);
        bio = findViewById(R.id.user_about_status);
        dob = findViewById(R.id.user_dob);
        coverPicture = findViewById(R.id.user_cover_picture);
        changePassword = findViewById(R.id.change_account_password);
        logoutFromAccount = findViewById(R.id.logout_from_account);
        goBack = findViewById(R.id.go_back_from_profile);
        friends = findViewById(R.id.user_friends);
        friendsLayout = findViewById(R.id.friends_layout);
        profession = findViewById(R.id.user_profile_profession);
        recyclerView = findViewById(R.id.user_friends_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        currentUserId = mAuth.getCurrentUser().getUid();
        friendReference = FirebaseDatabase.getInstance().getReference("Friends").child(currentUserId);
    }
    private void loadDetails(){
        databaseReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("ProfilePicture")){
                    String profile_picture = dataSnapshot.child("ProfilePicture").getValue().toString();
                    Picasso.get().load(profile_picture).placeholder(R.drawable.user_profile_holder).into(profilePicture);
                }
                if(dataSnapshot.hasChild("CoverPicture")){
                    String cover_picture = dataSnapshot.child("CoverPicture").getValue().toString();
                    Picasso.get().load(cover_picture).into(coverPicture);
                }
                String user_name = dataSnapshot.child("Name").getValue().toString().toString();
                name.setText(user_name);

                String user_bio = dataSnapshot.child("Status").getValue().toString().toString();
                if(user_bio.length() == 0){
                    bio.setVisibility(View.GONE);
                }
                else{
                    bio.setVisibility(View.VISIBLE);
                    bio.setText(user_bio);
                }

                String user_location = dataSnapshot.child("Location").getValue().toString();
                if(user_location.length() == 0){
                    location.setVisibility(View.GONE);
                }
                else{
                    location.setVisibility(View.VISIBLE);
                    location.setText(user_location);
                }

                String user_dob = dataSnapshot.child("Dob").getValue().toString();
                if(user_dob.length() == 0){
                    dob.setVisibility(View.GONE);
                }
                else{
                    dob.setVisibility(View.VISIBLE);
                    dob.setText(user_dob);
                }
                String user_profession = dataSnapshot.child("Profession").getValue().toString();
                if(user_profession.length() == 0){
                    profession.setVisibility(View.GONE);
                }
                else{
                    profession.setVisibility(View.VISIBLE);
                    profession.setText(user_profession);
                }

                String user_friends = dataSnapshot.child("Friends").getValue().toString().toString();
                friends.setText(user_friends);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void loadFriends(){
        Query query = friendReference.orderByChild("Friends");
        FirebaseRecyclerOptions<SearchUsers> options =
                new FirebaseRecyclerOptions.Builder<SearchUsers>()
                        .setQuery(query,SearchUsers.class)
                        .build();

        FirebaseRecyclerAdapter<SearchUsers,userProfile.FindFriendsViewHolder> adapter =
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
                                        holder.acceptReject.setVisibility(View.GONE);
                                        holder.send.setVisibility(View.VISIBLE);
                                        holder.sendRequest.setText("Send Message");
                                        holder.requestStatus.setText("Already friends,send message");

                                        holder.sendRequest.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(userProfile.this,ChatActivity.class);
                                                intent.putExtra("OtherUserId",otherUserId);
                                                startActivity(intent);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(userProfile.this,OtherUserCompleteProfile.class);
                                intent.putExtra("OtherUserId",otherUserId);
                                startActivity(intent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.other_user_outside_view,parent,false);
                        userProfile.FindFriendsViewHolder viewHolder = new userProfile.FindFriendsViewHolder(view);
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
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(userProfile.this,HomeActivity.class));
    }
    private void openChangePasswordDialog(){
        ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
        changePasswordDialog.show(getSupportFragmentManager(),"Change Password");
    }
}