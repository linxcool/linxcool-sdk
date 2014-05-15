package com.linxcool.sdk.entry;

import android.content.Context;
import android.content.Intent;

/**
 * 生命周期接口
 * <p><b>Time:</b> 2013-11-18
 * @author 胡昌海(linxcool.hu)
 */
public interface LifeCycle {
	/**
	 * 生命周期 创建
	 * @param context
	 */
	public void onCreate(Context context,Object ...args);
	
	/**
	 * 生命周期 收到广播的命令
	 * @param intent
	 * @return
	 */
	public void onCommand(Context context,Intent intent);
	
	/**
	 * 生命周期 销毁
	 */
	public void onDestory();
}
