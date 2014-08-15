package com.linxcool.sdk;

import android.content.Context;
import android.content.Intent;

/**
 * SDK启动器
 * @author 胡昌海(linxcool.hu)
 */
public class SdkLauncher {
	
	private static final String NAME_SDK_KERNEL = "kernelSdk";
	private static final String SUFFIX_SDK_PLUGIN = ".jar";
	
	private static KernelInstance kernelInstance;
	
	public static boolean launch(Context context, Object... args){
		try{
			KernelInfo info = KernelInfoManager.getLocalPluginInfo(
					context, NAME_SDK_KERNEL, SUFFIX_SDK_PLUGIN);
			if(info == null) return false;
			Class<?> cls = KernelLoader.load(context, info);
			if(cls == null) return false;
			kernelInstance = new KernelInstance(cls);
			kernelInstance.onCreate(context, info.version, args);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public static void onCommand(Context context, Intent intent){
		if(kernelInstance != null){
			kernelInstance.onCommand(context, intent);
		}
	}
	
	public static void onDestory(){
		if(kernelInstance != null){
			kernelInstance.onDestroy();
		}
	}
}
