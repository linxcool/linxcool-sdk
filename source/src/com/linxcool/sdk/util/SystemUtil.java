package com.linxcool.sdk.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Locale;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;

/**
 * 系统操作工具类
 * @author 胡昌海(linxcool.hu)
 *
 */
public class SystemUtil {

	/**
	 * 检测应用是否已安装
	 * @param context
	 * @param pkgName 
	 * @return
	 */
	public static boolean isApkInstalled(Context context,String pkgName){
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo info = pm.getPackageInfo(pkgName, 1);
			if (info != null && info.activities.length > 0)
				return true;
		} catch (NameNotFoundException e) {
			//Empty
		}
		return false;
	}

	/**
	 * 安装APK文件
	 * @param context
	 * @param cachePath
	 */
	public static void installApk(Context context,String cachePath){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(
				Uri.parse("file://" + cachePath),
		"application/vnd.android.package-archive");
		context.startActivity(intent);
	}

	/**
	 * 卸载 APK
	 * @param context
	 * @param pkg
	 */
	public static void uninstallApk(Context context, String pkgName) {
		Intent intent = new Intent(
				Intent.ACTION_DELETE, Uri.parse("package:" + pkgName));
		context.startActivity(intent);
	}

	/**
	 * 运行APK
	 * @param context
	 * @param pkgName
	 */
	public static void launchApk(Context context, String pkgName) {
		PackageManager pm = context.getPackageManager();
		Intent intent = pm.getLaunchIntentForPackage(pkgName);
		context.startActivity(intent);
	}

	/**
	 * 获取网络代理信息
	 * @param context
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static Proxy getProxy(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
				Context.CONNECTIVITY_SERVICE);

		NetworkInfo ni = cm.getActiveNetworkInfo();
		if(ni==null || !ni.isAvailable() || ni.getType() != ConnectivityManager.TYPE_MOBILE)
			return null;

		String imsi = getImsi(context);
		if (imsi != null && !imsi.equals("") && !imsi.startsWith("46001")) 
			return null;

		String apn = ni.getExtraInfo().toLowerCase(Locale.CHINA);
		if (apn.contains("wap")) {
			String proxyHost = android.net.Proxy.getDefaultHost();
			int port = android.net.Proxy.getDefaultPort();
			if (proxyHost != null) {
				InetSocketAddress sa = new InetSocketAddress(proxyHost,port);
				return new Proxy(Proxy.Type.HTTP, sa);
			}
		}
		return null;
	}

	/**
	 * 获取手机IMSI
	 * @param context
	 * @return
	 */
	public static String getImsi(Context context) {
		String imsi=null;
		TelephonyManager phoneManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (phoneManager != null)imsi = phoneManager.getSubscriberId();
		if (imsi == null) imsi = "";
		return imsi;
	}

	/**
	 * 获取权限
	 * @param permission 权限
	 * @param path 路径
	 */
	public static void chmod(String permission, String path) {
		try {
			String command = String.format("chmod %s %s", permission,path);
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 检查网络是否已连接
	 * @param context
	 * @return
	 */
	public static boolean checkNetConnected(Context context){
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(
				Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if(wifi.isConnectedOrConnecting() || mobile.isConnectedOrConnecting()){
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public static void exitApplication(Context context){
		ActivityManager am = (ActivityManager)context.getSystemService(
				Context.ACTIVITY_SERVICE);   
		am.restartPackage(context.getPackageName());
	}
}
