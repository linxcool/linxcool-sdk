package com.linxcool.sdk;

import java.io.File;
import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.content.Context;
import dalvik.system.DexClassLoader;
/**
 * 插件加载器
 * <p><b>Time:</b> 2013-10-31
 * @author 胡昌海(linxcool.hu)
 */
public class PluginLoader {

	@SuppressLint("NewApi")
	public static Class<?> load(Context context,PluginInfo pluginInfo){
		try {
			// JAR文件路径
			String dexPath = pluginInfo.fileFolder + pluginInfo.fileName;
			// 系统优化DEX后存放路径
			File optimizedDir = context.getDir("outdex", Context.MODE_PRIVATE);
			// SO文件存放路径
			String libPath = context.getApplicationInfo().nativeLibraryDir;
			// 生成类加载器
			pluginInfo.dexLoader = new DexClassLoader(
					dexPath, 
					optimizedDir.getAbsolutePath(), 
					libPath, 
					PluginLoader.class.getClassLoader());
			return pluginInfo.dexLoader.loadClass(pluginInfo.apiClsName);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void setParent(ClassLoader src, ClassLoader parent) {
		try {
			Class<?> clazz = Class.forName("java.lang.ClassLoader");
			Field field = clazz.getDeclaredField("parent");
			field.setAccessible(true);
			field.set(src, parent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
