package com.linxcool.sdk;

import java.io.File;
import java.io.InputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * 插件信息管理
 * <p><b>Time:</b> 2013-10-29
 * @author 胡昌海(linxcool.hu)
 */
public class KernelInfoManager {

	private static final String TAG = "PluginInfoManager";
	private static final String PREFERENCES_NAME = "plg_launch_cfg";

	private static SharedPreferences preferences;
	
	private synchronized static SharedPreferences getPreferences(Context context){
		if(preferences == null)
			preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
		return preferences;
	}

	/**
	 * 根据插件名获取该插件曾被加载的文件路径
	 * @param context 上下文对象
	 * @param name 插件名
	 * @return
	 */
	public static String getPluginHistory(Context context,String name){
		return getPreferences(context).getString(name, null);
	}

	/**
	 * 保存插件被加载的文件路径
	 * @param context 上下文对象
	 * @param info 插件信息
	 */
	public static void setPluginHistory(Context context,KernelInfo info){
		Editor editor = getPreferences(context).edit();
		editor.putString(info.name, info.fileName);
		editor.commit();
	}

	/**
	 * 获取ASSETS目录下的插件信息
	 * @param context 上下文对象
	 * @param name 插件名
	 * @param suffix 插件文件后缀名
	 * @return 
	 */
	public static KernelInfo getAssetsPluginInfo(Context context, String name, String suffix){
		InputStream is = null;
		try{
			is = context.getAssets().open(name + suffix);
		}catch (Exception e) {
			Log.w(TAG, "assets not exist plugin " + name);
			return null;
		}
		try {
			KernelInfo assetsInfo = KernelVerifier.verify(is);
			if(assetsInfo == null) return null;
			if(!name.equals(assetsInfo.name)) return null;
			assetsInfo.suffix = suffix;
			return assetsInfo;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 获取本地可加载插件的信息（调用该方法前 请保证存储空间可用）
	 * @param context 上下文对象
	 * @param name 插件名
	 * @param suffix 插件文件后缀名
	 * @return
	 */
	public static KernelInfo getLocalPluginInfo(Context context, String name, String suffix){
		String fileName = getPluginHistory(context, name);
		if(fileName == null){
			return getAhistoricalPluginInfo(context, name, suffix);
		}
		
		File pluginFile = KernelResource.newPluginFile(context, name, fileName);
		if(!pluginFile.exists()){
			return getAhistoricalPluginInfo(context, name, suffix);
		}
		
		KernelInfo pluginInfo = null;
		try {
			pluginInfo = KernelVerifier.verify(pluginFile);
		} catch (Exception e) {
			e.printStackTrace();
			pluginInfo = null;
		}
		if(pluginInfo == null){
			return getAhistoricalPluginInfo(context, name, suffix);
		}
		
		KernelInfo assetsInfo = getAssetsPluginInfo(context, name, suffix);
		if(assetsInfo != null && assetsInfo.version > pluginInfo.version){
			return retrieveAssetsPlugin(context, assetsInfo, suffix);
		}
		
		pluginInfo.fileName = fileName;
		pluginInfo.suffix = suffix;
		pluginInfo.fileFolder = KernelResource.getPluginsFolder(context, name);
		
		return pluginInfo;
	}

	/**
	 * 无加载记录情况下获取本地插件信息
	 * @param context 上下文对象
	 * @param name 插件名
	 * @param suffix 插件文件后缀名
	 * @return
	 */
	private static KernelInfo getAhistoricalPluginInfo(Context context, String name, String suffix){
		KernelInfo assetsInfo = getAssetsPluginInfo(context, name, suffix);
		String folder = KernelResource.getPluginsFolder(context, name);
		KernelInfo localInfo = searchMaxVerPluginInfo(folder, name, suffix);
		if(localInfo == null && assetsInfo == null)
			return null;
		if(localInfo == null || (localInfo != null && assetsInfo != null && assetsInfo.version > localInfo.version))
			return retrieveAssetsPlugin(context, assetsInfo, suffix);
		return localInfo;
	}

	/**
	 * 释放ASSETS目录下的插件
	 * @param context 上下文对象
	 * @param assetsInfo ASSETS目录下的插件信息
	 * @param suffix 插件文件后缀名
	 * @return 返回存储在本地的插件信息
	 */
	private static KernelInfo retrieveAssetsPlugin(Context context, KernelInfo assetsInfo, String suffix){
		String targetFolder = KernelResource.getPluginsFolder(context, assetsInfo.name);
		String targetFileName = assetsInfo.name + "_" + assetsInfo.version + suffix;
		String targetFilePath = targetFolder + targetFileName;
		
		File file = new File(targetFilePath);
		if(file.exists()) {
			file.delete();
		}
		
		String assetFileName = assetsInfo.name + suffix;
		if(KernelResource.retrieveFileFromAssets(context, assetFileName, targetFilePath)){
			assetsInfo.fileName = targetFileName;
			assetsInfo.fileFolder = targetFolder;
			assetsInfo.suffix = suffix;
			Log.d(TAG, "retrieve plugin from assets success " + assetsInfo.name);
			return assetsInfo;
		}
		
		Log.d(TAG, "retrieve plugin from assets fail " + assetsInfo.name);
		return null;
	}
	
	/**
	 * 获取插件目录下最高版本的已校验插件文件
	 * @param folder 本地存放插件的文件目录
	 * @param name 插件名
	 * @param suffix 插件文件后缀名
	 * @return 
	 */
	private static KernelInfo searchMaxVerPluginInfo(String folder, String name, String suffix){
		File[] list = new File(folder).listFiles();
		if(list == null || list.length == 0){
			return null;
		}
		
		long maxVer = -1;
		File maxVerFile = null;
		
		for (int i = 0; i < list.length; i++) {
			String itemName = list[i].getName();
			if(!itemName.startsWith(name) || !itemName.endsWith(suffix))
				continue;
			if(maxVerFile == null){
				long ver = getVerFromFileName(itemName);
				if(ver == -1) continue;
				maxVerFile = list[i];
				maxVer = ver;
			} else {
				long ver = getVerFromFileName(itemName);
				if(ver == -1) continue;
				if(ver <= maxVer) continue;
				maxVerFile =  list[i];
				maxVer = ver;
			}
		}
		
		if(maxVerFile == null)
			return null;
		
		// 校验文件信息
		KernelInfo info = null;
		try {
			info = KernelVerifier.verify(maxVerFile);
		} catch (Exception e) {
			e.printStackTrace();
			info = null;
		}
		
		// 文件不合法 删除 并递归搜索
		if(info == null 
				|| info.version != maxVer 
				|| !info.name.equals(name)){
			if(!maxVerFile.delete()) return null;
			return searchMaxVerPluginInfo(folder, name, suffix);
		}
		
		// 文件合法 补充信息
		info.fileName = maxVerFile.getName();
		info.fileFolder = folder;
		info.suffix = suffix;

		return info;
	}

	public static String getPluginFileName(String plgName, long ver, String suffix){
		return plgName + "_" + ver + suffix;
	}
	
	/**
	 * 取得插件文件名对应的版本号
	 * @param fileName
	 * @return
	 */
	public static long getVerFromFileName(String fileName){
		try{
			String ver = fileName.substring(fileName.lastIndexOf("_") + 1 , fileName.lastIndexOf("."));
			return Long.parseLong(ver);
		}catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static void release(){
		preferences = null;
	}
	
}
