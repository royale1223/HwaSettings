package com.cyanogenmod.settings.device.hwa;

import java.io.File;
import java.io.IOException;

import android.app.ActivityManager;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class PackageListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener,
		OnItemClickListener {

	protected static final String TAG = "PackageListFragment";
	private static final int PACKAGE_LIST_LOADER = 0;
	private PackageListAdapater adapter;
	private SearchView mSearchView;
	protected Context mContext;
	private String query = "";
	private ListView mListView;
	private ActivityManager mActivityManager;
	private ContentResolver mContentResolver;
	private LoaderManager mLoaderManager;
	private CheckBox checkBox;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = getActivity();
		mListView = (ListView) getListView();
		mActivityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		mContentResolver = mContext.getContentResolver();
		mLoaderManager = getLoaderManager();
		mSearchView = (SearchView) getActivity().findViewById(
				R.id.hwa_package_list_search_view);
		String[] from = new String[] { PackageListProvider.APPLICATION_LABEL,
				PackageListProvider.PACKAGE_NAME,
				PackageListProvider.HWA_DISABLED, PackageListProvider._ID };
		int[] to = new int[] { R.id.hwa_settings_name,
				R.id.hwa_settings_packagename, R.id.hwa_settings_blocked };
		adapter = new PackageListAdapater(getActivity(),
				R.layout.hwa_settings_row, null, from, to,
				SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		setListAdapter(adapter);
		mListView.setTextFilterEnabled(true);
		mListView.setOnItemClickListener(this);
		mSearchView.setOnQueryTextListener(this);
		mSearchView.setSubmitButtonEnabled(false);
		startLoading();
	}

	@Override
	public void onResume() {
		super.onResume();
		restartLoading();
	}

	private void startLoading() {
		adapter.notifyDataSetChanged();
		getListView().invalidateViews();
		mLoaderManager.initLoader(PACKAGE_LIST_LOADER, null, this);
	}

	private void restartLoading() {
		mLoaderManager.restartLoader(PACKAGE_LIST_LOADER, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader cursorLoader = new CursorLoader(
				mContext,
				PackageListProvider.CONTENT_URI,
				null,
				(!TextUtils.isEmpty(query) ? PackageListProvider.APPLICATION_LABEL
						+ " LIKE '%" + query + "%'"
						: null), null, PackageListProvider.APPLICATION_LABEL);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		adapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
		if (query == null && newFilter == null) {
			return true;
		}
		if (query != null && query.equals(newFilter)) {
			return true;
		}
		query = newFilter;
		if (newText.length() > 0)
			mListView.setFilterText(newText);
		else
			mListView.clearTextFilter();
		restartLoading();
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		checkBox = (CheckBox) view.findViewById(R.id.hwa_settings_blocked);
		TextView tv = (TextView) view
				.findViewById(R.id.hwa_settings_packagename);
		String packageName = (String) tv.getText();
		if (checkBox.isChecked()) {
			checkBox.setChecked(false);
			boolean disabled = disableHwa(packageName);
			if (disabled)
				Toast.makeText(
						mContext,
						mContext.getString(
								R.string.hwa_settings_hwa_disabled_toast,
								packageName), Toast.LENGTH_SHORT).show();
			else {
				Toast.makeText(
						mContext,
						mContext.getString(
								R.string.hwa_settings_hwa_disable_failed_toast,
								packageName), Toast.LENGTH_SHORT).show();
				checkBox.setChecked(true);
			}
		} else {
			checkBox.setChecked(true);
			boolean enabled = enableHwa(packageName);
			if (enabled)
				Toast.makeText(
						mContext,
						mContext.getString(
								R.string.hwa_settings_hwa_enabled_toast,
								packageName), Toast.LENGTH_SHORT).show();

			else {
				Toast.makeText(
						mContext,
						mContext.getString(
								R.string.hwa_settings_hwa_enable_failed_toast,
								packageName), Toast.LENGTH_SHORT).show();
				checkBox.setChecked(false);
			}
		}
		mActivityManager.killBackgroundProcesses(packageName);
	}

	private boolean enableHwa(String packageName) {
		boolean enabled = false;
		File file = new File("/data/local/hwui.deny/" + packageName);
		if (file.exists()) {
			enabled = file.delete();
		} else
			enabled = true;
		if (enabled) {
			ContentValues values = new ContentValues();
			values.put(PackageListProvider.HWA_DISABLED, "false");
			mContentResolver.update(
					Uri.parse("content://" + PackageListProvider.AUTHORITY
							+ "/" + PackageListProvider.BASE_PATH + "/package/"
							+ packageName), values, null, null);
			mContentResolver
					.notifyChange(PackageListProvider.CONTENT_URI, null);
		}
		return enabled;
	}

	private boolean disableHwa(String packageName) {
		boolean disabled = false;
		File file = new File("/data/local/hwui.deny/" + packageName);
		if (!file.exists()) {
			try {
				disabled = file.createNewFile();
			} catch (IOException e) {
				Log.w(TAG, "Creation of /data/local/hwui.deny/" + packageName
						+ " failed : IOException");
			}
		} else
			disabled = true;
		if (disabled) {
			ContentValues values = new ContentValues();
			values.put(PackageListProvider.HWA_DISABLED, "true");
			mContentResolver.update(
					Uri.parse("content://" + PackageListProvider.AUTHORITY
							+ "/" + PackageListProvider.BASE_PATH + "/package/"
							+ packageName), values, null, null);
			mContentResolver
					.notifyChange(PackageListProvider.CONTENT_URI, null);
		}
		return disabled;
	}
}
