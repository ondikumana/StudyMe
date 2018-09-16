package com.example.android.studyme.QuizMode;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.studyme.Files.EntryFiles;
import com.example.android.studyme.Firebase.FirebaseMethod;
import com.example.android.studyme.Home.HomeActivity;
import com.example.android.studyme.Profile.ProfileActivity;
import com.example.android.studyme.R;
import com.example.android.studyme.StudyMode.StudyMultipleChoiceActivity;
import com.example.android.studyme.StudyMode.StudyTextActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.example.android.studyme.Home.UserFilesFragment.CURRENTLY_SELECTED_FILE;

/**
 * Created by Backmod on 1/10/18.
 */

public class QuizMultipleChoiceActivity extends AppCompatActivity {

    private static final String TAG = "QuizMultipleChoiceActivity";

    private int n1;

    private TextView questionTextView,feedbackTextView, nameOfFileTextView;
    private Button buttonA,buttonB,buttonC,buttonD,submitBtn,nextBtn;
    private ProgressBar progressBar;

    private Context mContext;
    private FirebaseAuth mAuth;

    private ArrayList<Integer> answered = new ArrayList<>();
    private ArrayList<String> incorrectlyAnswered = new ArrayList<>();
    double progressBarStatus = 0;
    private Random rand = new Random();

    private ImageView backArrowActionBar,editModeActionBar;
    private TextView activityNameActionBar;

    private CheckBox studyCheckBox,quizCheckBox,multipleChoiceCheckBox,textCheckBox;
    private Switch randomQuestionsSwitch,reverseOrderSwitch;
    private TextView cancelTextView,proceedTextView;

    private String fileID = "";
    private String nameOfFile = "";
    private String pathOfFileSelected = "";

    private int incorrectAnswers = 0;
    private int unAnsweredQuestions = 0;
    private boolean randomQuestions;
    private boolean reversedOrder;
    private int currentQuestion = 0;

    private Map<String,String> currentSettings = new HashMap();

    private TextView learningMode,answerInputMethod,random;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_multiple_choice_activity);
        mContext = QuizMultipleChoiceActivity.this;

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.getMenu().findItem(R.id.navigation_learning).setChecked(true);
        mAuth = FirebaseAuth.getInstance();

        SharedPreferences prefs = getSharedPreferences(CURRENTLY_SELECTED_FILE, MODE_PRIVATE);
        fileID = prefs.getString("fileID", "");
        pathOfFileSelected = prefs.getString("pathOfFileSelected","");

        SharedPreferences prefsSelectedFile = getSharedPreferences(fileID, MODE_PRIVATE);
        randomQuestions = prefsSelectedFile.getBoolean("randomQuestions", false); // by default, I set it to false.
        reversedOrder = prefsSelectedFile.getBoolean("reversedOrder",false);
        nameOfFile = prefsSelectedFile.getString("nameOfFile","Unnamed File");

        Log.d(TAG, "onCreate: Selected file info: "+ fileID +" "+pathOfFileSelected);

        learningMode = (TextView) findViewById(R.id.learningMode);
        answerInputMethod = (TextView) findViewById(R.id.answerInputMethd);
        random = (TextView) findViewById(R.id.randomQuestions);

        learningMode.setText("Quiz ");
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

        nameOfFileTextView = (TextView) findViewById(R.id.nameOfFileTextView);
        nameOfFileTextView.setText(nameOfFile);

