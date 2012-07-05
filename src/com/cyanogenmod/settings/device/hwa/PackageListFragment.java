package com.cyanogenmod.settings.device.hwa;

import java.util.List;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import com.cyanogenmod.settings.device.hwa.R;

public class PackageListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = "PackageListFragment";
	private Context mContext;
	private PackageManager packageManager;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = getActivity();
		packageManager = mContext.getPackageManager();
		Log.d(TAG, "dump test");
		List<PackageInfo> packages = packageManager
				.getInstalledPackages(PackageManager.GET_META_DATA);
		Log.d(TAG, "there are " + String.valueOf(packages.size()) + "packages");
		for (PackageInfo packageInfo : packages) {
			Log.i(TAG, packageInfo.packageName + " : "
					+ packageInfo.applicationInfo.packageName + ": "
					+ packageManager.getApplicationLabel(packageInfo.applicationInfo));
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub

	}

}
