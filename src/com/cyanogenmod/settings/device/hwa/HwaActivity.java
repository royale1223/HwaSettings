package com.cyanogenmod.settings.device.hwa;

import android.app.Activity;
import android.os.Bundle;
import com.cyanogenmod.settings.device.hwa.R;

public class HwaActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getFragmentManager().findFragmentById(android.R.id.content) == null) {
			PackageListFragment list = new PackageListFragment();
			getFragmentManager().beginTransaction()
					.add(android.R.id.content, list).commit();
		}
	}
}