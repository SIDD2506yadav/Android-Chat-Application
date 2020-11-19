package com.example.mfast;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ResetPasswordDialog extends AppCompatDialogFragment {
    private Button sendmailButton;
    private LinearLayout linearLayout;
    private EditText resetMailId;
    private FirebaseAuth mAuth;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.reset_password_layout,null);

        builder.setView(view);

        sendmailButton = view.findViewById(R.id.send_reset_mail);
        linearLayout = view.findViewById(R.id.reset_link_sent);
        resetMailId = view.findViewById(R.id.mail_id_to_reset);

        mAuth = FirebaseAuth.getInstance();
        sendmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayout.setVisibility(View.GONE);
                if(validate()){
                    String resetMail = resetMailId.getText().toString().replaceAll(" ","");
                    mAuth.sendPasswordResetEmail(resetMail)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        linearLayout.setVisibility(View.VISIBLE);
                                    }
                                    else{
                                        String message = task.getException().toString();
                                        Toast.makeText(getContext(), "Failed\n"+message, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
        return builder.create();
    }
    public boolean validate(){
        String resetMail = resetMailId.getText().toString().replaceAll(" ","");
        if(resetMail.length() == 0){
            resetMailId.setError("Please enter your email id");
            return false;
        }
        return true;
    }
}
