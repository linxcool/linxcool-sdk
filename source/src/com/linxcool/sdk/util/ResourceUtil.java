package com.linxcool.sdk.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * 资源管理类
 * <p>实现图片、文本及文件的持久化 
 * @author: linxcool.hu
 */
public class ResourceUtil {

	private static final String TAG = "ResourceUtil";
	
	public static String SD_IMG_PATH   = "i9xiao/sdk/imgs/";
	public static String ROM_IMG_PATH  = "sdk/imgs/";
	public static String SD_DATA_PATH  = "i9xiao/sdk/data/";
	public static String ROM_DATA_PATH = "sdk/data/";
	public static String SD_PLG_PATH   = "i9xiao/sdk/plgs/";
	public static String ROM_PLG_PATH  = "sdk/plgs/";
	
	private static final int OPTIMIZATION_SIZE = 4096;
	
	/**
	 * 检查SD卡是否存在
	 * @return 存在返回true，否则返回false
	 */
	public static boolean isSdcardReady(){
		return Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED);
	}
	
	/**
	 * 获得SD路径 
	 * @return sdCard path end with separator
	 */
	public static String getSdcardPath() {
		return Environment.getExternalStorageDirectory().toString()+ File.separator;
	}
	
	/**
	 * 获取手机该应用的私有路径
	 * @param activity
	 * @return cache directory end with separator
	 */
	public static String getRomCachePath(Context context){
		return context.getCacheDir() + File.separator;
	}
	
	/**
	 * 检查SD卡中是否存在该文件
	 * @param filePath 不包含SD卡目录的文件路径
	 * @return
	 */
	public static boolean isSdcardFileExist(String filePath){
		File file=new File(getSdcardPath()+filePath);
		return file.exists();
	}
	
	/**
	 *  检查手机内存中是否存在该文件
	 * @param activity
	 * @param fileName 不包含应用内存目录的文件路径
	 * @return
	 */
	public static boolean isRomCacheFileExist(Context context,String filePath){
		String cachePath = getRomCachePath(context);
		File file=new File(cachePath+filePath);
		return file.exists();
	}
	
	public static boolean isFileExist(String filePath){
		File file=new File(filePath);
		return file.exists();
	}
	
	/**
	 * 构建SD目录
	 * @param path 不包含SD卡目录的文件全路径
	 * @return
	 */
	public static String mkSdcardFileDirs(String path) {
		String rsPath =getSdcardPath() + path;
		File file = new File(rsPath);
		if (!file.exists())file.mkdirs();
		return rsPath;
	}
	
	/**
	 * 构建手机存储文件路径
	 * @param activity
	 * @param path 不包含应用内存目录的文件全路径
	 * @return
	 */
	public static String mkRomCacheDirs(Context context,String path) {
		String cachePath = getRomCachePath(context);
		String rsPath=cachePath+path;
		File file = new File(rsPath);
		if (!file.exists())file.mkdirs();
		return rsPath;
	}
	
	/**
	 * 构建文件路径
	 * @param fullPath 完整路径
	 */
	public static void mkFileDirs(String fullPath){
		File file = new File(fullPath);
		if (!file.exists())file.mkdirs();
	}
	
	/**
	 * 获取文件路径对应的文件名
	 * @param filePath
	 * @return
	 */
	public static String getFolder(String filePath) {
		String[] a = filePath.split(File.separator);
		if( a.length <= 0 )return null;
		int tL = filePath.length();
		int nL = a[a.length-1].length();
		return filePath.substring(0, tL-nL);
	}
	
	/**
	 * 写入图片数据
	 * <p>无需关心目录及文件是否存在
	 * @param activity
	 * @param bmp
	 * @param fileName
	 */
	public static void saveBitmapData(Context context,Bitmap bmp, String fileName){
		if (isSdcardReady()) {
			String filePath = SD_IMG_PATH + fileName;
			String folder = getFolder(filePath);
			mkSdcardFileDirs(folder);
			saveBitmap2Sdcard(bmp, filePath);
		} else {
			String filePath = ROM_IMG_PATH + fileName;
			String folder = getFolder(filePath);
			mkRomCacheDirs(context, folder);
			saveBitmap2RomCache(context, bmp, filePath);
		}
	}
	

	/**
	 * 保存图片到SD卡
	 * @param bmp 图片文件
	 * @param filePath 
	 */
	public static boolean saveBitmap2Sdcard(Bitmap bmp,String filePath) {
		if(!isSdcardReady()){
			Log.e(TAG, "save bitmap to sdCard fail as sdCard not exist");
			return false;
		}
		return saveBitmap(bmp, getSdcardPath()+filePath);
	}
	
	/**
	 * 保存图片到手机内存
	 * @param activity
	 * @param bmp 图片文件
	 * @param filePath 
	 * @return
	 */
	public static boolean saveBitmap2RomCache(Context context,Bitmap bmp,String filePath){
		return saveBitmap(bmp, getRomCachePath(context)+filePath);
	}
	
	/**
	 * 保存图片到指定路径
	 * <p>要检查路径存在与否
	 * @param bmp
	 * @param fullFilePath
	 * @return
	 */
	public static boolean saveBitmap(Bitmap bmp,String fullFilePath) {
		if(fullFilePath==null || fullFilePath.length()<1){
			Log.e(TAG, "save bitmap fail as file path invalid");
			return false;
		}
		FileOutputStream fos = null;
		File file = new File(fullFilePath);
		if(file.exists()){
			Log.e(TAG, "save bitmap fail as file already exists");
			return false;
		}
		try {
			fos = new FileOutputStream(file);
			if (null != fos) {
				bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();
				return true;
			}
		} catch (IOException e) {
			Log.e(TAG, "save bitmap fail as exception "+e.getMessage());
		}
		return false;
	}
	
	/**
	 *  读取图片数据
	 *  <p>优先从SD卡获取
	 * @param activity
	 * @param fileName
	 * @return
	 */
	public static Bitmap getBitmapData(Context context,String fileName){
		if(isSdcardFileExist(SD_IMG_PATH+fileName)){
			return getBitmapFromSdcard(SD_IMG_PATH+fileName);
		}else if(isRomCacheFileExist(context, ROM_IMG_PATH+fileName)){
			return getBitmapFromRomCache(context,ROM_IMG_PATH+fileName);
		}
		return null;
	}
	
	/**
	 * 从SD卡获取图片资源
	 * @param fullFilePath
	 * @return
	 */
	public static Bitmap getBitmapFromSdcard(String filePath){
		if(!isSdcardReady()){
			Log.e(TAG, "get bitmap from sdCard fail as sdCard not exist");
			return null;
		}
		return getBitmap(getSdcardPath()+filePath);
	}
	
	/**
	 * 从手机内存获取图片资源
	 * @param fullFilePath
	 * @return
	 */
	public static Bitmap getBitmapFromRomCache(Context context,String filePath){
		return getBitmap(getRomCachePath(context)+filePath);
	}
	
	/**
	 * 从指定目录读取图片资源
	 * @param fullFilePath
	 * @return
	 */
	public static Bitmap getBitmap(String fullFilePath){
		try {
			File file=new File(fullFilePath);
			if(file.exists()){
				return BitmapFactory.decodeFile(fullFilePath);
			}
		} catch (Exception e) {
			Log.e(TAG, "get bitmap fail as exception "+e.getMessage());
		}
		return null;
	}
	
	/**
	 * 获得包路径下的图片文件
	 * @param pkg
	 * @param fileName
	 * @return
	 */
	public static Bitmap getBitmapFromPackage(
			Class<?> clazz,String pkg,String fileName) {
		InputStream is = clazz.getResourceAsStream(pkg+fileName);
		try {
			if (null == is || is.available() <= 0)
				return null;
		} catch (IOException e) {
			Log.e(TAG, "get bitmap from package fail as exception "+e.getMessage());
			try {
				if (is != null)is.close();
			}catch (Exception er) {}
			return null;
		}
		try {
			return BitmapFactory.decodeStream(is);
		}catch (Exception e) {
			Log.e(TAG, "get bitmap from package fail as exception "+e.getMessage());
		}finally {
			try {
				if (is != null)is.close();
			}catch (Exception e) {}
		}
		return null;
	}
	
	/**
	 * 写入String数据 
	 * <p>无需关心目录及文件是否存在
	 * @param activity
	 * @param fileName
	 * @param content
	 */
	public static void writeStringData(Context context,String fileName,String content){
		if (isSdcardReady()) {
			String filePath = SD_DATA_PATH + fileName;
			String folder = getFolder(filePath);
			mkSdcardFileDirs(folder);
			writeString2Sdcard(filePath, content);
		} else {
			String filePath = ROM_DATA_PATH + fileName;
			String folder = getFolder(filePath);
			mkRomCacheDirs(context, folder);
			writeString2RomCache(context, filePath, content);
		}
	}
	
	/**
	 * 读取String数据
	 * <p>优先从SD卡读取
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static String readStringData(Context context,String fileName){
		if(isSdcardFileExist(SD_DATA_PATH+fileName)){
			return readStringFromSdcard(SD_DATA_PATH+fileName);
		}else if(isRomCacheFileExist(context, ROM_DATA_PATH+fileName)){
			return readStringFromRomCache(context,ROM_DATA_PATH+fileName);
		}
		return null;
	}
	
	/**
	 * 从文件中读取字符串
	 * @param file
	 * @return
	 */
	public static String readStringFromPackage(Class<?> clazz,String pkgPath,String fileName){
		try {
			InputStream is=clazz.getResourceAsStream(pkgPath+fileName);
			return readString(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 从文件中读取字符
	 * @param file
	 * @return
	 */
	public static String readString(File file){
		try {
			InputStream is = new FileInputStream(file);
			return readString(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 从流中读取字符
	 * <p>会自动关闭流
	 * @param is
	 * @return
	 */
	public static String readString(InputStream is){
		ByteArrayOutputStream bos = null;
		try{
			bos = new ByteArrayOutputStream();
			byte[] buffer = new byte[OPTIMIZATION_SIZE];
			int length = -1;
			while( (length = is.read(buffer)) != -1){
				bos.write(buffer,0,length);
			}
			return bos.toString(); 
		} catch(Exception e){
			Log.e(TAG, "read string fail as exception "+e.getMessage());
		}finally{
			try {bos.close();
			} catch (IOException e) {}
			try {is.close();
			} catch (IOException e) {}
		}
		return null;
	}
	
	/**
	 * 从SD卡读取字符文件
	 * @param filePath
	 * @return
	 */
	public static String readStringFromSdcard(String filePath) {
		if(!isSdcardReady()){
			Log.e(TAG, "read string fail as sdCard not exist");
			return null;
		}
		File file=new File(getSdcardPath()+filePath);
		return readString(file);
	}
	
	/**
	 * 从手机内存读取字符文件
	 * @param filePath
	 * @return
	 */
	public static String readStringFromRomCache(Context context,String filePath) {
		File file=new File(getRomCachePath(context)+filePath);
		return readString(file);
	}
	
	/**
	 * 保存字符到指定目录
	 * <p>需要检查目录及文件存在与否
	 * @param fullFilePath
	 * @param content
	 * @return
	 */
	public static boolean writeString(String fullFilePath,String content){
		File file = new File(fullFilePath);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(content.getBytes());
			return true;
		} catch (IOException e) {
			Log.e(TAG, "save string fail as "+e.getMessage());
		}finally{
			try {
				if(fos!=null) fos.close();
			} catch (IOException e) {}
		}
		return false;
	}
	
	/**
	 *  存储字符文件到手机内存
	 * @param activity
	 * @param fileName
	 * @param content
	 * @return
	 */
	public static boolean writeString2RomCache(Context context,String filePath,String content){
		String cachePath = getRomCachePath(context);
		return writeString(cachePath+filePath, content);
	}
	
	/**
	 * 存储字符文件到SD卡
	 * @param filePath
	 * @param content
	 * @return
	 */
	public static boolean writeString2Sdcard(String filePath,String content) {
		if(!isSdcardReady()){
			Log.e(TAG, "save string to sdCard fail as sdCard not exist");
			return false;
		}
		return writeString(getSdcardPath()+filePath,content);
	}
	
	/**
	 * 将assets中的文件释放到指定目录
	 * @param context
	 * @param fromPath assets下文件位置
	 * @param toPath 目标位置
	 * @return
	 */
	public static boolean retrieveFileFromAssets(
			Context context,String fromPath,String toPath){
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			is=context.getAssets().open(fromPath);

			File file = new File(toPath);
			file.createNewFile();
			fos = new FileOutputStream(file);

			byte[] temp = new byte[OPTIMIZATION_SIZE];
			int i = 0;
			while ((i = is.read(temp)) > 0) {
				fos.write(temp, 0, i);
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if (null != fos) 
					fos.close();
			} catch (IOException e) {}
			try {
				if (null != is) 
					is.close();
			} catch (IOException e) {}
		}
		return false;
	}
	
	/**
	 * 检查SD卡空间是否大于limit
	 * @param limit
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static boolean isSdcardAvailable(long limit){
		if(!isSdcardReady())
			return true;
		File sdcardDir = Environment.getExternalStorageDirectory(); 
		StatFs sf = new StatFs(sdcardDir.getPath()); 
		long blockSize = sf.getBlockSize();
		long availCount = sf.getAvailableBlocks();
		if(availCount*blockSize > limit)
			return true;
		return false;
	}
	
	/**
	 * 检查手机私存空间是否大于limit
	 * @param limit
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static boolean isRomCacheAvailable(long limit){
		File root = Environment.getRootDirectory(); 
		StatFs sf = new StatFs(root.getPath()); 
		long blockSize = sf.getBlockSize(); 
		long availCount = sf.getAvailableBlocks(); 
		if(availCount*blockSize > limit)
			return true;
		return false;
	}
	
	/**
	 * 实现文件的拷贝
	 * <p>未对文件路径及合法性进行检查
	 * @param srcFile 源文件
	 * @param targetFile 目标位置
	 */
	public static void copyFile(File srcFile, File targetFile){
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new BufferedInputStream(new FileInputStream(srcFile));
			out = new BufferedOutputStream(new FileOutputStream(targetFile));
			byte[] b = new byte[OPTIMIZATION_SIZE];
			int len;
			while ((len = in.read(b)) != -1) {
				out.write(b, 0, len);
			}
			out.flush();
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
			}
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * 删除目标路径的文件
	 * @param path
	 * @return
	 */
	public static boolean deleteFile(String path){
		File file = new File(path);
		if(file.exists() && file.isFile())
			return file.delete();
		return false;
	}
	
	/**
	 * 同步建立软链接
	 * @param src 实际位置
	 * @param des 对外使用的位置
	 */
	public static void cmdSoftLinks(String src,String des){
		try{
			String command = String.format("ln -s %s %s", src,des);
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(command);
			InputStream in = process.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(in));
			while(rd.readLine() != null);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getPluginsFolder(Context context, String pluginName) {
		String end = "";
		if(pluginName.contains("_")){
			String[] exps = pluginName.split("_");
			end = exps[0] + File.separator + exps[1] + File.separator;
		}
		else end = pluginName + File.separator;
		
		if (isSdcardAvailable(OPTIMIZATION_SIZE)) {
			mkSdcardFileDirs(SD_PLG_PATH + end);
			return getSdcardPath() + SD_PLG_PATH + end;
		}
		
		if (!isRomCacheAvailable(OPTIMIZATION_SIZE)) {
			mkRomCacheDirs(context, ROM_PLG_PATH + end);
			return getRomCachePath(context) + ROM_PLG_PATH + end;
		}
		
		return null;
	}

	public static File newPluginFile(Context context, String pluginName, String fileName) {
		String folder = getPluginsFolder(context, pluginName);
		if(folder != null)
			return new File(folder + fileName);
		return null;
	}

}
