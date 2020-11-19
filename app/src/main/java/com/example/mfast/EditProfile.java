package com.example.mfast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private TextView nameCounter,locationCounter,bioCounter,professionCounter;
    private EditText name,bio,location,date,month,year,profession;
    private ImageView saveButton,goBackButton,coverPhoto;
    private ImageButton datePicker,removeDob;
    private Button editCoverPhoto;
    private CircleImageView profilePicture;
    private TextView changeProfilePictureText,userDob;
    private Uri imageUri,coverUri;
    private FirebaseStorage storage;
    private StorageReference storageReference,coverPhotoRef;
    private FirebaseAuth mAuth;
    private String currentUserId,currentDate,myUri = "",from;
    private DatabaseReference databaseReference,nameReference;
    private Intent CamIntent, GalIntent, CropIntent ;
    private Uri mCropImageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        declareVariables();
        loadUserDetails();

        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePick = new DatePickerFragment();
                datePick.show(getSupportFragmentManager(),"date picker");
            }
        });

        changeProfilePictureText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                from = "profile";
                changeProfilePicture();
            }
        });
        editCoverPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                from = "cover";
                changeCoverPhoto(v);
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()){
                    final String userName = name.getText().toString();
                    String userLocation = location.getText().toString();
                    String userBio = bio.getText().toString().replaceAll("\n"," ");
                    String dateOfBirth = userDob.getText().toString();
                    String userProfession = profession.getText().toString();

                    Map<String ,Object> map = new HashMap<>();
                    map.put("Name",userName);
                    map.put("Location",userLocation);
                    map.put("Dob",dateOfBirth);
                    map.put("Status",userBio);
                    map.put("Profession",userProfession);

                    databaseReference.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(EditProfile.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                nameReference.child("Name").setValue(userName.toLowerCase());
                                startActivity(new Intent(EditProfile.this,userProfile.class));
                            }
                        }
                    });
                }
            }
        });

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditProfile.this,userProfile.class);
                startActivity(intent);
            }
        });

        removeDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDob.setText("");
            }
        });

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                int length = 20 - name.length();
                String convert = String.valueOf(length);
                nameCounter.setText(convert);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = 20 - name.length();
                String convert = String.valueOf(length);
                nameCounter.setText(convert);
            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = 20 - name.length();
                String convert = String.valueOf(length);
                nameCounter.setText(convert);
            }
        });
        location.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                int length = 15 - location.length();
                String convert = String.valueOf(length);
                locationCounter.setText(convert);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = 15 - location.length();
                String convert = String.valueOf(length);
                locationCounter.setText(convert);
            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = 15 - location.length();
                String convert = String.valueOf(length);
                locationCounter.setText(convert);
            }
        });
        bio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                int length = 120 - bio.length();
                String convert = String.valueOf(length);
                bioCounter.setText(convert);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = 120 - bio.length();
                String convert = String.valueOf(length);
                bioCounter.setText(convert);
            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = 120 - bio.length();
                String convert = String.valueOf(length);
                bioCounter.setText(convert);
            }
        });
        profession.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                int length = 20 - profession.length();
                String convert = String.valueOf(length);
                professionCounter.setText(convert);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = 20 - profession.length();
                String convert = String.valueOf(length);
                professionCounter.setText(convert);
            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = 20 - profession.length();
                String convert = String.valueOf(length);
                professionCounter.setText(convert);
            }
        });
    }

    private void loadUserDetails(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("ProfilePicture")){
                    String profile_picture = dataSnapshot.child("ProfilePicture").getValue().toString();
                    Picasso.get().load(profile_picture).placeholder(R.drawable.user_profile_holder).into(profilePicture);
                }
                if(dataSnapshot.hasChild("CoverPicture")){
                    String cover_picture = dataSnapshot.child("CoverPicture").getValue().toString();
                    Picasso.get().load(cover_picture).into(coverPhoto);
                }
                String user_name = dataSnapshot.child("Name").getValue().toString();
                name.setText(user_name);
                String user_profession = dataSnapshot.child("Profession").getValue().toString();
                profession.setText(user_profession);
                if(dataSnapshot.hasChild("Location")){
                    String user_location = dataSnapshot.child("Location").getValue().toString();
                    location.setText(user_location);
                }
                if(dataSnapshot.hasChild("Status")){
                    String user_bio = dataSnapshot.child("Status").getValue().toString();
                    bio.setText(user_bio);
                }
                String dateBirth = dataSnapshot.child("Dob").getValue().toString();
                userDob.setText(dateBirth);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void declareVariables(){
        nameCounter = findViewById(R.id.user_name_counter);
        locationCounter = findViewById(R.id.user_location_counter);
        bioCounter = findViewById(R.id.user_bio_counter);
        name = findViewById(R.id.edit_user_name);
        profession = findViewById(R.id.edit_user_profession);
        professionCounter = findViewById(R.id.user_profession_counter);
        goBackButton = findViewById(R.id.go_back_to_profile);
        location = findViewById(R.id.edit_user_location);
        bio = findViewById(R.id.edit_user_bio);
        profilePicture = findViewById(R.id.edit_profile_picture);
        saveButton = findViewById(R.id.save_details);
        datePicker = findViewById(R.id.user_dob_picker);
        userDob = findViewById(R.id.edit_user_dob);
        removeDob = findViewById(R.id.remove_user_dob);
        editCoverPhoto = findViewById(R.id.edit_user_cover_photo);
        coverPhoto = findViewById(R.id.edit_view_user_cover_photo);
        changeProfilePictureText = findViewById(R.id.edit_profile_picture_text);
        coverPhotoRef = FirebaseStorage.getInstance().getReference().child("Cover Photos");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);
        nameReference = FirebaseDatabase.getInstance().getReference("UserNames").child(currentUserId);

    }

    private void changeProfilePicture(){
        CropImage.startPickImageActivity(this);
    }

    @SuppressLint("WrongViewCast")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(from.equals("cover")){
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
                    ((ImageView) findViewById(R.id.edit_view_user_cover_photo)).setImageURI(result.getUri());
                    mCropImageUri = result.getUri();
                    uploadCoverPhoto();
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
                }
            }
        }
        else{
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
                    ((ImageView) findViewById(R.id.edit_profile_picture)).setImageURI(result.getUri());
                    mCropImageUri = result.getUri();
                    uploadPicture();
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    private void startCropImageActivity(Uri imageUri) {
        if(from.equals("cover")){
            CropImage.activity(imageUri)
                    .setAspectRatio(3,1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setMultiTouchEnabled(true)
                    .start(this);
        }
        else{
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setMultiTouchEnabled(true)
                    .start(this);
        }

    }
    private void uploadPicture(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Profile Picture...");
        progressDialog.show();

        final StorageReference riversRef = storageReference.child("images/"+currentUserId+".jpg");
        riversRef.putFile(mCropImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    riversRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()){
                                Snackbar.make(findViewById(android.R.id.content),"Image Uploaded",Snackbar.LENGTH_LONG).show();
                                String profileUrl = task.getResult().toString();
                                databaseReference.child("ProfilePicture").setValue(profileUrl);
                                progressDialog.dismiss();

                            }
                            else{
                                progressDialog.dismiss();
                                Toast.makeText(EditProfile.this, "Failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(EditProfile.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double percent = (100 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                progressDialog.setMessage("Percentage"+(int)percent + "%");
            }
        });
    }
    private void uploadCoverPhoto(){
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Uploading cover picture");
        dialog.setMessage("Please wait.....");
        dialog.show();

        final StorageReference riversRef = storageReference.child("coverImages/"+currentUserId+".jpg");
        riversRef.putFile(mCropImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    riversRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()){
                                Snackbar.make(findViewById(android.R.id.content),"Cover Photo Uploaded",Snackbar.LENGTH_LONG).show();
                                String profileUrl = task.getResult().toString();
                                databaseReference.child("CoverPicture").setValue(profileUrl);
                                dialog.dismiss();

                            }
                            else{
                                dialog.dismiss();
                                Toast.makeText(EditProfile.this, "Failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else{
                    dialog.dismiss();
                    Toast.makeText(EditProfile.this, "Failed", Toast.LENGTH_SHORT).show();
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
    private boolean validate(){
        boolean result = true;
        String userName = name.getText().toString();
        String userLocation = location.getText().toString();
        String userBio = bio.getText().toString();
        String userProfession = profession.getText().toString();


        if(userName.replaceAll(" ","").length() == 0){
            name.setError("User Name is must");
            return false;
        }
        if(userLocation.replaceAll(" ","").length() == 0){
            location.setText("");
        }
        if(userBio.replaceAll("\n","").replaceAll(" ","").length() == 0){
            bio.setText("");
        }
        if(userProfession.replaceAll(" ","").length() == 0){
            profession.setText("");
        }

        return result;
    }
    private void changeCoverPhoto(View view){
        CropImage.startPickImageActivity(this);
    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR,year);
        c.set(Calendar.MONTH,month);
        c.set(Calendar.DATE,dayOfMonth);

        currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime());
        userDob.setText(currentDate);

    }
}