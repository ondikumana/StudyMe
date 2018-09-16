package com.example.android.studyme.Files;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.studyme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Backmod on 1/29/18.
 */

public class SharedFilesRowListAdapter extends ArrayAdapter<SharedFilesRow> {

    private static final String TAG = "SharedFilesRowListAdapt";

    private Context mContext;
    int mResource;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    static class ViewHolder {
        TextView TVfromNameOfUser,TVnameOfFile,TVpercentage,TVlearningMode,TVanswerInputMode,TVrandomQuestions;
        ProgressBar progressBar;
    }

    public SharedFilesRowListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<SharedFilesRow> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        String fromUserID = getItem(position).getFromUserID();
        String nameOfFile = getItem(position).getNameOfFile();
        Double progressBarStatus = getItem(position).getProgressBarStatus();
        int percentage = getItem(position).getPercentage();

        String learningMode = getItem(position).getLearningMode();
        String answerInputMethod = getItem(position).getAnswerInputMode();
        boolean randomQuestions = getItem(position).isRandomQuestions();

//        SharedFilesRow row = new SharedFilesRow(fromUserID,nameOfFile,progressBarStatus,percentage,learningMode,answerInputMethod,randomQuestions);

        ViewHolder holder = new ViewHolder();

        if (convertView == null){

            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource,parent,false);

            holder.TVfromNameOfUser = (TextView) convertView.findViewById(R.id.fromNameOfUserTextView);
            holder.TVnameOfFile = (TextView) convertView.findViewById(R.id.nameOfFile);
            holder.TVpercentage = (TextView) convertView.findViewById(R.id.progressPercentage);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);

            holder.TVlearningMode = (TextView) convertView.findViewById(R.id.learningMode);
            holder.TVanswerInputMode = (TextView) convertView.findViewById(R.id.answerInputMethd);
            holder.TVrandomQuestions = (TextView) convertView.findViewById(R.id.randomQuestions);

            convertView.setTag(holder);

        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mAuth.getCurrentUser()!=null){
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myRef = database.getReference("users").
                    child(fromUserID).child("user_info").child("name");

            final ViewHolder finalHolder = holder;

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                        finalHolder.TVfromNameOfUser.setText(dataSnapshot.getValue(String.class));
                    else
                        finalHolder.TVfromNameOfUser.setText("Former StudyMe User");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        holder.TVnameOfFile.setText(nameOfFile);

        if(percentage == 0){
            holder.TVpercentage.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
        }
        else {
            holder.TVpercentage.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.TVpercentage.setText(percentage+"%");
            holder.progressBar.setProgress((int)(Math.round(progressBarStatus)));
        }

        holder.TVlearningMode.setText(learningMode);
        holder.TVanswerInputMode.setText(answerInputMethod);
        if (randomQuestions)
            holder.TVrandomQuestions.setVisibility(View.VISIBLE);

        return convertView;
    }
}
