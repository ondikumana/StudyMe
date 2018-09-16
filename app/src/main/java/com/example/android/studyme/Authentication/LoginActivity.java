package com.example.android.studyme.Authentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.studyme.Home.HomeActivity;
import com.example.android.studyme.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Backmod on 12/27/17.
 */

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private Button normalLogin;
    private Button googleLogin;
    private EditText mEmail, mPassword;
    private TextView mRegister,mForgotPassword;
    private ProgressBar signInProgressBar,signInGoogleProgressBar;
    private FirebaseAuth mAuth;
    private Context mContext;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    public static final Parcelable.Creator<GoogleSignInAccount> CREATOR = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        normalLogin = (Button) findViewById(R.id.normalLogin);
        normalLogin.setText("Log In");
        googleLogin = (Button) findViewById(R.id.googleLogin);
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.passowrd);
        mRegister = (TextView) findViewById(R.id.tvRegistration);
        mForgotPassword = (TextView) findViewById(R.id.forgotPassword);
        signInProgressBar = (ProgressBar) findViewById(R.id.signInProgressBar);
        signInGoogleProgressBar = (ProgressBar) findViewById(R.id.signInGoogleProgressBar);
        mContext = LoginActivity.this;

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,RegisterActivity.class);
                startActivity(intent);
            }
        });
        googleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignInGoogle();
            }
        });

        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    public void SignInNormal(View view){
        signInProgressBar.setVisibility(View.VISIBLE);
        normalLogin.setText("");
        if (!mEmail.getText().toString().equals("") && !mPassword.getText().toString().equals("")){
            mAuth.signInWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");

                                saveSignedInMethodToPreferences(true,false);

                                Intent intent = new Intent(mContext, HomeActivity.class);
                                startActivity(intent);

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(mContext, "Email or Password is Incorrect",
                                        Toast.LENGTH_SHORT).show();
                                signInProgressBar.setVisibility(View.INVISIBLE);
                                normalLogin.setText("Log In");
                                //updateUI(null);
                            }

                            // ...
                        }
                    });
        }
        else {
            Toast.makeText(mContext,"Fields cannot be left blank",Toast.LENGTH_SHORT).show();
            signInProgressBar.setVisibility(View.INVISIBLE);
            normalLogin.setText("Log In");
        }
    }

    private void SignInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with FirebaseMethod
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        signInGoogleProgressBar.setVisibility(View.VISIBLE);
        googleLogin.setText("");

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            writeToDatabase();
                            saveSignedInMethodToPreferences(false,true);

                            Intent intent = new Intent(mContext, HomeActivity.class);
                            startActivity(intent);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(mContext, "Authentication Failed.", Toast.LENGTH_SHORT).show();

                            signInGoogleProgressBar.setVisibility(View.INVISIBLE);
                            googleLogin.setText("Log In With Google");

                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null){
            Intent intent = new Intent(mContext, HomeActivity.class);
            startActivity(intent);
        }
    }

    public void writeToDatabase(){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users").child(mAuth.getCurrentUser().getUid()).child("user_info");

        myRef.child("email").setValue(mAuth.getCurrentUser().getEmail());
        myRef.child("name").setValue(mAuth.getCurrentUser().getDisplayName());

        //myRef.setValue("Hello, World!");
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
