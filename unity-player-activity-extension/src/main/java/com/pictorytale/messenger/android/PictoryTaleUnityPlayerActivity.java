package com.pictorytale.messenger.android;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.google.firebase.MessagingUnityPlayerActivity;

public class PictoryTaleUnityPlayerActivity extends MessagingUnityPlayerActivity
{
	public static final int SHARE_REQUEST_CODE = 123;

	public static PictoryTaleUnityPlayerActivity instance;

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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PictoryTaleUnityPlayerActivity.SHARE_REQUEST_CODE) {
			NativeShare.sendMessageToUnityObject(NativeShare.unitySuccessCallbackName, "requestCode=" + requestCode);
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

	public int getScreenWidth()
	{
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size.x;
	}

	public int getScreenHeight()
	{
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size.y;
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
