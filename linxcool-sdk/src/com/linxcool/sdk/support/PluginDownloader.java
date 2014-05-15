package com.linxcool.sdk.support;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.linxcool.sdk.download.AppInfo;
import com.linxcool.sdk.download.DownloadFile;
import com.linxcool.sdk.download.DownloadListener;
import com.linxcool.sdk.download.DownloadTask;
import com.linxcool.sdk.util.SecurityUtil;

/**
 * 插件下载器
 * <p><b>Time:</b> 2013-10-30
 * @author 胡昌海(linxcool.hu)
 */
public class PluginDownloader{

	/**
	 * 插件下载监听接口
	 * <p><b>Time:</b> 2013-10-30
	 * @author 胡昌海(linxcool.hu)
	 */
	public interface OnPluginDownloadListener{
		/**
		 * 下载成功
		 * @param pluginInfo
		 */
		public void onComplete(PluginInfo pluginInfo);

		/**
		 * 下载失败
		 * @param code
		 * @param msg
		 */
		public void onError(int code, String msg);
	}

	private static final String TAG = "PluginDownloader";
	private static Map<String, DownloadTask> tasks;

	/**
	 * 初始化下载器 实例化任务列表
	 */
	public static void init(Context context){
		if(tasks == null)
			tasks = new HashMap<String, DownloadTask>();
	}
	
	/**
	 * 借用APK的下载器进行插件下载
	 * @param context
	 * @param pluginInfo
	 */
	public synchronized static void start(
			Context context, final PluginInfo pluginInfo,final OnPluginDownloadListener listener){
		if(tasks == null){
			Log.w(TAG, "tasks is null as resource has released");
			return;
		}

		String key = SecurityUtil.md5(pluginInfo.updateUrl);
		if(tasks.containsKey(key)){
			Log.w(TAG, "plugin is on downloading " + pluginInfo.plugName);
			return ;
		}

		DownloadTask task = new DownloadTask(context, null, new DownloadListener() {
			@Override
			public void onBegin(DownloadFile fileInfo) {
				// Empty
			}
			
			@Override
			public void onUpdate(DownloadFile fileInfo) {
				// Empty
			}
			
			@Override
			public void onError(int code, DownloadFile fileInfo) {
				String key = SecurityUtil.md5(fileInfo.appInfo.apkUrl);
				tasks.remove(key);
				listener.onError(code, "download plugin fail");
			}
			
			@Override
			public void onComplete(DownloadFile fileInfo) {
				String key = SecurityUtil.md5(fileInfo.appInfo.apkUrl);
				tasks.remove(key);
				listener.onComplete(pluginInfo);
			}
		});

		AppInfo appInfo = new AppInfo();
		appInfo.apkUrl = pluginInfo.updateUrl;
		DownloadFile downloadFile = new DownloadFile(appInfo);
		downloadFile.filePath = pluginInfo.savePath + pluginInfo.fileName;

		tasks.put(key, task);
		task.execute(downloadFile);
	}

	/**
	 * 释放资源 若有插件正在下载 则停止
	 */
	public synchronized static void release(){
		if(tasks == null || tasks.size() == 0)
			return;
		Iterator<DownloadTask> it = tasks.values().iterator();
		while (it.hasNext()) {
			it.next().cancel();
		}
	}
}
