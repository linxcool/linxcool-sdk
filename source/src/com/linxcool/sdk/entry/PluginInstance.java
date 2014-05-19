package com.linxcool.sdk.entry;

import java.lang.reflect.Method;

import android.content.Context;

import com.linxcool.sdk.support.PluginInfo;

/**
 * 插件实例
 * <p><b>Time:</b> 2014-1-9
 * @author 胡昌海(Linxcool.Hu)
 */
public class PluginInstance {

	private Class<?> apiCls;
	private Object instance;

	private PluginInfo pluginInfo;
	
	public PluginInfo getPluginInfo() {
		return pluginInfo;
	}

	public void setPluginInfo(PluginInfo pluginInfo) {
		this.pluginInfo = pluginInfo;
	}

	public PluginInstance(Class<?> apiCls){
		try {
			this.apiCls = apiCls;
			this.instance = apiCls.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public PluginInstance(Object instance){
		this.instance = instance;
		this.apiCls = instance.getClass();
	}

	public void callCreate(Context context,Object ...args){
		try {
			Method method = apiCls.getMethod("onCreate", new Class[]{Context.class,Object[].class});
			method.invoke(instance, context, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void callCommand(Context context,Object ...args){
		try {
			Method method = apiCls.getMethod("onCommand", new Class[]{Context.class,Object[].class});
			method.invoke(instance, context, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void callDestroy(){
		try {
			Method method = apiCls.getMethod("onDestroy");
			method.invoke(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
