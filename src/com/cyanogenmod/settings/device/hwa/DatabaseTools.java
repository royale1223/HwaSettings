package com.cyanogenmod.settings.device.hwa;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseTools {

	private static final String TAG = "DatabaseTools";

	public static void scanPackages(SQLiteDatabase database, Context context) {
		File denyFolder = new File("/data/local/hwui.deny");
		if (!denyFolder.exists()) {
			Log.d(TAG, "hwui.deny does not exist");
			denyFolder.mkdirs();
		} else
			Log.d(TAG, "hwui.deny does exist");
		//File[] files = cmdListFiles(denyFolder);
		File[] files = denyFolder.listFiles();
		String[] packageBlacklist = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			packageBlacklist[i] = files[i].getName();
		}
		Log.d(TAG, files.length + " packages blacklisted.");
		Cursor cursor = database.query(DatabaseHelper.PACKAGE_TABLE,
				new String[] { PackageListProvider.PACKAGE_NAME }, null, null,
				null, null, null);
		String[] packages = new String[cursor.getCount()];
		if (cursor.moveToFirst()) {
			do
				packages[cursor.getPosition()] = cursor.getString(cursor
						.getColumnIndex(PackageListProvider.PACKAGE_NAME));
			while (cursor.moveToNext());
		}
		Log.d(TAG, packages.length + " packages are present in db.");
		cursor.close();
		PackageManager pm = context.getPackageManager();
		List<ApplicationInfo> list = pm
				.getInstalledApplications(PackageManager.GET_META_DATA);
		Log.d(TAG, list.size() + " packages are present");
		String[] allApps = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			ApplicationInfo info = list.get(i);
			boolean hwaIsDiabled = false;
			String packageName = info.packageName;
			allApps[i] = info.packageName;
			if (Arrays.asList(packageBlacklist).contains(packageName))
				hwaIsDiabled = true;
			else
				hwaIsDiabled = false;
			ContentValues values = new ContentValues();
			values.put(PackageListProvider.APPLICATION_LABEL,
					(String) pm.getApplicationLabel(info));
			values.put(PackageListProvider.PACKAGE_NAME, packageName);
			values.put(PackageListProvider.HWA_DISABLED,
					String.valueOf(hwaIsDiabled));
			Log.d(TAG, "adding " + packageName + " to database.");
			if (Arrays.asList(packages).contains(packageName)) {
				database.update(DatabaseHelper.PACKAGE_TABLE, values,
						PackageListProvider.PACKAGE_NAME + " IS ?",
						new String[] { packageName });
			} else
				database.insert(DatabaseHelper.PACKAGE_TABLE, null, values);
		}
	}

	private static File[] cmdListFiles(File folder) {
		String path = folder.getPath();
		InputStream inputStream = null;
		try {
			Runtime.getRuntime().exec("su -c 'mkdir " + path + "'");
			inputStream = Runtime.getRuntime().exec("su -c 'ls " + path + "'")
					.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Scanner scanner = new Scanner(inputStream);
		List<String> list = new ArrayList<String>();
		while (scanner.hasNext()) {
			list.add(scanner.next());
		}
		Object[] names = list.toArray();
		File[] files = new File[names.length];
		for (int j = 0; j < names.length; j++) {
			files[j] = new File("/data/local/hwui.deny/" + names[j]);
		}
		return files;
	}
}
