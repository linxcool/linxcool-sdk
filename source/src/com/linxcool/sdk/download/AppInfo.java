package com.linxcool.sdk.download;

import java.io.Serializable;

/**
 * 应用基本信息
 * 
 * @author: 胡昌海(linxcool.hu)
 */
public class AppInfo implements Serializable {
	
	public enum Type{
		PluginUpdate,//插件下载，不写入数据库，故该项并未使用
		AppDeltaUpdate,
		AppCommonUpdate
	}

	private static final long serialVersionUID = 10000L;

	public static final String KEY = "AppInfo";

	
	// 应用ID
	@DbTag(name = KEY_APID, constraint = "primary key")
	public String appId;
	public static final String KEY_APID = "id";
	// 数据类型
	@DbTag(name = KEY_TYPE, type = "integer")
	public int type;
	public static final String KEY_TYPE = "type";
	// 应用名称
	@DbTag(name = KEY_NAME)
	public String appName;
	public static final String KEY_NAME = "name";
	// 应用图标
	@DbTag(name = KEY_ICON, type = "varchar(512)")
	public String iconUrl;
	public static final String KEY_ICON = "icon";
	// 应用版本
	@DbTag(name = KEY_VER)
	public String appVer;
	public static final String KEY_VER = "ver";
	// 下载次数
	@DbTag(name = KEY_DOWN)
	public String downCount;
	public static final String KEY_DOWN = "cout";
	// APK下载地址
	@DbTag(name = KEY_APK, type = "varchar(512)")
	public String apkUrl;
	public static final String KEY_APK = "apk";
	// 应用短描述
	@DbTag(name = KEY_SDES, type = "text",ignore=true)
	public String shortDes;
	public static final String KEY_SDES = "sdes";
	// 应用长描述
	@DbTag(name = KEY_DDES, type = "text",ignore=true)
	public String detailDes;
	public static final String KEY_DDES = "desc";
	// 应用文件大小
	@DbTag(name = KEY_SIZE, type = "integer")
	public int totalSize;
	public static final String KEY_SIZE = "size";
	// 应用图片序列
	@DbTag(name = KEY_IMGS, type = "text",ignore=true)
	public String imgs;
	public static final String KEY_IMGS = "imgs";
	// 应用包名
	@DbTag(name = KEY_PKG, type = "varchar(64)")
	public String pkg;
	public static final String KEY_PKG = "pkg";
}
