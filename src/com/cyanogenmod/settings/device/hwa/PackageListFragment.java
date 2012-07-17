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
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class PackageListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener,
		OnItemClickListener, ListView.OnScrollListener {

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
	private CheckBox hwaCheck;
	private boolean mBusy;

	private LayoutInflater mInflater;
	private Cursor mCursor;
	private PackageManager mPackageManager;

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
				PackageListProvider.HWA_ENABLED, PackageListProvider._ID };
		int[] to = new int[] { R.id.hwa_settings_name,
				R.id.hwa_settings_packagename, R.id.hwa_settings_enabled };
		adapter = new PackageListAdapater(getActivity(),
				R.layout.hwa_settings_row, null, from, to, 0);
		setListAdapter(adapter);
		setListShown(false);
		mListView.setTextFilterEnabled(true);
		mListView.setOnItemClickListener(this);
		mListView.setOnScrollListener(this);
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
						+ " LIKE '" + query + "%'"
						: null), null, PackageListProvider.HWA_ENABLED + ", "
						+ PackageListProvider.APPLICATION_LABEL);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		adapter.swapCursor(cursor);
		setListShown(true);
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
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
		case OnScrollListener.SCROLL_STATE_IDLE:
			mBusy = false;

			int count = view.getChildCount();
			for (int i = 0; i < count; i++) {
				View v = view.getChildAt(i);
				ImageView icon = (ImageView) v
						.findViewById(R.id.hwa_settings_app_icon);
				if (icon.getTag() != null) {
					String packageName = (String) ((TextView) v
							.findViewById(R.id.hwa_settings_packagename))
							.getText();
					try {
						icon.setImageDrawable(mPackageManager
								.getApplicationIcon(packageName));
					} catch (NameNotFoundException e) {
						icon.setTag(null);
						e.printStackTrace();
					}
					icon.setTag(null);
				}
			}

			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
			mBusy = true;
			break;
		case OnScrollListener.SCROLL_STATE_FLING:
			mBusy = true;
			break;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Cursor cursor = mContentResolver.query(PackageListProvider.PACKAGE_URI,
				new String[] { PackageListProvider.PACKAGE_NAME },
				PackageListProvider._ID + " IS ?",
				new String[] { String.valueOf(id) }, null);
		String packageName;
		if (cursor.moveToFirst()) {
			packageName = cursor.getString(cursor
					.getColumnIndex(PackageListProvider.PACKAGE_NAME));
		} else
			return;
		cursor.close();
		hwaCheck = (CheckBox) view.findViewById(R.id.hwa_settings_enabled);
		if (hwaCheck.isChecked()) {
			hwaCheck.setChecked(false);
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
				hwaCheck.setChecked(true);
			}
		} else {
			hwaCheck.setChecked(true);
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
				hwaCheck.setChecked(false);
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
			values.put(PackageListProvider.HWA_ENABLED, "true");
			mContentResolver.update(Uri.withAppendedPath(
					PackageListProvider.PACKAGE_URI, packageName), values,
					null, null);
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
			values.put(PackageListProvider.HWA_ENABLED, "false");
			mContentResolver.update(Uri.withAppendedPath(
					PackageListProvider.PACKAGE_URI, packageName), values,
					null, null);
		}
		return disabled;
	}

	public class PackageListAdapater extends SimpleCursorAdapter {

		private static final String TAG = "PackageListAdapater";
		private Drawable defaultIcon = PackageListFragment.this.getResources()
				.getDrawable(R.drawable.ic_default);

		public PackageListAdapater(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
			mInflater = LayoutInflater.from(context);
			mPackageManager = context.getPackageManager();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (mCursor == null) {
				mCursor = getCursor();
			}
			ViewHolder holder;
			mCursor.moveToPosition(position);
			if (convertView == null) {
				convertView = mInflater
						.inflate(R.layout.hwa_settings_row, null);
				holder = new ViewHolder();
				holder.label = (TextView) convertView
						.findViewById(R.id.hwa_settings_name);
				holder.packageName = (TextView) convertView
						.findViewById(R.id.hwa_settings_packagename);
				holder.enabled = (CheckBox) convertView
						.findViewById(R.id.hwa_settings_enabled);
				holder.icon = (ImageView) convertView
						.findViewById(R.id.hwa_settings_app_icon);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			String packageName = mCursor.getString(mCursor
					.getColumnIndex(PackageListProvider.PACKAGE_NAME));
			holder.label.setText(mCursor.getString(mCursor
					.getColumnIndex(PackageListProvider.APPLICATION_LABEL)));
			holder.packageName.setText(packageName);
			if (!mBusy) {
				try {
					holder.icon.setImageDrawable(mPackageManager
							.getApplicationIcon(packageName));
				} catch (NameNotFoundException e) {
					holder.icon.setImageResource(R.drawable.ic_default);
					Log.w(TAG, "Icon " + packageName + " for not found!");
				}
				holder.icon.setTag(null);
			} else {
				holder.icon.setImageDrawable(defaultIcon);
				holder.icon.setTag(this);
			}
			holder.enabled.setChecked(Boolean.parseBoolean(mCursor
					.getString(mCursor
							.getColumnIndex(PackageListProvider.HWA_ENABLED))));
			return convertView;
		}
	}

	static class ViewHolder {
		ImageView icon;
		TextView label;
		TextView packageName;
		CheckBox enabled;
	}
}
