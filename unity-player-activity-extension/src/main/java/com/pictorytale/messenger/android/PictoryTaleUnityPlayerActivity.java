package com.pictorytale.messenger.android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.google.firebase.MessagingUnityPlayerActivity;

public class PictoryTaleUnityPlayerActivity extends MessagingUnityPlayerActivity
{
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

		showSystemUi();
		addUiVisibilityChangeListener();
		PictoryTaleUnityPlayerActivity.instance = this;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == NativeShare.SHARE_REQUEST_CODE) {
			NativeShare.sendMessageToUnityObject(NativeShare.unitySuccessCallbackName, "requestCode=" + requestCode);
		}
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
		// Works from API level 11
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
			return;

		mUnityPlayer.setSystemUiVisibility(mUnityPlayer.getSystemUiVisibility() & ~getLowProfileFlag());
	}

	private void addUiVisibilityChangeListener()
	{
		// Works from API level 11
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
			return;

		mUnityPlayer.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
		{
			@Override
			public void onSystemUiVisibilityChange(final int visibility)
			{
				// Whatever changes - force status/nav bar to be visible
				showSystemUi();
			}
		});
	}

	/**
	 * this method is called in Unity
	 * @param name
	 */
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
