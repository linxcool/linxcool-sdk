package com.linxcool.sdk.support;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 事件信息数据库
 * <p><b>Time:</b> 2013-10-25
 * @author 胡昌海(linxcool.hu)
 */
public class EventDatabase extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "event_data.db";
	private static final String TABLE_NAME = "events";

	private static final Object lock = new Object();
	
	private Context context;

	private static final String[][] COLUMNS = new String[][]{
		{"_id",			"INTEGER PRIMARY KEY"},
		{"mid",			"INTEGER NOT NULL"},
		{"channelid",	"INTEGER NOT NULL"},
		{"appid",		"INTEGER NOT NULL"},
		{"verid",		"INTEGER NOT NULL"},

		{"eventid",		"TEXT NOT NULL"},
		{"ext1",		"TEXT"},
		{"ext2",		"TEXT"},
		{"ext3",		"TEXT"},
		{"eventtime",	"DATATIME NOT NULL"},

		{"libver",		"INTEGER"},
		{"plugver",		"INTEGER"},
	};

	public Context getContext() {
		return context;
	}

	public EventDatabase(Context context) {
		super(context, DATABASE_NAME, null, 2000);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String args = "";
		for (int i = 0; i < COLUMNS.length; i++)
			args += String.format(",%s %s", COLUMNS[i][0], COLUMNS[i][1]);
		String sql = String.format(
				"CREATE TABLE IF NOT EXISTS %s (%s)",TABLE_NAME,args.substring(1));
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(String.format("DROP TABLE IF EXISTS %s",TABLE_NAME));
		this.onCreate(db);
	}

	/**
	 * 插入数据
	 * @param data
	 * @return
	 */
	public boolean insert(EventInfo data) {
		ContentValues cv = new ContentValues();

		cv.put(COLUMNS[1][0], data.mid);
		cv.put(COLUMNS[2][0], data.channelId);
		cv.put(COLUMNS[3][0], data.appId);
		cv.put(COLUMNS[4][0], data.appVer);

		cv.put(COLUMNS[5][0], data.eId);
		cv.put(COLUMNS[6][0], data.ext1);
		cv.put(COLUMNS[7][0], data.ext2);
		cv.put(COLUMNS[8][0], data.ext3);
		cv.put(COLUMNS[9][0], data.time);

		cv.put(COLUMNS[10][0], data.libVer);
		cv.put(COLUMNS[11][0], data.plgVer);

		synchronized (lock) {
			SQLiteDatabase db = getWritableDatabase();
			try{
				db.beginTransaction();
				long rs = db.insert(TABLE_NAME, null, cv);
				db.setTransactionSuccessful();
				return rs != -1;
			}catch (Exception e) {
				e.printStackTrace();
				return false;
			}finally{
				db.endTransaction();
				db.close();
			}
		}
	}

	/**
	 * 删除超过2天的记录
	 */
	public void clearOverdueData() {
		synchronized (lock) {
			SQLiteDatabase db = getWritableDatabase();
			try{
				String sql = String.format("DELETE FROM %s WHERE %s",
						TABLE_NAME,
						"(strftime('%s','now')-strftime('%s',eventtime)) > 172800"
				);
				db.beginTransaction();
				db.execSQL(sql);
				db.setTransactionSuccessful();
			}catch (Exception e) {
				e.printStackTrace();
			}finally{
				db.endTransaction();
				db.close();
			}
		}
	}

	/**
	 * 删除已发送数据
	 * @param _ids
	 */
	public void delete(String _ids) {
		synchronized (lock) {
			SQLiteDatabase db = getWritableDatabase();
			try{
				String sql = String.format("DELETE FROM %s WHERE %s IN (%s)", 
						TABLE_NAME,
						COLUMNS[0][0],
						_ids);
				db.beginTransaction();
				db.execSQL(sql);
				db.setTransactionSuccessful();
			}catch (Exception e) {
				e.printStackTrace();
			}finally{
				db.endTransaction();
				db.close();
			}
		}
	}

	/**
	 * 获取前50条数据
	 * @return
	 */
	public List<EventInfo> selectTop50(){
		synchronized (lock) {
			SQLiteDatabase db = getReadableDatabase();
			try{
				List<EventInfo> result = new ArrayList<EventInfo>();
				String sql = String.format("SELECT * FROM %s ORDER BY %s LIMIT 50", 
						TABLE_NAME,
						COLUMNS[0][0]);
				Cursor cursor = db.rawQuery(sql, null);
				while(cursor.moveToNext()){
					EventInfo data = new EventInfo();
					data._id = cursor.getInt(0);
					
					data.mid = cursor.getInt(1);
					data.channelId = cursor.getInt(2);
					data.appId = cursor.getInt(3);
					data.appVer = cursor.getInt(4);

					data.eId = cursor.getString(5);
					data.ext1 = cursor.getString(6);
					data.ext2 = cursor.getString(7);
					data.ext3 = cursor.getString(8);
					data.time = cursor.getString(9);

					data.libVer = cursor.getInt(10);
					data.plgVer = cursor.getInt(11);
					result.add(data);
				}
				cursor.close();
				return result;
			}catch (Exception e) {
				e.printStackTrace();
			}finally{
				db.close();
			}
		}
		return null;
	}
	
	@Override
	public synchronized void close() {
		try{
			super.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
