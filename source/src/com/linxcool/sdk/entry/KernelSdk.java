package com.linxcool.sdk.entry;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.linxcool.sdk.PluginDownloader;
import com.linxcool.sdk.action.UpdatePluginAction;

public class KernelSdk implements LifeCycle{

	private static final String TAG = "KernelSdk";
	
	@Override
	public void onCreate(Context context, Object... args) {
		int version = Integer.parseInt(String.valueOf(args[0]));
		Log.i(TAG, "kernel sdk created " + version);
		updateKernel(context, version);
	}

	private void updateKernel(Context context, int version){
		PluginDownloader.init(context);
		UpdatePluginAction action = new UpdatePluginAction(context);
		action.putReqData("kernelSdk", version);
		action.actionStart();
	}
	
	@Override
	public void onCommand(Context context, Intent intent) {
		
	}

	@Override
	public void onDestory() {
		
	}

}
