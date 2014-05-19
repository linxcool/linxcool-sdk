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
public class LaunchPluginInfoManager {

	private static final String TAG = "PluginInfoManager";
	private static final String PREFERENCES_NAME = "plg_launch_cfg";

	private static SharedPreferences preferences;
	
	private synchronized static SharedPreferences getPreferences(Context context){
		if(preferences == null)
			preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
		return preferences;
	}

	/**
	 * 获取历史加载信息
	 * @param context
	 * @param pluginName
	 * @return
	 */
	public static String getPluginHistory(Context context,String pluginName){
		return getPreferences(context).getString(pluginName, null);
	}

	/**
	 * 设置历史加载信息
	 * @param context
	 * @param pluginInfo
	 */
	public static void setPluginHistory(Context context,LaunchPluginInfo pluginInfo){
		Editor editor = getPreferences(context).edit();
		editor.putString(pluginInfo.plugName, pluginInfo.fileName);
		editor.commit();
	}

	/**
	 * 获取Assets目录下的插件信息
	 * @param context
	 * @param pluginName
	 * @param suffix
	 * @return
	 */
	public static LaunchPluginInfo getAssetsPluginInfo(Context context, String pluginName, String suffix){
		InputStream is = null;
		String assetFileName = pluginName + suffix;
		try{
			is = context.getAssets().open(assetFileName);
		}catch (Exception e) {
			Log.w(TAG, "assets not exist plugin " + pluginName);
			return null;
		}
		// assets目录下插件的信息
		LaunchPluginInfo assetsInfo = null;
		try {
			if(is != null)assetsInfo = LaunchPluginVerifier.verify(is);
			if(assetsInfo != null && !pluginName.equals(assetsInfo.plugName)){
				assetsInfo = null;
			}else{
				assetsInfo.suffix = suffix;
			}
		} catch (Exception e) {
			e.printStackTrace();
			assetsInfo = null;
		}
		return assetsInfo;
	}

	/**
	 * 获取本地可加载插件的信息
	 * <p>调用该方法前 请保证存储空间可用
	 * @param context
	 * @param pluginName
	 * @return
	 */
	public static LaunchPluginInfo getLocalPluginInfo(Context context, String pluginName, String suffix){
		String fileName = getPluginHistory(context, pluginName);
		if(fileName == null)
			return getAhistoricalPluginInfo(context, pluginName, suffix);
		else{
			File pluginFile = LaunchResourceUtil.newPluginFile(context, pluginName, fileName);
			if(!pluginFile.exists())
				return getAhistoricalPluginInfo(context, pluginName, suffix);
			
			LaunchPluginInfo pluginInfo = null;
			try {
				pluginInfo = LaunchPluginVerifier.verify(pluginFile);
			} catch (Exception e) {
				e.printStackTrace();
				pluginInfo = null;
			}
			if(pluginInfo == null)
				return getAhistoricalPluginInfo(context, pluginName, suffix);
			
			LaunchPluginInfo assetsInfo = getAssetsPluginInfo(context, pluginName, suffix);
			if(assetsInfo != null && assetsInfo.plugVer > pluginInfo.plugVer){
				return retrieveAssetsPlugin(context, assetsInfo, suffix);
			}
			
			pluginInfo.fileName = fileName;
			pluginInfo.suffix = suffix;
			pluginInfo.savePath = LaunchResourceUtil.getPluginsFolder(context, pluginName);
			
			return pluginInfo;
		}
	}

	/**
	 * 无历史记录情况下获取本地插件信息
	 * @param context
	 * @param pluginName
	 * @return
	 */
	private static LaunchPluginInfo getAhistoricalPluginInfo(Context context, String pluginName, String suffix){
		// assets目录下插件的信息
		LaunchPluginInfo assetsInfo = getAssetsPluginInfo(context, pluginName, suffix);
		// 检索插件目录下的插件信息
		String folder = LaunchResourceUtil.getPluginsFolder(context, pluginName);
		LaunchPluginInfo localInfo = searchMaxVerPluginInfo(folder, pluginName, suffix);
		// 需要联网下载
		if(localInfo == null && assetsInfo == null){
			return null;
		}
		// 从assets中释放
		else if(localInfo == null 
				|| (localInfo != null && assetsInfo != null && assetsInfo.plugVer > localInfo.plugVer)){
			return retrieveAssetsPlugin(context, assetsInfo, suffix);
		}
		// 返回本地信息
		else return localInfo;
	}

