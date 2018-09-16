package com.example.android.studyme.StudyMode;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.studyme.Authentication.LoginActivity;
import com.example.android.studyme.Files.EntryFiles;
import com.example.android.studyme.Firebase.FirebaseMethod;
import com.example.android.studyme.Home.HomeActivity;
import com.example.android.studyme.Profile.ProfileActivity;
import com.example.android.studyme.QuizMode.QuizMultipleChoiceActivity;
import com.example.android.studyme.QuizMode.QuizTextActivity;
import com.example.android.studyme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.example.android.studyme.Home.UserFilesFragment.CURRENTLY_SELECTED_FILE;

public class StudyMultipleChoiceActivity extends AppCompatActivity {
    private static final String TAG = "StudyMultipleChoiceActivity";
    Random rand = new Random();
    int n1 = -1;
    ArrayList<Integer> answered = new ArrayList<>();
    ArrayList<Integer> skipped = new ArrayList<>();
    TextView questionTextView,feedbackTextView, nameOfFileTextView;
    Button buttonA,buttonB,buttonC,buttonD,skipOrRestartBtn,restartNowBtn;
    ProgressBar progressBar;
    double progressBarStatus = 0;
    Context mContext;
    private FirebaseAuth mAuth;

    private ImageView backArrowActionBar,editModeActionBar;
    private TextView activityNameActionBar;

    private CheckBox studyCheckBox,quizCheckBox,multipleChoiceCheckBox,textCheckBox;
    private Switch randomQuestionsSwitch,reverseOrderSwitch;
    private TextView cancelTextView,proceedTextView;

    private String fileID = "";
    private String nameOfFile = "";
    private String pathOfFileSelected = "";

    private boolean randomQuestions;
    private boolean reversedOrder;
    private int currentQuestion = 0;
    private boolean goneThroughAllEntries = false;

    private Map<String,String> currentSettings = new HashMap();

    private TextView learningMode,answerInputMethod,random;


    View.OnClickListener redoButtonRestart = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (answered.size()==readText().length)
                restart(skipOrRestartBtn); //this restarts
            else
                skip(skipOrRestartBtn); //this skips
        }
    };
    View.OnClickListener correctAnswer = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            answeredQuestion(n1);

            view.setBackgroundResource(R.drawable.green_button_layout);

            new CountDownTimer(500, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    resetButtonColors();
                    reGenerate();
                }
            }.start();

        }
    };
    View.OnClickListener wrongAnswer = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.setBackgroundResource(R.drawable.red_button_layout);
            new CountDownTimer(500, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    resetButtonColors();
                }
            }.start();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.study_multiple_choice_activity);
        mContext = StudyMultipleChoiceActivity.this;

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.getMenu().findItem(R.id.navigation_learning).setChecked(true);
        mAuth = FirebaseAuth.getInstance();

        SharedPreferences prefsCurrentFile = getSharedPreferences(CURRENTLY_SELECTED_FILE, MODE_PRIVATE);
        fileID = prefsCurrentFile.getString("fileID", "");
        pathOfFileSelected = prefsCurrentFile.getString("pathOfFileSelected","");

        SharedPreferences prefsSelectedFile = getSharedPreferences(fileID, MODE_PRIVATE);
        randomQuestions = prefsSelectedFile.getBoolean("randomQuestions", false); // by default, I set it to false.
        reversedOrder = prefsSelectedFile.getBoolean("reversedOrder",false);
        nameOfFile = prefsSelectedFile.getString("nameOfFile", "Unnamed File");

        Log.d(TAG, "onCreate: Selected file info: "+ fileID +" "+pathOfFileSelected);

        learningMode = (TextView) findViewById(R.id.learningMode);
        answerInputMethod = (TextView) findViewById(R.id.answerInputMethd);
        random = (TextView) findViewById(R.id.randomQuestions);

        learningMode.setText("Study ");
        answerInputMethod.setText("Multiple Choice ");

        if (randomQuestions)
            random.setVisibility(View.VISIBLE);


        backArrowActionBar = (ImageView) findViewById(R.id.backArrow);
        editModeActionBar = (ImageView) findViewById(R.id.editMode);
        activityNameActionBar = (TextView) findViewById(R.id.activityName);

        activityNameActionBar.setText("Learning");
        editModeActionBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileOptionsDialog(fileID);
            }
        });
        backArrowActionBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(0, 0);
            }
        });

        retrieveProgressFromPreferences();
        reGenerate();

