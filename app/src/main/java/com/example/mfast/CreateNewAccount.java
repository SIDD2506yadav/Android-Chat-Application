package com.example.mfast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class CreateNewAccount extends AppCompatActivity {
    private ImageView goBackArrow;
    private TextView goToLogin,nameLimit;
    private Button createAccount;
    private EditText email,name,password,reEnterPassword;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private String currentUser;
    private DatabaseReference databaseReference,userReference;
    private FirebaseDatabase firebaseDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_account);

        goBackArrow = (ImageView) findViewById(R.id.go_back_button_reg);
        goToLogin = (TextView) findViewById(R.id.go_to_login);
        email = (EditText)findViewById(R.id.email_for_reg);
        name = (EditText)findViewById(R.id.name_for_reg);
        password = (EditText)findViewById(R.id.password_for_reg);
        reEnterPassword = (EditText)findViewById(R.id.re_enter_password_for_reg);
        createAccount = (Button)findViewById(R.id.create_new_account);
        nameLimit = findViewById(R.id.create_account_name_limit);
        progressBar =(ProgressBar) findViewById(R.id.reg_progress_bar);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        userReference = FirebaseDatabase.getInstance().getReference("UserNames");

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateNewAccount.this,loginActivity.class);
                startActivity(intent);
            }
        });
        goBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateNewAccount.this,MainActivity.class);
                startActivity(intent);
            }
        });
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(valid()){
                    progressBar.setVisibility(View.VISIBLE);
                    final String sName,sEmail,sPassword,rePassword;
                    sName = name.getText().toString();
                    sEmail = email.getText().toString().replaceAll(" ","");
                    sPassword = password.getText().toString().trim();
                    rePassword = reEnterPassword.getText().toString().trim();
                    mAuth.createUserWithEmailAndPassword(sEmail,sPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                currentUser = mAuth.getCurrentUser().getUid();
                                Map<String,Object> map = new HashMap<>();
                                map.put("Name",sName);
                                map.put("Email",sEmail);
                                map.put("Location","");
                                map.put("Dob","");
                                map.put("Status","");
                                map.put("Profession","");
                                map.put("Friends","0");
                                databaseReference.child(currentUser).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(CreateNewAccount.this, "Details Uploaded", Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            String message = task.getException().toString();
                                            Toast.makeText(CreateNewAccount.this, message, Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(CreateNewAccount.this,HomeActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                });
                                userReference.child(currentUser).child("Name").setValue(sName.toLowerCase());
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(CreateNewAccount.this, "User Registered", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(CreateNewAccount.this,HomeActivity.class));
                            }
                            else{
                                progressBar.setVisibility(View.INVISIBLE);
                                String message = task.getException().toString();
                                Toast.makeText(CreateNewAccount.this, "User Registration failed" + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                int length = 20 - name.length();
                String convert = String.valueOf(length);
                nameLimit.setText(convert);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = 20 - name.length();
                String convert = String.valueOf(length);
                nameLimit.setText(convert);
            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = 20 - name.length();
                String convert = String.valueOf(length);
                nameLimit.setText(convert);
            }
        });
    }
    public boolean valid(){
        boolean result = true;
        String sName,sEmail,sPassword,rePassword;
        sName = name.getText().toString();
        sEmail = email.getText().toString();
        sPassword = password.getText().toString();
        rePassword = reEnterPassword.getText().toString();

        if(sName.trim().length() == 0){
            name.setError("Please enter your name");
            result = false;
        }
        if(sEmail.trim().length() == 0){
            email.setError("Please enter email id");
            result = false;
        }
        if(sPassword.trim().length() == 0){
            password.setError("Please create a password");
            result = false;
        }
        if(sPassword.trim().length() < 6){
            password.setError("Password must be atleast 6 characters long");
            result = false;
        }
        if(!sPassword.equals(rePassword)){
            reEnterPassword.setError("Re-entered password must be same");
            result = false;
        }
        return result;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CreateNewAccount.this,MainActivity.class);
        startActivity(intent);
    }
}