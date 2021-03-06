package com.pictorytale.messenger.android;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.firebase.MessagingUnityPlayerActivity;
import com.unity3d.player.UnityPlayer;

public class PictoryTaleUnityPlayerActivity extends MessagingUnityPlayerActivity
{
	private static final String TAG = PictoryTaleUnityPlayerActivity.class.getSimpleName();

	public static final int GRANT_PERMISSIONS = 1;

	public static final int VIEW_DIR_REQUEST_CODE = 121;
	public static final int PLAY_VIDEO_REQUEST_CODE = 122;
	public static final int SHARE_FILE_REQUEST_CODE = 123;
	public static final int GOOGLE_SIGN_IN_REQUEST_CODE = 124;
	public static final int MOVE_ACTIVITY_BACK_AND_FORTH_REQUEST_CODE = 125;

	public static PictoryTaleUnityPlayerActivity instance;

	public static PictoryTaleUnityPlayerActivity getInstance()
	{
		return instance;
	}

	private GoogleAuth googleAuth;

	/**
	 * Send message to Unity's GameObject
	 * @param method name of the method in GameObject's script
	 * @param message the actual message
	 */
	public static void sendMessageToUnityObject(String objectName, String method, String message){
		UnityPlayer.UnitySendMessage(objectName, method, message);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setFullScreen(false);
		setStatusBarColor(Color.TRANSPARENT);
		//showSystemUi();
		setSystemUiVisibility(true, true);
		addUiVisibilityChangeListener();
		PictoryTaleUnityPlayerActivity.instance = this;
	}
	@Override
	protected void onStart()
	{
		super.onStart();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.d(TAG, "onActivityResult: requestCode=" + requestCode + " resultCode=" + resultCode);

		if (requestCode == PictoryTaleUnityPlayerActivity.SHARE_FILE_REQUEST_CODE)
		{
			String appData = "None";
			if (data != null) {
				String dataString = data.getDataString();
				if (dataString != null && !dataString.isEmpty()) {
					String[] appDataArray = dataString.replace("content://", "").split("/");
					if (appDataArray != null && appDataArray.length > 0)
						appData = appDataArray[0];
				}
			}
			sendMessageToUnityObject(NativeShare.unityGameObjectName, NativeShare.unitySuccessCallbackName, appData);
		}
		else if (requestCode == PictoryTaleUnityPlayerActivity.GOOGLE_SIGN_IN_REQUEST_CODE)
		{
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
			if (result.isSuccess()) {
				// Google Sign In was successful, authenticate with Firebase
				GoogleSignInAccount account = result.getSignInAccount();
				Log.w(TAG, "onActivityResult: account.getEmail()=" + account.getEmail());
				if (googleAuth != null) {
					googleAuth.clientIsSignedIn = true;
					//googleAuth.firebaseAuthWithGoogle(this, account);
					googleAuth.getAccessToken(account);
				}
			} else {
				// Google Sign In failed, update UI appropriately
				// [START_EXCLUDE]
				// [END_EXCLUDE]
				if (googleAuth != null) {
					googleAuth.disconnectFromGoogleAccount();
				}
				sendMessageToUnityObject(GoogleAuth.unityGameObjectName, GoogleAuth.unityErrorCallbackName, "");
			}
		}
		else if (requestCode == PictoryTaleUnityPlayerActivity.MOVE_ACTIVITY_BACK_AND_FORTH_REQUEST_CODE)
		{
			// resultCode for this requestCode is RESULT_CANCELED = 0
			Intent intent = new Intent(this, PictoryTaleUnityPlayerActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);

			//			Intent intent = new Intent(this, PictoryTaleUnityPlayerActivity.class);
			//			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // You need this if starting the activity from a service
			//			intent.setAction(Intent.ACTION_MAIN);
			//			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			//			startActivity(intent);
		}
	}

	public void moveActivityToBackAndForth()
	{
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivityForResult(startMain, MOVE_ACTIVITY_BACK_AND_FORTH_REQUEST_CODE);
	}

	public void playVideo(String videoPath)
	{
		Intent intent = new Intent(this, ViewIntentActivity.class);
		intent.putExtra("videoPath", videoPath);
		startActivity(intent);
	}

