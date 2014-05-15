package com.linxcool.sdk.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

import android.Manifest.permission;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

/**
 * 设备信息
 * <p><b>Time:</b> 2013-10-22
 * @author 胡昌海(linxcool.hu)
 */
public class DeviceInfo {

	public static final int NETWORK_TYPE_GSM = 1;
	public static final int NETWORK_TYPE_CDMA = 2;
	public static final int NETWORK_TYPE_CDMA2000 = 3;
	public static final int NETWORK_TYPE_WCDMA = 4;
	public static final int NETWORK_TYPE_TDSCDMA = 5;
	public static final int NERWORK_TYPE_WIFI = 0;

	enum Operator{
		UNKNOW,
		MOBILE,	//中国移动
		TELECOM,//中国电信
		UNICOM	//中国联通
	}

	enum Standard{
		TYPE_XX,//未知网络
		TYPE_2G,//2G网络
		TYPE_3G	//3G网络
	}

	public int mid;

	/** 网络类型 */
	public int netType;

	public String mac;
	public String imei;
	public String imsi;
	public int density;

	public short screenWidth;
	public short screenHeight;

	/** SDK版本号 */
	public int sdkVersion;

	/** 制造商 */
	public String manufactory;
	/** 型号 */
	public String model;
	/** 系统版本 2.3.6 */
	public String osVer;
	/** 是否为模拟器 */
	public int isEmulator;

	/** IP地址 */
	public String ipAddr;
	/** 手机内存总大小 */
	public String memoryTotal;
	/** 手机内存当前可用大小 */
	public String memoryAvail;

	/** CPU最大频率 */
	public String cpuMaxFreq;
	/** CPU名称 */
	public String cpuName;
	/** CPU核心数 */
	public int cpuCoresNum;

	/** SD卡空间总大小 */
	public String sdCardTotalSize;
	/** SD卡可用空间大小 */
	public String sdCardAvailSize;

	/** 手机号码 */
	public String phoneNumber;

	/** 保留字段 */
	public String reserved;

	/**
	 * 构造器
	 * @param context
	 */
	public DeviceInfo(Context context){
		init(context);
		check();
	}

	void init(Context context){
		// MAC地址
		WifiManager wm = (WifiManager) context.getSystemService(
				Context.WIFI_SERVICE);
		WifiInfo info = wm.getConnectionInfo();
		mac = wm.getConnectionInfo().getMacAddress();
		// IMSI及IMEI
		TelephonyManager tm = (TelephonyManager) context.getSystemService(
				Context.TELEPHONY_SERVICE);
		imsi = tm.getSubscriberId();
		imei = tm.getDeviceId();
		// 网络信息
		if (info.getIpAddress() != 0) netType = NERWORK_TYPE_WIFI;
		else netType = getNetWorkType(context);
		// SD卡信息
		long[] sdCardMen = getSDCardMemory(context);
		sdCardTotalSize = String.valueOf(sdCardMen[0]);
		sdCardAvailSize = String.valueOf(sdCardMen[1]);
		// IP地址
		ipAddr = getIpAddress();
		// 手机SIM卡信息 移动神州行,联通的卡是可以取到的.动感地带的取不到
		phoneNumber = tm.getLine1Number();
		// 内存信息
		memoryTotal = String.valueOf(getTotalMemory(context));
		memoryAvail = String.valueOf(getAvailMemory(context));
		// 物理信息
		sdkVersion = Build.VERSION.SDK_INT;
		manufactory = Build.MANUFACTURER;
		model = Build.MODEL;
		osVer = Build.VERSION.RELEASE;
		// <supports-screens android:anyDensity="true"/>
		// 加上这一句才能正确获得屏幕的物理像素尺寸. 否则安卓会返回一个经过调整的逻辑尺寸
		DisplayMetrics display = context.getResources().getDisplayMetrics();
		if (display.widthPixels < display.heightPixels) {
			screenWidth = (short) display.widthPixels;
			screenHeight = (short) display.heightPixels;
		}
		else {
			screenWidth = (short) display.heightPixels;
			screenHeight = (short) display.widthPixels;
		}
		density = display.densityDpi;
		// 模拟器判定
		isEmulator = isEmulator(tm)?1:0;
		// CPU信息
		cpuMaxFreq = getMaxCpuFreq();
		cpuName = getCpuName();
		cpuCoresNum = getNumCores();
	}

