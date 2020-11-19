package com.example.mfast;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment {
    private DatabaseReference friendsReference,userReference;
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private TextView ifFriends;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ifFriends = view.findViewById(R.id.if_user_has_friends);
        recyclerView = view.findViewById(R.id.friend_list_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(Objects.requireNonNull(getActivity()).getApplication()));

        friendsReference = FirebaseDatabase.getInstance().getReference("Friends");
        userReference  = FirebaseDatabase.getInstance().getReference("Users");
        mAuth =FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid().toString();

        friendsReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    ifFriends.setVisibility(View.GONE);
                }
                else{
                    ifFriends.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        displayFriends(view);
    }
    private void displayFriends(View view){
        Query query = friendsReference.child(currentUserId).orderByChild("Friends");

        FirebaseRecyclerOptions<SearchUsers> options =
                new FirebaseRecyclerOptions.Builder<SearchUsers>()
                        .setQuery(query,SearchUsers.class)
                        .build();

        FirebaseRecyclerAdapter<SearchUsers,FriendsFragment.FindFriendsViewHolder> adapter =
                new FirebaseRecyclerAdapter<SearchUsers, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final FindFriendsViewHolder holder, int position, @NonNull SearchUsers model) {
                        final String otherUserId = getRef(position).getKey();
                        userReference.child(otherUserId).addValueEventListener(new ValueEventListener() {
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
                                        Intent intent = new Intent(getActivity().getApplication(),ChatActivity.class);
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
                                if(otherUserId.equals(currentUserId)){
                                    startActivity(new Intent(getActivity().getApplication(),userProfile.class));
                                }
                                else{
                                    Intent intent = new Intent(getActivity().getApplication(),OtherUserCompleteProfile.class);
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
                        FriendsFragment.FindFriendsViewHolder holder = new FriendsFragment.FindFriendsViewHolder(view);
                        return holder;
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
}