	public void openMoviesFolder()
	{
		Intent intent = new Intent(this, ViewIntentActivity.class);
		intent.putExtra("envDirType", Environment.DIRECTORY_MOVIES);
		startActivity(intent);
	}


	public void getGmailAccessToken(String unityGameObjectName, String clientSecretJSONString, String googleTokenEndpoint) {
		googleAuth = new GoogleAuth(this, unityGameObjectName, clientSecretJSONString, googleTokenEndpoint);
		googleAuth.connectWithGoogleAccount(this);
	}

	public void releaseGmailAccessToken()
	{
		if (googleAuth != null)
		{
			googleAuth.signOutFromGoogleAccount();
			googleAuth = null;
		}
	}

	public String GetNetworkCountyIso()
	{
		TelephonyManager mTelephonyMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		return mTelephonyMgr.getNetworkCountryIso();
	}

	public String GetNetworkOperatorMCC()
	{
		TelephonyManager mTelephonyMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		String mccmnc = mTelephonyMgr.getNetworkOperator();
		if (!TextUtils.isEmpty(mccmnc) && mccmnc.length() >= 3)
			return mccmnc.substring(0, 3);
		else
			return mccmnc;
	}

	public String GetNetworkOperatorName()
	{
		TelephonyManager mTelephonyMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		return mTelephonyMgr.getNetworkOperatorName();
	}

	// Clear low profile flags to apply non-fullscreen mode before splash screen
	public void setFullScreen(boolean value) {
		if (value)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		else
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	}

