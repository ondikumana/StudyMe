package com.example.android.studyme.Files;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import static com.example.android.studyme.Home.UserFilesFragment.savedFilesDirectory;

/**
 * Created by Backmod on 1/4/18.
 */

public class EntryFiles {
    private static final String TAG = "EntryFiles";


    public String readFile (String pathOfFileSelected) {
        /* This method reads the file in the directory. It gets the path of the file form the
        preferences which in the onCreate method. It then returns a string containing
        the contents of the file. It returns a null String when the path does not exist, this should
        never happen. It's just a safeguard.
        */

        File file = new File(pathOfFileSelected);

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
            Log.d(TAG, "readFile: text: " +text.toString());
            return text.toString().trim();
        }
        catch (IOException e) {
            Log.e(TAG, "readFile: ",e );
            return null;
        }
    }

    public void saveFile(String pathOfFileSelected, String content){
        /*
        This method checks if any changes have been made then updates the file with the file path from
        preferences.
         */
            FileOutputStream fos = null;
            try {
                final File myFile = new File(pathOfFileSelected);

                myFile.createNewFile();

                fos = new FileOutputStream(myFile);

                fos.write(content.getBytes());
                fos.close();
                Log.d(TAG, "saveFile: Saved Successfully");

            } catch (IOException e) {
                e.printStackTrace();
                Log.w(TAG, "saveFile: Unable to save",e);
            }
    }

    public boolean textCheck(String content){
        /*
        This method checks each line in the edittext to make sure it has ':'. It returns false if any
        of the line does not.
         */
        String [] text = content.split("\r\n|\r|\n");
        if (text.length<4)
            return false;
        for (int i=0;i<text.length;i++){
            if (!text[i].contains(":")){
                Log.d(TAG, "textCheck: Entry does not contain ':'");
                return false;
            }
        }
        for (int i = 0;i<text.length;i++){
            String [] one = text[i].split(":");
            if (one.length > 2){
                Log.d(TAG, "textCheck: Entry contains more than one ':'");
                return false;
            }
            if (one[0].toLowerCase().trim().equals(one[1].toLowerCase().trim())){
                Log.d(TAG, "textCheck: Word and Definition are identical "+one[0]);
                return false;
            }
            for (int j = i+1;j<text.length;j++){
                String [] two = text[j].split(":");
                if (one[0].toLowerCase().trim().equals(two[0].toLowerCase().trim())){
                    Log.d(TAG, "textCheck: Repeated word "+one[0]);
                    return false;
                }
                if (one[1].toLowerCase().trim().equals(two[1].toLowerCase().trim())){
                    Log.d(TAG, "textCheck: Repeated definition "+one[1]);
                    return false;
                }
                if (one[0].toLowerCase().trim().equals(two[1].toLowerCase().trim())){
                    Log.d(TAG, "textCheck: Word or Definition already used in another entry " + one[0].toLowerCase().trim());
                    return false;
                }
                if (one[1].toLowerCase().trim().equals(two[0].toLowerCase().trim())){
                    Log.d(TAG, "textCheck: Word or Definition already used in another entry " + one[1].toLowerCase().trim());
                    return false;
                }
            }
        }
        return true;
    }

    public String makePretty(String nameOfFile){
        if (nameOfFile.equals(""))
            return "";
        nameOfFile = nameOfFile.toLowerCase();

        if (nameOfFile.contains(" ")){
            String [] nameOfFileArr = nameOfFile.split(" ");
            String prettyName = "";
            for (int i = 0;i<nameOfFileArr.length;i++){
                prettyName+= nameOfFileArr[i].substring(0, 1).toUpperCase() + nameOfFileArr[i].substring(1) +" ";
            }
            return prettyName.trim();
        }
        else
            return nameOfFile.substring(0, 1).toUpperCase() + nameOfFile.substring(1);

    }

    public void deleteAllFiles(){
        if (savedFilesDirectory.isDirectory())
        {
            String[] files = savedFilesDirectory.list();
            for (int i = 0; i < files.length; i++)
            {
                new File(savedFilesDirectory, files[i]).delete();
            }
        }
        Log.d(TAG, "deleteFiles: All files in directory deleted");
    }
}
