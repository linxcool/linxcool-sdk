package com.linxcool.sdk;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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
			comment = mixDecrypt(comment);

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
	
	public static String mixDecrypt(String in) {
		byte[] bytes = hexStringToBytes(in);
		resolveByte(bytes);
		bytes = aesDecrypt(bytes, "linxcool_aes_mix");
		return reduceCotent(new String(bytes));
	}
	
	public static byte[] hexStringToBytes(String str) {
		if (str == null || str.equals(""))
			return null;
		str = str.toUpperCase(Locale.getDefault());
		int length = str.length() / 2;
		char[] hexChars = str.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}
	
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}
	
	public static void resolveByte(byte[] bytes) {
		for (int i = bytes.length - 1; i > 0; i--) {
			// 上一字节高低位
			int pMSB = bytes[i - 1] & 0xf0;
			int pLSB = bytes[i - 1] & 0x0f;
			// 当前字节高地位
			int cMSB = bytes[i] & 0xf0;
			int cLSB = bytes[i] & 0x0f;

			if ((i & 0x1) == 1) {
				bytes[i] = (byte) (pMSB | cLSB);
				bytes[i - 1] = (byte) (cMSB | pLSB);
			} 
			else {
				bytes[i] = (byte) (cMSB | pLSB);
				bytes[i - 1] = (byte) (pMSB | cLSB);
			}
		}
	}
	
	public static byte[] aesDecrypt(byte[] str, String in) {
		try {
			SecretKeySpec key = new SecretKeySpec(in.getBytes(), "AES");
			// 创建密码器
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			IvParameterSpec zeroIv = new IvParameterSpec(
					"0102030405060708".getBytes());
			// 初始化
			cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
			// 加密
			return cipher.doFinal(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static String reduceCotent(String content) {
		String rs = "";
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			if (c != '?') rs += c;
			else return rs;
		}
		return rs;
	}
}
