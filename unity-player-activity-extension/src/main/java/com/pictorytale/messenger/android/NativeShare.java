package com.pictorytale.messenger.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.unity3d.player.UnityPlayer;

import java.io.File;

/**
 * Created by yasirkula on 22.06.2017.
 */

public class NativeShare
{
	public static String unityGameObjectName = "VideoSharingMessageReceiver";
	public static final String unitySuccessCallbackName = "OnDone";
	public static final String unityErrorCallbackName = "OnError";

	/**
	 * this method is called in Unity
	 * @param name
	 */
	public static void setUnityObjectName(String name) {
		unityGameObjectName = name;
	}

	public static void shareFile(Activity context, String mediaPath, String authority) {
		if (mediaPath != null && mediaPath.length() > 0) {
			File file = new File(mediaPath);

			Log.e("NativeShare", "shareFile: file.getAbsolutePath=" + file.getAbsolutePath());
			Log.e("NativeShare", "shareFile: file.exists=" + file.exists());
			if (file.exists())
				Log.e("NativeShare", "shareFile: file.length=" + file.length());
			//Uri contentUri = UnitySSContentProvider.getUriForFile( context, authority, file);
			//Uri contentUri = Uri.fromFile(file);
			Uri contentUri = FileProvider.getUriForFile(context, authority, file);
			Log.e("NativeShare", "shareFile: contentUri.getPath=" + contentUri.getPath());

			Intent intent = new Intent(Intent.ACTION_SEND);
			if (contentUri != null) {
				intent.putExtra(Intent.EXTRA_STREAM, contentUri);
				String mimeType = context.getContentResolver().getType(contentUri);
				intent.setType(mimeType);
				intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				// Put the Uri and MIME type in the result Intent
				//			intent.setDataAndType(contentUri, mimeType);
				//			if( isMediaImage )
				//				mimeType = "image/*";
				//			else
				//				mimeType = "video/mp4";
				intent = Intent.createChooser(intent, "");
				//intent.setClass(context, NativeShareActivity.class);
				//context.startActivity(intent);
				context.startActivityForResult(intent, PictoryTaleUnityPlayerActivity.SHARE_FILE_REQUEST_CODE);
				//context.setResult(Activity.RESULT_OK, intent);
			} else {
				intent.setDataAndType(null, "");
				context.setResult(Activity.RESULT_CANCELED, intent);
			}
		}
	}
}


