package com.example.android.studyme.Files;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.studyme.Firebase.FirebaseMethod;
import com.example.android.studyme.Home.HomeActivity;
import com.example.android.studyme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.android.studyme.Home.UserFilesFragment.CURRENTLY_SELECTED_FILE;
import static com.example.android.studyme.Home.UserFilesFragment.savedFilesDirectory;

/**
 * Created by Backmod on 1/1/18.
 */

public class EditFileActivity extends AppCompatActivity{

    private static final String TAG = "EditFileActivity";

    private StorageReference mStorageRef;
    private Context mContext;

    private TextView editFileTv,deleteFileTv,shareFileTv;
    private EditText editFileEt;
    private Button editFileCancelBtn,editFileSaveBtn, no, yes;
    private CheckBox deleteFromStorage;
    private CheckBox addOrRemoveToStorage;
    private CheckBox makeFilePublic;
    private String pathOfFileSelected;
    private String fileID;
    private String nameOfFile;

    private boolean availability;
    private String answeredQuestions;
    private Double progressBarStatus;

    private boolean publicFile;

    private FirebaseAuth mAuth;

    private ImageView backArrowActionBar,editModeActionBar;
    private TextView activityNameActionBar;

    private CheckBox studyCheckBox,quizCheckBox,multipleChoiceCheckBox,textCheckBox;
    private Switch randomQuestionsSwitch,reverseOrderSwitch;
    private TextView cancelTextView,proceedTextView;

