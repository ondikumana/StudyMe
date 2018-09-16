package com.example.android.studyme.Home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.studyme.Authentication.LoginActivity;
import com.example.android.studyme.Profile.ProfileActivity;
import com.example.android.studyme.QuizMode.QuizMultipleChoiceActivity;
import com.example.android.studyme.QuizMode.QuizTextActivity;
import com.example.android.studyme.R;
import com.example.android.studyme.StudyMode.StudyMultipleChoiceActivity;
import com.example.android.studyme.StudyMode.StudyTextActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.example.android.studyme.Home.UserFilesFragment.CURRENTLY_SELECTED_FILE;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";

    private FirebaseAuth mAuth;
    private Context mContext;

    private TextView personalWelcomeMessage;

    public String nameOfCurrentUser;

    public static final String MY_PREFS_NAME = "MyPreferenceFile";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        /* This is the bottom menu. It is in all the 3 main activities, and it starts one of the
        three each time it is tapped.
         */

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_learning:
                    startModeActivity();
                    return false;
                case R.id.navigation_profile:
                    Intent intent2 = new Intent(mContext, ProfileActivity.class);
                    startActivity(intent2);
                    overridePendingTransition(0, 0);
                    return false;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.getMenu().findItem(R.id.navigation_home).setChecked(true);
        mContext = HomeActivity.this;
        mAuth = FirebaseAuth.getInstance();

        setupHomeTabs();

        personalWelcomeMessage = (TextView) findViewById(R.id.personalWelcomeMessage);

        getUserName();

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void setupHomeTabs(){
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new UserFilesFragment());
        adapter.addFragment(new SharedFilesFragment());
        adapter.addFragment(new PublicFilesFragment());

        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabsTabLayout);
        tabLayout.setupWithViewPager(viewPager);
        Log.d(TAG, "setupHomeTabs: tabs were setup");

    }

    private void getUserName() {
        /*
        This method gets the name of the authenticated user from firebase's authentication thingy.
         */
        if (mAuth.getCurrentUser() != null) {
            nameOfCurrentUser = mAuth.getCurrentUser().getDisplayName();
            Log.d(TAG, "getUserName: getDisplayName is not null");
            if (nameOfCurrentUser != null && nameOfCurrentUser.contains(" ")) {
                String[] nameArray = nameOfCurrentUser.split(" ");
                personalWelcomeMessage.setText("Hello, " + nameArray[0]);
                Log.d(TAG, "Name is: " + personalWelcomeMessage.getText().toString());
            }
            else if (nameOfCurrentUser != null && !nameOfCurrentUser.contains(" ")) {
                personalWelcomeMessage.setText("Hello, " + nameOfCurrentUser);
                Log.d(TAG, "Name is: " + personalWelcomeMessage.getText().toString());
            }
            else if (nameOfCurrentUser==null){
                personalWelcomeMessage.setText("Hello");
            }
        }
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

}


