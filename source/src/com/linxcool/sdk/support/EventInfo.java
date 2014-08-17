package com.linxcool.sdk.support;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;

import com.linxcool.sdk.entry.AppContext;

/**
 * 事件信息
 * <p><b>Time:</b> 2013-10-25
 * @author 胡昌海(linxcool.hu)
 */
public class EventInfo {
	
	public int _id;
	
	public String eId;
	public String ext1;
	public String ext2;
	public String ext3;
	public String time;
	
	public String mid;
	public int appId;
	public int channelId;
	public int appVer;
	
	public int libVer;
	public int plgVer;
	
	/**
	 * 构造器 生成SDK运行环境信息
	 * @param context
	 */
	public EventInfo(Context context,AppContext environment){
		_id = -1;
		
		mid = DeviceInfoManager.getDeviceInfo(context).mid;
		
		appId = environment.getAppId();
		channelId = environment.getChannelId();
		appVer = environment.getAppVer();
		libVer = environment.getLibVer();
		plgVer = environment.getPluginVer();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		time = sdf.format(new Date());
	}

	/**
	 * 构造器 空处理
	 */
	public EventInfo() {
		_id = -1;
	}
	
	
}
