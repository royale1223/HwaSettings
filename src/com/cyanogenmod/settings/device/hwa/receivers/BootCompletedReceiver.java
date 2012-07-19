package com.cyanogenmod.settings.device.hwa.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.cyanogenmod.settings.device.hwa.DatabaseHelper;
import com.cyanogenmod.settings.device.hwa.PackageListProvider;

public class BootCompletedReceiver extends BroadcastReceiver {

	protected static final String TAG = "BootCompletedReceiver";
	private ContentResolver mContentResolver;
	private SharedPreferences mPreferences;
	private Context mContext;

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		mContentResolver = context.getContentResolver();
		mPreferences = context.getSharedPreferences("preferences",
				Context.MODE_PRIVATE);
		boolean firstTime = mPreferences.getBoolean("firstTime", true);
		if (firstTime) {
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putBoolean("firstTime", false);
			editor.commit();
			new CreateDatabase().execute();
		} else
			new ScanForPackages().execute();
	}

	private class ScanForPackages extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			mContentResolver.insert(PackageListProvider.SCAN_URI, null);
			return null;
		}
	}

	private class CreateDatabase extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			new DatabaseHelper(mContext);
			return null;
		}
	}
}
