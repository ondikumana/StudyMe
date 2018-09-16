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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.android.studyme.Home.UserFilesFragment.savedFilesDirectory;

/**
 * Created by Backmod on 1/4/18.
 */

public class NewFileActivity extends AppCompatActivity {
    private static final String TAG = "NewFileActivity";

    private Context mContext;
    private FirebaseAuth mAuth;

    EditText nameOfFileEt,editFileEt;
    CheckBox uploadCheckBox;
    Button editFileCancelBtn,saveFileBtn;

    private ImageView backArrowActionBar,editModeActionBar;
    private TextView activityNameActionBar;

    private CheckBox studyCheckBox,quizCheckBox,multipleChoiceCheckBox,textCheckBox;
    private Switch randomQuestionsSwitch,reverseOrderSwitch;
    private TextView cancelTextView,proceedTextView,warningMessageTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_file_activity);

        mContext = NewFileActivity.this;
        mAuth = FirebaseAuth.getInstance();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        nameOfFileEt = (EditText) findViewById(R.id.nameOfFile);
        editFileEt = (EditText) findViewById(R.id.editFileEt);
        uploadCheckBox = (CheckBox) findViewById(R.id.uploadCheckBox);
        editFileCancelBtn = (Button) findViewById(R.id.editFileCancelBtn);
        saveFileBtn = (Button) findViewById(R.id.editFileSaveBtn);

        backArrowActionBar = (ImageView) findViewById(R.id.backArrow);
        editModeActionBar = (ImageView) findViewById(R.id.editMode);
        activityNameActionBar = (TextView) findViewById(R.id.activityName);

        editModeActionBar.setVisibility(View.GONE);
        activityNameActionBar.setText("New File");
        backArrowActionBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
    public void saveFile(View view){

        if (!checkIfValid())
            return;

        EntryFiles currentFile = new EntryFiles();

        if (!currentFile.textCheck(editFileEt.getText().toString())){
            Toast.makeText(getBaseContext(),"Text Format is Wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        String nameOfFile = currentFile.makePretty(nameOfFileEt.getText().toString().trim());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userFilesReference = database.getReference("users");
        String fileID = userFilesReference.push().getKey()+"_"+nameOfFile;

//        String pathOfFile = savedFilesDirectory.getPath()+"/"+nameOfFile.toLowerCase()+".txt";

        String pathOfFile = savedFilesDirectory.getPath()+"/"+fileID+".txt";
        Log.d(TAG, "saveFile: path of new file: "+pathOfFile);

        currentFile.saveFile(pathOfFile,editFileEt.getText().toString());
        if (uploadCheckBox.isChecked()){
            FirebaseMethod firebaseMethod = new FirebaseMethod();
//            firebaseMethod.uploadFileFirebase(pathOfFile,nameOfFile);
            firebaseMethod.uploadFileFirebase(pathOfFile,fileID);
        }

//        fileOptionsDialog(nameOfFile);
        fileOptionsDialog(fileID,nameOfFile);
    }

    public void goBack(View view){
//        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
//        startActivity(intent);
        finish();
    }
//    private void updatePreferencesAndDatabase(String nameOfFileSelected, String learningMode, String answerInputMethod, boolean randomQuestions){
//        /*
//        This method resets the user's progress to 0 when the entries in the new file are added.
//         */
//
//        SharedPreferences.Editor editor = getSharedPreferences(nameOfFileSelected, MODE_PRIVATE).edit();
//        editor.putString("answeredQuestions","");
//        editor.putString("progressBarStatus","0.0");
//        if (uploadCheckBox.isChecked())
//            editor.putBoolean("availability", true);
//        else
//            editor.putBoolean("availability",false);
//        editor.apply();
//        if (uploadCheckBox.isChecked()){
//            FirebaseMethod firebaseMethod = new FirebaseMethod();
////            firebaseMethod.saveProgressToDatabase(nameOfFileSelected,"",0.0,
////                    0,false,"",0,
////                    "",0); I don't think it necessary to set progress as the file is created before the file is even executed.
//            firebaseMethod.saveFileModeToDatabse(nameOfFileSelected,learningMode,answerInputMethod,randomQuestions);
//        }
//        Log.d(TAG, "updatePreferences: Progress Has Been Set to 0");
//    }

    private void updatePreferencesAndDatabase(String fileID, String nameOfFile, String learningMode,
                                              String answerInputMethod, boolean randomQuestions, boolean reverseOrder){
        /*
        This method resets the user's progress to 0 when the entries in the new file are added.
         */

        SharedPreferences.Editor editor = getSharedPreferences(fileID, MODE_PRIVATE).edit();

        editor.putString("answeredQuestions","");
        editor.putString("progressBarStatus","0.0");
        if (uploadCheckBox.isChecked())
            editor.putBoolean("availability", true);
        else
            editor.putBoolean("availability",false);
        editor.apply();
        if (uploadCheckBox.isChecked()){
            FirebaseMethod firebaseMethod = new FirebaseMethod();
//            firebaseMethod.saveProgressToDatabase(fileID,"",0.0,
//                    0,false,"",0,
//                    "",0); I don't think it necessary to set progress as the file is created before the file is even executed.
            firebaseMethod.saveFileModeToDatabse(fileID,nameOfFile,learningMode,answerInputMethod,randomQuestions,reverseOrder);
        }
        Log.d(TAG, "updatePreferences: Progress Has Been Set to 0");
    }

    private boolean checkIfValid(){
        if (nameOfFileEt.getText().toString().trim().equals("")||editFileEt.getText().toString().trim().equals("")){
            Toast.makeText(getApplicationContext(),"Fields cannot be left empty",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

//    public void fileOptionsDialog(final String nameOfFile){
//        final Dialog dialog = new Dialog(mContext);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.file_options_dialog);
//
//        studyCheckBox = (CheckBox) dialog.findViewById(R.id.studyCheckBox);
//        quizCheckBox = (CheckBox) dialog.findViewById(R.id.quizCheckBox);
//        multipleChoiceCheckBox = (CheckBox) dialog.findViewById(R.id.multipleChoiceCheckBox);
//        textCheckBox = (CheckBox) dialog.findViewById(R.id.textCheckBox);
//        randomQuestionsSwitch = (Switch) dialog.findViewById(R.id.randomQuestionsSwitch);
//        cancelTextView = (TextView) dialog.findViewById(R.id.no);
//        proceedTextView = (TextView) dialog.findViewById(R.id.yes);
//        warningMessageTextView = (TextView) dialog.findViewById(R.id.warningMessage);
//
//        warningMessageTextView.setVisibility(View.GONE);
//
//        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        dialog.show();
//
//        SharedPreferences prefsFileSelected = getSharedPreferences(nameOfFile, MODE_PRIVATE);
//
//        final String learningMode = prefsFileSelected.getString("learningMode","");
//        final String answerInputMode = prefsFileSelected.getString("answerInputMethod","");
//        final boolean randomQuestions = prefsFileSelected.getBoolean("randomQuestions",false);
//
//        if (learningMode.equals("Study"))
//            studyCheckBox.setChecked(true);
//        if (learningMode.equals("Quiz"))
//            quizCheckBox.setChecked(true);
//        if (answerInputMode.equals("MultipleChoice"))
//            multipleChoiceCheckBox.setChecked(true);
//        if (answerInputMode.equals("Text"))
//            textCheckBox.setChecked(true);
//        if (randomQuestions)
//            randomQuestionsSwitch.setChecked(true);
//
//        studyCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                quizCheckBox.setChecked(false);
//            }
//        });
//        quizCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                studyCheckBox.setChecked(false);
//            }
//        });
//        multipleChoiceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                textCheckBox.setChecked(false);
//            }
//        });
//        textCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                multipleChoiceCheckBox.setChecked(false);
//            }
//        });
//        cancelTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//            }
//        });
//        proceedTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!studyCheckBox.isChecked() && !quizCheckBox.isChecked()){
//                    Toast.makeText(mContext,"You must choose a learning mode",Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if (!multipleChoiceCheckBox.isChecked() && !textCheckBox.isChecked()){
//                    Toast.makeText(mContext,"You must choose an answer input method",Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                SharedPreferences.Editor editor = getSharedPreferences(nameOfFile, MODE_PRIVATE).edit();
//                if (studyCheckBox.isChecked())
//                    editor.putString("learningMode","Study");
//                if (quizCheckBox.isChecked())
//                    editor.putString("learningMode","Quiz");
//                if (multipleChoiceCheckBox.isChecked())
//                    editor.putString("answerInputMethod","MultipleChoice");
//                if (textCheckBox.isChecked())
//                    editor.putString("answerInputMethod","Text");
//                if (randomQuestionsSwitch.isChecked())
//                    editor.putBoolean("randomQuestions",true);
//                else
//                    editor.putBoolean("randomQuestions",false);
//
//                editor.apply();
//
//                SharedPreferences prefsFileSelected = getSharedPreferences(nameOfFile, MODE_PRIVATE);
//                String learningModeUpdated = prefsFileSelected.getString("learningMode","");
//                String answerInputModeUpdated = prefsFileSelected.getString("answerInputMethod","");
//                boolean randomQuestionsUpdated = prefsFileSelected.getBoolean("randomQuestions",false);
//
//                dialog.dismiss();
//
//                updatePreferencesAndDatabase(nameOfFile,learningModeUpdated,answerInputModeUpdated,randomQuestionsUpdated);
//                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
//                startActivity(intent);
//            }
//        });
//
//    }

    public void fileOptionsDialog(final String fileID, final String nameOfFile){
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

        warningMessageTextView = (TextView) dialog.findViewById(R.id.warningMessage);

        warningMessageTextView.setVisibility(View.GONE);

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        SharedPreferences prefsFileSelected = getSharedPreferences(fileID, MODE_PRIVATE);

        final String learningMode = prefsFileSelected.getString("learningMode","");
        final String answerInputMode = prefsFileSelected.getString("answerInputMethod","");
        final boolean randomQuestions = prefsFileSelected.getBoolean("randomQuestions",false);
        boolean reversedOrder = prefsFileSelected.getBoolean("reversedOrder",false);

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

                editor.putString("nameOfFile",nameOfFile);

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
                String learningModeUpdated = prefsFileSelected.getString("learningMode","");
                String answerInputModeUpdated = prefsFileSelected.getString("answerInputMethod","");
                boolean randomQuestionsUpdated = prefsFileSelected.getBoolean("randomQuestions",false);
                boolean reversedOrderUpdated = prefsFileSelected.getBoolean("reversedOrder",false);

                dialog.dismiss();

                updatePreferencesAndDatabase(fileID,nameOfFile,learningModeUpdated,answerInputModeUpdated,randomQuestionsUpdated,reversedOrderUpdated);
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
            }
        });

    }

}
