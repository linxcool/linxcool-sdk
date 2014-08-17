package com.linxcool.sdk.support;

import android.content.Context;

/**
 * 设备信息管理器
 * <p><b>Time:</b> 2013-10-23
 * @author 胡昌海(linxcool.hu)
 */
public class DeviceInfoManager {
	
	protected static final String TAG = "DeviceInfoManage";
	
	private static DeviceInfo deviceInfo;
	
	/**
	 * 获得设备信息
	 * @param context
	 * @return
	 */
	public synchronized static DeviceInfo getDeviceInfo(Context context){
		if(deviceInfo == null)
			deviceInfo = new DeviceInfo(context);
		else{
			deviceInfo.init(context);
			deviceInfo.check();
		}
		return deviceInfo;
	}

	public static void release(){
		deviceInfo = null;
	}
}
