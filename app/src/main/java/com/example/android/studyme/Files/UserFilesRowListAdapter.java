package com.example.android.studyme.Files;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.studyme.R;

import java.util.ArrayList;

/**
 * Created by Backmod on 1/3/18.
 */

public class UserFilesRowListAdapter extends ArrayAdapter<UserFilesRow> {

    private static final String TAG = "UserFilesRowListAdapter";
    private Context mContext;
    int mResource;

    static class ViewHolder {
        TextView TVnameOfFile,TVpercentage,TVavailability,TVlearningMode,TVanswerInputMode,TVrandomQuestions,TVsharedFile,TVpublicFile;
        ProgressBar progressBar;
    }

    public UserFilesRowListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<UserFilesRow> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        /*
        I honestly have no idea what this thing does. But it has to do with the custom listview
        that I made. The custom view contains the name of the file, a progress bar, and a percentage
        of the progress. It gets that from preferences.
         */
        String nameOfFile = getItem(position).getNameOfFile();
        Double progressBarStatus = getItem(position).getProgressBarStatus();
        int percentage = getItem(position).getPercentage();
        boolean availability = getItem(position).isAvailability();

        String learningMode = getItem(position).getLearningMode();
        String answerInputMethod = getItem(position).getAnswerInputMode();
        boolean randomQuestions = getItem(position).isRandomQuestions();

        boolean sharedFile = getItem(position).isSharedFile();
        boolean publicFile = getItem(position).isPublicFile();

//        UserFilesRow row = new UserFilesRow(nameOfFile,progressBarStatus,percentage,availability,learningMode,answerInputMethod,randomQuestions);

        ViewHolder holder = new ViewHolder();

        if (convertView == null){

            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource,parent,false);

            holder.TVnameOfFile = (TextView) convertView.findViewById(R.id.nameOfFile);
            holder.TVpercentage = (TextView) convertView.findViewById(R.id.progressPercentage);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
            holder.TVavailability = (TextView) convertView.findViewById(R.id.availability);

            holder.TVlearningMode = (TextView) convertView.findViewById(R.id.learningMode);
            holder.TVanswerInputMode = (TextView) convertView.findViewById(R.id.answerInputMethd);
            holder.TVrandomQuestions = (TextView) convertView.findViewById(R.id.randomQuestions);

            holder.TVsharedFile = (TextView) convertView.findViewById(R.id.sharedTextView);
            holder.TVpublicFile = (TextView) convertView.findViewById(R.id.publicTextView);

            convertView.setTag(holder);

        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.TVnameOfFile.setText(nameOfFile);
        holder.TVpercentage.setText(percentage+"%");
        holder.progressBar.setProgress((int)(Math.round(progressBarStatus)));
        if(!availability)
            holder.TVavailability.setVisibility(View.VISIBLE);

        holder.TVlearningMode.setText(learningMode);
        holder.TVanswerInputMode.setText(answerInputMethod);
        if (randomQuestions)
            holder.TVrandomQuestions.setVisibility(View.VISIBLE);

        if (sharedFile)
            holder.TVsharedFile.setVisibility(View.VISIBLE);
        else
            holder.TVsharedFile.setVisibility(View.INVISIBLE); //setting it to gone would screw up the xml layout

        if (publicFile)
            holder.TVpublicFile.setVisibility(View.VISIBLE);
        else
            holder.TVpublicFile.setVisibility(View.INVISIBLE); //setting it to gone would screw up the xml layout

        return convertView;
    }
}