	/**
	 * 释放Assets目录下的插件
	 * @param context
	 * @param assetsInfo
	 * @return 返回存储在本地的插件信息
	 */
	private static LaunchPluginInfo retrieveAssetsPlugin(
			Context context, LaunchPluginInfo assetsInfo, String suffix){
		
		String folder = LaunchResourceUtil.getPluginsFolder(context, assetsInfo.plugName);
		String fileName = assetsInfo.plugName + "_" + assetsInfo.plugVer + suffix;
		String filePath = folder + fileName;
		
		// 检查本地是否已存在
		File file = new File(filePath);
		if(file.exists()){
			LaunchPluginInfo localInfo = null;
			try {
				localInfo = LaunchPluginVerifier.verify(file);
			} catch (Exception e) {
				e.printStackTrace();
				localInfo = null;
				file.delete();
			}
			if(localInfo != null 
					&& localInfo.plugName.equals(assetsInfo.plugName) 
					&& localInfo.plugVer == assetsInfo.plugVer){
				localInfo.fileName = fileName;
				localInfo.savePath = folder;
				localInfo.suffix = suffix;
				Log.d(TAG, "retrieve plugin from assets but local exists " + localInfo.plugName);
				return localInfo;
			}
			else file.delete();
		}
		
		// 从Assets中释放
		String assetFileName = assetsInfo.plugName + suffix;
		if(LaunchResourceUtil.retrieveFileFromAssets(context, assetFileName, filePath)){
			assetsInfo.fileName = fileName;
			assetsInfo.savePath = folder;
			assetsInfo.suffix = suffix;
			Log.d(TAG, "retrieve plugin from assets success " + assetsInfo.plugName);
			return assetsInfo;
		}
		
		Log.d(TAG, "retrieve plugin from assets fail " + assetsInfo.plugName);
		return null;
	}
	
	/**
	 * 获取对应目录下 对应插件的最高版本的已校验文件
	 * @param folder
	 * @param pluginName
	 * @return 
	 */
	private static LaunchPluginInfo searchMaxVerPluginInfo(String folder, String pluginName, String suffix){
		File file = new File(folder);
		File[] list = file.listFiles();
		if(list == null || list.length == 0)
			return null;
		// 最高版本
		long maxVer = -1;
		File maxVerFile = null;
		// 循环遍历 找到最高版本文件
		for (int i = 0; i < list.length; i++) {
			String itemName = list[i].getName();
			if(!itemName.startsWith(pluginName) || !itemName.endsWith(suffix))
				continue;
			if(maxVerFile == null){
				long ver = getVerFromFileName(itemName);
				if(ver == -1) continue;
				maxVerFile = list[i];
				maxVer = ver;
			}
			else{
				long ver = getVerFromFileName(itemName);
				if(ver == -1) continue;
				if(ver <= maxVer) continue;
				maxVerFile =  list[i];
				maxVer = ver;
			}
		}
		if(maxVerFile == null)
			return null;
		LaunchPluginInfo pluginInfo = null;
		// 校验文件信息
		try {
			pluginInfo = LaunchPluginVerifier.verify(maxVerFile);
		} catch (Exception e) {
			e.printStackTrace();
			pluginInfo = null;
		}
		// 文件不合法 删除 并递归搜索
		if(pluginInfo == null 
				|| pluginInfo.plugVer != maxVer 
				|| !pluginInfo.plugName.equals(pluginName)){
			if(!maxVerFile.delete())
				return null;
			return searchMaxVerPluginInfo(folder, pluginName, suffix);
		}
		// 文件合法 补充信息
		pluginInfo.fileName = maxVerFile.getName();
		pluginInfo.savePath = folder;
		pluginInfo.suffix = suffix;

		return pluginInfo;
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
			String ver = fileName.substring(
					fileName.lastIndexOf("_") + 1 , fileName.lastIndexOf("."));
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
