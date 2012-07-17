package com.cyanogenmod.settings.device.hwa;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "package_database";
	private static final int DATABASE_VERSION = 1;
	public static final String PACKAGE_TABLE = "packages";
	protected Context mContext;
	private static final String DATABASE_CREATE_STATEMENT = "CREATE TABLE IF NOT EXISTS "
			+ PACKAGE_TABLE
			+ " ( "
			+ BaseColumns._ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, "
			+ PackageListProvider.APPLICATION_LABEL
			+ " VARCHAR(50), "
			+ PackageListProvider.PACKAGE_NAME
			+ " VARCHAR(100) UNIQUE, "
			+ PackageListProvider.HWA_ENABLED + " VARCHAR(10))";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE_STATEMENT);
		DatabaseTools.scanPackages(db, mContext);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		return;
	}
}
