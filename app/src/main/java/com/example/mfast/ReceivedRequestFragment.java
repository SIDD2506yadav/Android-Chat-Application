package com.example.mfast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReceivedRequestFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private TextView ifRequests;
    private String currentUser,currentUserFriends,otherUserFriends;
    private int cNum,oNum;
    private LayoutInflater in;
    private DatabaseReference userReference,databaseReference,requestReference,friendReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_received_request,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.received_frag_progress_bar);
        ifRequests = view.findViewById(R.id.if_received_friend_requests);

        userReference = FirebaseDatabase.getInstance().getReference("UserNames");
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        friendReference = FirebaseDatabase.getInstance().getReference("Friends");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid().toString();
        requestReference = FirebaseDatabase.getInstance().getReference("Requests");

        recyclerView = view.findViewById(R.id.received_frag_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(Objects.requireNonNull(getActivity()).getApplication()));

        loadRequests(view);

    }
    private void loadRequests(final View view){
        progressBar.setVisibility(View.VISIBLE);

        /*requestReference.child(currentUser).orderByChild("Status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue().toString().equals("Sent")){
                    ifRequests.setVisibility(View.GONE);
                }
                else{
                    ifRequests.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/


        FirebaseRecyclerOptions<SearchUsers> options =
                new FirebaseRecyclerOptions.Builder<SearchUsers>()
                        .setQuery(requestReference.child(currentUser).orderByChild("Status"),SearchUsers.class)
                        .build();
        final FirebaseRecyclerAdapter<SearchUsers,ReceivedRequestFragment.FindFriendsViewHolder> adapter =
                new FirebaseRecyclerAdapter<SearchUsers, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final FindFriendsViewHolder holder, int position, @NonNull SearchUsers model) {

                        final String otherUserId = getRef(position).getKey();
                        final String status = model.getStatus().toString();
                        if(status.equals("Received")){
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
                            requestReference.child(currentUser).child(otherUserId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    holder.send.setVisibility(View.GONE);
                                    holder.acceptReject.setVisibility(View.VISIBLE);
                                    holder.requestStatus.setText("Requested to be your friend");

                                    holder.acceptReject.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            acceptFriendRequest(otherUserId,view);
                                        }
                                    });

                                    holder.rejectRequest.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            cancelFriendRequest(otherUserId,view);
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
                        else{
                            holder.rLayout.setVisibility(View.GONE);
                            holder.rLayout.getLayoutParams().height = 0;
                            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)holder.rLayout.getLayoutParams();
                            params.topMargin = 0;
                            params.bottomMargin = 0;
                        }


                    }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.other_user_outside_view,parent,false);
                        FindFriendsViewHolder holder = new FindFriendsViewHolder(view);
                        return holder;
                    }
                };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        progressBar.setVisibility(View.INVISIBLE);
    }
    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        TextView name,connections,requestStatus,requestType,profession;
        CircleImageView profilePicture;
        TextView rejectRequest,acceptRequest,sendRequest;
        LinearLayout acceptReject,send;
        RelativeLayout relativeLayout,rLayout;

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
            rLayout = itemView.findViewById(R.id.other_user_outside_outer_layout);
        }
    }
    private void cancelFriendRequest(final String otherUserId, final View view){
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

                            }
                        }
                    });
                }
            }
        });
    }
    public void acceptFriendRequest(final String otherUserId,final View view){
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
                                                        Toast.makeText(getActivity(), "Friend request accepted", Toast.LENGTH_SHORT).show();
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
