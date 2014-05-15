package com.linxcool.sdk;

import dalvik.system.DexClassLoader;

/**
 * 插件信息
 * <p><b>Time:</b> 2013-10-31
 * @author 胡昌海(linxcool.hu)
 */
public class LaunchPluginInfo {
	
	public String plugName;
	public long plugVer;
	
	public boolean isLatest;
	public String updateUrl;
	public String savePath;
	public String fileName;
	
	/** 用于加载的接口类 */
	public String apiClsName;
	
	/** 记载该插件的DexClassLoader */
	public DexClassLoader dexLoader;
	
	/** 文件后缀名 */
	public String suffix;

	public LaunchPluginInfo() {
		super();
	}

	public LaunchPluginInfo(String plugName, long plugVer) {
		this.plugName = plugName;
		this.plugVer = plugVer;
	}
}
