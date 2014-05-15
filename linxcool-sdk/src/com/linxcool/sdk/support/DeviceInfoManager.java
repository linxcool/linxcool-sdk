package com.linxcool.sdk.support;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;

/**
 * 设备信息管理器
 * <p><b>Time:</b> 2013-10-23
 * @author 胡昌海(linxcool.hu)
 */
public class DeviceInfoManager {
	
	protected static final String TAG = "DeviceInfoManage";
	
	protected static final String MID_FILE_NAME = "device";

	public static class MidFile{
		public char[] imsi;//长度16
		public char[] mac;//长度20
		public int mid;
	}

	private static DeviceInfo deviceInfo;
	private static boolean needUpdateDeviceInfo;
	
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

	/**
	 * 是否需要联网更新服务端存储的设备信息
	 * @return
	 */
	public static boolean isNeedUpdateDeviceInfo() {
		return needUpdateDeviceInfo;
	}

	/**
	 * 从default文件中获取mid
	 * <p>若返回的mid>0，此时还需判断是否需要补全设备信息{@link #isNeedUpdateDeviceInfo()}
	 * <p>此时若设备信息不全且设备信息文件合法则会补全到{@link #getDeviceInfo(Context)}
	 * @return
	 */
	public static int getDefaultMid(Context context){
		needUpdateDeviceInfo = false;
		deviceInfo = getDeviceInfo(context);
		if(deviceInfo.mid > 0){
			return deviceInfo.mid;
		}
		
		// 在此处添加其他代码
		
		return 0;
	}

	/**
	 * 读取设备号文件数据
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public synchronized static MidFile readDeviceFile(File file) throws Exception{
		return null;
	}

	/**
	 * 保存设备信息
	 * <p>建议在执行过{@link #getDefaultMid(Context)}后再保存设备信息
	 * @param context
	 */
	public synchronized static void save(Context context){
		try {
			// 在此处添加其他代码
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 数据写入设备号文件
	 * @param file
	 * @param midFile
	 * @throws Exception
	 */
	public synchronized static void writeDeviceFile(File file, MidFile midFile) throws Exception{
		if(!file.exists())
			file.createNewFile();
		DataOutputStream data = null;
		try{
			data = new DataOutputStream(new FileOutputStream(file));
			// 在此处添加其他代码
		}catch (Exception e) {
			throw e;
		}finally{
			if(data != null)
				data.close();
		}
	}
	
	public static void release(){
		deviceInfo = null;
		needUpdateDeviceInfo = false;
	}
}