//        if (answered.size()!=0)
//            nameOfFileTextView.setText(answered.size()+"/"+readText().length+" Answered From "+fileID);

        questionTextView = (TextView) findViewById(R.id.textView);
        questionTextView.setTextColor(getResources().getColor(R.color.black));
        questionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        buttonA = (Button) findViewById(R.id.buttonA);
        buttonB = (Button) findViewById(R.id.buttonB);
        buttonC = (Button) findViewById(R.id.buttonC);
        buttonD = (Button) findViewById(R.id.buttonD);
        submitBtn = (Button) findViewById(R.id.submitBtn);
        feedbackTextView = (TextView) findViewById(R.id.feedbackTextView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        resetButtonColors();

        buttonA.setEnabled(true);
        buttonB.setEnabled(true);
        buttonC.setEnabled(true);
        buttonD.setEnabled(true);

        Log.d(TAG, "reGenerate: unansweredQuestions "+unAnsweredQuestions);

        if (unAnsweredQuestions !=0){
            submitBtn.setText("Restart");
            displayScore();

        }

        if (answered.size()==0){
            submitBtn.setVisibility(View.INVISIBLE);
        }
        else {
            submitBtn.setVisibility(View.VISIBLE);
        }

        /* The four buttons are set to enabled to make sure they can be pressed,
        because once the program goes through all the entries in the file, these buttons are disabled.*/

        progressBar.setProgress((int)(Math.round(progressBarStatus)));
            /* This updates the progress bar to reflect the user's progress */

        if (readText()==null)
            return;

//        int n1;

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

        if(!randomQuestions && (answered.size()==readText().length || progressBar.getProgress()==100)){
            buttonA.setEnabled(false);
            buttonB.setEnabled(false);
            buttonC.setEnabled(false);
            buttonD.setEnabled(false);
            currentQuestion = 0;
            displayScore();
            submitBtn.setText("Restart");
            return;
        }

        while (checkIfAnswered(n1)){
            /* This while loop continues to loop until the random number generated does not represent a question that has
            already been completed by the user. When the user has gone through all the questions, it disables the three buttons,
            makes the redoButton display "Restart", displays a message of completion, and if when the redoButton is pressed,
            it goes to the restart method.
             */
            if (answered.size()==readText().length || progressBar.getProgress()==100){
                buttonA.setEnabled(false);
                buttonB.setEnabled(false);
                buttonC.setEnabled(false);
                buttonD.setEnabled(false);
                currentQuestion = 0;
                displayScore();
                submitBtn.setText("Restart");
                break;
            }
            if (randomQuestions) {
                n1 = randomNumber();
                currentQuestion = n1;
            }
        }

        saveCurrentQuestionToPreferences();

        if (answered.size()==readText().length || progressBar.getProgress()==100)
            return;

        int n2 = randomNumber();
        int n3 = randomNumber();
        int n4 = randomNumber();
        int n5 = rand.nextInt(4)+1;

        /* Four more random numbers are generated. Three of them represent dummy and incorrect answers.
         The the last one generates a number between 1 and 3 and that number determines the order of texts in the three buttons.
         This way the correct answer is not always on the same button :)
         */

        while (n2==n3 || n2==n1 || n1==n3 || n3 == n4 || n2 == n4 || n1 == n4){
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

        final int n1Final = n1;
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

    public void pickOne (final int n1, int n2, int n3, int n4){
        /* This method puts the text on the four buttons in the first order. Once the button with
        the right text is pressed, a message of validity is displayed, the ArrayList containing the
        completed questions is updated, and a new unanswered question is generated.
         */

        View.OnClickListener correctAnswer = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answeredQuestion(n1);
                view.setBackgroundResource(R.drawable.green_button_layout);
                counterToReGenerate();
                currentQuestion++;
            }
        };
        View.OnClickListener wrongAnswer = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button b = (Button)view;
                incorrectAnswers++;
                answeredQuestion(n1);
                b.setBackgroundResource(R.drawable.red_button_layout);
                buttonA.setBackgroundResource(R.drawable.green_button_layout);
                incorrectlyAnswered.add(readText()[n1][0]+":\n"+"\t\t\tYou answered: " + b.getText().toString()+"\n"
                        + "\t\t\tCorrect answer: "+readText()[n1][1]+"\n");
                counterToReGenerate();
                currentQuestion++;

            }
        };

        if (reversedOrder){
            buttonA.setText(readText()[n1][0]);
        /* buttonA here is given the right text with n1 which is equal to the correct randomly selected row.*/
            buttonB.setText(readText()[n2][0]);
            buttonC.setText(readText()[n3][0]);
            buttonD.setText(readText()[n4][0]);
        }
        else {
            buttonA.setText(readText()[n1][1]);
        /* buttonA here is given the right text with n1 which is equal to the correct randomly selected row.*/
            buttonB.setText(readText()[n2][1]);
            buttonC.setText(readText()[n3][1]);
            buttonD.setText(readText()[n4][1]);
        }

        buttonA.setOnClickListener(correctAnswer);
        buttonB.setOnClickListener(wrongAnswer);
        buttonC.setOnClickListener(wrongAnswer);
        buttonD.setOnClickListener(wrongAnswer);

    }
    public void pickTwo(final int n1, int n2, int n3, int n4){
        /*This method puts the text on the four buttons in the second order.*/

        View.OnClickListener correctAnswer = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answeredQuestion(n1);
                view.setBackgroundResource(R.drawable.green_button_layout);
                counterToReGenerate();
                currentQuestion++;
            }
        };
        View.OnClickListener wrongAnswer = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button b = (Button)view;
                incorrectAnswers++;
                answeredQuestion(n1);
                b.setBackgroundResource(R.drawable.red_button_layout);
                buttonB.setBackgroundResource(R.drawable.green_button_layout);
                incorrectlyAnswered.add(readText()[n1][0]+":\n"+"\t\t\tYou answered: " + b.getText().toString()+"\n"
                        + "\t\t\tCorrect answer: "+readText()[n1][1]+"\n");
                counterToReGenerate();
                currentQuestion++;
            }
        };

        if (reversedOrder){
            buttonA.setText(readText()[n2][0]);
            buttonB.setText(readText()[n1][0]);
        /* buttonB here is given the right text with n1 which is equal to the correct randomly selected row.*/
            buttonC.setText(readText()[n3][0]);
            buttonD.setText(readText()[n4][0]);
        }
        else {
            buttonA.setText(readText()[n2][1]);
            buttonB.setText(readText()[n1][1]);
        /* buttonB here is given the right text with n1 which is equal to the correct randomly selected row.*/
            buttonC.setText(readText()[n3][1]);
            buttonD.setText(readText()[n4][1]);
        }

        buttonB.setOnClickListener(correctAnswer);
        buttonA.setOnClickListener(wrongAnswer);
        buttonC.setOnClickListener(wrongAnswer);
        buttonD.setOnClickListener(wrongAnswer);
    }
    public void pickThree(final int n1, int n2, int n3, int n4){
        /*This method puts the text on the four buttons in the third order.*/

        View.OnClickListener correctAnswer = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answeredQuestion(n1);
                view.setBackgroundResource(R.drawable.green_button_layout);
                counterToReGenerate();
                currentQuestion++;
            }
        };
        View.OnClickListener wrongAnswer = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button b = (Button)view;
                incorrectAnswers++;
                answeredQuestion(n1);
                b.setBackgroundResource(R.drawable.red_button_layout);
                buttonC.setBackgroundResource(R.drawable.green_button_layout);
                incorrectlyAnswered.add(readText()[n1][0]+":\n"+"\t\t\tYou answered: " + b.getText().toString()+"\n"
                        + "\t\t\tCorrect answer: "+readText()[n1][1]+"\n");
                counterToReGenerate();
                currentQuestion++;
            }
        };

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
    public void pickFour(final int n1, int n2, int n3, int n4){
        /*This method puts the text on the four buttons in the third order.*/

        View.OnClickListener correctAnswer = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answeredQuestion(n1);
                view.setBackgroundResource(R.drawable.green_button_layout);
                counterToReGenerate();
                currentQuestion++;
            }
        };
        View.OnClickListener wrongAnswer = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button b = (Button)view;
                incorrectAnswers++;
                answeredQuestion(n1);
                b.setBackgroundResource(R.drawable.red_button_layout);
                buttonD.setBackgroundResource(R.drawable.green_button_layout);
                incorrectlyAnswered.add(readText()[n1][0]+":\n"+"\t\t\tYou answered: " + b.getText().toString()+"\n"
                        + "\t\t\tCorrect answer: "+readText()[n1][1]+"\n");
                counterToReGenerate();
                currentQuestion++;
            }
        };

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

    public void answeredQuestion(int n1){
        /* This method checks if the current question has been answered, if yes, it updates the ArrayList
        containing the answered questions, and it updates the progress bar to reflect the user's progress.
        In addition, it adds the answered questions on to a string which it stores in the progress preferences.
         */
        double sizeData = readText().length;
        double sizeOneProgress = 100/sizeData;
        if (answered.size()==0 || answered==null){
            answered.add(n1);
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
        answered.add(n1);
        progressBarStatus+=sizeOneProgress;
        saveProgressToPreferences();
    }

    private void resetButtonColors(){
        buttonA.setBackgroundResource(R.drawable.grey_button_layout);
        buttonB.setBackgroundResource(R.drawable.grey_button_layout);
        buttonC.setBackgroundResource(R.drawable.grey_button_layout);
        buttonD.setBackgroundResource(R.drawable.grey_button_layout);
    }

    private void displayScore(){
        feedbackTextView.setText(R.string.view_report);

        buttonA.setText("");
        buttonB.setText("");
        buttonC.setText("");
        buttonD.setText("");

        final double sizeData = readText().length;
        final double percentage = (sizeData-incorrectAnswers)/sizeData;
        final int actualPercentage = (int)Math.round(percentage*100);
//        Double percentage = ((readText().length - incorrectAnswers)/(readText().length))*100.0;
        Log.d(TAG, "displayScore: percentage: "+percentage +" incorrectAnswers: "+incorrectAnswers+" lengthOfArray: "+sizeData);
//        questionTextView.setText(String.valueOf(actualPercentage)+"%");
        if (actualPercentage >= 90) {
            questionTextView.setTextColor(getResources().getColor(R.color.myGreen));
            questionTextView.setText("A");
        }
        else if (actualPercentage >=80 && actualPercentage <90) {
            questionTextView.setTextColor(getResources().getColor(R.color.gradeB));
            questionTextView.setText("B");
        }
        else if (actualPercentage >=70 && actualPercentage <80) {
            questionTextView.setTextColor(getResources().getColor(R.color.gradeC));
            questionTextView.setText("C");
        }
        else if (actualPercentage >=60 && actualPercentage <70) {
            questionTextView.setTextColor(getResources().getColor(R.color.gradeD));
            questionTextView.setText("D");
        }
        else {
            questionTextView.setTextColor(getResources().getColor(R.color.myRed));
            questionTextView.setText("F");
        }

        questionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(mContext);
                dialog.setContentView(R.layout.quiz_report_dialog);
                TextView rawScoreTextView = (TextView) dialog.findViewById(R.id.rawScoreTextView);
                TextView scorePercentageTextView = (TextView) dialog.findViewById(R.id.scorePercentageTextView);
                TextView dismissDialog = (TextView) dialog.findViewById(R.id.dismissDialog);
                TextView difficultiesTextView = (TextView) dialog.findViewById(R.id.difficultiesTextView);
                TextView incorrectlyAnsweredTextView = (TextView) dialog.findViewById(R.id.incorrectlyAnsweredTextView);
                TextView awesomeTextView = (TextView) dialog.findViewById(R.id.awesomeTextView);
                ScrollView scrollView = (ScrollView) dialog.findViewById(R.id.scrollView);
                RelativeLayout relativeLayout3 = (RelativeLayout) dialog.findViewById(R.id.relativeLayout3);
                TextView unAnsweredQuestionsTextView = (TextView) dialog.findViewById(R.id.unAnsweredQuestionsTextView);

                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.show();

                rawScoreTextView.setText((readText().length-incorrectAnswers)+"/"+readText().length);
                scorePercentageTextView.setText(String.valueOf(actualPercentage)+"%");
                unAnsweredQuestionsTextView.setText(String.valueOf(unAnsweredQuestions));

                if (incorrectAnswers==0){
                    awesomeTextView.setVisibility(View.VISIBLE);
                    relativeLayout3.setVisibility(View.GONE);
                }

                if (incorrectlyAnswered.size()==0 || incorrectlyAnswered==null){
                    difficultiesTextView.setVisibility(View.GONE);
                    scrollView.setVisibility(View.GONE);
                    incorrectlyAnsweredTextView.setVisibility(View.GONE);
                }

                if (incorrectlyAnswered.size() <= 3 && incorrectlyAnswered.size() > 0){
                    scrollView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                }

                StringBuilder sb = new StringBuilder();
                for (int i = 0;i<incorrectlyAnswered.size();i++){
                    sb.append(incorrectlyAnswered.get(i)+"\n");
                }
                incorrectlyAnsweredTextView.setText(sb.toString().trim());

                dismissDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });
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

        String incorrectlyAnsweredToSave = "";

        for (int i = 0; i < incorrectlyAnswered.size();i++){
            if (incorrectlyAnswered.get(i) != null){
                incorrectlyAnsweredToSave += incorrectlyAnswered.get(i) +"\n\n";
            }
        }

        String skippedQuestions = String.valueOf(unAnsweredQuestions);

        SharedPreferences.Editor editor = getSharedPreferences(fileID, MODE_PRIVATE).edit();
        editor.putString("answeredQuestions", answeredToSave.trim());
        editor.putString("incorrectlyAnswered", incorrectlyAnsweredToSave.trim());
        editor.putString("progressBarStatus", Double.toString(progressBarStatus));
        editor.putInt("currentQuestion", currentQuestion);
        editor.putInt("incorrectAnswers", incorrectAnswers);
        editor.putString("skippedQuestions", skippedQuestions);
        editor.apply();

        FirebaseMethod firebaseMethod = new FirebaseMethod();
        firebaseMethod.saveProgressToDatabase(fileID,answeredToSave.trim(),progressBarStatus,
                currentQuestion,false,"",0,
                incorrectlyAnsweredToSave.trim(),incorrectAnswers);

        Log.d(TAG, "saveProgressToPreferences Saved: answeredToSave "+answeredToSave.trim());
        Log.d(TAG, "saveProgressToPreferences Saved: incorrectlyAnsweredToSave "+incorrectlyAnsweredToSave.trim());
        Log.d(TAG, "saveProgressToPreferences Saved: progressBarStatus "+Double.toString(progressBarStatus));
        Log.d(TAG, "saveProgressToPreferences: Saved: currentQuestion "+ currentQuestion);
        Log.d(TAG, "saveProgressToPreferences: Saved: incorrectAnswers "+ incorrectAnswers);
        Log.d(TAG, "saveProgressToPreferences: Saved: skippedQuestions "+skippedQuestions);
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
        String incorrectlyAnsweredToSave = prefs.getString("incorrectlyAnswered","");
        String progressBarStatusString = prefs.getString("progressBarStatus","");
        int savedCurrentQuestion = prefs.getInt("currentQuestion",0);
        int savedIncorrectAnswers = prefs.getInt("incorrectAnswers", 0);
        String savedSkippedQuestions = prefs.getString("skippedQuestions", "");

        if (answeredQuestions.equals("") || progressBarStatusString.equals("")){
            Log.d(TAG, "retrieveProgressFromPreferences: progress data in prefs is empty. Nothing is saved on device.");
            return;
        }

        double progressBarStatusDouble = Double.parseDouble(progressBarStatusString);
        progressBarStatus = progressBarStatusDouble;

        if (savedCurrentQuestion!=-1){
            currentQuestion = savedCurrentQuestion;
            n1 = savedCurrentQuestion;
        }

        incorrectAnswers = savedIncorrectAnswers;

        if (!savedSkippedQuestions.equals(""))
            unAnsweredQuestions = Integer.parseInt(savedSkippedQuestions);

        String [] answeredQuestionsStringArray = answeredQuestions.split(" ");

        String [] incorrectlyAnsweredStringArray = incorrectlyAnsweredToSave.split("\n\n");


        for (int i=0;i<answeredQuestionsStringArray.length;i++){
            Log.d(TAG, "retrieveProgressFromPreferences: Answered String numbers in array: "+answeredQuestionsStringArray[i]);
        }

        for (int i=0;i<incorrectlyAnsweredStringArray.length;i++){
            Log.d(TAG, "retrieveProgressFromPreferences: Answered String numbers in array: "+incorrectlyAnsweredStringArray[i]);
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

        if (incorrectlyAnsweredStringArray.length != 0){
            ArrayList<String> incorrectlyAnsweredArrayList = new ArrayList<>();
            for (int i = 0; i<incorrectlyAnsweredStringArray.length;i++){
                if (!incorrectlyAnsweredStringArray[i].equals(""))
                    incorrectlyAnsweredArrayList.add(incorrectlyAnsweredStringArray[i]);
            }
            incorrectlyAnswered = incorrectlyAnsweredArrayList;
        }

        Log.d(TAG, "retrieveProgressFromPreferences: answered: "+answered);
        Log.d(TAG, "retrieveProgressFromPreferences: progressBarStatus: "+progressBarStatus);
        Log.d(TAG, "retrieveProgressFromPreferences: incorrectlyAnswered: "+ incorrectlyAnswered);
    }

    private void counterToReGenerate(){
        new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                View.OnClickListener nothing = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                };
                buttonA.setOnClickListener(nothing);
                buttonB.setOnClickListener(nothing);
                buttonC.setOnClickListener(nothing);
                buttonD.setOnClickListener(nothing);

            }

            @Override
            public void onFinish() {
                reGenerate();
            }
        }.start();
    }

    public void restartQuiz(View view){
        if (answered.size()!=readText().length && progressBar.getProgress()!=100){
            unAnsweredQuestions = readText().length-answered.size();
            incorrectAnswers += unAnsweredQuestions;
            progressBar.setProgress(100);
            progressBarStatus = 100.0;
            buttonA.setEnabled(false);
            buttonB.setEnabled(false);
            buttonC.setEnabled(false);
            buttonD.setEnabled(false);
            displayScore();
            submitBtn.setText("Restart");
            saveProgressToPreferences();
            return;
        }
        if (answered.size()==readText().length || progressBar.getProgress()==100){
            answered = new ArrayList<Integer>();

            if (!randomQuestions)
                currentQuestion = 0;

            progressBarStatus=0;
            progressBar.setProgress((int)(Math.round(progressBarStatus)));
            incorrectAnswers = 0;
            incorrectlyAnswered = new ArrayList<String>();
            submitBtn.setText("Submit Now");
            unAnsweredQuestions = 0;
            saveProgressToPreferences();
            feedbackTextView.setText("");
            reGenerate();
        }
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
                || !currentSettings.get("answerInputMode").equals(answerInputMethod)
                || !currentSettings.get("reversedOrder").equals(reversedOrderString)){
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
