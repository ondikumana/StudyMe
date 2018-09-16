package com.example.android.studyme.Firebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.Context.MODE_PRIVATE;
import static com.example.android.studyme.Home.SharedFilesFragment.sharedFilesDirectory;
import static com.example.android.studyme.Home.UserFilesFragment.savedFilesDirectory;

/**
 * Created by Backmod on 1/4/18.
 */

public class FirebaseMethod {

    private static final String TAG = "FirebaseMethod";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public void sendEmailResetPassword (String email){
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                        }
                    }
                });
    }

//    public void uploadFileFirebase(String pathOfFileSelected, String nameOfFileSelected){
//        /*
//        This method uploads the edited file to the firebase storage and it replaces the already existing
//        file in the storage.
//         */
//
//        Uri file = Uri.fromFile(new File(pathOfFileSelected));
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference storageRef = storage.getReferenceFromUrl("gs://studyme-dbc63.appspot.com/Users/").child(mAuth.getCurrentUser().getUid()).child("Files");
//        StorageReference islandRef = storageRef.child(nameOfFileSelected.toLowerCase()+".txt");
//
//
//        islandRef.putFile(file)
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//                        Log.d(TAG, "onSuccess: Uploaded Successfully");
//
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception exception) {
//
//                    }
//                });
//    }

    public void uploadFileFirebase(String pathOfFileSelected, String fileID){
        /*
        This method uploads the edited file to the firebase storage and it replaces the already existing
        file in the storage.
         */

        Uri file = Uri.fromFile(new File(pathOfFileSelected));
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://studyme-dbc63.appspot.com/Users/").child(mAuth.getCurrentUser().getUid()).child("Files");
        StorageReference islandRef = storageRef.child(fileID+".txt");


        islandRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Log.d(TAG, "onSuccess: Uploaded Successfully");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                    }
                });
    }

    public void saveFileModeToDatabse (String fileID, String nameOfFile, String learningMode, String answerInputMethod, boolean randomQuestions, boolean reversedOrder){

        String randomQuestionsString = "false";
        String reversedOrderString = "false";

        if (randomQuestions)
            randomQuestionsString = "true";
        if (reversedOrder)
            reversedOrderString = "true";


        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference myRef = database.getReference("users").child(mAuth.getCurrentUser().getUid()).child("files").child(fileID);
        myRef.child("name_of_file").setValue(nameOfFile);

        DatabaseReference myRefFileMode = database.getReference("users").child(mAuth.getCurrentUser().getUid()).child("files").child(fileID).child("file_mode");

        myRefFileMode.child("learning_mode").setValue(learningMode);
        myRefFileMode.child("answer_input_method").setValue(answerInputMethod);
        myRefFileMode.child("random_questions").setValue(randomQuestionsString);
        myRefFileMode.child("reversed_order").setValue(reversedOrderString);

        Log.d(TAG, "saveFileModeToDatabse: Saved To Database");
    }

    public void saveProgressToDatabase(String fileID, String answeredToSave, double progressBarStatus,
                                       int currentQuestion, boolean goneThroughAllEntries, String skippedQuestions, int attempts,
                                       String incorrectlyAnswered, int incorrectAnswers){
        /*
        This method uploads the string with the answered numbers and the status of the progressbar to
        the firebase database. In the future I hope to use this progress when the user uninstalls the
        app and wants to retrieve their progress.
         */

        String goneThroughAllEntriesString;
        if (goneThroughAllEntries)
            goneThroughAllEntriesString = "true";
        else
            goneThroughAllEntriesString = "false";

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users").child(mAuth.getCurrentUser().getUid()).child("files").child(fileID).child("progress");

        myRef.child("answered").setValue(answeredToSave);
        myRef.child("progress_bar").setValue(Double.toString(progressBarStatus));
        myRef.child("current_question").setValue(currentQuestion);
        myRef.child("gone_through_all_entries").setValue(goneThroughAllEntriesString);
        myRef.child("skipped_questions").setValue(skippedQuestions);
        myRef.child("attempts").setValue(attempts);
        myRef.child("incorrectly_answered").setValue(incorrectlyAnswered);
        myRef.child("incorrect_answers").setValue(incorrectAnswers);

        Log.d(TAG, "saveProgressToDatabase: Saved To Database");
    }

    public void saveCurrentAnswerToDatabase(String nameOfFileSelected, String currentAnswer){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users").child(mAuth.getCurrentUser().getUid()).child("files").child(nameOfFileSelected).child("progress");

        myRef.child("current_answer").setValue(currentAnswer);
    }

    public void sendFeedbackToOlivier(String feedback){
        /*
        This method places the feedback in the appropriate node in the firebase database
         */
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("feedback").child(mAuth.getCurrentUser().getDisplayName()).push();

        myRef.setValue(feedback);
    }

    public void deleteFileStorage (String fileID){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://studyme-dbc63.appspot.com/Users/").child(mAuth.getCurrentUser().getUid()).child("Files");
        StorageReference islandRef = storageRef.child(fileID+".txt");

        islandRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "deleteFileStorage: File deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "deleteFileStorage: File was not deleted");

            }
        });
    }

    public void deleteFileDatabase (String nameOfFileSelected){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users").child(mAuth.getCurrentUser().getUid()).child("files");

        myRef.child(nameOfFileSelected).removeValue();
        Log.d(TAG, "deleteFileDatabase: File deleted");
    }

    public void updateEmail (String email){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.updateEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User email address updated.");
                        }
                    }
                });
    }

    public void sendEmailVerification(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                        }
                    }
                });
    }

    public void updateEmailDatabase(String email){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users").child(mAuth.getCurrentUser().getUid()).child("user_info");

        myRef.child("email").setValue(email);
        Log.d(TAG, "updateEmailDatabase: Email Updated");
    }

    public void updateNameDatabase(String name){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users").child(mAuth.getCurrentUser().getUid()).child("user_info");

        myRef.child("name").setValue(name);
        Log.d(TAG, "updateNameDatabase: Name Updated");
    }

    public void updatePassword(String newPassword){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User password updated.");
                        }
                    }
                });
    }

    private void deleteUserDatabase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");

        myRef.child(mAuth.getCurrentUser().getUid()).removeValue();
        Log.d(TAG, "deleteUserDatabase: User Deleted From Database");
    }

    public void deleteAllFilesStorageAndDatabase(final boolean deleteUser){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users")
                .child(mAuth.getCurrentUser().getUid()).child("files");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap:dataSnapshot.getChildren()){
                    String fileID = snap.getKey();
                    deleteFileStorage(fileID);
                    Log.d(TAG, "deleteAllFilesStorageAndDatabase: deleted:  "+fileID);
                }
                deleteAllFilesDatabase();

                if (deleteUser){
                    deleteUserDatabase();

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        user.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mAuth.signOut();
                                            Log.d(TAG, "User account deleted.");
                                        }
                                    }
                                });
                    }
                    Log.d(TAG, "deleteAccount: user is null");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void deleteAllFilesDatabase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users").child(mAuth.getCurrentUser().getUid());

        myRef.child("files").removeValue();
        Log.d(TAG, "deleteAllFilesDatabase: All files deleted");
    }

    public void shareFileDatabase(String fileID, final String nameOfFile, final String toUserID, final boolean shareProgress){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRefShare = database.getReference("users").child(toUserID).child("shared_files").child(fileID);

        final DatabaseReference myRefRetrieve = database.getReference("users").
                child(mAuth.getCurrentUser().getUid()).child("files").child(fileID);

        myRefRetrieve.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (shareProgress) {

                    DataSnapshot progress = dataSnapshot.child("progress");

                    String progressBarStatus = progress.child("progress_bar").getValue(String.class);
                    String answered = progress.child("answered").getValue(String.class);
                    int currentQuestion = progress.child("current_question").getValue(Integer.class);
                    String goneThroughAllQuestionsString = progress.child("gone_through_all_entries").getValue(String.class);
                    String skippedQuestions = progress.child("skipped_questions").getValue(String.class);
                    String currentAnswer = progress.child("current_answer").getValue(String.class);
                    int attempts = progress.child("attempts").getValue(Integer.class);
                    String incorrectlyAnswered = progress.child("incorrectly_answered").getValue(String.class);
                    int incorrectAnswers = progress.child("incorrect_answers").getValue(Integer.class);

                    myRefShare.child("progress").child("progress_bar").setValue(progressBarStatus);
                    myRefShare.child("progress").child("answered").setValue(answered);
                    myRefShare.child("progress").child("current_question").setValue(currentQuestion);
                    myRefShare.child("progress").child("gone_through_all_entries").setValue(goneThroughAllQuestionsString);
                    myRefShare.child("progress").child("skipped_questions").setValue(skippedQuestions);
                    myRefShare.child("progress").child("current_answer").setValue(currentAnswer);
                    myRefShare.child("progress").child("attempts").setValue(attempts);
                    myRefShare.child("progress").child("incorrectly_answered").setValue(incorrectlyAnswered);
                    myRefShare.child("progress").child("incorrect_answers").setValue(incorrectAnswers);
                }

                DataSnapshot fileMode = dataSnapshot.child("file_mode");

                String learningMode = fileMode.child("learning_mode").getValue(String.class);
                String answerInputMethod = fileMode.child("answer_input_method").getValue(String.class);
                String randomQuestionsString = fileMode.child("random_questions").getValue(String.class);

                myRefShare.child("file_mode").child("learning_mode").setValue(learningMode);
                myRefShare.child("file_mode").child("answer_input_method").setValue(answerInputMethod);
                myRefShare.child("file_mode").child("random_questions").setValue(randomQuestionsString);

                myRefShare.child("from").setValue(mAuth.getCurrentUser().getUid());
                myRefShare.child("name_of_file").setValue(nameOfFile);

                myRefRetrieve.child("shared_with").push().setValue(toUserID);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    public void addSharedToUserFilesDatabase(String fileID){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRefAdd = database.getReference("users").child(mAuth.getCurrentUser().getUid()).child("files").child(fileID);

        final DatabaseReference myRefRetrieve = database.getReference("users").
                child(mAuth.getCurrentUser().getUid()).child("shared_files").child(fileID);

        myRefRetrieve.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String sharedFromUser = dataSnapshot.child("from").getValue(String.class);
                String nameOfFile = dataSnapshot.child("name_of_file").getValue(String.class);

                myRefAdd.child("from").setValue(sharedFromUser);
                myRefAdd.child("name_of_file").setValue(nameOfFile);

                DataSnapshot progress = dataSnapshot.child("progress");

                if (progress.exists()) {

                    String progressBarStatus = progress.child("progress_bar").getValue(String.class);
                    String answered = progress.child("answered").getValue(String.class);
                    int currentQuestion = progress.child("current_question").getValue(Integer.class);
                    String goneThroughAllQuestionsString = progress.child("gone_through_all_entries").getValue(String.class);
                    String skippedQuestions = progress.child("skipped_questions").getValue(String.class);
                    String currentAnswer = progress.child("current_answer").getValue(String.class);
                    int attempts = progress.child("attempts").getValue(Integer.class);
                    String incorrectlyAnswered = progress.child("incorrectly_answered").getValue(String.class);
                    int incorrectAnswers = progress.child("incorrect_answers").getValue(Integer.class);

                    myRefAdd.child("progress").child("progress_bar").setValue(progressBarStatus);
                    myRefAdd.child("progress").child("answered").setValue(answered);
                    myRefAdd.child("progress").child("current_question").setValue(currentQuestion);
                    myRefAdd.child("progress").child("gone_through_all_entries").setValue(goneThroughAllQuestionsString);
                    myRefAdd.child("progress").child("skipped_questions").setValue(skippedQuestions);
                    myRefAdd.child("progress").child("current_answer").setValue(currentAnswer);
                    myRefAdd.child("progress").child("attempts").setValue(attempts);
                    myRefAdd.child("progress").child("incorrectly_answered").setValue(incorrectlyAnswered);
                    myRefAdd.child("progress").child("incorrect_answers").setValue(incorrectAnswers);
                }

                DataSnapshot fileMode = dataSnapshot.child("file_mode");

                String learningMode = fileMode.child("learning_mode").getValue(String.class);
                String answerInputMethod = fileMode.child("answer_input_method").getValue(String.class);
                String randomQuestionsString = fileMode.child("random_questions").getValue(String.class);

                myRefAdd.child("file_mode").child("learning_mode").setValue(learningMode);
                myRefAdd.child("file_mode").child("answer_input_method").setValue(answerInputMethod);
                myRefAdd.child("file_mode").child("random_questions").setValue(randomQuestionsString);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    public void addPublicToUserFilesDatabase(String fileID){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRefAdd = database.getReference("users").child(mAuth.getCurrentUser().getUid()).child("files").child(fileID);

        final DatabaseReference myRefRetrieve = database.getReference("public_files").child(fileID);

        myRefRetrieve.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String createdByUser = dataSnapshot.child("created_by").getValue(String.class);
                String nameOfFile = dataSnapshot.child("name_of_file").getValue(String.class);

                myRefAdd.child("created_by").setValue(createdByUser);
                myRefAdd.child("name_of_file").setValue(nameOfFile);

                DataSnapshot progress = dataSnapshot.child("progress");

                if (progress.exists()) {

                    String progressBarStatus = progress.child("progress_bar").getValue(String.class);
                    String answered = progress.child("answered").getValue(String.class);
                    int currentQuestion = progress.child("current_question").getValue(Integer.class);
                    String goneThroughAllQuestionsString = progress.child("gone_through_all_entries").getValue(String.class);
                    String skippedQuestions = progress.child("skipped_questions").getValue(String.class);
                    String currentAnswer = progress.child("current_answer").getValue(String.class);
                    int attempts = progress.child("attempts").getValue(Integer.class);
                    String incorrectlyAnswered = progress.child("incorrectly_answered").getValue(String.class);
                    int incorrectAnswers = progress.child("incorrect_answers").getValue(Integer.class);

                    myRefAdd.child("progress").child("progress_bar").setValue(progressBarStatus);
                    myRefAdd.child("progress").child("answered").setValue(answered);
                    myRefAdd.child("progress").child("current_question").setValue(currentQuestion);
                    myRefAdd.child("progress").child("gone_through_all_entries").setValue(goneThroughAllQuestionsString);
                    myRefAdd.child("progress").child("skipped_questions").setValue(skippedQuestions);
                    myRefAdd.child("progress").child("current_answer").setValue(currentAnswer);
                    myRefAdd.child("progress").child("attempts").setValue(attempts);
                    myRefAdd.child("progress").child("incorrectly_answered").setValue(incorrectlyAnswered);
                    myRefAdd.child("progress").child("incorrect_answers").setValue(incorrectAnswers);
                }

                DataSnapshot fileMode = dataSnapshot.child("file_mode");

                String learningMode = fileMode.child("learning_mode").getValue(String.class);
                String answerInputMethod = fileMode.child("answer_input_method").getValue(String.class);
                String randomQuestionsString = fileMode.child("random_questions").getValue(String.class);

                myRefAdd.child("file_mode").child("learning_mode").setValue(learningMode);
                myRefAdd.child("file_mode").child("answer_input_method").setValue(answerInputMethod);
                myRefAdd.child("file_mode").child("random_questions").setValue(randomQuestionsString);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    public void downloadUserFileFromStorage(String fileID) {
        /*
        This method downloads the file requested in the dialog. It gets it from the firebase storage
        and stores it in the temp data directory of the app.
         */

        final String nameOfFileToDownload = fileID+ ".txt";

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://studyme-dbc63.appspot.com/Users/").child(mAuth.getCurrentUser().getUid()).child("Files");
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
                // Local temp file has been created
                //Toast.makeText(mContext, "File Downloaded Successfully", Toast.LENGTH_SHORT).show();
                moveFile(nameOfFileToDownload,filePath,savedFileName2);
                //readFromDatabase();
                //dialog.dismiss();
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

    public void downloadSharedFileFromStorage(String fileID, String fromUserID) {
        /*
        This method downloads the file requested in the dialog. It gets it from the firebase storage
        and stores it in the temp data directory of the app.
         */

        final String nameOfFileToDownload = fileID+ ".txt";

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
                // Local temp file has been created
                //Toast.makeText(mContext, "File Downloaded Successfully", Toast.LENGTH_SHORT).show();
                moveFile(nameOfFileToDownload,filePath,savedFileName2);
                //readFromDatabase();
                //dialog.dismiss();
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

    private void moveFile(String nameOfFileToDownload, String filePath,  String savedFileName) {
        /*
        This method moves the downloaded file from the temp location to a different directory. It also
        renames the file for easier future reference.
         */
        //Log.d(TAG, "moveFile: savedFileName: " + savedFileName + "Context " + mContext.toString());

        InputStream in = null;
        OutputStream out = null;
        try {

            if (!savedFilesDirectory.exists()) {
                savedFilesDirectory.mkdirs();
            }

            Log.d(TAG, "moveFile: Path: " + savedFilesDirectory.getPath());

            in = new FileInputStream(filePath);
            out = new FileOutputStream(savedFilesDirectory.getPath() + "/" + nameOfFileToDownload);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            new File(filePath + savedFileName).delete();
            Log.d(TAG, "moveFile: File Moved to " + savedFilesDirectory.getPath());
        } catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    public void getSharedFileContent(String nameOfFile, String fromUserID){

        final String nameOfFileToDownload = nameOfFile.toLowerCase() + ".txt";

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
                // Local temp file has been created
                //Toast.makeText(mContext, "File Downloaded Successfully", Toast.LENGTH_SHORT).show();
                moveSharedFile(nameOfFileToDownload,filePath,savedFileName2);
                //readFromDatabase();
                //dialog.dismiss();
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

    public void moveSharedFile(String nameOfFileToDownload, String filePath,  String savedFileName) {
        /*
        This method moves the downloaded file from the temp location to a different directory. It also
        renames the file for easier future reference.
         */
        //Log.d(TAG, "moveFile: savedFileName: " + savedFileName + "Context " + mContext.toString());

        InputStream in = null;
        OutputStream out = null;
        try {

            if (!sharedFilesDirectory.exists()) {
                sharedFilesDirectory.mkdirs();
            }

            Log.d(TAG, "moveFile: Path: " + sharedFilesDirectory.getPath());

            in = new FileInputStream(filePath);
            out = new FileOutputStream(sharedFilesDirectory.getPath() + "/" + nameOfFileToDownload);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            new File(filePath + savedFileName).delete();
            Log.d(TAG, "moveFile: File Moved to " + sharedFilesDirectory.getPath());
        } catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    public void deleteSharedFileDatabase (String fileID){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users").child(mAuth.getCurrentUser().getUid()).child("shared_files");

        myRef.child(fileID).removeValue();
        Log.d(TAG, "deleteSharedFileDatabase: File deleted");
    }

    public void deletePublicFileDatabase (String fileID){
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference myRefPublicFile = database.getReference("public_files");

        DatabaseReference myRefUserFile = database.getReference("users").child(mAuth.getCurrentUser().getUid()).child("files").child(fileID);

        myRefPublicFile.child(fileID).removeValue();

        myRefUserFile.child("public").setValue("false");
        Log.d(TAG, "deleteSharedFileDatabase: File deleted");


    }

    public void makeFilePublicDatabase(String fileID, final String nameOfFile){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRefShare = database.getReference("public_files").child(fileID);

        final DatabaseReference myRefRetrieve = database.getReference("users").
                child(mAuth.getCurrentUser().getUid()).child("files").child(fileID);

        myRefRetrieve.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                DataSnapshot fileMode = dataSnapshot.child("file_mode");

                String learningMode = fileMode.child("learning_mode").getValue(String.class);
                String answerInputMethod = fileMode.child("answer_input_method").getValue(String.class);
                String randomQuestionsString = fileMode.child("random_questions").getValue(String.class);

                myRefShare.child("file_mode").child("learning_mode").setValue(learningMode);
                myRefShare.child("file_mode").child("answer_input_method").setValue(answerInputMethod);
                myRefShare.child("file_mode").child("random_questions").setValue(randomQuestionsString);

                myRefShare.child("created_by").setValue(mAuth.getCurrentUser().getUid());
                myRefShare.child("name_of_file").setValue(nameOfFile);

                myRefRetrieve.child("public").setValue("true");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

}
