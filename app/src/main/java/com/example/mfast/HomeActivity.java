package com.example.mfast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.errorprone.annotations.Var;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {
    private CircleImageView profilePicture;
    private DatabaseReference databaseReference;
    private FirebaseDatabase firebase;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private Toolbar toolbar;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        checkUserLoggedIn();
        toolbar = findViewById(R.id.custom_toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        firebase = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUserID);
        manageConnections();
        declareVariables();
        loadProfilePicture();

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,userProfile.class);
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.home_screen_bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new MessagesFragment()).commit();
        }

    }
    private  void manageConnections(){

        final DatabaseReference connectionReference,infoConnect;
        connectionReference = FirebaseDatabase.getInstance().getReference().child("connections");
        infoConnect = FirebaseDatabase.getInstance().getReference(".info/connected");

        infoConnect.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if(connected){
                    DatabaseReference con = connectionReference.child(currentUserID);
                    con.setValue(true);
                    con.onDisconnect().setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void declareVariables(){
        profilePicture = findViewById(R.id.home_page_profile_picture);
    }
    private void loadProfilePicture(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("ProfilePicture")){
                    String profilePic = dataSnapshot.child("ProfilePicture").getValue().toString();
                    Picasso.get().load(profilePic).placeholder(R.drawable.user_profile_holder).into(profilePicture);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void checkUserLoggedIn(){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser= firebaseAuth.getCurrentUser();
        if(firebaseUser == null){
            startActivity(new Intent(HomeActivity.this,MainActivity.class));
        }

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()){
                        case R.id.search_user_bot_nav :
                            selectedFragment = new SearchFragment();
                            break;
                        case R.id.your_friend_list_bottom_nav :
                            selectedFragment = new FriendsFragment();
                            break;
                        case R.id.friend_request_bot_nav :
                            selectedFragment = new RequestFragment();
                            break;
                        case R.id.chat_bot_nav :
                            selectedFragment = new MessagesFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                    return true;
                }
            };
}