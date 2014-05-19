package com.linxcool.sdk;

import android.annotation.SuppressLint;
import java.security.MessageDigest;
import java.util.Locale;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加解密相关类
 * @author: linxcool.hu
 */
public class LaunchSecurityUtil {

	/**
	 * 字节数组转16进制字符
	 * @param src
	 * @return
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder sb = new StringBuilder();
		if (src == null || src.length <= 0)
			return null;
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2)
				sb.append(0);
			sb.append(hv);
		}
		return sb.toString();
	}

	/**
	 * 16进制字符串转字节数组
	 * @param str
	 * @return
	 */
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

	static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/**
	 * md5加密
	 * @param input
	 * @param bit 16位或32位
	 * @return
	 */
	public static String md5(String input, int bit) {
		try {
			String algorithm = System.getProperty("MD5.algorithm", "MD5");
			MessageDigest md = MessageDigest.getInstance(algorithm);
			byte[] bs = md.digest(input.getBytes("utf-8"));
			if (bit == 16) {
				return bytesToHexString(bs).substring(8, 24);
			}
			return bytesToHexString(bs);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String md5(String input) {
		try {
			return md5(input, 16);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * aes加密
	 * @param content 需要加密的内容 待加密内容的长度必须是16的倍数
	 * @param in 加密密码 必须是16位
	 * @return
	 */
	@SuppressLint("TrulyRandom") 
	public static byte[] aesEncrypt(String content, String in) {
		try {
			SecretKeySpec key = new SecretKeySpec(in.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			byte[] byteContent = content.getBytes("utf-8");
			IvParameterSpec zeroIv = new IvParameterSpec(
					"0102030405060708".getBytes());
			cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
			return cipher.doFinal(byteContent);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * aes解密
	 * @param str 待解密内容
	 * @param in 解密密钥 必须为16的整数倍
	 * @return
	 */
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

	/**
	 * 补充内容长度为16的倍数 补充的字符为 ?
	 * @param content
	 * @return
	 */
	static String addContent(String content) {
		int srcLen = content.length();
		int addedLen = srcLen - 16 * (srcLen / 16);
		for (int i = 0; i < addedLen; i++) {
			content += "?";
		}
		return content;
	}

	/**
	 * 减去末尾为 ? 的字符
	 * @param content
	 * @return
	 */
	static String reduceCotent(String content) {
		String rs = "";
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			if (c != '?') rs += c;
			else return rs;
		}
		return rs;
	}

	/**
	 * 混淆字节 效果如下： <p>
	 * a1 b1,a2 b2,a3 b3,a4,b4,a5 b5<br>
	 * a2 b1,a1 b3,a4 b2,a3 b5,a5 b4</p>
	 * @param bytes
	 * @return
	 */
	public static void mixByte(byte[] bytes) {
		for (int i = 1; i < bytes.length; i++) {
			// 上一字节高低位
			int pMSB = bytes[i - 1] & 0xf0;
			int pLSB = bytes[i - 1] & 0x0f;
			// 当前字节高地位
			int cMSB = bytes[i] & 0xf0;
			int cLSB = bytes[i] & 0x0f;
			// 奇数是高位互换
			if ((i & 0x1) == 1) {
				bytes[i] = (byte) (pMSB | cLSB);
				bytes[i - 1] = (byte) (cMSB | pLSB);
			} 
			// 偶数时低位互换
			else {
				bytes[i] = (byte) (cMSB | pLSB);
				bytes[i - 1] = (byte) (pMSB | cLSB);
			}
		}
	}

	/**
	 * 解析混淆后的字节
	 * @param bytes
	 */
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

	/**
	 * 混淆加密
	 * @param in
	 * @return
	 */
	public static String mixEncrypt(String in) {
		in = addContent(in);
		byte[] bytes = aesEncrypt(in, "linxcool_aes_mix");
		mixByte(bytes);
		return bytesToHexString(bytes);
	}

	/**
	 * 混淆解密
	 * @param in
	 * @return
	 */
	public static String mixDecrypt(String in) {
		byte[] bytes = hexStringToBytes(in);
		resolveByte(bytes);
		bytes = aesDecrypt(bytes, "linxcool_aes_mix");
		return reduceCotent(new String(bytes));
	}
}
