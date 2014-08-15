package com.linxcool.sdk;

import android.content.Context;
import android.content.Intent;

/**
 * 插件实例
 * <p><b>Time:</b> 2014-1-9
 * @author 胡昌海(Linxcool.Hu)
 */
public class PluginInstance extends ReflectMapping{

	public PluginInstance(Class<?> cls) {
		super(cls);
	}

	public void onCreate(Context context, Object ...args){
		try {
			invoke(context, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onCommand(Context context,Intent intent){
		try {
			invoke(context, intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onDestroy(){
		try {
			invoke();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
