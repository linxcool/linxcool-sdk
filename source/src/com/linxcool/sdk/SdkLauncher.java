package com.linxcool.sdk;

import android.content.Context;
import android.content.Intent;

public class SdkLauncher {
	
	private static final String NAME_SDK_KERNEL = "kernelSdk";
	private static final String SUFFIX_SDK_PLUGIN = ".jar";
	
	private static LaunchPluginInstance kernelInstance;
	
	public static boolean launch(Context context, Object... args){
		try{
			LaunchPluginInfo info = LaunchPluginInfoManager.getLocalPluginInfo(
					context, NAME_SDK_KERNEL, SUFFIX_SDK_PLUGIN);
			if(info == null) 
				return false;
			Class<?> cls = LaunchPluginLoader.load(context, info);
			if(cls == null) 
				return false;
			kernelInstance = new LaunchPluginInstance(cls);
			kernelInstance.callCreate(context, args);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public void onCommand(Context context, Intent intent){
		if(kernelInstance != null){
			kernelInstance.callCommand(context, intent);
		}
	}
	
	public void onDestory(){
		if(kernelInstance != null){
			kernelInstance.callDestroy();
		}
	}
}
