package com.example.mfast;

import android.app.Activity;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private FirebaseAuth mAuth;
    private DatabaseReference userReference;
    private List<Messages> userMessagesList ;

    public MessageAdapter(List<Messages> userMessagesList){
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_messages_background,parent,false);
        mAuth = FirebaseAuth.getInstance();


        return new MessageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String currentUserId = mAuth.getCurrentUser().getUid().toString();
        Messages messages = userMessagesList.get(position);
        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        if(position == userMessagesList.size()-1){
            if(fromMessageType.equals("picture")){
                if(messages.getIsSeen().equals("true")){
                    holder.isPictureSeen.setVisibility(View.VISIBLE);
                }
                else{
                    holder.isPictureSeen.setVisibility(View.GONE);
                }
            }
            else{
                if(messages.getIsSeen().equals("true")){
                    holder.isPictureSeen.setVisibility(View.GONE);
                    holder.messageSeen.setVisibility(View.VISIBLE);
                }
                else{
                    holder.isPictureSeen.setVisibility(View.GONE);
                    holder.messageSeen.setVisibility(View.GONE);
                }
            }
        }
        else{
            holder.messageSeen.setVisibility(View.GONE);
            holder.isPictureSeen.setVisibility(View.GONE);
        }

        if(fromMessageType.equals("text")){
            holder.receivedMessage.setVisibility(View.GONE);
            holder.sentMessage.setVisibility(View.GONE);
            holder.receivedPicture.setVisibility(View.GONE);
            holder.sentPicture.setVisibility(View.GONE);
            holder.isPictureSeen.setVisibility(View.GONE);
            if(fromUserId.equals(currentUserId)){
                holder.sentMessage.setVisibility(View.VISIBLE);
                holder.receivedMessage.setVisibility(View.GONE);
                holder.receivedPicture.setVisibility(View.GONE);
                holder.sentPicture.setVisibility(View.GONE);
                holder.sentMessage.setText(messages.getMessage().toString());
            }
            else{
                holder.sentMessage.setVisibility(View.GONE);
                holder.receivedMessage.setVisibility(View.VISIBLE);
                holder.receivedPicture.setVisibility(View.GONE);
                holder.sentPicture.setVisibility(View.GONE);
                holder.receivedMessage.setText(messages.getMessage().toString());
            }
        }
        else{
            holder.receivedMessage.setVisibility(View.GONE);
            holder.sentMessage.setVisibility(View.GONE);
            holder.receivedPicture.setVisibility(View.GONE);
            holder.sentPicture.setVisibility(View.GONE);
            if(fromUserId.equals(currentUserId)){
                holder.receivedMessage.setVisibility(View.GONE);
                holder.sentMessage.setVisibility(View.GONE);
                holder.receivedPicture.setVisibility(View.GONE);
                holder.sentPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage().toString()).into(holder.sentPicture);
            }
            else{
                holder.receivedMessage.setVisibility(View.GONE);
                holder.sentMessage.setVisibility(View.GONE);
                holder.receivedPicture.setVisibility(View.VISIBLE);
                holder.sentPicture.setVisibility(View.GONE);
                Picasso.get().load(messages.getMessage()).into(holder.receivedPicture);
            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        private TextView sentMessage,receivedMessage,messageSeen,isPictureSeen;
        private ImageView sentPicture,receivedPicture;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sentMessage = itemView.findViewById(R.id.sent_message);
            receivedMessage = itemView.findViewById(R.id.received_message);
            messageSeen = itemView.findViewById(R.id.message_seen);
            sentPicture = itemView.findViewById(R.id.sent_picture);
            receivedPicture = itemView.findViewById(R.id.received_picture);
            isPictureSeen = itemView.findViewById(R.id.is_picture_seen);
        }
    }
}
