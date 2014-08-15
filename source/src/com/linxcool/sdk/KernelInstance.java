package com.linxcool.sdk;

import android.content.Context;
import android.content.Intent;

/**
 * 插件实例
 * <p><b>Time:</b> 2014-1-9
 * @author 胡昌海(Linxcool.Hu)
 */
public class KernelInstance extends KernelMapping{

	public KernelInstance(Class<?> cls) {
		super(cls);
	}

	public void onCreate(Context context, Object ...args){
		try {
			invokeByName("onCreate", context, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onCommand(Context context,Intent intent){
		try {
			invokeByName("onCommand", context, intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onDestroy(){
		try {
			invokeByName("onDestroy");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