	public void setFullScreenOnUiThread(final boolean value) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (instance != null)
					instance.setFullScreen(value);
			}
		});
	}

	public void setSystemUiVisibility(boolean statusBarVisible, boolean navBarVisible)
	{
		int systemUiFlag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
		systemUiFlag |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
		systemUiFlag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
		if (!statusBarVisible) {
			systemUiFlag |= View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar
		}
		if (!navBarVisible) {
			systemUiFlag |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION; // hide nav bar
		}
		if ((!navBarVisible || !statusBarVisible) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			systemUiFlag |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		}
		mUnityPlayer.setSystemUiVisibility(systemUiFlag);
	}

	private static int getLowProfileFlag()
	{
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
				?
				View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
						View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
						View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
						View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
						View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
						View.SYSTEM_UI_FLAG_FULLSCREEN
				:
				View.SYSTEM_UI_FLAG_LOW_PROFILE;
	}

	private void showSystemUi()
	{
		mUnityPlayer.setSystemUiVisibility(mUnityPlayer.getSystemUiVisibility() & ~getLowProfileFlag());
	}

	private void addUiVisibilityChangeListener()
	{
		mUnityPlayer.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
		{
			@Override
			public void onSystemUiVisibilityChange(final int visibility)
			{
				// Whatever changes - force status/nav bar to be visible
				//showSystemUi();
				setSystemUiVisibility(true, true);
			}
		});
	}

	private DisplayMetrics getDisplayMetrics() {
		//		DisplayMetrics metrics = new DisplayMetrics();
		//		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		//		return metrics;
		return getResources().getDisplayMetrics();
	}

	private Display getDefaultDisplay()
	{
		return getWindowManager().getDefaultDisplay();
	}

	public int getScreenWidth()
	{
		Point size = new Point();
		getDefaultDisplay().getSize(size);
		return size.x;
	}

	public int getScreenHeight()
	{
		Point size = new Point();
		getDefaultDisplay().getSize(size);
		return size.y;
	}

	public int getHeightPixels()
	{
		return getDisplayMetrics().heightPixels;
	}

	private int getStatusBarHeightResourceId()
	{
		return getResources().getIdentifier("status_bar_height", "dimen", "android");
	}

	private int getNavigationBarHeightResourceId()
	{
		return getResources().getIdentifier("navigation_bar_height", "dimen", "android");
	}

	public float getStatusBarHeight() {
		int resourceId = getStatusBarHeightResourceId();
		if (resourceId > 0) {
			return getResources().getDimension(resourceId);
		}
		return 0;
	}

	public float getStatusBarPixelHeight() {
		int resourceId = getStatusBarHeightResourceId();
		if (resourceId > 0) {
			return getResources().getDimensionPixelSize(resourceId);
		}
		return 0;
	}

	public float getNavigationBarHeight()
	{
		int resourceId = getNavigationBarHeightResourceId();
		if (resourceId > 0) {
			return getResources().getDimension(resourceId);
		}
		return 0;
	}

	public int getNavigationBarPixelHeight()
	{
		int resourceId = getNavigationBarHeightResourceId();
		if (resourceId > 0) {
			return getResources().getDimensionPixelSize(resourceId);
		}
		return 0;
	}

	public boolean hasNavigationBar()
	{
		Resources resources = getResources();
		int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
		return id > 0 && resources.getBoolean(id);
		//		boolean hasMenuKey = ViewConfiguration.get(this).hasPermanentMenuKey();
		//		boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
		//		return !hasMenuKey && !hasBackKey; // Do whatever you need to do, this device has a navigation bar
	}

	public float getScreenYDPI()
	{
		return getDisplayMetrics().ydpi; // The exact physical pixels per inch of the screen in the Y dimension.
	}

	public float getScreenDensity()
	{
		return getDisplayMetrics().density;
	}

	public float getScreenScaledDensity()
	{
		return getDisplayMetrics().scaledDensity;
	}

	public int getScreenDensityDPI()
	{
		return getDisplayMetrics().densityDpi;
	}

	private void setStatusBarColor(int color) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(color);
		}
	}

	public void setStatusBarThemeTransparent()
	{
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (instance!=null)
					instance.setTheme(R.style.UnityStatusBarThemeTransparent);
				setStatusBarColor(Color.TRANSPARENT);
				//Log.e("setStatusBarThemeLight", "setStatusBarThemeLight");
			}
		});
	}

	public void setStatusBarThemeLight()
	{
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (instance!=null)
					instance.setTheme(R.style.UnityStatusBarThemeLight);
				setStatusBarColor(Color.LTGRAY);
				//Log.e("setStatusBarThemeDark", "setStatusBarThemeDark");
			}
		});
	}

	public boolean grantPermissions(String[] permissions)
	{
		boolean hasAllPermissions = hasPermissions(this, permissions);
		if (hasAllPermissions)
		{
			for (int i = 0; i < permissions.length; i++) {
				sendMessageToUnityObject("PermissionsMessageReceiver", "OnGranted", permissions[i]);
			}
		}
		else
		{
			ActivityCompat.requestPermissions(this, permissions, GRANT_PERMISSIONS);
		}
		return hasAllPermissions;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, final String permissions[], int[] grantResults) {
		switch (requestCode) {
			case GRANT_PERMISSIONS: {
				// If request is cancelled, the result arrays are empty.
				boolean allPermissionsGranted = true;
				if (grantResults.length > 0) {
					for (int i=0; i<grantResults.length; i++)
					{
						if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
						{
							sendMessageToUnityObject("PermissionsMessageReceiver", "OnGranted", permissions[i]);
						}
						else
						{
							sendMessageToUnityObject("PermissionsMessageReceiver", "OnDenied", permissions[i]);
							allPermissionsGranted = false;
						}
					}
				} else {
					for (int i=0; i<permissions.length; i++)
					{
						sendMessageToUnityObject("PermissionsMessageReceiver", "OnDenied", permissions[i]);
					}
					allPermissionsGranted = false;
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
				}
				if (!allPermissionsGranted)
				{
					AlertDialog.Builder alertDialog = Utils.BuildAlertDialog(this, "Permissions required", "You have to grant all requested permissions.");
					alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(instance, permissions, GRANT_PERMISSIONS);
							//instance.finish();
						}
					});
					alertDialog.show();
				}
			}
			// other 'case' lines to check for other
			// permissions this app might request.
		}
	}

	public static boolean hasPermissions(Context context, String... permissions) {
		if (context != null && permissions != null) {
			for (String permission : permissions) {
				if (!hasPermission(context, permission)) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean hasPermission(Context context, String permission)
	{
		if (Build.VERSION.SDK_INT >= 23) {
			//return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
			return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
		} else {
			//PackageManager pm = context.getPackageManager();
			//return pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED;
			return PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED;
		}
	}

	public long getTotalMemory() {
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);
		return memoryInfo.totalMem;
	}

}
