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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Backmod on 1/31/18.
 */

public class PublicFilesRowListAdapter extends ArrayAdapter<PublicFilesRow> {

    private static final String TAG = "PublicFilesRowListAdapt";

    private Context mContext;
    int mResource;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    static class ViewHolder {

        TextView TVnameOfFile,TVaddedToMyFiles,TVcreatedByNameOfUser,TVlearningMode,TVanswerInputMode,TVrandomQuestions;

    }

    public PublicFilesRowListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<PublicFilesRow> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final String createdByUserID = getItem(position).getCreatedByUserID();
        String nameOfFile = getItem(position).getNameOfFile();
        boolean addedToMyFiles = getItem(position).isAddedToMyFiles();

        String learningMode = getItem(position).getLearningMode();
        String answerInputMethod = getItem(position).getAnswerInputMode();
        boolean randomQuestions = getItem(position).isRandomQuestions();

//        PublicFilesRow row = new PublicFilesRow(createdByUserID,nameOfFile,addedToMyFiles,learningMode,answerInputMethod,randomQuestions);

        ViewHolder holder = new ViewHolder();

        if (convertView == null){

            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource,parent,false);

            holder.TVcreatedByNameOfUser = (TextView) convertView.findViewById(R.id.createdByNameOfUserTextView);
            holder.TVnameOfFile = (TextView) convertView.findViewById(R.id.nameOfFile);
            holder.TVaddedToMyFiles = (TextView) convertView.findViewById(R.id.addedToMyFilesTextView);

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
                    child(createdByUserID).child("user_info").child("name");

            final ViewHolder finalHolder = holder;

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())

                        if (mAuth.getCurrentUser().getUid().equals(createdByUserID))
                            finalHolder.TVcreatedByNameOfUser.setText("You");
                        else
                            finalHolder.TVcreatedByNameOfUser.setText(dataSnapshot.getValue(String.class));

                    else
                        finalHolder.TVcreatedByNameOfUser.setText("Former StudyMe User");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        holder.TVnameOfFile.setText(nameOfFile);

        if (addedToMyFiles){
            holder.TVaddedToMyFiles.setVisibility(View.VISIBLE);
        }
        else {
            holder.TVaddedToMyFiles.setVisibility(View.GONE);
        }

        holder.TVlearningMode.setText(learningMode);
        holder.TVanswerInputMode.setText(answerInputMethod);
        if (randomQuestions)
            holder.TVrandomQuestions.setVisibility(View.VISIBLE);

        return convertView;
    }

}
