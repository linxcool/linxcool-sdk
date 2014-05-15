package com.linxcool.sdk.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.util.Log;

import com.linxcool.sdk.entry.AppContext;

/**
 * 事件信息管理器
 * <p><b>Time:</b> 2013-10-25
 * @author 胡昌海(linxcool.hu)
 */
public class EventInfoManager {
	
	private static final String TAG = "EventInfoManager";
	
	private static boolean debug;
	private static boolean inited;
	
	private static EventDatabase db;
	private static List<EventInfo> cache;
	private static ExecutorService addEventService;
	
	private static Timer timer;
	// 数据上报服务任务
	private static TimerTask remoteTask;
	private static boolean isRemoteTaskRunning;
	private static Runnable remoteRunnable;
	// 本地数据添加任务
	private static TimerTask localTask;
	private static boolean isLocalTaskRunning;
	private static Runnable localRunnable = new Runnable() {
		@Override
		public void run() {
			if(db == null || cache == null || cache.isEmpty())
				return;
			if(db.insert(cache.get(0))){
				if(debug)Log.d(TAG, "add event info to db success");
				cache.remove(0);
				awakeRemoteTask();
			}
			else
				Log.w(TAG, "add event info to db fail");
			if(cache.isEmpty()){
				Log.d(TAG, "event info cache is empty, cancel local task");
				cancelLocalTask();
			}
		}
	};
	
	/**
	 * 初始化
	 * @param context
	 * @param runnable
	 */
	public static void init(Context context,final Runnable runnable){
		if(inited){
			if(db != null && db.getContext() == null && context != null){
				db.close();
				db = new EventDatabase(context);
			}
			else return;
		}
		
		debug = true;

		remoteRunnable = runnable;
		
		if(db == null){
			db = new EventDatabase(context);
			db.clearOverdueData();
		}
		if(cache == null)
			cache = Collections.synchronizedList(new ArrayList<EventInfo>());
		if(timer == null)
			timer = new Timer();
		if(addEventService == null)
			addEventService = Executors.newFixedThreadPool(5);
		
		awakeRemoteTask();
		awakeLocalTask();
		
		inited = true;
	}

	/**
	 * 想数据库中添加事件埋点
	 * @param context
	 * @param id
	 * @param ext1
	 * @param ext2
	 * @param ext3
	 */
	public static void addEventInfo(
			final Context context,
			final AppContext environment,
			final String eId,final String ext1,final String ext2,final String ext3){
		if(addEventService == null)
			return;
		addEventService.submit(new Runnable() {
			@Override
			public void run() {
				if(cache == null)
					return;
				EventInfo event = new EventInfo(context,environment);
				event.eId = eId;
				event.ext1 = ext1;
				event.ext2 = ext2;
				event.ext3 = ext3;
				cache.add(event);
				awakeLocalTask();
			}
		});
	}

	/**
	 * 获取事件信息
	 * @param context
	 */
	public static List<EventInfo> getEventInfos(){
		return db.selectTop50();
	}

	/**
	 * 删除事件信息
	 * @param context
	 */
	public static void deleteEventInfos(List<EventInfo> events){
		if(db == null)
			return;
		String _ids = "";
		for (int i = 0; i < events.size(); i++) {
			EventInfo event = events.get(i);
			if(event._id == -1)continue;
			else _ids += "," + event._id;
		}
		_ids = _ids.substring(1);
		db.delete(_ids);
	}

	/**
	 * 暂停数据上传任务
	 */
	public static void cancelRemoteTask(){
		if(remoteTask != null)
			remoteTask.cancel();
		isRemoteTaskRunning = false;
	}

	/**
	 * 唤醒数据上传任务
	 */
	public static void awakeRemoteTask(){
		if(isRemoteTaskRunning || remoteRunnable == null)
			return;
		cancelRemoteTask();
		remoteTask = new TimerTask() {
			@Override
			public void run() {
				synchronized (remoteRunnable) {
					remoteRunnable.run();
				}
			}
		};
		timer.schedule(remoteTask, 1000 , 1000*5);
		isRemoteTaskRunning = true;
		Log.d(TAG, "schedule remote task");
	}
	
	/**
	 * 暂停本地数据插入任务
	 */
	private static void cancelLocalTask(){
		if(localTask != null)
			localTask.cancel();
		isLocalTaskRunning = false;
	}
	
	/**
	 * 唤醒本地数据插入任务
	 */
	private static void awakeLocalTask(){
		if(isLocalTaskRunning || localRunnable == null)
			return;
		cancelLocalTask();
		localTask = new TimerTask() {
			@Override
			public void run() {
				synchronized (localRunnable) {
					localRunnable.run();
				}
			}
		};
		timer.schedule(localTask, 1000 , 10);
		isLocalTaskRunning = true;
		Log.d(TAG, "schedule local task");
	}

	/**
	 * 资源释放
	 */
	public static void release(){
		cancelRemoteTask();
		cancelLocalTask();
		
		if(timer != null){
			timer.cancel();
			timer.purge();
		}
		
		timer = null;
		remoteTask = null;
		remoteRunnable = null;
		localTask = null;
		db = null;
		addEventService = null;
		
		inited = false;
	}
}
