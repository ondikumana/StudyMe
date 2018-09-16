package com.example.android.studyme.Home;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.studyme.Files.EntryFiles;
import com.example.android.studyme.Files.SharedFilesRow;
import com.example.android.studyme.Files.SharedFilesRowListAdapter;
import com.example.android.studyme.Firebase.FirebaseMethod;
import com.example.android.studyme.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Backmod on 1/28/18.
 */

public class SharedFilesFragment extends Fragment {

    private static final String TAG = "SharedFilesFragment";

    private FirebaseAuth mAuth;
    private Context mContext;
    private ListView filesListView;
    private ProgressBar progressBarListView;
    private TextView loadingFilesTextView;
    private TextView noFilesTextView;

    public static File sharedFilesDirectory;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: started");

        View view = inflater.inflate(R.layout.shared_files_fragment,container,false);

        filesListView = (ListView) view.findViewById(R.id.filesListView);
        progressBarListView = (ProgressBar) view.findViewById(R.id.progressBarListView);
        loadingFilesTextView = (TextView) view.findViewById(R.id.loadingFilesTextView);
        noFilesTextView = (TextView) view.findViewById(R.id.noFilesTextView);

        mContext = this.getActivity();
        mAuth = FirebaseAuth.getInstance();

        sharedFilesDirectory = mContext.getDir("sharedFiles", Context.MODE_PRIVATE);

        readFromDatabase();

        onSharedFilesDatabaseChange();

