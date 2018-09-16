package com.example.android.studyme.Files;

/**
 * Created by Backmod on 1/31/18.
 */

public class PublicFilesRow {

    private static final String TAG = "PublicFilesRow";

    private String createdByUserID;
    private String fileID;
    private String nameOfFile;
    private boolean addedToMyFiles;

    private String learningMode;
    private String answerInputMode;
    private boolean randomQuestions;

    public PublicFilesRow(String createdByUserID, String fileID, String nameOfFile, boolean addedToMyFiles, String learningMode, String answerInputMode, boolean randomQuestions) {
        this.createdByUserID = createdByUserID;
        this.fileID = fileID;
        this.nameOfFile = nameOfFile;
        this.addedToMyFiles = addedToMyFiles;
        this.learningMode = learningMode;
        this.answerInputMode = answerInputMode;
        this.randomQuestions = randomQuestions;
    }

    public static String getTAG() {
        return TAG;
    }

    public String getCreatedByUserID() {
        return createdByUserID;
    }

    public void setCreatedByUserID(String createdByUserID) {
        this.createdByUserID = createdByUserID;
    }

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public String getNameOfFile() {
        return nameOfFile;
    }

    public void setNameOfFile(String nameOfFile) {
        this.nameOfFile = nameOfFile;
    }

    public boolean isAddedToMyFiles() {
        return addedToMyFiles;
    }

    public void setAddedToMyFiles(boolean addedToMyFiles) {
        this.addedToMyFiles = addedToMyFiles;
    }

    public String getLearningMode() {
        return learningMode;
    }

    public void setLearningMode(String learningMode) {
        this.learningMode = learningMode;
    }

    public String getAnswerInputMode() {
        return answerInputMode;
    }

    public void setAnswerInputMode(String answerInputMode) {
        this.answerInputMode = answerInputMode;
    }

    public boolean isRandomQuestions() {
        return randomQuestions;
    }

    public void setRandomQuestions(boolean randomQuestions) {
        this.randomQuestions = randomQuestions;
    }
}