    private Map<String,String> currentSettings = new HashMap();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_file_activity);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mContext = EditFileActivity.this;
        getFromPreferences();
        editFileTv = (TextView) findViewById(R.id.editFileTv);
        editFileEt = (EditText) findViewById(R.id.editFileEt);
        deleteFileTv = (TextView) findViewById(R.id.deleteFileTV);
        shareFileTv = (TextView) findViewById(R.id.shareFileTV);
        editFileCancelBtn = (Button) findViewById(R.id.editFileCancelBtn);
        editFileSaveBtn = (Button) findViewById(R.id.editFileSaveBtn);
        addOrRemoveToStorage = (CheckBox) findViewById(R.id.uploadCheckBox);
        makeFilePublic = (CheckBox) findViewById(R.id.makePublicCheckBox);
        editFileEt.setText(readFile());
        editFileTv.setText(nameOfFile);

        backArrowActionBar = (ImageView) findViewById(R.id.backArrow);
        editModeActionBar = (ImageView) findViewById(R.id.editMode);
        activityNameActionBar = (TextView) findViewById(R.id.activityName);

        activityNameActionBar.setText("File Edit");
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
            }
        });

        if (availability)
            addOrRemoveToStorage.setText("Delete From Storage");

        if (publicFile)
            makeFilePublic.setVisibility(View.GONE);

        deleteFileTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.delete_file_prompt_dialog);


                TextView no = (TextView) dialog.findViewById(R.id.no);
                TextView yes = (TextView) dialog.findViewById(R.id.yes);
                deleteFromStorage = (CheckBox) dialog.findViewById(R.id.deleteFromStorageCb);

                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.show();

                yes.setText("Delete "+ nameOfFile);
                yes.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (savedFilesDirectory.isDirectory())
                            new File(pathOfFileSelected).delete();
                        Log.d(TAG, "deleteFile: File Deleted: "+ fileID);
                        if (deleteFromStorage.isChecked()){
                            FirebaseMethod firebaseMethod = new FirebaseMethod();
                            firebaseMethod.deleteFileStorage(fileID);
                            firebaseMethod.deleteFileDatabase(fileID);
                        }
                        updatePreferences(true);
                        Intent intent = new Intent(mContext, HomeActivity.class);
                        startActivity(intent);
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

        shareFileTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.share_file_dialog);

                final TextView no = (TextView) dialog.findViewById(R.id.no);
                final TextView yes = (TextView) dialog.findViewById(R.id.yes);
                final CheckBox shareProgressCheckBox = (CheckBox) dialog.findViewById(R.id.shareProgressCheckBox);
                final AutoCompleteTextView toNameOfUser = (AutoCompleteTextView) dialog.findViewById(R.id.toAutoCompleteTextVIew);

                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.show();

                yes.setText("Share "+ nameOfFile);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference myRef = database.getReference("users");

                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final List<String> usersName = new ArrayList<>();
                        final List<String> usersID = new ArrayList<>();

                        for (DataSnapshot snap:dataSnapshot.getChildren()){
                            if (!snap.getKey().equals(mAuth.getCurrentUser().getUid())){
                                String name = snap.child("user_info").child("name").getValue(String.class);
                                String ID = snap.getKey();
                                usersName.add(name);
                                usersID.add(ID);
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,android.R.layout.simple_list_item_1,usersName);
                        toNameOfUser.setAdapter(adapter);

                        yes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                String toUserID = "";
                                for (int i = 0; i<usersName.size();i++){
                                    if (usersName.get(i).equals(toNameOfUser.getText().toString().trim())){
                                        toUserID = usersID.get(i);
                                        Log.d(TAG, "onClick: toUserID = "+toUserID);
                                    }
                                }

                                if (!toNameOfUser.getText().toString().equals("")){

                                    if (!toUserID.equals("")) {

                                        FirebaseMethod firebaseMethod = new FirebaseMethod();

                                        if (shareProgressCheckBox.isChecked()) {
                                            firebaseMethod.shareFileDatabase(fileID, nameOfFile, toUserID, true);
                                            dialog.dismiss();
                                        }
                                        else {
                                            firebaseMethod.shareFileDatabase(fileID, nameOfFile, toUserID, false);
                                            dialog.dismiss();
                                        }

                                        Toast.makeText(mContext,"Shared",Toast.LENGTH_SHORT).show();

                                        SharedPreferences.Editor prefs = getSharedPreferences(fileID,MODE_PRIVATE).edit();
                                        prefs.putBoolean("sharedFile",true);
                                        prefs.apply();

                                    }
                                    else
                                        Toast.makeText(mContext,"This User does not Exist",Toast.LENGTH_SHORT).show();
                                }
                                else
                                    Toast.makeText(mContext,"Field cannot be left empty",Toast.LENGTH_SHORT).show();
                            }
                        });

                        no.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    public String readFile () {
        EntryFiles currentFile = new EntryFiles();
        return currentFile.readFile(pathOfFileSelected);
    }

    private void getFromPreferences(){
        /*
        This method checks for the currently selected file. It retrieves that info from prefrernces
         */
        SharedPreferences prefsFileSelected1 = getSharedPreferences(CURRENTLY_SELECTED_FILE, MODE_PRIVATE);

        fileID = prefsFileSelected1.getString("fileID", "");
        pathOfFileSelected = prefsFileSelected1.getString("pathOfFileSelected","");
        Log.d(TAG, "onCreate: Selected file info: "+ fileID +" "+pathOfFileSelected);

        SharedPreferences prefsFileSelected2 = getSharedPreferences(fileID, MODE_PRIVATE);

        nameOfFile = prefsFileSelected2.getString("nameOfFile","Unnamed File");

        availability = prefsFileSelected2.getBoolean("availability",true);
        answeredQuestions = prefsFileSelected2.getString("answeredQuestions","");
        publicFile = prefsFileSelected2.getBoolean("publicFile",false);

        //progressBarStatus = Double.parseDouble(prefsFileSelected2.getString("progressBarStatus","0.0"));
        String progressBarStatusString = prefsFileSelected2.getString("progressBarStatus","0.0");
        if (progressBarStatusString.equals("")){
            progressBarStatusString = "0.0";
        }
        progressBarStatus = Double.parseDouble(progressBarStatusString);

    }

    private void updatePreferences(boolean reset){
        /*
        This method resets the user's progress to 0 when the entries in the file are updated.
         */
        SharedPreferences.Editor editor1 = getSharedPreferences(fileID, MODE_PRIVATE).edit();
        if (reset){
            editor1.putString("answeredQuestions","");
            editor1.putString("progressBarStatus","0.0");
        }
        if (availability)
            editor1.putBoolean("availability",true);
        else
            editor1.putBoolean("availability",false);
        editor1.apply();
        Log.d(TAG, "updatePreferences: Progress Has been reset");

        SharedPreferences.Editor editor2 = getSharedPreferences(CURRENTLY_SELECTED_FILE, MODE_PRIVATE).edit();
        editor2.putString("fileID", "");
        editor2.putString("pathOfFileSelected", "");
        editor2.apply();
        Log.d(TAG, "updatePreferences: File Selection reset to nothing");
    }

    public void goBack(View view){
        finish();
    }

    public void saveFile(View view){
        /*
        This method checks if any changes have been made then updates the file with the file path from
        preferences.
         */

        EntryFiles currentFile = new EntryFiles();

        if (editFileEt.getText().toString().trim().equals(readFile())){
            if (!addOrRemoveToStorage.isChecked() && !makeFilePublic.isChecked()){
                Toast.makeText(getBaseContext(),"No Changes Were Made", Toast.LENGTH_SHORT).show();
                finish();
            }
            else if (makeFilePublic.isChecked() && !addOrRemoveToStorage.isChecked()){
                Toast.makeText(mContext,"Making File Public",Toast.LENGTH_SHORT).show();

                FirebaseMethod firebaseMethod = new FirebaseMethod();
                firebaseMethod.makeFilePublicDatabase(fileID,nameOfFile);

                SharedPreferences.Editor prefs = getSharedPreferences(fileID,MODE_PRIVATE).edit();
                prefs.putBoolean("publicFile",true);
                prefs.apply();

                Intent intent = new Intent(mContext, HomeActivity.class);
                startActivity(intent);
            }
            else if (!availability && addOrRemoveToStorage.isChecked()){
                FirebaseMethod firebaseMethod = new FirebaseMethod();
                firebaseMethod.uploadFileFirebase(pathOfFileSelected, fileID);

                availability = true;
                updatePreferences(false); //to update the fact that it's now available but to keep progress.
                Toast.makeText(getBaseContext(),"Uploading To Storage", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "saveFile: Added to Storage and Database");
                Intent intent = new Intent(mContext, HomeActivity.class);
                startActivity(intent);
            }
            else if (availability && addOrRemoveToStorage.isChecked()){
                FirebaseMethod firebaseMethod = new FirebaseMethod();
                firebaseMethod.deleteFileStorage(fileID);
                firebaseMethod.deleteFileDatabase(fileID);
                availability = false;
                updatePreferences(false); //to update the fact that it's no longer available but to keep progress.
                Toast.makeText(getBaseContext(),"Deleting From Storage", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "saveFile: Deleted From Storage and Database");
                Intent intent = new Intent(mContext, HomeActivity.class);
                startActivity(intent);
            }
        }

        else if (!currentFile.textCheck(editFileEt.getText().toString())){
//            Toast.makeText(getBaseContext(),"There must be a minimum of 4 entries. Each Entry Must Have a Definition and Separated by ':'", Toast.LENGTH_LONG).show();
            Toast.makeText(getBaseContext(),"Text Format is wrong", Toast.LENGTH_SHORT).show();
        }
        else if (!editFileEt.getText().toString().trim().equals(readFile()) && currentFile.textCheck(editFileEt.getText().toString())){
            currentFile.saveFile(pathOfFileSelected,editFileEt.getText().toString());
            if (!availability && addOrRemoveToStorage.isChecked()){
                FirebaseMethod firebaseMethod = new FirebaseMethod();
                firebaseMethod.uploadFileFirebase(pathOfFileSelected, fileID);
                firebaseMethod.saveProgressToDatabase(fileID,"",0.0,
                        0,false,"",0,
                        "",0);
                availability = true;
                Log.d(TAG, "saveFile: Added to Storage and Database");
            }
            if (availability && addOrRemoveToStorage.isChecked()){
                FirebaseMethod firebaseMethod = new FirebaseMethod();
                firebaseMethod.deleteFileStorage(fileID);
                firebaseMethod.deleteFileDatabase(fileID);
                availability = false;
                Log.d(TAG, "saveFile: Deleted From Storage and Database");
            }
            updatePreferences(true); //to update its availability and to reset the progress.
            Intent intent = new Intent(mContext, HomeActivity.class);
            startActivity(intent);
        }
    }

    private boolean textCheck(){
        /*
        This method checks each line in the edittext to make sure it has ':'. It returns false if any
        of the line does not.
         */
        String [] text = editFileEt.getText().toString().split("\r\n|\r|\n");
        for (int i=0;i<text.length;i++){
            if (!text[i].contains(":"))
                return false;
        }
        return true;
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

        String nameOfFile = prefsFileSelected.getString("nameOfFile","unnamedFile");
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

                SharedPreferences prefsFileSelected = getSharedPreferences(fileID, MODE_PRIVATE);

                String nameOfFile = prefsFileSelected.getString("nameOfFile","unnamedFile");
                String learningMode = prefsFileSelected.getString("learningMode","");
                String answerInputMode = prefsFileSelected.getString("answerInputMethod","");
                boolean randomQuestions = prefsFileSelected.getBoolean("randomQuestions",false);
                boolean reversedOrder = prefsFileSelected.getBoolean("reversedOrder",false);


                if (checkIfChangeWasMade()){
                    FirebaseMethod firebaseMethod = new FirebaseMethod();
                    firebaseMethod.saveFileModeToDatabse(fileID,nameOfFile,learningMode,answerInputMode,randomQuestions,reversedOrder);
                    Toast.makeText(mContext,"File Mode Changed",Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
                else
                    Toast.makeText(mContext,"No Changes Were Made",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean checkIfChangeWasMade() {

        SharedPreferences prefs = getSharedPreferences(fileID, MODE_PRIVATE);
        String learningMode = prefs.getString("learningMode", "Study");
        String answerInputMethod = prefs.getString("answerInputMethod", "MultipleChoice");
        boolean randomQuestions = prefs.getBoolean("randomQuestions",false);

        String randomQuestionsString = "";

        if (randomQuestions)
            randomQuestionsString = "true";
        else
            randomQuestionsString = "false";

        if (currentSettings.get("learningMode").equals(learningMode)
                && currentSettings.get("answerInputMode").equals(answerInputMethod)
                && currentSettings.get("randomQuestions").equals(randomQuestionsString)){
            return false;
        }

        return true;
    }


}
