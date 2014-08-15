package com.linxcool.sdk;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.linxcool.sdk.util.SecurityUtil;

/**
 * 插件校验
 * <p><b>Time:</b> 2013-10-29
 * @author 胡昌海(linxcool.hu)
 */
public class KernelVerifier {

	private static final int LENGTH_COMMENT = 200;
	private static final String CHARACTER_NULL = "*";

	public static KernelInfo verify(File file){
		try {
			return verify(new FileInputStream(file));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static KernelInfo verify(InputStream is){
		return verify(new DataInputStream(is));
	}

	public static KernelInfo verify(DataInputStream dis){
		try{
			dis.skip(dis.available() - LENGTH_COMMENT);
			byte[] buffer = new byte[LENGTH_COMMENT];
			dis.read(buffer);
			dis.close();

			String comment = new String(buffer);
			if(comment == null || comment.length() == 0)
				return null;

			comment = comment.replace(CHARACTER_NULL, "");
			comment = SecurityUtil.mixDecrypt(comment);

			String[] data = comment.split("\\|");
			if(data.length < 3)
				return null;

			KernelInfo info = new KernelInfo();
			info.name = data[0];
			info.version = Integer.parseInt(data[1]);
			info.apiClsName = data[2];
			
			return info;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
}
