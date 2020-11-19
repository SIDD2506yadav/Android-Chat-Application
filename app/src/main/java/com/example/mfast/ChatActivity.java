package com.example.mfast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private CircleImageView profilePicture;
    private ImageView sendButton,sendImageButton;
    private EditText message;
    private TextView name,ifUserOnline;
    private String otherUserId,currentUserId;
    private DatabaseReference userReference,rootRef;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private int initial,last;
    private StorageReference storageReference,coverPhotoRef;
    private DatabaseReference checkMessageRef;
    private Uri mCropImageUri;
    private FirebaseStorage storage;
    private ValueEventListener seenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        otherUserId = intent.getExtras().getString("OtherUserId").toString();

        declareVariables();
        loadProfileDetails();
        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(bottom<oldBottom){
                    recyclerView.scrollBy(0,oldBottom-bottom);
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateMessage()){
                    String user_message = message.getText().toString();
                    SendMessage();
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ssz");
                    final String format = simpleDateFormat.format(new Date());
                    Map<String,Object> map = new HashMap<>();
                    map.put("from",currentUserId);
                    map.put("lastMessage",user_message);
                    map.put("timeStamp",format);

                    rootRef.child("LastMessage").child(currentUserId).child(otherUserId).updateChildren(map);
                    rootRef.child("LastMessage").child(otherUserId).child(currentUserId).updateChildren(map);
                }
            }
        });
        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.startPickImageActivity(ChatActivity.this);
            }
        });
        if(savedInstanceState == null){
            displayMessages();
        }
        seenMessage();
    }
    @Override
    protected void onStart() {
        super.onStart();
        //displayMessages();
    }
    private void seenMessage(){
        seenListener = checkMessageRef.child("Messages").child(otherUserId).child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    Messages messages = snapshot1.getValue(Messages.class);
                    if(messages.getFrom().equals(otherUserId) && messages.getTo().equals(currentUserId)){
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen","true");
                        snapshot1.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == EditProfile.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            } else {
                // no permissions required or already grunted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }

        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //((ImageView) findViewById(R.id.edit_view_user_cover_photo)).setImageURI(result.getUri());
                mCropImageUri = result.getUri();
                sendPicture();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }
    private void sendPicture(){
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Sending");
        dialog.setMessage("Please wait.....");
        dialog.show();
        final StorageReference riversRef = storageReference.child("messageImages/"+currentUserId+".jpg");
        riversRef.putFile(mCropImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    riversRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()){
                                String user_message = task.getResult().toString();

                                String messageSenderRef = "Messages/" + currentUserId + "/" + otherUserId;
                                String messageReceiverRef = "Messages/" + otherUserId + "/" + currentUserId;

                                DatabaseReference userMessageRef = rootRef.child("Messages").child(currentUserId)
                                        .child(otherUserId).push();

                                String messagePushId = userMessageRef.getKey();

                                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ssz");
                                final String format = simpleDateFormat.format(new Date());

                                Map<String ,Object> messageBody = new HashMap();
                                messageBody.put("message",user_message);
                                messageBody.put("type","picture");
                                messageBody.put("time",format);
                                messageBody.put("from",currentUserId);
                                messageBody.put("to",otherUserId);
                                messageBody.put("isSeen","false");

                                Map<String,Object> messageDetails = new HashMap();
                                messageDetails.put(messageSenderRef+"/"+messagePushId,messageBody);
                                messageDetails.put(messageReceiverRef+ "/" +messagePushId,messageBody);

                                rootRef.updateChildren(messageDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            message.setText("");
                                            Toast.makeText(ChatActivity.this, "Picture Sent", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }
                                        else{
                                            Toast.makeText(ChatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }
                                    }
                                });
                            }
                            else{
                                Toast.makeText(ChatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(ChatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double percent = (100 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                dialog.setMessage("Percentage"+(int)percent + "%");
            }
        });
    }
    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);

    }
    private void declareVariables(){
        profilePicture = findViewById(R.id.user_chat_profile_picture);
        name = findViewById(R.id.user_chat_name);
        message = findViewById(R.id.enter_message);
        sendButton = findViewById(R.id.send_message);
        ifUserOnline = findViewById(R.id.if_user_online);
        sendImageButton = findViewById(R.id.send_picture);
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        checkMessageRef = FirebaseDatabase.getInstance().getReference();

        messageAdapter = new MessageAdapter(messagesList);
        recyclerView = findViewById(R.id.chat_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

    }
    private void loadProfileDetails(){
        userReference.child(otherUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("ProfilePicture").exists()){
                    String profile_picture = snapshot.child("ProfilePicture").getValue().toString();
                    Picasso.get().load(profile_picture).placeholder(R.drawable.user_profile_holder).into(profilePicture);
                }
                String user_name = snapshot.child("Name").getValue().toString();
                name.setText(user_name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        final DatabaseReference connectionReference;
        connectionReference = FirebaseDatabase.getInstance().getReference().child("connections");
        connectionReference.child(otherUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    boolean ifOnline = snapshot.getValue(Boolean.class);
                    if(ifOnline){
                        ifUserOnline.setVisibility(View.VISIBLE);
                    }
                    else{
                        ifUserOnline.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void SendMessage(){
        String user_message = message.getText().toString();

        String messageSenderRef = "Messages/" + currentUserId + "/" + otherUserId;
        String messageReceiverRef = "Messages/" + otherUserId + "/" + currentUserId;

        DatabaseReference userMessageRef = rootRef.child("Messages").child(currentUserId)
                .child(otherUserId).push();

        String messagePushId = userMessageRef.getKey();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ssz");
        final String format = simpleDateFormat.format(new Date());

        Map<String ,Object> messageBody = new HashMap();
        messageBody.put("message",user_message);
        messageBody.put("type","text");
        messageBody.put("time",format);
        messageBody.put("from",currentUserId);
        messageBody.put("to",otherUserId);
        messageBody.put("isSeen","false");

        Map<String,Object> messageDetails = new HashMap();
        messageDetails.put(messageSenderRef+"/"+messagePushId,messageBody);
        messageDetails.put(messageReceiverRef+ "/" +messagePushId,messageBody);

        rootRef.updateChildren(messageDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    message.setText("");
                    Toast.makeText(ChatActivity.this, "messageSent", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private boolean validateMessage(){
        String user_message = message.getText().toString().replaceAll(" ","").replaceAll("\n","");
        String new_message = message.getText().toString();
        if(user_message.length() == 0){
            return false;
        }

        for(int i=0;i<new_message.length();i++){
            if(!String.valueOf(new_message.charAt(i)).equals(String.valueOf("\n")) && !String.valueOf(new_message.charAt(i)).equals(String.valueOf(" "))){
                initial = i;
                break;
            }
        }
        for(int i=new_message.length()-1;i>=0;i--){
            if(!String.valueOf(new_message.charAt(i)).equals(String.valueOf("\n")) && !String.valueOf(new_message.charAt(i)).equals(String.valueOf(" ")) ){
                last = i;
                break;
            }
        }
        new_message = new_message.substring(initial,last+1);
        message.setText(new_message);
        return true;
    }
    private void displayMessages(){

        rootRef.child("Messages").child(currentUserId).child(otherUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messagesList.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Messages messages = dataSnapshot.getValue(Messages.class);
                            messagesList.add(messages);
                            messageAdapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        /*rootRef.child("Messages").child(currentUserId).child(otherUserId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if(snapshot.exists()){
                            Messages messages = snapshot.getValue(Messages.class);
                            messagesList.add(messages);
                            messageAdapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        messageAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        checkMessageRef.child("Messages").child(otherUserId).child(currentUserId).removeEventListener(seenListener);
    }
}