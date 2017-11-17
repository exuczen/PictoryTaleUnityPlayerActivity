package com.pictorytale.messenger.android;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.MessagingUnityPlayerActivity;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.unity3d.player.UnityPlayer;

public class PictoryTaleUnityPlayerActivity extends MessagingUnityPlayerActivity implements
		GoogleApiClient.OnConnectionFailedListener
{
	private static final String TAG = PictoryTaleUnityPlayerActivity.class.getSimpleName();

	private static String googleTokenReceiverUnityGameObjectName = "GoogleTokenMessageReceiver";
	public static final String googleTokenReceivedUnitySuccessCallbackName = "OnDone";
	public static final String googleTokenReceivedUnityErrorCallbackName = "OnError";


	public static final int SHARE_FILE_REQUEST_CODE = 123;
	public static final int GOOGLE_SIGN_IN_REQUEST_CODE = 124;

	// [START declare_auth]
//	private FirebaseAuth mAuth;
	// [END declare_auth]

	private GoogleApiClient mGoogleApiClient;

	public static PictoryTaleUnityPlayerActivity instance;

	public static PictoryTaleUnityPlayerActivity getInstance()
	{
		return instance;
	}

	public static void setGoogleTokenReceiverUnityObjectName(String name) {
		googleTokenReceiverUnityGameObjectName = name;
	}

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
		com.google.android.gms.auth.api.signin.internal.SignInConfiguration asd;
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
//				firebaseAuthWithGoogle(account);
				sendMessageToUnityObject(googleTokenReceiverUnityGameObjectName, googleTokenReceivedUnitySuccessCallbackName, account.getIdToken());
			} else {
				// Google Sign In failed, update UI appropriately
				// [START_EXCLUDE]
				// [END_EXCLUDE]
				sendMessageToUnityObject(googleTokenReceiverUnityGameObjectName, googleTokenReceivedUnitySuccessCallbackName, "");
			}
		}
	}

	public void signInToGoogleAccount(String defaultWebClientId)
	{
		Log.e(TAG, "signInToGoogleAccount: defaultWebClientId=" +defaultWebClientId);
		// [START config_signin]
		// Configure Google Sign In
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(defaultWebClientId)
				.requestEmail()
				.build();
		// [END config_signin]

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				//.enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
				.addOnConnectionFailedListener(this)
				.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
				.build();

		// [START initialize_auth]
//		mAuth = FirebaseAuth.getInstance();
		// [END initialize_auth]
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
		startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE);
	}

	// [START auth_with_google]
//	private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
//		Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
//
//		AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
//		mAuth.signInWithCredential(credential)
//				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//					@Override
//					public void onComplete(@NonNull Task<AuthResult> task) {
//						if (task.isSuccessful()) {
//							// Sign in success, update UI with the signed-in user's information
//							Log.d(TAG, "signInWithCredential:success");
//							FirebaseUser user = mAuth.getCurrentUser();
//
//						} else {
//							// If sign in fails, display a message to the user.
//							Log.w(TAG, "signInWithCredential:failure", task.getException());
//							Toast.makeText(PictoryTaleUnityPlayerActivity.this, "Authentication failed.",
//									Toast.LENGTH_SHORT).show();
//
//						}
//
//						// [START_EXCLUDE]
//						//hideProgressDialog();
//						// [END_EXCLUDE]
//					}
//				});
//	}
	// [END auth_with_google]

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

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		// An unresolvable error has occurred and Google APIs (including Sign-In) will not
		// be available.
		Log.d(TAG, "onConnectionFailed:" + connectionResult);
		Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
	}
}