        return view;
    }

    public void readFromDatabase(){

        progressBarListView.setVisibility(View.VISIBLE);
        loadingFilesTextView.setVisibility(View.VISIBLE);

        Log.d(TAG, "readFromDatabase: Reading From Database");
        if (mAuth.getCurrentUser()==null)
            return;

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("users").
                child(mAuth.getCurrentUser().getUid()).child("shared_files");


        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                noFilesTextView.setVisibility(View.GONE);

                if (!dataSnapshot.exists()){
                    progressBarListView.setVisibility(View.GONE);
                    loadingFilesTextView.setVisibility(View.GONE);
                    Log.d(TAG, "onDataChange: Does not exist");
                    noFilesTextView.setVisibility(View.VISIBLE);

                    ArrayList<SharedFilesRow> rows = new ArrayList<>();
                    displayFiles(rows);
                    return;
                }

                ArrayList<SharedFilesRow> rows = new ArrayList<>();
                Log.d(TAG, "onDataChange: arrayCreated");

                for (DataSnapshot snap:dataSnapshot.getChildren()){

                    String fileID = snap.getKey();
                    String nameOfFile = snap.child("name_of_file").getValue(String.class);
                    String fromUserID = snap.child("from").getValue(String.class);
                    DataSnapshot progress = snap.child("progress");
                    DataSnapshot fileMode = snap.child("file_mode");

                    Double progressBarStatus = 0.0;
                    String answered = "";
                    int currentQuestion = 0;
                    String goneThroughAllQuestionsString = "false";
                    String skippedQuestions = "";
                    String currentAnswer = "";
                    int attempts = 0;
                    String incorrectlyAnswered = "";
                    int incorrectAnswers = 0;

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
                    }
                    else {

                    }

                    String learningMode = fileMode.child("learning_mode").getValue(String.class);
                    String answerInputMethod = fileMode.child("answer_input_method").getValue(String.class);
                    String randomQuestionsString = fileMode.child("random_questions").getValue(String.class);

                    Log.d(TAG, "onDataChange: randomQuestionsString "+randomQuestionsString);

                    boolean goneThroughAllQuestions;
                    boolean randomQuestions;

                    if (goneThroughAllQuestionsString.equals("true"))
                        goneThroughAllQuestions = true;
                    else
                        goneThroughAllQuestions = false;
                    if (randomQuestionsString.equals("true"))
                        randomQuestions = true;
                    else
                        randomQuestions = false;

                    Log.d(TAG, "onDataChangeSharedFile: fileID "+fileID);
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

                    SharedFilesRow row = new SharedFilesRow(fromUserID,fileID,nameOfFile,progressBarStatus,percentage,learningMode,answerInputMethod,randomQuestions);
                    rows.add(row);

                }
                displayFiles(rows);
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

    public void displayFiles(final ArrayList<SharedFilesRow> rows) {

        SharedFilesRowListAdapter adapter = new SharedFilesRowListAdapter(mContext,R.layout.shared_files_row_layout,rows);

        filesListView.setAdapter(adapter);

        filesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                final String fileID = rows.get(i).getFileID();
                final String nameOfFile = rows.get(i).getNameOfFile();
                final String fromUserID = rows.get(i).getFromUserID();
                final String pathOfFileSelected = sharedFilesDirectory.getPath() + "/" + fileID + ".txt";

                Log.d(TAG, "onItemClick: nameOfFile: "+nameOfFile);

                final Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.shared_file_content_dialog);


                TextView fileNameTextView = (TextView) dialog.findViewById(R.id.nameOfFileTextView);
                final TextView contentTextView = (TextView) dialog.findViewById(R.id.contentTextView);
                TextView no = (TextView) dialog.findViewById(R.id.no);
                TextView addToMyFiles = (TextView) dialog.findViewById(R.id.addToMyFilesTextView);
                final TextView remove = (TextView) dialog.findViewById(R.id.removeTexTView);
                final ProgressBar progressBarContent = (ProgressBar) dialog.findViewById(R.id.progressBarContent);
                final ScrollView scrollView = (ScrollView) dialog.findViewById(R.id.scrollView);

                progressBarContent.setVisibility(View.VISIBLE);

                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        remove.setText("Confirm");
                        remove.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (sharedFilesDirectory.isDirectory())
                                    new File(pathOfFileSelected).delete();
                                FirebaseMethod firebaseMethod = new FirebaseMethod();
                                firebaseMethod.deleteSharedFileDatabase(nameOfFile);
                                Toast.makeText(mContext,"Removed",Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                    }
                });

                fileNameTextView.setText(nameOfFile);

                if (new File(pathOfFileSelected).exists()){
                    progressBarContent.setVisibility(View.GONE);

                    EntryFiles entryFiles = new EntryFiles();
                    String contentString = entryFiles.readFile(pathOfFileSelected);
                    contentTextView.setText(contentString);

                    if(contentString.split("\r\n|\r|\n").length < 20){
                        scrollView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    }
                }
                else {
                    final String nameOfFileToDownload = fileID + ".txt";

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReferenceFromUrl("gs://studyme-dbc63.appspot.com/Users/").child(fromUserID).child("Files");
                    StorageReference islandRef = storageRef.child(nameOfFileToDownload);

                    File localFile = null;
                    String path = "";
                    String savedFileName1 = "";
                    try {
                        localFile = File.createTempFile("data", ".txt");
                        path = localFile.getPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final String filePath = path;
                    final String savedFileName2 = savedFileName1;

                    islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Log.d(TAG, "onSuccess: File Created Successfully " + filePath);
                            FirebaseMethod firebaseMethod = new FirebaseMethod();
                            firebaseMethod.moveSharedFile(nameOfFileToDownload,filePath,savedFileName2);

                            progressBarContent.setVisibility(View.GONE);

                            EntryFiles entryFiles = new EntryFiles();
                            String contentString = entryFiles.readFile(pathOfFileSelected);
                            contentTextView.setText(contentString);

                            if(contentString.split("\r\n|\r|\n").length < 20){
                                scrollView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.e(TAG, "onFailure: File was not Created" + exception);
                            //Toast.makeText(mContext, "File Does not Exist. Make Sure the Name of the File is correct", Toast.LENGTH_SHORT).show();
                            // Handle any errors
                        }
                    });
                }


                addToMyFiles.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseMethod firebaseMethod = new FirebaseMethod();

                        firebaseMethod.addSharedToUserFilesDatabase(fileID);

                        firebaseMethod.downloadSharedFileFromStorage(fileID, fromUserID);

                        retrieveAndSetPreferencesFromDatabase(fileID);

                        firebaseMethod.deleteSharedFileDatabase(fileID);

                        Toast.makeText(mContext,"Added",Toast.LENGTH_SHORT).show();

                        dialog.dismiss();
                    }
                });


                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.show();

            }
        });

    }

    public void onSharedFilesDatabaseChange(){
        Log.d(TAG, "readFromDatabase: Reading From Database");
        if (mAuth.getCurrentUser()==null)
            return;

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("users").
                child(mAuth.getCurrentUser().getUid()).child("shared_files");
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                /*
                Added a wait of 50 milliseconds because readFromDatabase reads the firebase database
                 quickly. seconds before the file has been fully put in the database.
                 */
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                readFromDatabase();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                readFromDatabase();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void retrieveAndSetPreferencesFromDatabase(String fileID){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("users").
                child(mAuth.getCurrentUser().getUid()).child("files").child(fileID);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {

                String fileID = snap.getKey();
                String nameOfFile = snap.child("name_of_file").getValue(String.class);

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

                }

                DataSnapshot fileMode = snap.child("file_mode");

                String learningMode = fileMode.child("learning_mode").getValue(String.class);
                String answerInputMethod = fileMode.child("answer_input_method").getValue(String.class);
                String randomQuestionsString = fileMode.child("random_questions").getValue(String.class);

                boolean goneThroughAllQuestions;
                boolean randomQuestions;

                if (goneThroughAllQuestionsString.equals("true"))
                    goneThroughAllQuestions = true;
                else
                    goneThroughAllQuestions = false;
                if (randomQuestionsString.equals("true"))
                    randomQuestions = true;
                else
                    randomQuestions = false;

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
                editor.apply();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
