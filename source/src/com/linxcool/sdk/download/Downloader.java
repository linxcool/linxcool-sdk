package com.linxcool.sdk.download;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * 下载器
 * @author: 胡昌海(linxcool.hu)
 */
public class Downloader extends Service implements DownloadListener{

	DownloadDBHelper dbHelper;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent == null){
			onDestroy();
			return super.onStartCommand(intent, flags, startId);
		}
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	void doTask(DownloadFile fileInfo){
		// TODO Auto-generated method stub
		
		DownloadTask task = new DownloadTask(this, dbHelper, this);
		task.execute(fileInfo);
		
	}


	@Override
	public void onComplete(DownloadFile fileInfo) {
		// TODO Auto-generated method stub 
	}

	@Override
	public void onError(int code, DownloadFile fileInfo) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onUpdate(DownloadFile fileInfo) {
		if (fileInfo.nfn == null) 
			return;
		// TODO Auto-generated method stub
	}

	@Override
	public void onBegin(DownloadFile fileInfo) {
		// TODO Auto-generated method stub
		
	}

}