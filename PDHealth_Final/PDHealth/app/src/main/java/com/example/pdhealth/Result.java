package com.example.pdhealth;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class Result extends AppCompatActivity {

    ImageView showFile;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    TextView textView;
    File pathFile = android.os.Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);

    StorageReference ref;
    Intent intent;
    private String filename;

    private String rec[];
    private String phn_number;
    private String rec_ID;
    private String fname;
    private String fnametxt;
    private TextView textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        intent=getIntent();
        filename= intent.getExtras().getString("filename");
        filename=filename.substring(0,filename.length()-4);
        rec= filename.split("_");
        phn_number=rec[1];
        rec_ID=rec[2];
        fname="Results_"+phn_number+"_"+rec_ID;
        fnametxt=fname+".txt";
        textView2=findViewById(R.id.textresult);


        showFile = findViewById(R.id.imagedownload);
        textView = (TextView)findViewById(R.id.textView);

        showFile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                textView.setText("");
                download();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void display(View view) throws IOException {
        String filename = pathFile.toString() + "/" + fnametxt;

        File file=new File(filename);

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                //Toast.makeText(this,line,Toast.LENGTH_LONG).show();
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        String temp=text.toString();
        extract_result(temp);

        /*try {
            content = new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i;
        try {
            i = inputStream.read();
            while (i != -1)
            {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        textView.setText(byteArrayOutputStream.toString());*/
    }

    private void extract_result(String temp) {

        String result;
        String []temparray=temp.split("and");
        String l1=temparray[0];
        l1=extractInt(l1);
        //Integer L1=Integer.parseInt(l1);
        Float L1=Float.parseFloat(l1);
        String l2=temparray[1];
        l2=extractInt(l2);
        //Integer L2=Integer.parseInt(l2);
        Float L2=Float.parseFloat(l2);
        String tt="Motor UPDRS Value: "+L1+"\n"+"\n"+"\n"+"Total UPDRS Value: "+L2;

        textView.setText(tt);

        if(L1>20&&L2>25){
            result="Severe";
        }
        else {
            result="Not Severe";
        }

        textView2.setText(result);
    }

    static String extractInt(String str)
    {
        // Replacing every non-digit number
        // with a space(" ")
        str = str.replaceAll("[^\\d||.]", " ");

        // Remove extra spaces from the beginning
        // and the ending of the string
        str = str.trim();

        // Replace all the consecutive white
        // spaces with a single space
        str = str.replaceAll(" +", " ");

        if (str.equals(""))
            return "-1";

        return str;
    }

    public void download()
    {
        storageReference = firebaseStorage.getInstance().getReference();
        ref = storageReference.child(phn_number).child(rec_ID).child(fnametxt);
        ref.getDownloadUrl().addOnSuccessListener( new OnSuccessListener<Uri>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSuccess(Uri uri) {
                String url = uri.toString();

                downloadfiles(Result.this,fname,".txt",pathFile.toString(),url,ref);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    public void downloadfiles(final Context context, String filename, String fileExtention, String destinationDirectory, String url, StorageReference ref)
    {
        final File localFile = new File(destinationDirectory,filename + fileExtention);
        ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(context,"File Downloaded",Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context,"File Not Downloaded",Toast.LENGTH_LONG).show();
            }
        });
    }
}
