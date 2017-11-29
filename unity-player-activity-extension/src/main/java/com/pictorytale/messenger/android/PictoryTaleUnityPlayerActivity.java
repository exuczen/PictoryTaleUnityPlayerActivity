package com.pictorytale.messenger.android;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

	public static final int VIEW_DIR_REQUEST_CODE = 121;
	public static final int PLAY_VIDEO_REQUEST_CODE = 122;
	public static final int SHARE_FILE_REQUEST_CODE = 123;
	public static final int GOOGLE_SIGN_IN_REQUEST_CODE = 124;

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
		// Clear low profile flags to apply non-fullscreen mode before splash screen
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// https://stackoverflow.com/questions/29311078/android-completely-transparent-status-bar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		}

		this.setStatusBarColor(Color.TRANSPARENT);

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

		if (requestCode == PictoryTaleUnityPlayerActivity.SHARE_FILE_REQUEST_CODE)
		{
			sendMessageToUnityObject(NativeShare.unityGameObjectName, NativeShare.unitySuccessCallbackName, "requestCode=" + requestCode);
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

}
