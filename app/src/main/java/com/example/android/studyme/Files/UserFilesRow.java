package com.example.android.studyme.Files;

/**
 * Created by Backmod on 1/3/18.
 */

public class UserFilesRow {

    private String fileID;
    private String nameOfFile;
    private Double progressBarStatus;
    private int percentage;

    private boolean availability;

    private String learningMode;
    private String answerInputMode;
    private boolean randomQuestions;

    private boolean sharedFile;
    private boolean publicFile;

    public UserFilesRow(String fileID, String nameOfFile, Double progressBarStatus, int percentage,
                        boolean availability, String learningMode, String answerInputMode,
                        boolean randomQuestions, boolean sharedFile, boolean publicFile) {
        this.fileID = fileID;
        this.nameOfFile = nameOfFile;
        this.progressBarStatus = progressBarStatus;
        this.percentage = percentage;
        this.availability = availability;
        this.learningMode = learningMode;
        this.answerInputMode = answerInputMode;
        this.randomQuestions = randomQuestions;
        this.sharedFile = sharedFile;
        this.publicFile = publicFile;
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

    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
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

    public boolean isSharedFile() {
        return sharedFile;
    }

    public void setSharedFile(boolean sharedFile) {
        this.sharedFile = sharedFile;
    }

    public boolean isPublicFile() {
        return publicFile;
    }

    public void setPublicFile(boolean publicFile) {
        this.publicFile = publicFile;
    }
}
