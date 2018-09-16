package com.example.android.studyme.Profile;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.studyme.Files.EntryFiles;
import com.example.android.studyme.Firebase.FirebaseMethod;
import com.example.android.studyme.QuizMode.QuizMultipleChoiceActivity;
import com.example.android.studyme.QuizMode.QuizTextActivity;
import com.example.android.studyme.StudyMode.StudyMultipleChoiceActivity;
import com.example.android.studyme.Authentication.LoginActivity;
import com.example.android.studyme.Home.HomeActivity;
import com.example.android.studyme.Home.SplashScreenActivity;
import com.example.android.studyme.R;
import com.example.android.studyme.StudyMode.StudyTextActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import static com.example.android.studyme.Home.HomeActivity.MY_PREFS_NAME;
import static com.example.android.studyme.Home.UserFilesFragment.CURRENTLY_SELECTED_FILE;
import static com.example.android.studyme.Home.UserFilesFragment.savedFilesDirectory;


/**
 * Created by Backmod on 12/28/17.
 */

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    private FirebaseAuth mAuth;
    Context mContext;

    private ImageView backArrowActionBar,editModeActionBar;
    private TextView activityNameActionBar;

    TextView updateName,updateEmail,updatePassword,resetAllProgress,deleteAllFiles,feedbackToOlivier,deleteAccount,signOut;
    View updatePasswordLine;
    EditText newName,newEmail,currentPassword,password1,password2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mAuth = FirebaseAuth.getInstance();
        mContext = ProfileActivity.this;

        backArrowActionBar = (ImageView) findViewById(R.id.backArrow);
        editModeActionBar = (ImageView) findViewById(R.id.editMode);
        activityNameActionBar = (TextView) findViewById(R.id.activityName);

        activityNameActionBar.setText("Profile");
        editModeActionBar.setVisibility(View.INVISIBLE);
        backArrowActionBar.setVisibility(View.INVISIBLE);

        newName = (EditText) findViewById(R.id.name);
        newEmail = (EditText) findViewById(R.id.email);

        newName.setText(mAuth.getCurrentUser().getDisplayName().trim());
        newEmail.setText(mAuth.getCurrentUser().getEmail().trim());

        updateName = (TextView) findViewById(R.id.updateName);
        updateEmail = (TextView) findViewById(R.id.updateEmail);
        updatePassword = (TextView) findViewById(R.id.updatePassword);
        updatePasswordLine = (View) findViewById(R.id.updatePasswordLineView);

        resetAllProgress = (TextView) findViewById(R.id.resetProgress);
        deleteAllFiles = (TextView) findViewById(R.id.deleteAllFiles);
        feedbackToOlivier = (TextView) findViewById(R.id.feedbackToOlivier);
        deleteAccount = (TextView) findViewById(R.id.deleteAccount);
        signOut = (TextView) findViewById(R.id.signOut);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.getMenu().findItem(R.id.navigation_profile).setChecked(true);

        if (getSignedInMethod().equals("google")){
            updateEmail.setVisibility(View.INVISIBLE);
            updatePassword.setVisibility(View.GONE);
            updatePasswordLine.setVisibility(View.GONE);
        }

        updateName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newName.getText().toString().trim().equals(mAuth.getCurrentUser().getDisplayName().trim()))
                    Toast.makeText(mContext,"No Change Was Made",Toast.LENGTH_SHORT).show();
                else if (newName.getText().toString().trim().equals(""))
                    Toast.makeText(mContext,"Field cannot be empty",Toast.LENGTH_SHORT).show();
                else {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(newName.getText().toString().trim()).build();
                    user.updateProfile(profileUpdate);
                    FirebaseMethod firebaseMethod = new FirebaseMethod();
                    firebaseMethod.updateNameDatabase(newName.getText().toString().trim());
                    Toast.makeText(mContext,"Name Updated",Toast.LENGTH_SHORT).show();
                }
            }
        });
        updateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newEmail.getText().toString().trim().equals(mAuth.getCurrentUser().getEmail().trim()))
                    Toast.makeText(mContext,"No Change Was Made",Toast.LENGTH_SHORT).show();
                else if (!checkIfValidEmail(newEmail.getText().toString().trim()))
                    Toast.makeText(mContext,"Invalid New Email",Toast.LENGTH_SHORT).show();
                else {
                    final Dialog dialog = new Dialog(mContext);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.update_email_dialog);
                    TextView yes = (TextView) dialog.findViewById(R.id.yes);
                    TextView no = (TextView) dialog.findViewById(R.id.no);

                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.show();

                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            EditText currentPassword = (EditText) dialog.findViewById(R.id.currentPassword);

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            AuthCredential credential = EmailAuthProvider
                                    .getCredential(mAuth.getCurrentUser().getEmail(), currentPassword.getText().toString());
                            user.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.d(TAG, "User re-authenticated.");
                                            if (task.isSuccessful()){
                                                FirebaseMethod firebaseMethod = new FirebaseMethod();
                                                firebaseMethod.updateEmail(newEmail.getText().toString().trim());
                                                firebaseMethod.updateEmailDatabase(newEmail.getText().toString().trim());
                                                firebaseMethod.sendEmailVerification();
                                                Toast.makeText(mContext,"Verification Email Sent",Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }
                                            else
                                                Toast.makeText(mContext,"Current Password is Incorrect",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });

                    no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                }
            }
        });
        updatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.update_password_dialog);

                TextView updatePasswordDialog = (TextView) dialog.findViewById(R.id.updatePasswordDialog);
                TextView cancelDialog = (TextView) dialog.findViewById(R.id.cancelDialog);

                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.show();

                updatePasswordDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        currentPassword = (EditText) dialog.findViewById(R.id.currentPassword);
                        password1 = (EditText) dialog.findViewById(R.id.newPassword1);
                        password2 = (EditText) dialog.findViewById(R.id.newPassword2);

                        if (Ready(currentPassword.getText().toString(),password1.getText().toString(),password2.getText().toString())){

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            AuthCredential credential = EmailAuthProvider
                                    .getCredential(mAuth.getCurrentUser().getEmail(), currentPassword.getText().toString());
                            user.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.d(TAG, "User re-authenticated.");
                                            if (task.isSuccessful()){
                                                Log.d(TAG, "onClick: Updating password");
                                                FirebaseMethod firebaseMethod = new FirebaseMethod();
                                                firebaseMethod.updatePassword(password1.getText().toString());
                                                Toast.makeText(mContext,"Password Updated",Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }
                                            else
                                                Toast.makeText(mContext,"Current Password is Incorrect",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });

                cancelDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

            }
        });
        resetAllProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.reset_progress_dialog);

                TextView yes = (TextView) dialog.findViewById(R.id.yes);
                TextView no = (TextView) dialog.findViewById(R.id.no);

                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.show();

                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        resetPreferences(true);
                        dialog.dismiss();
                        Toast.makeText(mContext,"All Saved Progress Has Been Reset",Toast.LENGTH_SHORT).show();
                    }
                });

                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });
        deleteAllFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.delete_all_files_dialog);

                TextView yes = (TextView) dialog.findViewById(R.id.yes);
                TextView no = (TextView) dialog.findViewById(R.id.no);

                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.show();

                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteAllFiles();
                        dialog.dismiss();
                        Toast.makeText(mContext,"All Files Have Been Deleted",Toast.LENGTH_SHORT).show();
                    }
                });

                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });

        feedbackToOlivier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.send_feedback_dialog);

                TextView yes = (TextView) dialog.findViewById(R.id.yes);
                TextView no = (TextView) dialog.findViewById(R.id.no);
                final EditText feedbackEditText = (EditText) dialog.findViewById(R.id.feedbackEditText);

                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.show();

                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String feedback = feedbackEditText.getText().toString().trim();
                        if (!feedback.equals("")){
                            FirebaseMethod firebaseMethod = new FirebaseMethod();
                            firebaseMethod.sendFeedbackToOlivier(feedback);
                            Toast.makeText(mContext,"Thank you for your feedback",Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                        else
                            Toast.makeText(mContext,"Field cannot be empty",Toast.LENGTH_SHORT).show();
                    }
                });
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });

        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.delete_account_dialog);

                TextView yes = (TextView) dialog.findViewById(R.id.yes);
                TextView no = (TextView) dialog.findViewById(R.id.no);
                currentPassword = (EditText) dialog.findViewById(R.id.currentPassword);

                if (!getSignedInMethod().equals("email"))
                    currentPassword.setVisibility(View.GONE);

                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.show();


                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (getSignedInMethod().equals("email")) {

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            AuthCredential credential = EmailAuthProvider
                                    .getCredential(mAuth.getCurrentUser().getEmail(), currentPassword.getText().toString());
                            user.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.d(TAG, "User re-authenticated.");
                                            if (task.isSuccessful()) {
                                                deleteAccount();
                                                dialog.dismiss();
                                                Toast.makeText(mContext, "Account Deleted", Toast.LENGTH_SHORT).show();
                                            } else
                                                Toast.makeText(mContext, "Current Password is Incorrect", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        else {
                            deleteAccount();
                            dialog.dismiss();
                            Toast.makeText(mContext, "Account Deleted", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.sign_out_dialog);

                TextView yes = (TextView) dialog.findViewById(R.id.yes);
                TextView no = (TextView) dialog.findViewById(R.id.no);

                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.show();

                yes.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        SignOut();
                        dialog.dismiss();
                    }
                });
                no.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

            }
        });
    }

    public void SignOut(){
        if (mAuth.getCurrentUser()!=null){
            mAuth.signOut();
            resetPreferences(false);

            EntryFiles entryFiles = new EntryFiles();
            entryFiles.deleteAllFiles();

            Intent intent = new Intent(mContext, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            resetSignedInMethodPreferences();

            Log.d(TAG, "SignOut: Signed out");
        }
    }

    public void resetPreferences(boolean resetDatabaseProgress){
        if (savedFilesDirectory !=null){
            String [] result = savedFilesDirectory.list();
            Log.d(TAG, "resetPreferences: Length of array: "+result.length);
            for (int i= 0;i<result.length;i++){
                Log.d(TAG, "resetPreferences: file in array: "+result[i]);
                int wordLength = result[i].length();
                String lowerCase = result[i].toLowerCase();
                String input = lowerCase.substring(0,wordLength-4);
                String output = input.substring(0, 1).toUpperCase() + input.substring(1);
                result[i]=output;
            }
            for (int i=0; i<result.length;i++){
                SharedPreferences editor = getSharedPreferences(result[i], MODE_PRIVATE);
                editor.edit().clear().commit();
                Log.d(TAG, "resetPreferences: Progress for " +result[i] + " Has been reset");
                if (resetDatabaseProgress){
                    FirebaseMethod firebaseMethod = new FirebaseMethod();
                    firebaseMethod.saveProgressToDatabase(result[i],"",0.0,
                            0,false,"",0,"",0);
                }
            }
            SharedPreferences editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            editor.edit().clear().commit();
            Log.d(TAG, "resetPreferences: All Preferences have been reset");

        }
        else {
            Log.d(TAG, "resetPreference: savedFilesDirectory is null");
        }
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent1 = new Intent(mContext,HomeActivity.class);
                    startActivity(intent1);
                    overridePendingTransition(0, 0);
                    return false;
                case R.id.navigation_learning:
//                    Intent intent2 = new Intent(mContext,StudyMultipleChoiceActivity.class);
//                    startActivity(intent2);
//                    overridePendingTransition(0, 0);
                    startModeActivity();
                    return false;
                case R.id.navigation_profile:
                    return true;
            }
            return false;
        }

    };

    private boolean checkIfValidEmail(String email){
        if (!email.contains("@"))
            return false;
        String [] emailArr = email.split("@");

        if (emailArr.length!=2)
            return false;

        if (emailArr[1].charAt(emailArr[1].length()-4)!='.')
            return false;

        return true;
    }

    private void deleteAllFiles(){
        EntryFiles entryFiles = new EntryFiles();
        entryFiles.deleteAllFiles();

        FirebaseMethod firebaseMethod = new FirebaseMethod();
        firebaseMethod.deleteAllFilesStorageAndDatabase(false);


    }

    private void deleteAccount (){
        //deleteAllFiles();

        EntryFiles entryFiles = new EntryFiles();
        entryFiles.deleteAllFiles();

        FirebaseMethod firebaseMethod = new FirebaseMethod();
        firebaseMethod.deleteAllFilesStorageAndDatabase(true);

        resetPreferences(false);

        resetSignedInMethodPreferences();

        Intent intent = new Intent(mContext, SplashScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private boolean Ready(String currentPasswrod, String password1, String password2){
        if (currentPasswrod.equals("") || password1.equals("") || password2.equals("")) {
            Toast.makeText(mContext,"Fields cannot be left empty",Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password1.equals(password2)){
            Toast.makeText(mContext, "Both passwords must match",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void startModeActivity (){

        SharedPreferences prefs1 = getSharedPreferences(CURRENTLY_SELECTED_FILE, MODE_PRIVATE);
        String fileID = prefs1.getString("fileID","");

        if (fileID.equals("")){
            Toast.makeText(mContext,"No File Was Selected",Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(fileID, MODE_PRIVATE);
        String learningMode = prefs.getString("learningMode", "Study");
        String answerInputMethod = prefs.getString("answerInputMethod", "MultipleChoice");

        if (learningMode.equals("Study") && answerInputMethod.equals("MultipleChoice")){
            Intent intent = new Intent(mContext,StudyMultipleChoiceActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
        if (learningMode.equals("Study") && answerInputMethod.equals("Text")){
            Intent intent = new Intent(mContext,StudyTextActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
        if (learningMode.equals("Quiz") && answerInputMethod.equals("MultipleChoice")){
            Intent intent = new Intent(mContext,QuizMultipleChoiceActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
        if (learningMode.equals("Quiz") && answerInputMethod.equals("Text")){
            Intent intent = new Intent(mContext,QuizTextActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    private String getSignedInMethod(){
        SharedPreferences prefs = getSharedPreferences("signedInMethod",MODE_PRIVATE);
        return prefs.getString("method",null);
    }

    private void resetSignedInMethodPreferences(){
        SharedPreferences editor = getSharedPreferences("signedInMethod", MODE_PRIVATE);
        editor.edit().clear().commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser==null){
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }
}
