package com.pictorytale.messenger.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

import java.io.File;

/**
 * Created by Marcin on 21.11.2017.
 */

public class ViewIntentActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            if (bundle.containsKey("videoPath")) {
                String videoPath = bundle.getString("videoPath");
                Log.e("ViewIntentActivity", "onCreate: videoPath=" + videoPath);
                playVideo(videoPath);
            } else if (bundle.containsKey("envDirType")) {
                String envDirType = bundle.getString("envDirType");
                Log.e("ViewIntentActivity", "onCreate: envDirType=" + envDirType);
                openFolder(envDirType);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
        } else if (resultCode == RESULT_CANCELED) {
        } else {
        }
        finish();
    }

    private void playVideo(String filePath) {
        File videoFile = new File(filePath);
        if (videoFile.exists()) {
            Log.e("ViewIntentActivity", "playVideo: videoFile exists");
            Uri uri = Uri.fromFile(videoFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            //Uri uri = Uri.parse(filePath);
            //Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            String mimeType = "video/mp4";
            intent.setDataAndType(uri, mimeType);
            startActivityForResult(intent, PictoryTaleUnityPlayerActivity.PLAY_VIDEO_REQUEST_CODE);
        }
    }

    private void openFolder(String envDirType) {

        File dirPath = Environment.getExternalStoragePublicDirectory(envDirType);
        if (dirPath.exists()) {
            String mimeType;
            Log.e("ViewIntentActivity", "openFolder: " + envDirType + " folder exists");
            if (envDirType == Environment.DIRECTORY_MOVIES)
                mimeType = "video/mp4";
            else
                mimeType = "*/*";
            Uri uri = Uri.parse(dirPath.getAbsolutePath());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            startActivityForResult(intent, PictoryTaleUnityPlayerActivity.VIEW_DIR_REQUEST_CODE);
        }
    }


}
