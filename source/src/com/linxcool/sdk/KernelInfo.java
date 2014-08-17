package com.linxcool.sdk;

import dalvik.system.DexClassLoader;

/**
 * 插件信息
 * <p><b>Time:</b> 2013-10-31
 * @author 胡昌海(linxcool.hu)
 */
public class KernelInfo {
	
	public String name;
	public long version;
	
	public String fileFolder;
	public String fileName;
	
	/** 用于加载的接口类 */
	public String apiClsName;
	/** 记载该插件的DexClassLoader */
	public DexClassLoader dexLoader;
	/** 文件后缀名 */
	public String suffix;

	public KernelInfo() {
	}

	public KernelInfo(String name, long version) {
		this.name = name;
		this.version = version;
	}
}