	void check(){
		if(imsi == null) imsi = "";
		if(imei == null) imei = "";
		if(mac == null) mac = "";

		if(manufactory == null) manufactory = "";
		if(model == null) model = "";
		if(osVer == null) osVer = "";

		if(phoneNumber == null) phoneNumber="0";
		if(ipAddr == null) ipAddr = "";
	}

	public void update(Context context){
		init(context);
		check();
	}

	/**
	 * 电话状态是否可读
	 * @param context
	 * @return
	 */
	private static boolean isPhoneStateReadable(Context context){
		PackageManager pm = context.getPackageManager();
		String pkgName = context.getPackageName();
		int readable = pm.checkPermission(permission.READ_PHONE_STATE, pkgName);
		return readable == PackageManager.PERMISSION_GRANTED;
	}

	/**
	 * 获取网络运营商
	 * @param context
	 * @return
	 */
	private static Operator getNetworkOperator(Context context) {
		if (!isPhoneStateReadable(context))
			return Operator.UNKNOW;
		TelephonyManager tm = (TelephonyManager) context.getSystemService(
				Context.TELEPHONY_SERVICE);
		String imsi = tm.getSubscriberId();
		if(imsi == null || imsi.length() < 10)
			return Operator.UNKNOW;
		int mcc = context.getResources().getConfiguration().mcc;
		if (mcc == 0) mcc = Integer.valueOf(imsi.substring(0, 3));
		int mnc = context.getResources().getConfiguration().mnc;
		if (mnc == 0) mnc = Integer.valueOf(imsi.substring(4, 5));
		if(mcc != 460)
			return Operator.UNKNOW;
		switch (mnc) {
		case 0:
		case 2:
		case 7:
			return Operator.MOBILE;
		case 1:
			return Operator.UNICOM;
		case 3:
			return Operator.TELECOM;
		default:
			return Operator.UNKNOW;
		}
	}

	/**
	 * 获取网络选项
	 * @param context
	 * @return
	 */
	private static Standard getNetworkStandard(Context context) {
		if (!isPhoneStateReadable(context))
			return Standard.TYPE_XX;
		TelephonyManager tm = (TelephonyManager) context.getSystemService(
				Context.TELEPHONY_SERVICE);
		switch (tm.getNetworkType()) {
		case TelephonyManager.NETWORK_TYPE_UMTS:
		case TelephonyManager.NETWORK_TYPE_HSDPA:
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
		case TelephonyManager.NETWORK_TYPE_HSUPA:
		case TelephonyManager.NETWORK_TYPE_HSPA:
		case 15:
			return Standard.TYPE_3G;
		case TelephonyManager.NETWORK_TYPE_GPRS:
		case TelephonyManager.NETWORK_TYPE_EDGE:
		case TelephonyManager.NETWORK_TYPE_CDMA:
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return Standard.TYPE_2G;
		default:
			return Standard.TYPE_XX;
		}
	}

	/**
	 * 获取网络类型 = 运营商 + 选项
	 * @param context
	 * @return
	 */
	public static final int getNetWorkType (Context context) {
		Operator operator = getNetworkOperator(context);
		Standard standard = getNetworkStandard(context);
		if( standard == Standard.TYPE_2G 
				&& (operator == Operator.MOBILE || operator == Operator.UNICOM) )
			return NETWORK_TYPE_GSM;
		if( standard == Standard.TYPE_2G 
				&& operator == Operator.TELECOM )
			return NETWORK_TYPE_CDMA;
		if( standard == Standard.TYPE_3G 
				&& operator == Operator.MOBILE )
			return NETWORK_TYPE_TDSCDMA;
		if( standard == Standard.TYPE_3G 
				&& operator == Operator.UNICOM )
			return NETWORK_TYPE_WCDMA;
		if( standard == Standard.TYPE_3G 
				&& operator == Operator.TELECOM )
			return NETWORK_TYPE_CDMA2000;
		return 0;
	}

