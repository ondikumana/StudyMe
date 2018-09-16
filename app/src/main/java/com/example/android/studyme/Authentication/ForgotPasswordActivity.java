package com.example.android.studyme.Authentication;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.studyme.Firebase.FirebaseMethod;
import com.example.android.studyme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Backmod on 1/4/18.
 */

public class ForgotPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ForgotPasswordActivity";

    private TextView mEmail;
    private Button mResetPassword;
    private ProgressBar passwordResetProgressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_activity);
        mAuth = FirebaseAuth.getInstance();
        mEmail = (TextView) findViewById(R.id.email);
        mResetPassword = (Button) findViewById(R.id.btnResetPassword);
        passwordResetProgressBar = (ProgressBar) findViewById(R.id.passwordResetProgressBar);
        mResetPassword.setText("Reset Password");
    }

    public void resetPassword(View view){
        passwordResetProgressBar.setVisibility(View.VISIBLE);
        mResetPassword.setText("");

        if (!checkIfValidEmail()){
            Toast.makeText(getApplicationContext(),"Invalid Email Address",Toast.LENGTH_SHORT).show();
            passwordResetProgressBar.setVisibility(View.INVISIBLE);
            mResetPassword.setText("Reset Password");
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("users");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean emailExists = false;
                if (!dataSnapshot.exists()){
//                    progressBarListView.setVisibility(View.GONE);
//                    loadingFilesTextView.setVisibility(View.GONE);
                    Log.d(TAG, "onDataChange: Does not exist");
                }

                for (DataSnapshot snap:dataSnapshot.getChildren()){
                    DataSnapshot userInfo = snap.child("user_info");

                    String currentLoopEmail = userInfo.child("email").getValue(String.class);
                    Log.d(TAG, "onDataChange: currentLoopEmail "+currentLoopEmail);

                    if (mEmail.getText().toString().trim().equals(currentLoopEmail)){
                        Log.d(TAG, "resetPassword: email exists!");
                        emailExists = true;
                        passwordResetProgressBar.setVisibility(View.INVISIBLE);
                        FirebaseMethod firebaseMethod = new FirebaseMethod();
                        firebaseMethod.sendEmailResetPassword(mEmail.getText().toString().trim());
                        Toast.makeText(getApplicationContext(), "Password Reset Email Sent", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
                if (!emailExists){
                    Toast.makeText(getApplicationContext(),"This Account Does Not Exist",Toast.LENGTH_SHORT).show();
                    passwordResetProgressBar.setVisibility(View.INVISIBLE);
                    mResetPassword.setText("Reset Password");
                    Log.d(TAG, "resetPassword: account does not exist ");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    private boolean checkIfValidEmail(){
        if (!mEmail.getText().toString().contains("@"))
            return false;
        String [] email = mEmail.getText().toString().split("@");

        if (email.length!=2)
            return false;

        if (email[1].charAt(email[1].length()-4)!='.')
            return false;

        return true;
    }




}
