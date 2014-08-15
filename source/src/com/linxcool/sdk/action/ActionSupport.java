package com.linxcool.sdk.action;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Observable;
import java.util.TreeMap;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.linxcool.sdk.util.SecurityUtil;


/**
 * 基础动作类
 * @author: linxcool.hu
 */
public abstract class ActionSupport extends Observable implements HttpListener,Runnable{
	
	private static final String TAG="ActionSupport";
	
	// 响应状态结果
	public static final int SUCCESS = 0;	//成功
	public static final int FAILURE = 1;	//失败
	public static final int ERROR = 2;		//错误
	
	//上下文对象
	protected Context context;
	
	//动作信息列表
	protected HttpHelper httpHelper;
	
	protected TreeMap<String, String> gContent;
	protected String pContent;
	
	//响应信息
	protected String response;
	protected int errorCode;
	protected String errorMsg;
	
	protected Object data;
	
	/**
	 * 获取通信失败时的错误码
	 * @return
	 */
	public int getErrorCode(){
		return errorCode;
	}
	
	/**
	 * 获取响应的错误消息
	 * @return
	 */
	public String getErrorMsg() {
		return errorMsg;
	}
	
	@SuppressWarnings("unchecked")
	public <T>T getData() {
		return (T) data;
	}

	public ActionSupport(Context context){
		this.context = context;
		httpHelper = new HttpHelper(context);
		httpHelper.setMethod(HttpHelper.HTTP_METHOD_POST);
		gContent = new TreeMap<String, String>();
	}

	/**
	 * 执行网络请求动作
	 */
	public void superAction(){
		try {
			doRequest(getURL());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void actionStart(){
		superAction();
	}
	
	/**
	 * 重写以传入基本信息 未被任何方法调用 仅用于功能性设计
	 * @param jsonObj
	 * @throws JSONException
	 */
	protected void putBasicData(JSONObject jsonObj) throws JSONException{
		// Empty
	}
	
	/**
	 * 重写以传入对应业务所需的数据 未被任何方法调用 仅用于功能性设计
	 * @param datas
	 */
	public abstract void putReqData(Object... datas);
	
	public void doRequest(String url) throws UnsupportedEncodingException{
		Log.d(TAG, "action request url is " + url);
		
		StringBuilder sb = new StringBuilder();
		Iterator<String> ks = gContent.keySet().iterator();
		while(ks.hasNext()){
			String k = ks.next();
			String v = gContent.get(k);
			sb.append("&" + k + "=" + v);
		}
		if(sb.length() > 0) {
			url += "?" + sb.substring(1);
		}

		HttpUriRequest request = httpHelper.createHttpRequest(url);
		String sign = SecurityUtil.md5(url + "hDRr92iF");
		request.addHeader("SDK-SIGN", sign);
		
		if(pContent != null){
			HttpPost post = (HttpPost) request;
			post.setEntity(new StringEntity(pContent));
		}
		
		httpHelper.request(url, request, this);
	}
	
	@Override
	public void onComplete(String response) {
		this.response=response;
		if(context instanceof Activity)
			((Activity)context).runOnUiThread(this);
		else run();
	}

	@Override
	public void onError(int code,String msg) {
		Log.e(TAG,"do action response error");
		errorCode = code;
		errorMsg = msg;
		response = null;
		if(context instanceof Activity)
			((Activity)context).runOnUiThread(this);
		else run();
	}
	
	/**
	 * 获取网络请求的地址 请根据动作ID筛选
	 * @return
	 */
	protected abstract String getURL();

	@Override
	public void run() {
		try {
			setChanged();
			if(response == null){
				notifyObservers(ERROR);	
				return;
			}
			JSONObject obj = new JSONObject(response);
			int code = obj.getInt("code");
			if (code == HttpHelper.RES_CODE_SUCCESS) {
				onSuccess(obj);
				notifyObservers(SUCCESS);
			} else {
				errorMsg = obj.optString("msg");
				notifyObservers(FAILURE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			errorMsg="parse response json error";
			notifyObservers(ERROR);
		}
	}
	
	/**
	 * 网络请求成功后将回调以下方面
	 * @param obj
	 * @throws Exception
	 */
	protected abstract void onSuccess(final JSONObject obj) throws Exception;
	
}
