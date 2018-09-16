package com.example.android.studyme.Authentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.studyme.Firebase.FirebaseMethod;
import com.example.android.studyme.Home.HomeActivity;
import com.example.android.studyme.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Created by Backmod on 12/27/17.
 */

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private TextView mName, mUsername, mEmail, mPassword1, mPassword2;
    private Button btnRegister;
    private ProgressBar registerProgressbar;
    private FirebaseAuth mAuth;
    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mName = (TextView) findViewById(R.id.name);
        mEmail = (TextView) findViewById(R.id.email);
        mPassword1 = (TextView) findViewById(R.id.passowrd1);
        mPassword2 = (TextView) findViewById(R.id.passowrd2);
        btnRegister = (Button) findViewById(R.id.registerBtn);
        btnRegister.setText("Register");
        registerProgressbar = (ProgressBar) findViewById(R.id.registerProgressBar);
        mContext = RegisterActivity.this;

        mAuth = FirebaseAuth.getInstance();
        onStart();


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            finish();
        }
    }
    private boolean Ready(){
        boolean ready = true;
        if (mEmail.getText().toString().equals("") || mPassword1.getText().toString().equals("") || mPassword2.getText().toString().equals("") || mName.getText().toString().equals("")) {
            Toast.makeText(mContext,"Fields cannot be left empty",Toast.LENGTH_SHORT).show();
            ready = false;

            registerProgressbar.setVisibility(View.INVISIBLE);
            btnRegister.setText("Register");

        }
        if (!mPassword1.getText().toString().equals(mPassword2.getText().toString())){
            Toast.makeText(mContext, "Both passwords must match",Toast.LENGTH_SHORT).show();
            ready = false;

            registerProgressbar.setVisibility(View.INVISIBLE);
            btnRegister.setText("Register");

            }
        return ready;
    }
    public void Register(View view){
        registerProgressbar.setVisibility(View.VISIBLE);
        btnRegister.setText("");
        if (Ready()){
            mAuth.createUserWithEmailAndPassword(mEmail.getText().toString(), mPassword1.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                addUserDisplayName();

                                saveSignedInMethodToPreferences(true,false);

                                FirebaseMethod firebaseMethod = new FirebaseMethod();
                                firebaseMethod.updateEmailDatabase(mEmail.getText().toString().trim());
                                firebaseMethod.updateNameDatabase(mName.getText().toString().trim());

                                Intent intent = new Intent(mContext,HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(mContext, "Authentication failed. Email already registered",
                                        Toast.LENGTH_SHORT).show();
                                registerProgressbar.setVisibility(View.INVISIBLE);
                                btnRegister.setText("Register");
                            }

                            // ...
                        }
                    });
        }
    }

    public void addUserDisplayName(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(mName.getText().toString().trim()).build();
        user.updateProfile(profileUpdate);
        Log.d(TAG, "addUserDisplayName: Name of user put: "+mName.getText().toString());
        Log.d(TAG, "addUserDisplayName: Name of user:" +mAuth.getCurrentUser().getDisplayName());
    }

    private void saveSignedInMethodToPreferences(boolean email, boolean google){

        SharedPreferences.Editor prefs = getSharedPreferences("signedInMethod",MODE_PRIVATE).edit();

        if (email)
            prefs.putString("method","email");
        if (google)
            prefs.putString("method", "google");

        prefs.apply();
    }

}
