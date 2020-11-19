package com.example.mfast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class loginActivity extends AppCompatActivity {
    private ImageView goBackArrow;
    private TextView goToCreate,forgotPassword;
    private FirebaseAuth mAuth;
    private Button login;
    private EditText email,password;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        goBackArrow = (ImageView)findViewById(R.id.go_back_button_log);
        goToCreate = (TextView) findViewById(R.id.go_to_create_account);
        email = (EditText)findViewById(R.id.email_for_log);
        forgotPassword = findViewById(R.id.forgot_password);
        login = (Button) findViewById(R.id.log_to_account);
        password = (EditText)findViewById(R.id.password_for_log);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar_login);
        mAuth = FirebaseAuth.getInstance();

        goToCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(loginActivity.this,CreateNewAccount.class);
                startActivity(intent);
            }
        });
        goBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(loginActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openResetDialogBox();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(valid()){
                    progressBar.setVisibility(View.VISIBLE);
                    String sEmail,sPassword;
                    sEmail = email.getText().toString().replaceAll(" ","");
                    sPassword = password.getText().toString().trim();
                   mAuth.signInWithEmailAndPassword(sEmail,sPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                       @Override
                       public void onComplete(@NonNull Task<AuthResult> task) {
                           if(task.isSuccessful()){
                               Toast.makeText(loginActivity.this, "Logged In !", Toast.LENGTH_SHORT).show();
                               Intent intent = new Intent(loginActivity.this,HomeActivity.class);
                               startActivity(intent);
                               progressBar.setVisibility(View.INVISIBLE);
                           }
                           else{
                               String message = task.getException().toString();
                               progressBar.setVisibility(View.INVISIBLE);
                               Toast.makeText(loginActivity.this, "Login Failed " + message, Toast.LENGTH_SHORT).show();
                           }
                       }
                   });
                }
            }
        });
    }

    private boolean valid(){
        boolean result = true;
        String sName,sEmail,sPassword;
        sEmail = email.getText().toString().trim();
        sPassword = password.getText().toString().trim();
        if(sEmail.length() == 0){
            email.setError("Please enter your email id");
            result = false;
        }
        if(sPassword.length() == 0){
            password.setError("Please enter password");
            result = false;
        }
        return result;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(loginActivity.this,MainActivity.class);
        startActivity(intent);
    }
    private void openResetDialogBox(){
        ResetPasswordDialog resetPasswordDialog = new ResetPasswordDialog();
        resetPasswordDialog.show(getSupportFragmentManager(),"Reset Password Dialog");

    }
}