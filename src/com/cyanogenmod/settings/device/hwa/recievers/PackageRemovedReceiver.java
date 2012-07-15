package com.cyanogenmod.settings.device.hwa.recievers;

import com.cyanogenmod.settings.device.hwa.PackageListProvider;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class PackageRemovedReceiver extends BroadcastReceiver {

	private static final String TAG = "PackagesMonitor";
	private ContentResolver mContentResolver;
	private String mPackageName;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "Received broadcast : " + intent.toString());
		mContentResolver = context.getContentResolver();
		mPackageName = intent.getDataString().split(":")[1];
		new AddPackage().execute();
	}

	private class AddPackage extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			mContentResolver.delete(Uri.withAppendedPath(
					PackageListProvider.PACKAGE_URI, mPackageName),
					null, null);
			return null;
		}
	}
}
