package com.linxcool.sdk.download;

public interface DownloadListener {
	
	public void onBegin(DownloadFile fileInfo);
	
	public void onUpdate(DownloadFile fileInfo);
	
	public void onComplete(DownloadFile fileInfo);
	
	public void onError(int code,DownloadFile fileInfo);
	
}
