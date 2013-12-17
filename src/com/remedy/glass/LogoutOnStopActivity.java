package com.remedy.glass;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public abstract class LogoutOnStopActivity extends Activity {
	@Override
	protected void onStop() {
		super.onStop();
		
		if (AppLifecycleHandler.isApplicationBeingBackgrounded()) {
			Log.i("Remedy.activity", "In background, should logout");
			LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("logout"));
			finish();
		}		
	}
}
