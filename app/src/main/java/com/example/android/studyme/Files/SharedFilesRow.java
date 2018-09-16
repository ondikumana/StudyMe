package com.example.android.studyme.Files;

/**
 * Created by Backmod on 1/29/18.
 */

public class SharedFilesRow {

    private String fromUserID;
    private String fileID;
    private String nameOfFile;
    private Double progressBarStatus;
    private int percentage;
    private String learningMode;
    private String answerInputMode;
    private boolean randomQuestions;

    public SharedFilesRow(String fromUserID, String fileID, String nameOfFile, Double progressBarStatus, int percentage, String learningMode, String answerInputMode, boolean randomQuestions) {
        this.fromUserID = fromUserID;
        this.fileID = fileID;
        this.nameOfFile = nameOfFile;
        this.progressBarStatus = progressBarStatus;
        this.percentage = percentage;
        this.learningMode = learningMode;
        this.answerInputMode = answerInputMode;
        this.randomQuestions = randomQuestions;
    }

    public String getFromUserID() {
        return fromUserID;
    }

    public void setFromUserID(String fromUserID) {
        this.fromUserID = fromUserID;
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

    public Double getProgressBarStatus() {
        return progressBarStatus;
    }

    public void setProgressBarStatus(Double progressBarStatus) {
        this.progressBarStatus = progressBarStatus;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
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
