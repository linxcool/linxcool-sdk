package com.linxcool.sdk.download;

import java.io.Serializable;
import java.lang.reflect.Field;

import android.app.Notification;
import android.database.Cursor;
import android.util.Log;

/**
 * 下载的文件信息
 * @author: 胡昌海(linxcool.hu)
 */
public class DownloadFile implements Serializable{
	private static final long serialVersionUID = 1000001L;
	
	private static final boolean DEBUG = false;
	
	private static final String TB_NAME = "downFiles";
	private static final String TAG = "DownloadFile";

	public static final String KEY = TAG; 

	/**
	 * APK文件对应的状态
	 * @author: 胡昌海(linxcool.hu)
	 */
	public enum Status{
		NONE,			//列表中
		QUEUE,			//队列中
		DOWNLOADING,	//下载中
		PAUSE,			//暂停
		DOWNLOADED,		//下载完成
	}

	public AppInfo appInfo;

	public int downedSize;
	public static final String KEY_DOWNED_SIZE="downed_size";

	public String filePath;
	public static final String KEY_FILE_PATH="file_path";

	public Status status;
	public static final String KEY_STATUS="status";

	public DownloadTask task;
	public Notification nfn;
	
	public DownloadFile(){
		appInfo = new AppInfo();
	}
	
	public DownloadFile(AppInfo appInfo){
		this.appInfo = appInfo;
	}

	public DownloadFile(Cursor cursor){
		downedSize = cursor.getInt(
				cursor.getColumnIndex(KEY_DOWNED_SIZE));
		filePath = cursor.getString(
				cursor.getColumnIndex(KEY_FILE_PATH));
		status = Status.values()[cursor.getInt(
				cursor.getColumnIndex(KEY_STATUS))];
		
		appInfo = new AppInfo();

		appInfo.appId = cursor.getString(
				cursor.getColumnIndex(AppInfo.KEY_APID));
		appInfo.appName = cursor.getString(
				cursor.getColumnIndex(AppInfo.KEY_NAME));
		appInfo.iconUrl = cursor.getString(
				cursor.getColumnIndex(AppInfo.KEY_ICON));
		appInfo.appVer = cursor.getString(
				cursor.getColumnIndex(AppInfo.KEY_VER));
		appInfo.downCount = cursor.getString(
				cursor.getColumnIndex(AppInfo.KEY_DOWN));
		appInfo.apkUrl = cursor.getString(
				cursor.getColumnIndex(AppInfo.KEY_APK));
		appInfo.shortDes = cursor.getString(
				cursor.getColumnIndex(AppInfo.KEY_SDES));
		appInfo.detailDes = cursor.getString(
				cursor.getColumnIndex(AppInfo.KEY_DDES));
		appInfo.totalSize = cursor.getInt(
				cursor.getColumnIndex(AppInfo.KEY_SIZE));
		appInfo.type = cursor.getInt(
				cursor.getColumnIndex(AppInfo.KEY_TYPE));
		appInfo.imgs = cursor.getString(
				cursor.getColumnIndex(AppInfo.KEY_IMGS));
		appInfo.pkg = cursor.getString(
				cursor.getColumnIndex(AppInfo.KEY_PKG));
	}

	public static String getCreateSql() {
		String cnt = "";

		Field[] fields = AppInfo.class.getDeclaredFields();

		for (int i = 0; i < fields.length; i++) {
			if (!fields[i].isAnnotationPresent(DbTag.class))continue;
			DbTag tag = fields[i].getAnnotation(DbTag.class);
			cnt += String.format("%s %s %s,", tag.name(), tag.type(),tag.constraint());
		}

		cnt = String.format(
				"CREATE TABLE IF NOT EXISTS %s(%s%s integer,%s varchar(512),%s integer)",
				TB_NAME, 
				cnt,
				KEY_DOWNED_SIZE,
				KEY_FILE_PATH,
				KEY_STATUS);

		if(DEBUG)Log.d(TAG,"get create sql is "+cnt);

		return cnt;
	}

	public static String getDropSql() {
		String rs = "DROP TABLE IF EXISTS "+TB_NAME;

		Log.d(TAG,"get create sql is "+rs);

		return rs;
	}

	public static String getSelectSql(){
		String rs = "SELECT * FROM " + TB_NAME;

		if(DEBUG)Log.d(TAG,"get select sql is "+rs);

		return rs;
	}

	public static String getSelectSql(String appId){
		String rs = String.format(
				"SELECT * FROM %s WHERE %s = '%s'", 
				TB_NAME,
				AppInfo.KEY_APID,
				appId);

		if(DEBUG)Log.d(TAG,"get select sql is "+rs);

		return rs;
	}
	
	public static String getInsertSql(DownloadFile info) throws Exception{
		String cs = "";
		String vs = "";

		Field[] fields = AppInfo.class.getDeclaredFields();

		for (int i = 0; i < fields.length; i++) {
			if (!fields[i].isAnnotationPresent(DbTag.class))continue;
			DbTag tag = fields[i].getAnnotation(DbTag.class);
			if(tag.ignore())continue;
			cs += String.format("%s,", tag.name());
			fields[i].setAccessible(true);
			Object obj = fields[i].get(info.appInfo);
			if(obj instanceof String) vs += "'"+obj+"',";
			else vs += String.valueOf(obj)+",";
		}

		cs += String.format("%s,%s,%s", KEY_DOWNED_SIZE,KEY_FILE_PATH,KEY_STATUS);
		vs += String.format("%d,'%s',%d", info.downedSize,info.filePath,info.status.ordinal());

		String rs = String.format("INSERT INTO %s (%s) VALUES(%s)",TB_NAME, cs,vs);

		if(DEBUG)Log.d(TAG,"get insert sql is "+rs);

		return rs;
	}

	public static String getUpdateSql(DownloadFile info){
		String rs = String.format(
				"UPDATE %s SET %s = %s,%s = %s,%s = %s,%s = %s WHERE %s = '%s'", 
				TB_NAME,
				KEY_DOWNED_SIZE,
				info.downedSize,
				AppInfo.KEY_SIZE,
				info.appInfo.totalSize,
				KEY_STATUS,
				info.status.ordinal(),
				AppInfo.KEY_TYPE,
				info.appInfo.type,
				AppInfo.KEY_APID,
				info.appInfo.appId);

		if(DEBUG)Log.d(TAG,"get update sql is "+rs);

		return rs;
	}

	public static String getDeleteSql(DownloadFile info){
		AppInfo app = info.appInfo;

		String rs = String.format("DELETE FROM %s ",TB_NAME);

		if(app.appId != null) 
			rs += String.format("WHERE %s='%s'", AppInfo.KEY_APID,app.appId);
		else if(app.apkUrl != null) 
			rs += String.format("WHERE %s='%s'", AppInfo.KEY_APK,app.apkUrl);
		else if(app.appName != null) 
			rs += String.format("WHERE %s='%s'", AppInfo.KEY_NAME,app.appName);

		if(DEBUG)Log.d(TAG,"get delete sql is "+rs);

		return rs;
	};
}
