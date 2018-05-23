package com.shra1.s3upload;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

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
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static String finalImagePath = "";

    public static void uploadToS3(Context mCtx, String ACCESS_KEY, String SECRET_KEY, String BUCKET, String S3_FILE_PATH, String LOCAL_FILE_PATH, String AWS_CONTENT_TYPE, String FILE_EXTENSION, final UploadToS3Callback c)
    {
        AmazonS3 amazonS3 = new AmazonS3Client(new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY));
        amazonS3.setRegion(Region.getRegion(Regions.AP_SOUTH_1));

        TransferUtility transferUtility = new TransferUtility(
                amazonS3, mCtx);

        TransferObserver transferObserver = transferUtility.upload(
                BUCKET,
                S3_FILE_PATH,
                new File(LOCAL_FILE_PATH),
                getS3ObjectMetadata(AWS_CONTENT_TYPE, S3_FILE_PATH, BUCKET));

        GeneratePresignedUrlRequest generatePresignedUrlRequest
                = new GeneratePresignedUrlRequest(BUCKET, S3_FILE_PATH);

        URL urlFileToBeUploaded
                = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

        String tempUrl[] = (urlFileToBeUploaded.toString().trim()).split(FILE_EXTENSION);
        if (tempUrl != null && tempUrl.length > 0) {
            finalImagePath = tempUrl[0] + FILE_EXTENSION;
        }
        c.onUploadStarted();
        transferObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                switch (state) {
                    case COMPLETED:
                        c.onUploadCompleted();
                        c.onUploadSuccessfull(finalImagePath);
                        break;
                    case FAILED:
                        c.onUploadCompleted();
                        c.onUploadFailed();
                        break;
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {

            }
        });
    }

    public static String getPicturePathFromURI(Context context, Uri data) {
        String picturePath = "";
        Uri selectedImage = data;
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = context.getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }

    /**
     * Gets the object meta data which needs to be set for uploaded media
     *
     * @param contentType
     * @param userMetaData
     * @return
     */
    public static ObjectMetadata getS3ObjectMetadata(String contentType, String userMetaData, String bucket) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
        objectMetadata.setContentType(contentType);
        Map<String, String> userMetadata = new HashMap<String, String>();
        userMetadata.put(bucket, userMetaData);
        objectMetadata.setUserMetadata(userMetadata);
        return objectMetadata;
    }


    public interface UploadToS3Callback {
        public void onUploadStarted();

        public void onUploadCompleted();

        public void onUploadSuccessfull(String uploadedFileURL);

        public void onUploadFailed();
    }
}
