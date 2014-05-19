package com.linxcool.sdk.entry;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class KernelSdk implements LifeCycle{

	private static final String TAG = "KernelSdk";
	
	@Override
	public void onCreate(Context context, Object... args) {
		Log.i(TAG, "kernel sdk created");
	}

	@Override
	public void onCommand(Context context, Intent intent) {
		
	}

	@Override
	public void onDestory() {
		
	}

}
