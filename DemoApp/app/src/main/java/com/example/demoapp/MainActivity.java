package com.example.demoapp;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class MainActivity extends AppCompatActivity {

    Button showFile;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    TextView textView;
    File pathFile = android.os.Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);
    Context context;
    String content = "";
    StorageReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showFile = findViewById(R.id.button);
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
        String filename = pathFile.toString() + "/" + "results.txt";

        try {
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
        textView.setText(byteArrayOutputStream.toString());
    }

    public void download()
    {
        storageReference = firebaseStorage.getInstance().getReference();
        ref = storageReference.child("results.txt");
        ref.getDownloadUrl().addOnSuccessListener( new OnSuccessListener<Uri>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSuccess(Uri uri) {
                String url = uri.toString();
                downloadfiles(MainActivity.this,"results",".txt",pathFile.toString(),url,ref);
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
