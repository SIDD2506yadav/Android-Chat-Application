package com.example.mfast;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesFragment extends Fragment {
    private DatabaseReference chatReference,userReference,lastMessageRef;
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private TextView ifLastMessageAv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ifLastMessageAv = view.findViewById(R.id.if_chat_available);
        recyclerView = view.findViewById(R.id.message_names_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Objects.requireNonNull(getActivity()).getApplication());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        userReference  = FirebaseDatabase.getInstance().getReference("Users");
        mAuth =FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid().toString();
        chatReference = FirebaseDatabase.getInstance().getReference("Friends").child(currentUserId);
        lastMessageRef = FirebaseDatabase.getInstance().getReference("LastMessage");

        lastMessageRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    ifLastMessageAv.setVisibility(View.GONE);
                }
                else{
                    ifLastMessageAv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        loadUsers(view);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void  loadUsers(View view){
        Query query = lastMessageRef.child(currentUserId).orderByChild("timeStamp");

        FirebaseRecyclerOptions<SearchUsers> options =
                new FirebaseRecyclerOptions.Builder<SearchUsers>()
                        .setQuery(query,SearchUsers.class)
                        .build();
        FirebaseRecyclerAdapter<SearchUsers,MessagesFragment.FindFriendsViewHolder> adapter =
                new FirebaseRecyclerAdapter<SearchUsers, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final FindFriendsViewHolder holder, int position, @NonNull SearchUsers model) {
                        final String otherUserId = getRef(position).getKey();
                        userReference.child(otherUserId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                final String sName = snapshot.child("Name").getValue().toString();
                                holder.name.setText(sName);
                                if(snapshot.child("ProfilePicture").exists()){
                                    String profile_picture = snapshot.child("ProfilePicture").getValue().toString();
                                    Picasso.get().load(profile_picture).placeholder(R.drawable.user_profile_holder).into(holder.profilePicture);
                                }
                                holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getActivity().getApplication(),ChatActivity.class);
                                        intent.putExtra("OtherUserId",otherUserId);
                                        startActivity(intent);
                                    }
                                });

                                lastMessageRef.child(currentUserId).child(otherUserId).addValueEventListener(new ValueEventListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.O)
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String from = snapshot.child("from").getValue().toString();
                                        String message = snapshot.child("lastMessage").getValue().toString().replaceAll("\n"," ");
                                        String completeTime = snapshot.child("timeStamp").getValue().toString();
                                        if(from.equals(currentUserId)){
                                            String fullMessage = "You: " +message;
                                            holder.lastMessage.setText(fullMessage);
                                        }
                                        else{
                                            String fullMessage = sName +": " + message;
                                            holder.lastMessage.setText(fullMessage);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_with_users_layout,parent,false);
                        MessagesFragment.FindFriendsViewHolder holder = new MessagesFragment.FindFriendsViewHolder(view);
                        return holder;
                    }
                };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout relativeLayout;
        private TextView name,lastMessage,lastMessageTime;
        private CircleImageView profilePicture;
        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user_with_chat_name);
            profilePicture = itemView.findViewById(R.id.user_with_chat_profile_picture);
            relativeLayout = itemView.findViewById(R.id.chat_list_outer_view);
            lastMessage = itemView.findViewById(R.id.last_message);
            lastMessageTime = itemView.findViewById(R.id.last_message_time);
        }
    }
}