//        checkIfFileSelected();

    }

    public void skip (View view){
        /*
        When the Skip button is pressed, it goes to the reGenerate method.
        */
        Log.d(TAG, "skip: started");

        if (!randomQuestions){
            for (int i = 0;i<skipped.size();i++){
                if (skipped.get(i)==currentQuestion) {
                    skipped.remove(i); //it's removed.
                    Log.d(TAG, "skip: already skipped, removing from skipped "+ i);
                }
            }

            skipped.add(currentQuestion); //it's re-added but at the back of the arraylist

            Log.d(TAG, "skip: current question re-added to skipped: "+ currentQuestion+" "+skipped);

            Log.d(TAG, "skip: added currentQuestion to array"+skipped);

            if (!goneThroughAllEntries) {
                currentQuestion++;
                Log.d(TAG, "skip: Not gone through all entries. Current question is incremented by 1 "+ currentQuestion);
            }
            else {
                Log.d(TAG, "skip: Gone through all entries. skipped "+skipped);
                currentQuestion = skipped.get(0);
                Log.d(TAG, "skip: Gone through all entries. getting current question from skipped "+currentQuestion);
//            skipped.remove(0);
            }
            Log.d(TAG, "skip: current question: "+currentQuestion);
        }

        reGenerate();

    }

    public int randomNumber(){
        int n = rand.nextInt(readText().length);
        return n;
        /* This method creates a random number that is between 0 and the total number of entries in the file. */
    }

    public void reGenerate(){
        /*
        This is by far the longest method in the entire app. It's the core of the app.
         */
        skipOrRestartBtn = (Button) findViewById(R.id.skipOrRestartBtn);
        skipOrRestartBtn.setText("Skip");
        skipOrRestartBtn.setEnabled(true);

        nameOfFileTextView = (TextView) findViewById(R.id.nameOfFileTextView);
        nameOfFileTextView.setText(nameOfFile);

//        if (answered.size()!=0)
//            nameOfFileTextView.setText(answered.size()+"/"+readText().length+" Answered From "+fileID);

        /* The skipOrRestartBtn is set to display "Skip" to let the user know they can skip and come back to that question later.*/

        if (answered.size()==readText().length-1 || answered == null){
            skipOrRestartBtn.setEnabled(false);
        }

        questionTextView = (TextView) findViewById(R.id.textView);
        buttonA = (Button) findViewById(R.id.buttonA);
        buttonB = (Button) findViewById(R.id.buttonB);
        buttonC = (Button) findViewById(R.id.buttonC);
        buttonD = (Button) findViewById(R.id.buttonD);
        restartNowBtn = (Button) findViewById(R.id.restartNowBtn);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        feedbackTextView = (TextView) findViewById(R.id.feedbackTextView);

        buttonA.setEnabled(true);
        buttonB.setEnabled(true);
        buttonC.setEnabled(true);
        buttonD.setEnabled(true);

        if (answered.size()==0){
            restartNowBtn.setVisibility(View.INVISIBLE);
        }
        else {
            restartNowBtn.setVisibility(View.VISIBLE);
        }

        /* The four buttons are set to enabled to make sure they can be pressed,
        because once the program goes through all the entries in the file, these buttons are disabled.*/

        progressBar.setProgress((int)(Math.round(progressBarStatus)));
            /* This updates the progress bar to reflect the user's progress */

        if (readText()==null)
            return;

        //int n1

        Log.d(TAG, "reGenerate: n1 before : "+n1);

        if (randomQuestions && n1 ==-1) {
            /* n1 is -1 when nothing is retrieved from preferences. So if nothing is retrieved and the user opts
            for random questions, a new random number for the first question is generated. Otherwise, the first question
            is the very first entry.
             */
            n1 = randomNumber();
            currentQuestion = n1;
        }
        else if (!randomQuestions && n1 ==-1){
            n1 = 0;
            currentQuestion = n1;
        }
        else if (randomQuestions){
            n1 = randomNumber();
            currentQuestion = n1;
        }
        else
            n1 = currentQuestion;

        Log.d(TAG, "reGenerate: n1 after : "+n1);
        Log.d(TAG, "reGenerate: already answered? "+checkIfAnswered(n1));

        if(!randomQuestions && (answered.size()==readText().length || progressBar.getProgress()==100)){
            buttonA.setEnabled(false);
            buttonB.setEnabled(false);
            buttonC.setEnabled(false);
            buttonD.setEnabled(false);

            doneMessage();

            skipOrRestartBtn.setText("Restart");
            restartNowBtn.setVisibility(View.INVISIBLE);
            skipOrRestartBtn.setOnClickListener(redoButtonRestart);
            return;
        }

        if (currentQuestion > readText().length-2){
            goneThroughAllEntries = true;
        }

        for (int i = 0;i<skipped.size();i++){
            if (skipped.get(i)==currentQuestion) {
                skipped.remove(i); //it's removed.
            }
        }

//        if (!randomQuestions && (currentQuestion > readText().length-1 /*|| goneThroughAllEntries*/)){
//            currentQuestion = skipped.get(0);
//            n1 = currentQuestion;
////            skipped.remove(0);
////            goneThroughAllEntries = true;
//        }

        while (checkIfAnswered(n1)){
            /* This while loop continues to loop until the random number generated does not represent a question that has
            already been completed by the user. When the user has gone through all the questions, it disables the three buttons,
            makes the skipOrRestartBtn display "Restart", displays a message of completion, and if when the skipOrRestartBtn is pressed,
            it goes to the restart method.
             */
            if (answered.size()==readText().length){
                buttonA.setEnabled(false);
                buttonB.setEnabled(false);
                buttonC.setEnabled(false);
                buttonD.setEnabled(false);
                doneMessage();
                skipOrRestartBtn.setText("Restart");
                restartNowBtn.setVisibility(View.INVISIBLE);
                skipOrRestartBtn.setOnClickListener(redoButtonRestart);
                break;
            }
            if (randomQuestions){
                n1 =randomNumber();
                currentQuestion = n1;
            }
        }

        saveCurrentQuestionToPreferences();

        Log.d(TAG, "reGenerate: n1 after after : "+n1);

        int n2 = randomNumber();
        int n3 = randomNumber();
        int n4 = randomNumber();
        int n5 = rand.nextInt(4)+1;

        /* Four more random numbers are generated. Three of them represent dummy and incorrect answers.
         The the last one generates a number between 1 and 3 and that number determines the order of texts in the three buttons.
         This way the correct answer is not always on the same button :)
         */

        while (n2==n3 || n2== n1 || n1 ==n3 || n3 == n4 || n2 == n4 || n1 == n4){
            /* This loop makes sure that the first three numbers are not the same */
            n2=randomNumber();
            n3=randomNumber();
            n4=randomNumber();
        }

        Log.d(TAG, "reGenerate: "+"n1: "+n1+" n2: "+n2+" n3: "+n3+" n4: "+n4+" n5: "+n5);

        if (reversedOrder)
            questionTextView.setText(readText()[n1][1]);
        /* This displays the string in the 2nd column of the randomly generated row of the 2D array. */
        else
            questionTextView.setText(readText() [n1][0]);

        if (n5==1){
            pickOne(n1,n2,n3,n4);
        }
        if (n5==2){
            pickTwo(n1,n2,n3,n4);
        }
        if (n5==3){
            pickThree(n1,n2,n3,n4);
        }
        if (n5==4){
            pickFour(n1,n2,n3,n4);
        }
        /* These if statements determine the order the text in the four buttons.
        Each one calls on a method that is similar to the methods in the other if statements.
        */
    }

    public void restart(View view){
        /* This method restarts the whole process by making the ArrayList containing the
        rows of the completed questions equal to size zero, and by resetting the progress bar to 0.
         */
        if (answered.size()==readText().length){
            answered = new ArrayList<Integer>();
            if (!randomQuestions){
                skipped = new ArrayList<Integer>();
                currentQuestion = 0;
                goneThroughAllEntries = false;
            }
            progressBarStatus = 0;
            progressBar.setProgress((int)(Math.round(progressBarStatus)));
            saveProgressToPreferences();
            feedbackTextView.setText("");
        }
        reGenerate();
    }

    public void actualRestart(View view){
        /*
        This method is identical to restart(View view). The only difference is that it doesn't just
        become an option after all the entries have been dealt with. It can be launched at anytime
        during the studying.
         */
        answered = new ArrayList<Integer>();
        if (!randomQuestions){
            skipped = new ArrayList<Integer>();
            currentQuestion = 0;
            goneThroughAllEntries = false;
        }
        progressBarStatus=0;
        progressBar.setProgress((int)(Math.round(progressBarStatus)));
        saveProgressToPreferences();
        feedbackTextView.setText("");
        reGenerate();
    }

    public void pickOne (int n1, int n2, int n3, int n4){
        /* This method puts the text on the four buttons in the first order. Once the button with
        the right text is pressed, a message of validity is displayed, the ArrayList containing the
        completed questions is updated, and a new unanswered question is generated.
         */
        this.n1 = n1;
        if (reversedOrder){
            buttonA.setText(readText()[n1][0]);
        /* buttonTrue here is given the right text with n1 which is equal to the correct randomly selected row.*/
            buttonB.setText(readText()[n2][0]);
            buttonC.setText(readText()[n3][0]);
            buttonD.setText(readText()[n4][0]);
        }
        else {
            buttonA.setText(readText()[n1][1]);
        /* buttonTrue here is given the right text with n1 which is equal to the correct randomly selected row.*/
            buttonB.setText(readText()[n2][1]);
            buttonC.setText(readText()[n3][1]);
            buttonD.setText(readText()[n4][1]);
        }

        buttonA.setOnClickListener(correctAnswer);
        buttonB.setOnClickListener(wrongAnswer);
        buttonC.setOnClickListener(wrongAnswer);
        buttonD.setOnClickListener(wrongAnswer);
    }
    public void pickTwo(int n1, int n2, int n3, int n4){
        /*This method puts the text on the four buttons in the second order.*/
        this.n1 = n1;
        if (reversedOrder){
            buttonA.setText(readText()[n2][0]);
            buttonB.setText(readText()[n1][0]);
        /* buttonFalse here is given the right text with n1 which is equal to the correct randomly selected row.*/
            buttonC.setText(readText()[n3][0]);
            buttonD.setText(readText()[n4][0]);
        }
        else {
            buttonA.setText(readText()[n2][1]);
            buttonB.setText(readText()[n1][1]);
        /* buttonFalse here is given the right text with n1 which is equal to the correct randomly selected row.*/
            buttonC.setText(readText()[n3][1]);
            buttonD.setText(readText()[n4][1]);
        }

        buttonB.setOnClickListener(correctAnswer);
        buttonA.setOnClickListener(wrongAnswer);
        buttonC.setOnClickListener(wrongAnswer);
        buttonD.setOnClickListener(wrongAnswer);
    }
    public void pickThree(int n1, int n2, int n3, int n4){
        /*This method puts the text on the four buttons in the third order.*/
        this.n1 = n1;
        if (reversedOrder){
            buttonA.setText(readText()[n2][0]);
            buttonB.setText(readText()[n3][0]);
            buttonC.setText(readText()[n1][0]);
        /* buttonC here is given the right text with n1 which is equal to the correct randomly selected row.*/
            buttonD.setText(readText()[n4][0]);
        }
        else {
            buttonA.setText(readText()[n2][1]);
            buttonB.setText(readText()[n3][1]);
            buttonC.setText(readText()[n1][1]);
        /* buttonC here is given the right text with n1 which is equal to the correct randomly selected row.*/
            buttonD.setText(readText()[n4][1]);
        }

        buttonC.setOnClickListener(correctAnswer);
        buttonA.setOnClickListener(wrongAnswer);
        buttonB.setOnClickListener(wrongAnswer);
        buttonD.setOnClickListener(wrongAnswer);
    }
    public void pickFour(int n1, int n2, int n3, int n4){
        /*This method puts the text on the four buttons in the third order.*/
        this.n1 = n1;
        if (reversedOrder){
            buttonA.setText(readText()[n4][0]);
            buttonB.setText(readText()[n3][0]);
            buttonC.setText(readText()[n2][0]);
            buttonD.setText(readText()[n1][0]);
        /* buttonD here is given the right text with n1 which is equal to the correct randomly selected row.*/
        }
        else{
            buttonA.setText(readText()[n4][1]);
            buttonB.setText(readText()[n3][1]);
            buttonC.setText(readText()[n2][1]);
            buttonD.setText(readText()[n1][1]);
            /* buttonD here is given the right text with n1 which is equal to the correct randomly selected row.*/
        }
        buttonD.setOnClickListener(correctAnswer);
        buttonA.setOnClickListener(wrongAnswer);
        buttonB.setOnClickListener(wrongAnswer);
        buttonC.setOnClickListener(wrongAnswer);
    }

    public void answeredQuestion(int n1){
        /* This method checks if the current question has been answered, if yes, it updates the ArrayList
        containing the answered questions, and it updates the progress bar to reflect the user's progress.
        In addition, it adds the answered questions on to a string which it stores in the progress preferences.
         */
        double sizeData = readText().length;
        double sizeOneProgress = 100/sizeData;
        if (answered.size()==0 || answered==null){
            answered.add(n1);

            if (!randomQuestions){
                if (goneThroughAllEntries && skipped.size()!=0)
                    currentQuestion = skipped.get(0);
                else
                    currentQuestion++;
            }
            progressBarStatus+=sizeOneProgress;
            saveProgressToPreferences();
        }
        for (int i=0;i<answered.size();i++){
            /*
            This is to make sure that the current question has not been answered already. It's not
            really necessary, but I like to have it here.
             */
            if (answered.get(i)==n1){
                return;
            }
        }

        if (!randomQuestions){
            if (goneThroughAllEntries && skipped.size()!=0)
                currentQuestion = skipped.get(0);
            else
                currentQuestion++;
        }

        answered.add(n1);
        progressBarStatus+=sizeOneProgress;
        saveProgressToPreferences();
    }

    public boolean checkIfAnswered(int n1){
        /* This method also checks to see whether the current question has been answered before.
        It returns true if the it has been answered before, and false otherwise. This is helps know
        if we need to generate a new random number or not.
         */
        if (answered==null){
            return false;
        }
        if (answered.size()==0){
            return false;
        }
        for (int i=0;i<answered.size();i++) {
            if (answered.get(i) == n1) {
                return true;
            }
        }
        return false;
    }

    public void doneMessage(){
        /*This method displays a message when all the questions have been completed. */
        feedbackTextView.setText("You are Awesome!");
        feedbackTextView.setTextColor(getResources().getColor(R.color.myGreen));

        questionTextView.setText("Completed");
        buttonA.setText("");
        buttonB.setText("");
        buttonC.setText("");
        buttonD.setText("");
    }

    public void checkIfFileSelected(){
        /*
        This method checks to see if a file has been selected from HomeActivity by getting its name
        and directory path from the preferences. If no file has been previously selected (preferences are empty),
        The user is redirected to HomeActivity. Otherwise, it goes on with the studying.
         */
        if (fileID.equals("") || pathOfFileSelected.equals("")){
            Log.d(TAG, "checkIfFileSelected: No File was selected");
            Toast.makeText(mContext,"No File Was Selected",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(mContext, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            //Log.d(TAG, "checkIfFileSelected: No File was selected #2 check");
        }
        else {
            retrieveProgressFromPreferences();
            reGenerate();
        }
    }

    public void saveProgressToPreferences(){
        /*
        This method retrieves the numbers completed from the answered arrayList which is
        updated for each correct answer by the user and the status of the progress bar. It stores them
        into preferences, and the firebaseMethod database for future reference.
         */

        String answeredToSave = "";

        for (int i = 0; i<answered.size();i++){
            if (answered.get(i) != null){
                answeredToSave += answered.get(i)+ " ";
            }
        }

        String skippedQuestions = "";

        for (int i = 0;i<skipped.size();i++){
            if (skipped.get(i) != null){
                skippedQuestions += skipped.get(i)+ " ";
            }
        }

        SharedPreferences.Editor editor = getSharedPreferences(fileID, MODE_PRIVATE).edit();
        editor.putString("answeredQuestions", answeredToSave.trim());
        editor.putString("progressBarStatus", Double.toString(progressBarStatus));
        editor.putInt("currentQuestion", currentQuestion);
        editor.putBoolean("goneThroughAllEntries", goneThroughAllEntries);
        editor.putString("skippedQuestions", skippedQuestions.trim());
        editor.apply();

        FirebaseMethod firebaseMethod = new FirebaseMethod();
        firebaseMethod.saveProgressToDatabase(fileID,answeredToSave,progressBarStatus,
                currentQuestion,goneThroughAllEntries,skippedQuestions.trim(),0,
                "",0);

        Log.d(TAG, "saveProgressToPreferences Saved: answeredToSave "+answeredToSave.trim());
        Log.d(TAG, "saveProgressToPreferences Saved: progressBarStatus "+Double.toString(progressBarStatus));
        Log.d(TAG, "saveProgressToPreferences: Saved: currentQuestion "+ currentQuestion);
        Log.d(TAG, "saveProgressToPreferences: Saved: goneThroughAllEntries "+ goneThroughAllEntries);
        Log.d(TAG, "saveProgressToPreferences: Saved: skippedQuestions "+ skippedQuestions.trim());
    }

    private void saveCurrentQuestionToPreferences(){
        /* Saves current file each time reGenerate() runs. That way the user picks up where they left off
        when they reopen this specific file
         */

        SharedPreferences.Editor editor = getSharedPreferences(fileID, MODE_PRIVATE).edit();
        editor.putInt("currentQuestion", currentQuestion);
        editor.apply();
    }

    public void retrieveProgressFromPreferences(){
        /*
        This methods retrieves progress from preferences in the form of strings. It creates an arraylist
        which matches the answered questions and links that array list with the answered array list created
        when this activity is launched. It also converts the progressbar string into a double and updates it
        with the one created when this activity is launched.
        I initially thought about retrieving the progress from the database if the preferences was empty
        (if the user had recently deleted the app and lost their progress) but it proved to be difficult.
         */

        SharedPreferences prefs = getSharedPreferences(fileID, MODE_PRIVATE);

        String answeredQuestions = prefs.getString("answeredQuestions","");
        String progressBarStatusString = prefs.getString("progressBarStatus","");
        int savedCurrentQuestion = prefs.getInt("currentQuestion",0);
        boolean hasGoneThroughAllEntries = prefs.getBoolean("goneThroughAllEntries", false); //by default I set it to false
        String skippedQuestions = prefs.getString("skippedQuestions", "");

        if (answeredQuestions.equals("") || progressBarStatusString.equals("")){
            if (skippedQuestions.equals("")){
                Log.d(TAG, "retrieveProgressFromPreferences: progress data in prefs is empty. Nothing is saved on device.");
                return;
            }
        }

        double progressBarStatusDouble = Double.parseDouble(progressBarStatusString);
        progressBarStatus = progressBarStatusDouble;

        if (savedCurrentQuestion!=-1){
            currentQuestion = savedCurrentQuestion;
            n1 = savedCurrentQuestion;
        }

        goneThroughAllEntries = hasGoneThroughAllEntries;

        String [] answeredQuestionsStringArray = answeredQuestions.split(" ");

        String [] skippedQuestionsStringArray = skippedQuestions.split(" ");

        for (int i=0;i<answeredQuestionsStringArray.length;i++){
            Log.d(TAG, "retrieveProgressFromPreferences: Answered String numbers in array: "+answeredQuestionsStringArray[i]);
        }
        for (int i=0;i<skippedQuestionsStringArray.length;i++){
            Log.d(TAG, "retrieveProgressFromPreferences: Skipped String numbers in array: "+skippedQuestionsStringArray[i]);
        }

        if (answeredQuestionsStringArray.length != 0){
            ArrayList<Integer> answeredQuestionsArrayList = new ArrayList<>();
            for (int i = 0; i<answeredQuestionsStringArray.length;i++){
                int question;
                try {
                    question = Integer.parseInt(answeredQuestionsStringArray[i]);
                }
                catch (Exception e){
                    Log.d(TAG, "retrieveProgressFromPreferences: answeredQuestions not a valid number");
                    continue;
                }

                answeredQuestionsArrayList.add(question);
            }
            answered = answeredQuestionsArrayList;
        }


        if (skippedQuestionsStringArray.length != 0) {
            Log.d(TAG, "retrieveProgressFromPreferences: skippedQuestionsStringArray length: "+skippedQuestionsStringArray.length);
            ArrayList<Integer> skippedQuestionsArrayList = new ArrayList<>();
            for (int i = 0; i < skippedQuestionsStringArray.length; i++) {
                int question;
                try {
                    question = Integer.parseInt(skippedQuestionsStringArray[i]);
                }
                catch (Exception e){
                    Log.d(TAG, "retrieveProgressFromPreferences skippedQuestions: not a valid number");
                    continue;
                }
                skippedQuestionsArrayList.add(question);
            }
            skipped = skippedQuestionsArrayList;
        }

        Log.d(TAG, "retrieveProgressFromPreferences: answered: "+answered);
        Log.d(TAG, "retrieveProgressFromPreferences: progressBarStatus: "+progressBarStatus);
        Log.d(TAG, "retrieveProgressFromPreferences: skipped: "+skipped);
    }

    private void resetButtonColors(){
        buttonA.setBackgroundResource(R.drawable.grey_button_layout);
        buttonB.setBackgroundResource(R.drawable.grey_button_layout);
        buttonC.setBackgroundResource(R.drawable.grey_button_layout);
        buttonD.setBackgroundResource(R.drawable.grey_button_layout);
    }

    public void fileOptionsDialog(final String fileID){
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.file_options_dialog);

        studyCheckBox = (CheckBox) dialog.findViewById(R.id.studyCheckBox);
        quizCheckBox = (CheckBox) dialog.findViewById(R.id.quizCheckBox);
        multipleChoiceCheckBox = (CheckBox) dialog.findViewById(R.id.multipleChoiceCheckBox);
        textCheckBox = (CheckBox) dialog.findViewById(R.id.textCheckBox);
        randomQuestionsSwitch = (Switch) dialog.findViewById(R.id.randomQuestionsSwitch);
        reverseOrderSwitch = (Switch) dialog.findViewById(R.id.reversedOrderSwitch);

        cancelTextView = (TextView) dialog.findViewById(R.id.no);
        proceedTextView = (TextView) dialog.findViewById(R.id.yes);

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        SharedPreferences prefsFileSelected = getSharedPreferences(fileID, MODE_PRIVATE);

        String learningMode = prefsFileSelected.getString("learningMode","");
        String answerInputMode = prefsFileSelected.getString("answerInputMethod","");
        boolean randomQuestions = prefsFileSelected.getBoolean("randomQuestions",false);
        boolean reversedOrder = prefsFileSelected.getBoolean("reversedOrder",false);

        String randomQuestionsString = "false";
        String reversedOrderString = "false";

        if (randomQuestions)
            randomQuestionsString = "true";
        if (reversedOrder)
            reversedOrderString = "true";


        currentSettings.put("learningMode", learningMode);
        currentSettings.put("answerInputMode", answerInputMode);
        currentSettings.put("randomQuestions", randomQuestionsString);
        currentSettings.put("reversedOrder",reversedOrderString);

        if (learningMode.equals("Study"))
            studyCheckBox.setChecked(true);
        if (learningMode.equals("Quiz"))
            quizCheckBox.setChecked(true);
        if (answerInputMode.equals("MultipleChoice"))
            multipleChoiceCheckBox.setChecked(true);
        if (answerInputMode.equals("Text"))
            textCheckBox.setChecked(true);
        if (randomQuestions)
            randomQuestionsSwitch.setChecked(true);
        if (reversedOrder)
            reverseOrderSwitch.setChecked(true);

        studyCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                quizCheckBox.setChecked(false);
            }
        });
        quizCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                studyCheckBox.setChecked(false);
            }
        });
        multipleChoiceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                textCheckBox.setChecked(false);
            }
        });
        textCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                multipleChoiceCheckBox.setChecked(false);
            }
        });
        cancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        proceedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!studyCheckBox.isChecked() && !quizCheckBox.isChecked()){
                    Toast.makeText(mContext,"You must choose a learning mode",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!multipleChoiceCheckBox.isChecked() && !textCheckBox.isChecked()){
                    Toast.makeText(mContext,"You must choose an answer input method",Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences.Editor editor = getSharedPreferences(fileID, MODE_PRIVATE).edit();
                if (studyCheckBox.isChecked())
                    editor.putString("learningMode","Study");
                if (quizCheckBox.isChecked())
                    editor.putString("learningMode","Quiz");
                if (multipleChoiceCheckBox.isChecked())
                    editor.putString("answerInputMethod","MultipleChoice");
                if (textCheckBox.isChecked())
                    editor.putString("answerInputMethod","Text");

                if (randomQuestionsSwitch.isChecked())
                    editor.putBoolean("randomQuestions",true);
                else
                    editor.putBoolean("randomQuestions",false);

                if (reverseOrderSwitch.isChecked())
                    editor.putBoolean("reversedOrder", true);
                else
                    editor.putBoolean("reversedOrder", false);

                editor.apply();

                startModeActivity();

                dialog.dismiss();
            }
        });

    }

    private void startModeActivity (){

        SharedPreferences prefs = getSharedPreferences(fileID, MODE_PRIVATE);
        String nameOfFile = prefs.getString("nameOfFile","unnamedFile");
        String learningMode = prefs.getString("learningMode", "Study");
        String answerInputMethod = prefs.getString("answerInputMethod", "MultipleChoice");
        boolean randomQuestions = prefs.getBoolean("randomQuestions",false);
        boolean reversedOrder = prefs.getBoolean("reversedOrder",false);

        String randomQuestionsString = "false";
        String reversedOrderString = "false";

        if (randomQuestions)
            randomQuestionsString = "true";
        if (reversedOrder)
            reversedOrderString = "true";


        if (currentSettings.get("learningMode").equals(learningMode)
                && currentSettings.get("answerInputMode").equals(answerInputMethod)
                && currentSettings.get("randomQuestions").equals(randomQuestionsString)
                && currentSettings.get("reversedOrder").equals(reversedOrderString)){
            Toast.makeText(mContext,"No Changes Were Made",Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseMethod firebaseMethod = new FirebaseMethod();
        firebaseMethod.saveFileModeToDatabse(fileID,nameOfFile,learningMode,answerInputMethod,randomQuestions,reversedOrder);


        if (!currentSettings.get("learningMode").equals(learningMode)
                || !currentSettings.get("randomQuestions").equals(randomQuestionsString)
                || !currentSettings.get("answerInputMode").equals(answerInputMethod)){
            resetFileProgress(fileID);
        }


        if (learningMode.equals("Study") && answerInputMethod.equals("MultipleChoice")){
            Intent intent = new Intent(mContext,StudyMultipleChoiceActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
        if (learningMode.equals("Study") && answerInputMethod.equals("Text")){
            Intent intent = new Intent(mContext,StudyTextActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
        if (learningMode.equals("Quiz") && answerInputMethod.equals("MultipleChoice")){
            Intent intent = new Intent(mContext,QuizMultipleChoiceActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
        if (learningMode.equals("Quiz") && answerInputMethod.equals("Text")){
            Intent intent = new Intent(mContext,QuizTextActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    public String [] [] readText (){
        /* This methods makes an array by splitting the text from the file when there is a new line.
        Then it makes a 2D array by splitting the array each time ':' is used.
        If the text is empty it returns an empty 2D array. This should never happen. It's just a safeguard.
         */

        EntryFiles entryFiles = new EntryFiles();
        String text = entryFiles.readFile(pathOfFileSelected);

        if (text==null || text.equals(""))
            return null;
        String[] lines = text.split("\r\n|\r|\n");
        String [][] data = new String[lines.length][];
        for (int i = 0; i<lines.length;i++){
            String currentLine = lines [i];
            data [i] = currentLine.split(":");
        }
        return data;
        }

    private void resetFileProgress(String nameOfFileSelected){

        SharedPreferences.Editor editor = getSharedPreferences(nameOfFileSelected, MODE_PRIVATE).edit();
        editor.putString("answeredQuestions", "");
        editor.putString("progressBarStatus", "0.0");
        editor.putInt("currentQuestion", 0);
        editor.putBoolean("goneThroughAllEntries", false);
        editor.putString("skippedQuestions", "");
        editor.putString("currentAnswer", "");
        editor.putInt("attempts", 0);
        editor.apply();

        FirebaseMethod firebaseMethod = new FirebaseMethod();
        firebaseMethod.saveProgressToDatabase(nameOfFileSelected,"",0,
                0,false,"",0,
                "",0);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent = new Intent(mContext,HomeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return false;
                case R.id.navigation_learning:
                    return true;
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

    @Override
    protected void onStop() {
        super.onStop();
    }
}

