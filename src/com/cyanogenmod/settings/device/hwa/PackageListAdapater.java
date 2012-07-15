package com.cyanogenmod.settings.device.hwa;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class PackageListAdapater extends SimpleCursorAdapter {

	private static final String TAG = "PackageListAdapater";
	private LayoutInflater mInflater;
	private Cursor mCursor;
	private PackageManager mPackageManager;

	public PackageListAdapater(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		mInflater = LayoutInflater.from(context);
		mPackageManager = context.getPackageManager();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		mCursor = getCursor();
		ViewHolder holder;
		if (mCursor.isClosed()) {
			Log.d(TAG, "cursor is closed");
			return convertView;
		}
		mCursor.moveToPosition(position);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.hwa_settings_row, null);
			holder = new ViewHolder();
			holder.label = (TextView) convertView
					.findViewById(R.id.hwa_settings_name);
			holder.packageName = (TextView) convertView
					.findViewById(R.id.hwa_settings_packagename);
			holder.blocked = (CheckBox) convertView
					.findViewById(R.id.hwa_settings_blocked);
			holder.icon = (ImageView) convertView
					.findViewById(R.id.hwa_settings_app_icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.label.setText(mCursor.getString(mCursor
				.getColumnIndex(PackageListProvider.APPLICATION_LABEL)));
		holder.packageName.setText(mCursor.getString(mCursor
				.getColumnIndex(PackageListProvider.PACKAGE_NAME)));
		try {
			holder.icon
					.setImageDrawable(mPackageManager.getApplicationIcon(mCursor.getString(mCursor
							.getColumnIndex(PackageListProvider.PACKAGE_NAME))));
		} catch (NameNotFoundException e) {
			Log.w(TAG, "Package not found");
		}
		holder.blocked.setChecked(!Boolean.parseBoolean(mCursor
				.getString(mCursor
						.getColumnIndex(PackageListProvider.HWA_DISABLED))));
		return convertView;
	}

	static class ViewHolder {
		ImageView icon;
		TextView label;
		TextView packageName;
		CheckBox blocked;
	}
}
