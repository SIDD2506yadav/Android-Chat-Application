package com.example.mfast;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChangePasswordDialog extends AppCompatDialogFragment{
    private EditText oldPassword,newPassword,reEnteredNewPassword;
    private Button changePassword;
    private ProgressBar progressBar;
    private LinearLayout linearLayout;
    private TextView forgotPassword;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    String currentUserId;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.change_password_layout,null);

        builder.setView(view);
        oldPassword = view.findViewById(R.id.old_password);
        newPassword = view.findViewById(R.id.new_password);
        reEnteredNewPassword = view.findViewById(R.id.re_enter_new_password);
        changePassword = view.findViewById(R.id.change_current_password);
        linearLayout = view.findViewById(R.id.password_changed_message);
        forgotPassword = view.findViewById(R.id.if_forgot_password);
        progressBar = view.findViewById(R.id.change_password_progress_bar);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayout.setVisibility(View.GONE);
                if(validate()){
                    progressBar.setVisibility(View.VISIBLE);
                    final String oPassword,nPassword;
                    oPassword = oldPassword.getText().toString().replaceAll(" ","");
                    nPassword = newPassword.getText().toString().replaceAll(" ","");
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    final String email = user.getEmail().toString();
                    AuthCredential authCredential = EmailAuthProvider
                            .getCredential(email,oPassword);
                    user.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                user.updatePassword(nPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            linearLayout.setVisibility(View.VISIBLE);
                                            progressBar.setVisibility(View.INVISIBLE);
                                            oldPassword.setText("");
                                            newPassword.setText("");
                                            reEnteredNewPassword.setText("");
                                        }
                                        else{
                                            progressBar.setVisibility(View.INVISIBLE);
                                            String message = task.getException().toString();
                                            Toast toast = Toast.makeText(getContext(),"Failed\n"+message
                                                    , Toast.LENGTH_LONG);
                                            toast.setGravity(Gravity.CENTER,0,0);
                                            toast.show();
                                        }
                                    }
                                });
                            }
                            else{
                                progressBar.setVisibility(View.INVISIBLE);
                                String message = task.getException().toString();
                                Toast toast = Toast.makeText(getContext(),"Failed\n"+message
                                        , Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER,0,0);
                                toast.show();
                            }
                        }
                    });
                }
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResetLink();
            }
        });

        return builder.create();
    }
    private boolean validate(){
        boolean result = true;
        String oPassword,nPassword,rNewPassword;
        oPassword = oldPassword.getText().toString().replaceAll(" ","");
        nPassword = newPassword.getText().toString().replaceAll(" ","");
        rNewPassword = reEnteredNewPassword.getText().toString().replaceAll(" ","");

        if(oPassword.length() == 0){
            oldPassword.setError("Please enter old password");
            result = false;
        }
        if(nPassword.length() == 0){
            newPassword.setError("Please new password");
            result = false;
        }
        if(nPassword.length() < 6){
            newPassword.setError("Password must be atleast 6 characters long");
            result = false;
        }
        if(!nPassword.equals(rNewPassword)){
            reEnteredNewPassword.setError("Re-entered password must be\nsame as new password");
            result = false;
        }
        return result;
    }
    private void sendResetLink(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String email = dataSnapshot.child("Email").getValue().toString();
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast toast = Toast.makeText(getContext(),"We sent an email to\n"+email
                                            +"\nwith a link to reset your password", Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER,0,0);
                                    toast.show();
                                }
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
