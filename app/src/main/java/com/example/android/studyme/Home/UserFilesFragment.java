package com.example.android.studyme.Home;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.studyme.Files.EditFileActivity;
import com.example.android.studyme.Files.UserFilesRow;
import com.example.android.studyme.Files.NewFileActivity;
import com.example.android.studyme.Files.UserFilesRowListAdapter;
import com.example.android.studyme.Firebase.FirebaseMethod;
import com.example.android.studyme.QuizMode.QuizMultipleChoiceActivity;
import com.example.android.studyme.QuizMode.QuizTextActivity;
import com.example.android.studyme.R;
import com.example.android.studyme.StudyMode.StudyMultipleChoiceActivity;
import com.example.android.studyme.StudyMode.StudyTextActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Backmod on 1/27/18.
 */

public class UserFilesFragment extends Fragment{

    private static final String TAG = "UserFilesFragment";

    private FirebaseAuth mAuth;
    private Context mContext;
    private ListView filesListView;
    private ProgressBar progressBarListView;
    private TextView loadingFilesTextView;
    private TextView noFilesTextView;
    private TextView newFile;

    public static File savedFilesDirectory;

    public static final String CURRENTLY_SELECTED_FILE = "MyPreferenceFile";


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_files_fragment,container,false);


        filesListView = (ListView) view.findViewById(R.id.filesListView);
        progressBarListView = (ProgressBar) view.findViewById(R.id.progressBarListView);
        loadingFilesTextView = (TextView) view.findViewById(R.id.loadingFilesTextView);
        noFilesTextView = (TextView) view.findViewById(R.id.noFilesTextView);
        newFile = (TextView)  view.findViewById(R.id.newFile);

        mContext = this.getActivity();
        mAuth = FirebaseAuth.getInstance();

        savedFilesDirectory = mContext.getDir("myfiles", MODE_PRIVATE);

        if (savedFilesDirectory.list().length==0){
            readFromDatabase();
            progressBarListView.setVisibility(View.VISIBLE);
            loadingFilesTextView.setVisibility(View.VISIBLE);
        }

        else {
            Log.d(TAG, "onCreateView: number of files "+savedFilesDirectory.list().length);
            filesListView.setAdapter(new UserFilesRowListAdapter(mContext,R.layout.user_files_row_layout,new ArrayList<UserFilesRow>()));
            new MyTask().execute();
        }

        newFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, NewFileActivity.class);
                startActivity(intent);
            }
        });

        Log.d(TAG, "onCreateView: Done!");
        return view;
    }


    class MyTask extends AsyncTask<Void,UserFilesRow,Void> {

        private static final String TAG = "MyTask";

        UserFilesRowListAdapter adapter;

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute: Reading From local directory");
            adapter = (UserFilesRowListAdapter)filesListView.getAdapter();
            progressBarListView.setVisibility(View.VISIBLE);
            loadingFilesTextView.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
//            Toast.makeText(mContext,"Done",Toast.LENGTH_SHORT).show();
            progressBarListView.setVisibility(View.GONE);
            loadingFilesTextView.setVisibility(View.GONE);
            displayFiles(getArrayList(),false);
        }

        @Override
        protected void onProgressUpdate(UserFilesRow... values) {
            adapter.add(values[0]);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            for (UserFilesRow Row : getArrayList()){
                publishProgress(Row);
            }

            return null;
        }
    }

    public void displayFiles(final ArrayList<UserFilesRow> rows, boolean fromDatabase) {
        /*
        Method creates an array list with UserFilesRow type objects. It displays all the files downloaded
        or created. When a user taps on the file in the listview, they're taken to StudyMultipleChoiceActivity. When the user long taps
        on the file in the listview, they're taken to EditFileActivity.
         */

//        filesListView.setAdapter(new UserFilesRowListAdapter(mContext,R.layout.user_files_row_layout,new ArrayList<UserFilesRow>()));


        if (fromDatabase){
            UserFilesRowListAdapter adapter = new UserFilesRowListAdapter(mContext,R.layout.user_files_row_layout,rows);

            filesListView.setAdapter(adapter);
        }

        filesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //String nameOfFile = filesListView.getItemAtPosition(i).toString();
                String nameOfFile = rows.get(i).getNameOfFile();
                Log.d(TAG, "onItemClick: nameOfFile: "+nameOfFile);

                String fileID = rows.get(i).getFileID();
                Log.d(TAG, "onItemClick: fileID: "+fileID);

                /*
                This edits the preferences so that whenever StudyMultipleChoiceActivity is launched without selecting a file,
                the file that was previously selected is read in StudyMultipleChoiceActivity.
                 */
                SharedPreferences.Editor editor = mContext.getSharedPreferences(CURRENTLY_SELECTED_FILE, MODE_PRIVATE).edit();

                editor.putString("fileID",fileID);
                editor.putString("nameOfFile", nameOfFile);
                editor.putString("pathOfFileSelected", savedFilesDirectory.getPath() + "/" + fileID + ".txt");
                editor.putString("pathOfDirectory", savedFilesDirectory.getPath());
                editor.apply();

                SharedPreferences prefs = mContext.getSharedPreferences(fileID, MODE_PRIVATE);
                String learningMode = prefs.getString("learningMode", "Study");
                String answerInputMethod = prefs.getString("answerInputMethod", "MultipleChoice");

                if (learningMode.equals("Study") && answerInputMethod.equals("MultipleChoice")){
                    Intent intent = new Intent(mContext,StudyMultipleChoiceActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
//                    overridePendingTransition(0, 0);
                }
                if (learningMode.equals("Study") && answerInputMethod.equals("Text")){
                    Intent intent = new Intent(mContext,StudyTextActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
//                    overridePendingTransition(0, 0);
                }
                if (learningMode.equals("Quiz") && answerInputMethod.equals("MultipleChoice")){
                    Intent intent = new Intent(mContext,QuizMultipleChoiceActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
//                    overridePendingTransition(0, 0);
                }
                if (learningMode.equals("Quiz") && answerInputMethod.equals("Text")){
                    Intent intent = new Intent(mContext,QuizTextActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
//                    overridePendingTransition(0, 0);
                }
//                Intent intent = new Intent(mContext, StudyTextActivity.class);
//                startActivity(intent);
//                overridePendingTransition(0, 0);
            }
        });
        filesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Vibrator vibe = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(30);
                //String fileID = filesListView.getItemAtPosition(i).toString();
                String fileID = rows.get(i).getFileID();

                SharedPreferences.Editor editor = mContext.getSharedPreferences(CURRENTLY_SELECTED_FILE, MODE_PRIVATE).edit();
                editor.putString("fileID", fileID);
                editor.putString("pathOfFileSelected", savedFilesDirectory.getPath() + "/" + fileID + ".txt");
                editor.putString("pathOfDirectory", savedFilesDirectory.getPath());
                editor.apply();
                Intent intent = new Intent(mContext, EditFileActivity.class);
                startActivity(intent);

                return true;
            }
        });
    }

    public ArrayList<UserFilesRow> getArrayList() {
        /*
        Method checks if the file directory is empty. If not, it gets a list of the files in the directory,
        capitalizes the first letter and removes the file extension type(.txt). It creates an array list with
        UserFilesRow type objects in which it adds the names of the files in the directory, and the progress
        which is retrieved from preferences.
         */
        if (savedFilesDirectory != null) {
            String[] result = savedFilesDirectory.list();
            ArrayList<UserFilesRow> rows = new ArrayList<>();
            for (int i = 0; i < result.length; i++) {

                Log.d(TAG, "getArray: file in array: " + result[i]); //result[i] is the fileID;

                int wordLength = result[i].length();
                String fileID = result[i].substring(0, wordLength - 4); //removing ".txt"

                SharedPreferences prefs = mContext.getSharedPreferences(fileID, MODE_PRIVATE);

                String nameOfFile = prefs.getString("nameOfFile", "Unnamed File");
                String progressBarStatusString = prefs.getString("progressBarStatus","0");
                boolean availability = prefs.getBoolean("availability",true);

                String learningMode = prefs.getString("learningMode", "");
                String answerInputMethod = prefs.getString("answerInputMethod", "");
                boolean randomQuestions = prefs.getBoolean("randomQuestions", false);

                boolean sharedFile = prefs.getBoolean("sharedFile",false);
                boolean publicFile = prefs.getBoolean("publicFile",false);

                Log.d(TAG, "getArrayList: progressBarStatus "+ progressBarStatusString);
                Log.d(TAG, "getArrayList: availability "+ availability);
                Log.d(TAG, "getArrayList: learningMode "+ learningMode);
                Log.d(TAG, "getArrayList: answerInputMethod "+ answerInputMethod);
                Log.d(TAG, "getArrayList: randomQuestions "+randomQuestions);



                Log.d(TAG, "getArrayList: nameOfFile:" +nameOfFile);
                Log.d(TAG, "getArrayList: availability "+ availability);

                if (progressBarStatusString.equals(""))
                    progressBarStatusString="0";

                double progressBarStatus = Double.parseDouble(progressBarStatusString);
                int percentage = (int) Math.round(progressBarStatus);

                UserFilesRow row = new UserFilesRow(fileID,nameOfFile,progressBarStatus,percentage,availability,
                        learningMode,answerInputMethod,randomQuestions,sharedFile,publicFile);
                rows.add(row);
            }
            return rows;
        }
        Log.d(TAG, "getArray: savedFilesDirectory is null");
        return null;
    }

    public void readFromDatabase(){
        Log.d(TAG, "readFromDatabase: Reading From Database");
        if (mAuth.getCurrentUser()==null)
            return;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("users").
                child(mAuth.getCurrentUser().getUid()).child("files");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    progressBarListView.setVisibility(View.GONE);
                    loadingFilesTextView.setVisibility(View.GONE);
                    Log.d(TAG, "onDataChange: Does not exist");
                    noFilesTextView.setVisibility(View.VISIBLE);
                    return;
                }

                ArrayList<UserFilesRow> rows = new ArrayList<>();

                for (DataSnapshot snap:dataSnapshot.getChildren()){
                    String fileID = snap.getKey();
                    String nameOfFile = snap.child("name_of_file").getValue(String.class);

                    boolean sharedFile = false;
                    boolean publicFile = false;

                    if (snap.child("shared_with").exists())
                        sharedFile = true;
                    if (snap.child("public").exists()) {
                        String publicFileString = snap.child("public").getValue(String.class);
                        if (publicFileString.equals("true"))
                            publicFile = true;
                    }

                    DataSnapshot progress = snap.child("progress");

                    Double progressBarStatus = 0.0;
                    String answered = "";
                    int currentQuestion = 0;
                    String goneThroughAllQuestionsString = "false";
                    String skippedQuestions = "";
                    String currentAnswer = "";
                    int attempts = 0;
                    String incorrectlyAnswered = "";
                    int incorrectAnswers = 0;
                    boolean goneThroughAllQuestions = false;

                    if (progress.exists()){

                        progressBarStatus = Double.parseDouble(progress.child("progress_bar").getValue(String.class));
                        answered = progress.child("answered").getValue(String.class);
                        currentQuestion = progress.child("current_question").getValue(Integer.class);
                        goneThroughAllQuestionsString = progress.child("gone_through_all_entries").getValue(String.class);
                        skippedQuestions = progress.child("skipped_questions").getValue(String.class);
                        currentAnswer = progress.child("current_answer").getValue(String.class);
                        attempts = progress.child("attempts").getValue(Integer.class);
                        incorrectlyAnswered = progress.child("incorrectly_answered").getValue(String.class);
                        incorrectAnswers = progress.child("incorrect_answers").getValue(Integer.class);

                        if (goneThroughAllQuestionsString.equals("true"))
                            goneThroughAllQuestions = true;

                    }

                    DataSnapshot fileMode = snap.child("file_mode");

                    String learningMode = fileMode.child("learning_mode").getValue(String.class);
                    String answerInputMethod = fileMode.child("answer_input_method").getValue(String.class);
                    String randomQuestionsString = fileMode.child("random_questions").getValue(String.class);


                    boolean randomQuestions;

                    if (randomQuestionsString.equals("true"))
                        randomQuestions = true;
                    else
                        randomQuestions = false;

                    Log.d(TAG, "onDataChange: sharedFile "+sharedFile);
                    Log.d(TAG, "onDataChange: publicFile "+publicFile);
                    Log.d(TAG, "onDataChange: fileID "+fileID);
                    Log.d(TAG, "onDataChange: progressBarStatus "+progressBarStatus);
                    Log.d(TAG, "onDataChange: answered "+answered);
                    Log.d(TAG, "onDataChange: currentQuestion "+currentQuestion);
                    Log.d(TAG, "onDataChange: goneThroughAllQuestions "+goneThroughAllQuestions);
                    Log.d(TAG, "onDataChange: skippedQuestions "+skippedQuestions);
                    Log.d(TAG, "onDataChange: currentAnswer "+currentAnswer);
                    Log.d(TAG, "onDataChange: attempts "+attempts);
                    Log.d(TAG, "onDataChange: incorrectlyAnswered "+incorrectlyAnswered);
                    Log.d(TAG, "onDataChange: incorrectAnswers "+incorrectAnswers);

                    int percentage = (int) Math.round(progressBarStatus);

                    UserFilesRow row = new UserFilesRow(fileID,nameOfFile,progressBarStatus,percentage,true,
                            learningMode,answerInputMethod,randomQuestions,sharedFile,publicFile);
                    rows.add(row);

                    FirebaseMethod firebaseMethod = new FirebaseMethod();
                    firebaseMethod.downloadUserFileFromStorage(fileID);

                    SharedPreferences.Editor editor = mContext.getSharedPreferences(fileID, MODE_PRIVATE).edit();

                    editor.putString("nameOfFile",nameOfFile);

                    editor.putString("answeredQuestions", answered);
                    editor.putString("progressBarStatus", Double.toString(progressBarStatus));
                    editor.putInt("currentQuestion", currentQuestion);
                    editor.putBoolean("goneThroughAllQuestions", goneThroughAllQuestions);
                    editor.putString("skippedQuestions", skippedQuestions);
                    editor.putString("currentAnswer", currentAnswer);
                    editor.putInt("attempts", attempts);
                    editor.putString("incorrectlyAnswered", incorrectlyAnswered);
                    editor.putInt("incorrectAnswers", incorrectAnswers);

                    editor.putString("learningMode", learningMode);
                    editor.putString("answerInputMethod", answerInputMethod);
                    editor.putBoolean("randomQuestions", randomQuestions);

                    editor.putBoolean("sharedFile",sharedFile);
                    editor.putBoolean("publicFile",publicFile);

                    editor.apply();

                }
                displayFiles(rows,true);
                progressBarListView.setVisibility(View.GONE);
                loadingFilesTextView.setVisibility(View.GONE);

                //Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }


}