	/**
	 * 获取SD存储信息
	 * @param context
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static long[] getSDCardMemory(Context context) {
		long[] result = new long[2];
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(sdcardDir.getPath());
			long bSize = sf.getBlockSize();
			long bCount = sf.getBlockCount();
			long availBlocks = sf.getAvailableBlocks();
			result[0] = bSize * bCount;// 总大小
			result[1] = bSize * availBlocks;// 可用大小
		}
		return result;
	}

	/**
	 * 获取IP地址
	 * @return
	 */
	public static String getIpAddress() {
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			while(en.hasMoreElements()){
				NetworkInterface netIface = en.nextElement();
				Enumeration<InetAddress> ipAddrs = netIface.getInetAddresses();
				while(ipAddrs.hasMoreElements()){
					InetAddress ip = ipAddrs.nextElement();
					if(ip.isLoopbackAddress())continue;
					return ip.getHostAddress().toString();
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取系统总内存
	 * @param context
	 * @return
	 */
	public static long getTotalMemory(Context context) {
		long initMem = 0;
		try {
			// 系统内存信息文件
			FileReader localFileReader = new FileReader("/proc/meminfo");
			BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
			// 读取第一行 系统总内存大小
			String str2 = localBufferedReader.readLine().split("\\s+")[1];
			// 获得系统总内存 单位是KB 乘以1024转换为Byte
			initMem = Integer.valueOf(str2).intValue() * 1024;
			localBufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return initMem;
	}

	/**
	 * 获取当前可用内存
	 * @param context
	 * @return
	 */
	public static long getAvailMemory(Context context) {
		try{
			ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			MemoryInfo memInfo = new MemoryInfo();
			am.getMemoryInfo(memInfo);
			return memInfo.availMem;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 模拟器判断
	 * @param tm
	 * @return
	 */
	public static boolean isEmulator(TelephonyManager tm){
		try{
			String imei = tm.getDeviceId();
			if (imei != null && imei.equals("000000000000000"))
				return true;
			return (Build.MODEL.equals("sdk")) || (Build.MODEL.equals("google_sdk"));
		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 获取CPU最大频率(单位KHZ)
	 * @return
	 */
	public static String getMaxCpuFreq() {
		String result = "";
		try {
			String[] args = { 
					//命令行
					"/system/bin/cat", 
					//存储最大频率的文件的路径
					"/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" 
			};
			ProcessBuilder cmd = new ProcessBuilder(args);
			Process process = cmd.start();
			InputStream in = process.getInputStream();
			byte[] re = new byte[24];
			while (in.read(re) != -1) {
				result = result + new String(re);
			}
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			result = "N/A";
		}
		result = result.trim();
		if(result.length() == 0)
			result = "N/A";
		return result;
	}

	/**
	 * 获取CPU名字
	 */
	public static String getCpuName() {
		try {
			FileReader fr = new FileReader("/proc/cpuinfo");
			BufferedReader br = new BufferedReader(fr);
			String text = br.readLine();
			br.close();
			String[] array = text.split(":\\s+", 2);
			return array[1];
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取核心数
	 * @return
	 */
	public static int getNumCores() {
		class CpuFilter implements FileFilter {
			@Override
			public boolean accept(File pathname) {
				return Pattern.matches("cpu[0-9]", pathname.getName());
			}
		}
		try {
			File dir = new File("/sys/devices/system/cpu/");
			File[] files = dir.listFiles(new CpuFilter());
			return files.length;
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}
}

