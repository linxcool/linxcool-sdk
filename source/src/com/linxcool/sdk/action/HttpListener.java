package com.linxcool.sdk.action;

/**
 * 网络监听
 * @author: linxcool.hu
 */
public interface HttpListener {
	/**
	 * 网络请求完成
	 * @param response
	 */
	public void onComplete(String response);
	
	/**
	 * 网络请求失败
	 * @param code 错误码
	 * @param msg 错误信息
	 */
	public void onError(int code,String msg);
}
