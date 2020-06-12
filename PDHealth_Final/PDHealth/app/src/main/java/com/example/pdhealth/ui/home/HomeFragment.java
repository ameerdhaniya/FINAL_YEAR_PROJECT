package com.example.pdhealth.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.pdhealth.R;
import com.example.pdhealth.login;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class HomeFragment extends Fragment {

    private int seconds = 0;
    private boolean running;
    private boolean wasRunning;


    private static final int REQUEST_PERMISSION = 1011;
    private static final int REQUEST_MICROPHONE = 1010;
    private static final int REQUEST_PERMISSION2 = 1012;
    private TextView timeView;
    private HomeViewModel homeViewModel;
    private TextView mRecordLabel;
    public MediaRecorder recorder;
    private String mFilename = null;
    private StorageReference mStorageRef;
    private ProgressDialog mProgress;
    private static int count=0;
    private String username;
    private String s;
    private ImageButton startButton;
    private ImageButton stopButton;
    private ImageButton resetButton;
    private static final String LOG_TAG = "Record_log";
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}; // List of permissions required

    public void askPermission(){
        for (String permission : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS, PERMISSION_ALL);
                return;
            }
        }
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);


        username = getActivity().getIntent().getStringExtra("UserID");

        mProgress=new ProgressDialog(getActivity());
        mRecordLabel = (TextView) root.findViewById(R.id.textrecord);
        mStorageRef = FirebaseStorage.getInstance().getReference();


        recorder = new MediaRecorder();
        startButton= root.findViewById(R.id.recordButton);
        stopButton= root.findViewById(R.id.stopButton);
        resetButton= root.findViewById(R.id.resetButton);
        timeView=root.findViewById(R.id.time_view);

       /* if (ActivityCompat.checkSelfPermission(getContext(),android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.RECORD_AUDIO},REQUEST_MICROPHONE);
        }

        if(ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(getActivity(),new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_PERMISSION);
        }
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(getActivity(),new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION2);
        }*/

       askPermission();

        if (savedInstanceState != null) {

            // Get the previous state of the stopwatch
            // if the activity has been
            // destroyed and recreated.
            seconds
                    = savedInstanceState
                    .getInt("seconds");
            running
                    = savedInstanceState
                    .getBoolean("running");
            wasRunning
                    = savedInstanceState
                    .getBoolean("wasRunning");
        }
        runTimer();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialogDemo();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetRecording();
            }
        });

        return root;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    //Do your work.
                } else {
                    Toast.makeText(getContext(), "Until you grant the permission, we cannot proceed further", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
        /*switch (requestCode)
        {
            case REQUEST_PERMISSION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getContext(),"permission granted",Toast.LENGTH_LONG).show();
                    startActivity(getActivity().getIntent());
                }

                else
                    Toast.makeText(getContext(),"permission denied",Toast.LENGTH_LONG).show();

            case REQUEST_PERMISSION2:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "permission granted", Toast.LENGTH_LONG).show();
                    startActivity(getActivity().getIntent());
                }
                else
                    Toast.makeText(getContext(),"permission denied",Toast.LENGTH_LONG).show();

            case REQUEST_MICROPHONE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "permission granted", Toast.LENGTH_LONG).show();
                    startActivity(getActivity().getIntent());
                }
                else
                    Toast.makeText(getContext(),"permission denied",Toast.LENGTH_LONG).show();
        }*/
    }
    private void startRecording() {
        count=ThreadLocalRandom.current().nextInt(1000,9999);
        s=String.valueOf(count);
        mFilename=null;
        mFilename = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilename += "/Recording_";
        mFilename +=username;
        mFilename +="_";
        mFilename +=s;
        mFilename +=".wav";

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setOutputFile(mFilename);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        onClickStart();
        recorder.start();
    }

    private void stopRecording() {

        onClickStop();
        recorder.stop();
        recorder.reset();
        //recorder.release();
        //recorder = null;
        Toast.makeText(getContext(),"File Saved", Toast.LENGTH_LONG).show();
        //uploadAudio();
        onClickReset();
        uploadtofirebase();
    }

    private void uploadtofirebase() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Upload to FireBase");
        builder.setMessage("Do you want to upload this file to FireBase?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                uploadAudio();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();

    }

    private void uploadAudio() {
        mProgress.setMessage("Uploading Audio...");
        mProgress.show();
        if(username.isEmpty())
            username="0000";

        String file="Recording_";
        file +=username;
        file +="_";
        file +=s;
        file +=".wav";
        StorageReference filepath=mStorageRef.child(username).child(s).child(file);
        Uri uri=Uri.fromFile(new File(mFilename));

        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mProgress.dismiss();
                mRecordLabel.setText("Uploading FInished.");
            }
        });
    }

    private void confirmDialogDemo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirm Saving File");
        builder.setMessage("Are you sure you want to save this file?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopRecording();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetRecording();
            }
        });

        builder.show();
    }

    private void resetRecording() {
        onClickReset();
        recorder.reset();

        File file=new File(mFilename);

        if (!file.delete()) {
            Toast.makeText(getContext(),"Error..", Toast.LENGTH_LONG).show();
        }

            Toast.makeText(getContext(), "File Not Saved", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onSaveInstanceState(
            Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState
                .putInt("seconds", seconds);
        savedInstanceState
                .putBoolean("running", running);
        savedInstanceState
                .putBoolean("wasRunning", wasRunning);
    }

    // If the activity is paused,
    // stop the stopwatch.
    @Override
    public void onPause()
    {
        super.onPause();
        wasRunning = running;
        running = false;
    }

    // If the activity is resumed,
    // start the stopwatch
    // again if it was running previously.
    @Override
    public void onResume()
    {
        super.onResume();
        if (wasRunning) {
            running = true;
        }
    }

    // Start the stopwatch running
    // when the Start button is clicked.
    // Below method gets called
    // when the Start button is clicked.
    public void onClickStart()
    {
        running = true;
    }

    // Stop the stopwatch running
    // when the Stop button is clicked.
    // Below method gets called
    // when the Stop button is clicked.
    public void onClickStop()
    {
        running = false;
    }

    // Reset the stopwatch when
    // the Reset button is clicked.
    // Below method gets called
    // when the Reset button is clicked.
    public void onClickReset()
    {
        running = false;
        seconds = 0;
    }

    // Sets the NUmber of seconds on the timer.
    // The runTimer() method uses a Handler
    // to increment the seconds and
    // update the text view.
    private void runTimer()
    {
        // Creates a new Handler
        final Handler handler
                = new Handler();

        // Call the post() method,
        // passing in a new Runnable.
        // The post() method processes
        // code without a delay,
        // so the code in the Runnable
        // will run almost immediately.
        handler.post(new Runnable() {
            @Override

            public void run()
            {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                // Format the seconds into hours, minutes,
                // and seconds.
                String time
                        = String
                        .format(Locale.getDefault(),
                                "%d:%02d:%02d", hours,
                                minutes, secs);

                // Set the text view text.
                timeView.setText(time);

                // If running is true, increment the
                // seconds variable.
                if (running) {
                    seconds++;
                }

                // Post the code again
                // with a delay of 1 second.
                handler.postDelayed(this, 1000);
            }
        });
    }

}