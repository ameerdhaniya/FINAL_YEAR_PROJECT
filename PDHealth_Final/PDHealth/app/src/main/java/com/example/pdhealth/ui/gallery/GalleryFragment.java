package com.example.pdhealth.ui.gallery;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.pdhealth.R;
import com.example.pdhealth.Remote.RetrofitClient;
import com.example.pdhealth.Remote.UploadAPI;
import com.example.pdhealth.Result;
import com.example.pdhealth.Utils.ProgressRequestBody;
import com.example.pdhealth.Utils.UploadCallBacks;

import java.io.File;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.pdhealth.login;
import com.example.pdhealth.signup;
import com.ipaulpro.afilechooser.utils.FileUtils;

public class GalleryFragment extends Fragment implements UploadCallBacks {

    private GalleryViewModel galleryViewModel;

    public static final String BASE_URL = "http://9d4df69a.ngrok.io/";
    private static final int REQUEST_PERMISSION = 1000;
    private static final int PICK_FILE_REQUEST = 1001;

    private ProgressDialog progressDialog;
    private UploadAPI mService;
    private Button btnUpload;
    private ImageView imageView;

    private Uri selectedFileUrl;
    private String temp;

    private ProgressDialog dialog;

    private UploadAPI getAPIUpload()
    {
        //return RetrofitCient.getClient(BASE_URL).create(UploadAPI.class);
        return RetrofitClient.getClient(BASE_URL).create(UploadAPI.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);



        progressDialog=new ProgressDialog(getActivity());

        if(ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(getActivity(),new String[]{
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
            },REQUEST_PERMISSION);
        }

        mService = getAPIUpload();

        btnUpload = root.findViewById(R.id.btn_upload);
        imageView = root.findViewById(R.id.image_view);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFile();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
            }
        });


        return root;
    }

    private void uploadFile() {
        if(selectedFileUrl != null)
        {
            dialog = new ProgressDialog(getContext());
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMessage("Uploading.....");
            dialog.setIndeterminate(false);
            dialog.setMax(100);
            dialog.setCancelable(false);
            dialog.show();


            File file;
            file = com.ipaulpro.afilechooser.utils.FileUtils.getFile(getContext(),selectedFileUrl);
            ProgressRequestBody requestFile = new ProgressRequestBody(this,file);

            temp=file.getName();
            final MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file",file.getName(),requestFile);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mService.uploadFile(body)
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response){
                                    dialog.dismiss();
                                    //Toast.makeText(getContext(),"Uploaded !"+temp,Toast.LENGTH_LONG).show();
                                    showresult(temp);
                                    //imageView.setImageURI(null);
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    dialog.dismiss();
                                    Toast.makeText(getContext(),t.getMessage(),Toast.LENGTH_LONG).show();
                                    //imageView.setImageURI(null);
                                }
                            });
                }
            }).start();
        }
    }

    private void showresult(String temp1) {
        progressDialog.setMessage("Loading Result");
        progressDialog.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                progressDialog.dismiss();
            }
        }, 3000); // 3000 milliseconds delay

        //temp1="Recording_9004171356_1234.wav";
        Toast.makeText(getContext(),"Uploaded !"+temp1,Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getActivity(),Result.class);
        intent.putExtra("filename",temp1);
        startActivity(intent);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_PERMISSION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(getContext(),"permission granted",Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getContext(),"permission denied",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK)
        {
            if(requestCode == PICK_FILE_REQUEST)
            {
                if(data != null)
                {
                    selectedFileUrl = data.getData();
                    if(selectedFileUrl != null && !selectedFileUrl.getPath().isEmpty())
                        imageView.setImageURI(selectedFileUrl);
                    else
                        Toast.makeText(getContext(),"Cannot Upload file to server",Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void chooseFile() {
        Intent intent = Intent.createChooser(FileUtils.createGetContentIntent(),"Select a file");
        startActivityForResult(intent,PICK_FILE_REQUEST);
    }

    @Override
    public void onProgressUpdate(int percentage) {
        dialog.setProgress(percentage);
    }
}