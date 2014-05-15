package com.linxcool.sdk.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.os.AsyncTask;

import com.linxcool.sdk.util.ResourceUtil;
import com.linxcool.sdk.util.SystemUtil;

/**
 * 下载任务
 * @author: 胡昌海(linxcool.hu)
 */
public class DownloadTask extends AsyncTask<DownloadFile, Integer, Integer> {
	public static final int SUCCESS_NORMAL=200;
	public static final int SUCCESS_ON_CHECK=201;

	public static final int ERROR_UNKNOW = 400;
	public static final int ERROR_OPEN_CONNECTION_FAIL = 401;
	public static final int ERROR_CONTENT_LENGTH_INVALID = 402;
	public static final int ERROR_PAUSE = 403;

	public static int RW_BUF_SIZE = 1024;

	public DownloadFile fileInfo;
	
	private DownloadDBHelper dbHelper;
	private DownloadListener listener;
	private Context context;

	private int lastProgress;
	private boolean pause;

	public Object obj;

	public DownloadTask(
			Context context,DownloadDBHelper dbHelper,DownloadListener listener){
		this.context = context;
		this.dbHelper = dbHelper;
		this.listener = listener;
	}

	@Override
	protected Integer doInBackground(DownloadFile... params) {
		fileInfo = params[0];

		InputStream is = null;
		RandomAccessFile raf = null;

		if(dbHelper != null){
			DownloadFile dbCache = dbHelper.select(fileInfo.appInfo.appId);
			if(dbCache == null)dbHelper.insert(fileInfo);
		}

		String folder = ResourceUtil.getFolder(fileInfo.filePath);
		File folderFile = new File(folder);
		if (!folderFile.exists())folderFile.mkdirs();

		HttpURLConnection conn = null;

		try {
			int startPos = 0;
			File file = new File(fileInfo.filePath);
			if(file.exists()){
				fileInfo.downedSize = fileInfo.appInfo.totalSize = (int) file.length();
				if(listener != null)listener.onBegin(fileInfo);
				return SUCCESS_ON_CHECK;
			} 
			else{
				file = new File(fileInfo.filePath + ".tmp");
				if(file.exists()){
					if(file.isFile())startPos = (int) file.length();
					else file.delete();
				}
				else file.createNewFile();
			}

			raf = new RandomAccessFile(file, "rw");
			raf.seek(startPos);

			if (listener != null)listener.onBegin(fileInfo);
			
			// 创建连接
			conn = getHttpConnection(fileInfo.appInfo.apkUrl, startPos);
			if(conn == null)return ERROR_OPEN_CONNECTION_FAIL;

			conn.connect();

			int resCode = conn.getResponseCode();
			if (resCode != HttpStatus.SC_PARTIAL_CONTENT && resCode != HttpStatus.SC_OK)
				return ERROR_OPEN_CONNECTION_FAIL;

			int cntLength = conn.getContentLength();
			int totalLength = startPos + cntLength;
			if(cntLength == -1) return ERROR_CONTENT_LENGTH_INVALID;
			else fileInfo.appInfo.totalSize = totalLength;

			int read = 0;
			byte buf[] = new byte[RW_BUF_SIZE];

			// 获取文件大小
			int curLength = startPos;
			is = conn.getInputStream();

			while((read = is.read(buf))!=-1){
				raf.write(buf, 0, read);
				curLength += read;
				//更新进度
				fileInfo.downedSize = curLength;
				if(dbHelper != null)dbHelper.update(fileInfo);
				publishProgress(curLength,totalLength);
				sleep();
				if(pause) {
					raf.close();
					is.close();
					return ERROR_PAUSE;
				}
			}

			if(read <= 0){
				fileInfo.status = DownloadFile.Status.DOWNLOADED;
				file.renameTo(new File(fileInfo.filePath));
				return SUCCESS_NORMAL; 
			}

			return ERROR_UNKNOW;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(raf != null)raf.close();
			} catch (Exception e) {}
			try {
				if(is != null)is.close();
			} catch (Exception e) {}
			try {
				if(conn != null)conn.disconnect();
			} catch (Exception e) {}
		}
		return ERROR_UNKNOW;
	}

	public void sleep(){
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}
	}

	HttpURLConnection getHttpConnection(String url,int start){
		try {
			URL httpUrl = new URL(url);

			HttpURLConnection httpURLConnection = null;
			//添加网络代理
			if(SystemUtil.getProxy(context)!=null){
				Proxy proxy = new Proxy(Proxy.Type.HTTP,new InetSocketAddress("10.0.0.172", 80));  
				httpURLConnection = (HttpURLConnection) httpUrl.openConnection(proxy);
			}else{
				httpURLConnection = (HttpURLConnection) httpUrl.openConnection();
			}

			httpURLConnection.setAllowUserInteraction(true);
			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.setReadTimeout(5000);
			httpURLConnection.setRequestProperty("User-Agent", "Mopo sdk service 1.0");
			httpURLConnection.setRequestProperty("Range", "bytes=" + start + "-");

			return httpURLConnection;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPreExecute() {
		pause = false;
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(Integer result) {
		switch (result) {
		case SUCCESS_NORMAL:
		case SUCCESS_ON_CHECK:
			if(listener!=null)listener.onComplete(fileInfo);
			if(dbHelper!=null)dbHelper.update(fileInfo);
			break;
		case ERROR_PAUSE:
		case ERROR_UNKNOW:
		case ERROR_OPEN_CONNECTION_FAIL:
		case ERROR_CONTENT_LENGTH_INVALID:
			if(listener!=null)listener.onError(result, fileInfo);
			if(dbHelper!=null)dbHelper.update(fileInfo);
			break;

		}
		super.onPostExecute(result);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		int progress = (int) (fileInfo.downedSize*100.0/fileInfo.appInfo.totalSize);
		if(progress <= lastProgress)return;
		lastProgress = progress;
		if(listener!=null)listener.onUpdate(fileInfo);
		super.onProgressUpdate(values);
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}

	public void cancel(){
		pause = true;
		if(fileInfo!=null)fileInfo.status = DownloadFile.Status.PAUSE;
	}
}
