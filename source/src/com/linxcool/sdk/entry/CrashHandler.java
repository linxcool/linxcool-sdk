package com.linxcool.sdk.entry;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.linxcool.sdk.util.ResourceUtil;

import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.widget.Toast;

/**
 * 崩溃处理
 * <p><b>Time:</b> 2013-11-5
 * @author 胡昌海(linxcool.hu)
 */
public final class CrashHandler implements UncaughtExceptionHandler,Runnable{

	private Context context;
	private Map<String, String> infos;
	private UncaughtExceptionHandler defaultHandler;

	private static CrashHandler instance;

	private CrashHandler(Context context) {
		this.context = context;
		infos = new HashMap<String, String>();
		defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	public static CrashHandler getInstance(Context context) {
		if(instance == null){
			instance = new CrashHandler(context);
		}
		return instance;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable exc) {
		exc.printStackTrace();
		if (!handleException(exc) && defaultHandler != null) 
			defaultHandler.uncaughtException(thread, exc);
		else {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1);
		}
	}

	/**
	 * 自定义异常处理，收集错误信息
	 * @param exc
	 * @return
	 */
	private boolean handleException(Throwable exc) {
		if (exc == null) 
			return false;

		new Thread(this).start();
		collectDeviceInfo(context, infos);
		saveCrashInfo(exc);

		return true;
	}

	@Override
	public void run() {
		Looper.prepare();
		Toast.makeText(context, "很抱歉，程序出现异常，即将退出。", Toast.LENGTH_LONG).show();
		Looper.loop();
	}

	/**
	 * 收集设备参数信息
	 * @param context
	 */
	public void collectDeviceInfo(Context context, Map<String, String> infos) {
		try {
			Field[] fields = Build.class.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/***
	 * 保存错误信息到文件中
	 * @param exc
	 * @return
	 */
	private String saveCrashInfo(Throwable exc) {
		try {
			StringBuffer sb = new StringBuffer();

			for (Map.Entry<String, String> entry : infos.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				sb.append(key + "=" + value + "\n");
			}

			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			exc.printStackTrace(printWriter);
			Throwable cause = exc.getCause();
			while (cause != null) {
				cause.printStackTrace(printWriter);
				cause = cause.getCause();
			}
			printWriter.close();

			sb.append(writer.toString());

			JSONObject obj = new JSONObject();
			obj.put("content", sb.toString());
			
			ResourceUtil.writeStringData(context, "crash" + System.currentTimeMillis(), obj.toString());
			
			return obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}

