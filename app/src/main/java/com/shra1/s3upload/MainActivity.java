package com.shra1.s3upload;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import java.io.File;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ID = 121;
    public static final String TAG = "SHraX";
    public static final String accessKey = "AKIAJCRH7DL5W2ESKVAQ";
    public static final String secretKey = "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";
    public static final String bucket = "tippingpoint";
    public static final String AWS_CONTENT_TYPE_IMAGE = "image/*";
    String picturePath;
    ProgressDialog p;
    private ImageView ivMASelectedImage;
    private Button bMABrowse;
    private Button bMAUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        p = new ProgressDialog(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 232);
        } else {
            MAIN();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "please grant all permissions", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }
        MAIN();
    }

    private void MAIN() {
        ivMASelectedImage = (ImageView) findViewById(R.id.ivMASelectedImage);
        bMABrowse = (Button) findViewById(R.id.bMABrowse);
        bMAUpload = (Button) findViewById(R.id.bMAUpload);

    }

    public void bMABrowse(View view) {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUEST_ID);
    }

    public void bMAUpload(View view) {
        Utils.uploadToS3(
                MainActivity.this,
                accessKey,
                secretKey,
                bucket,
                "logos/" + System.currentTimeMillis() + ".jpg",
                picturePath,
                AWS_CONTENT_TYPE_IMAGE,
                ".jpg",
                new Utils.UploadToS3Callback() {
                    @Override
                    public void onUploadStarted() {
                        p.setMessage("Uploading please wait...");
                        p.show();
                    }

                    @Override
                    public void onUploadCompleted() {
                        p.dismiss();
                    }

                    @Override
                    public void onUploadSuccessfull(String uploadedFileURL) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("URL");
                        builder.setMessage(uploadedFileURL);
                        builder.create().show();
                    }

                    @Override
                    public void onUploadFailed() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Error");
                        builder.setMessage("Error");
                        builder.create().show();
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ID && resultCode == Activity.RESULT_OK) {

            picturePath = Utils.getPicturePathFromURI(MainActivity.this, data.getData());

            ivMASelectedImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        }
    }


}
