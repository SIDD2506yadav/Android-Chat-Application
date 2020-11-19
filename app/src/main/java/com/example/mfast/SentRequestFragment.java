package com.example.mfast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SentRequestFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private String currentUser,currentUserFriends,otherUserFriends;
    private int cNum,oNum;
    private LayoutInflater in;
    private DatabaseReference userReference,databaseReference,requestReference,friendReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sent_request,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.sent_frag_progress_bar);

        userReference = FirebaseDatabase.getInstance().getReference("UserNames");
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        friendReference = FirebaseDatabase.getInstance().getReference("Friends");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid().toString();
        requestReference = FirebaseDatabase.getInstance().getReference("Requests");

        recyclerView = view.findViewById(R.id.sent_frag_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(Objects.requireNonNull(getActivity()).getApplication()));

        loadRequests(view);
    }
    private void loadRequests(final View view){
        progressBar.setVisibility(View.VISIBLE);
        FirebaseRecyclerOptions<SearchUsers> options =
                new FirebaseRecyclerOptions.Builder<SearchUsers>()
                        .setQuery(requestReference.child(currentUser).orderByChild("Status"),SearchUsers.class)
                        .build();
        final  FirebaseRecyclerAdapter<SearchUsers,SentRequestFragment.FindFriendsViewHolder> adapter =
                new FirebaseRecyclerAdapter<SearchUsers, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final FindFriendsViewHolder holder, int position, @NonNull SearchUsers model) {
                        final String otherUserId = getRef(position).getKey();
                        final String status = model.getStatus().toString();


                        if(!status.equals("Received")){
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
                                    holder.send.setVisibility(View.VISIBLE);
                                    holder.acceptReject.setVisibility(View.GONE);
                                    holder.requestStatus.setText("Want to cancel sent request?");

                                    holder.sendRequest.setText("Cancel Request");
                                    holder.send.setOnClickListener(new View.OnClickListener() {
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
                        SentRequestFragment.FindFriendsViewHolder holder = new SentRequestFragment.FindFriendsViewHolder(view);
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
}

