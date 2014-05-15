package com.linxcool.sdk.download;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.linxcool.sdk.download.DownloadFile.Status;
import com.linxcool.sdk.util.ResourceUtil;

/**
 * 数据服务对象
 * @author: 胡昌海(linxcool.hu)
 */
public class DownloadDBHelper extends SQLiteOpenHelper {

	static final String DB_NAME = "downloadDB";

	Object lock = new Object();

	public DownloadDBHelper(Context context) {
		super(context, DB_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DownloadFile.getCreateSql());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(DownloadFile.getDropSql());
		this.onCreate(db);
	}

	public List<DownloadFile> select(){
		synchronized (lock) {
			List<DownloadFile> rsList=new ArrayList<DownloadFile>();
			List<DownloadFile> clList=new ArrayList<DownloadFile>();

			SQLiteDatabase db = getReadableDatabase();
			Cursor cursor = null;

			try{
				String sql = DownloadFile.getSelectSql();
				cursor = db.rawQuery(sql, new String[]{});
				while (cursor.moveToNext()){
					DownloadFile fileInfo = new DownloadFile(cursor);
					if(fileInfo.status == Status.DOWNLOADED && !ResourceUtil.isFileExist(fileInfo.filePath))
						clList.add(fileInfo);
					else{
						if(fileInfo.status != Status.DOWNLOADED)
							fileInfo.status = Status.PAUSE;
						rsList.add(fileInfo);
					}
				}
			}catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(cursor!=null)cursor.close();
				db.close();
			}

			for (DownloadFile clearFlie : clList) {
				delete(clearFlie);
			}

			return rsList;
		}
	}

	public DownloadFile select(String appId){
		synchronized (lock) {
			try{
				SQLiteDatabase db = getReadableDatabase();
				String sql = DownloadFile.getSelectSql(appId);
				Cursor cursor = db.rawQuery(sql, new String[]{});
				if (cursor.moveToNext())
					return new DownloadFile(cursor);
				cursor.close();
				db.close();
			}catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public void insert(DownloadFile downloadFile) {
		synchronized (lock) {
			SQLiteDatabase db = getWritableDatabase();
			try {
				db.beginTransaction();
				String sql = DownloadFile.getInsertSql(downloadFile);
				db.execSQL(sql, new Object[]{});
				db.setTransactionSuccessful();
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				db.endTransaction();
				db.close();
			}
		}
	}

	public void update(DownloadFile info) {
		synchronized (lock) {
			SQLiteDatabase db = getWritableDatabase();
			try {
				db.beginTransaction();
				String sql = DownloadFile.getUpdateSql(info);
				db.execSQL(sql, new String[] {});
				db.setTransactionSuccessful();
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				db.endTransaction();
				db.close();
			}
		}
	}

	public void delete(DownloadFile downloadFile) {
		synchronized (lock) {
			SQLiteDatabase db = getWritableDatabase();
			try{
				db.beginTransaction();
				String sql = DownloadFile.getDeleteSql(downloadFile);
				db.execSQL(sql, new Object[] {});
				db.setTransactionSuccessful();
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				db.endTransaction();
				db.close();
			}
		}
	}
}
