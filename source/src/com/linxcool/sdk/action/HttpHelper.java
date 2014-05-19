package com.linxcool.sdk.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;

import com.linxcool.sdk.util.SystemUtil;

/**
 * HTTP工具类
 * @author: linxcool.hu
 */
public class HttpHelper {
	private Context context;
	
	/** 网络配置 */
	public static String REQUEST_HOST;

	/** HTTP常量-GET请求 */
	public static final int HTTP_METHOD_GET = 1;
	/** HTTP常量-POST请求 */
	public static final int HTTP_METHOD_POST = 2;
	/** HTTP常量-请求限制时间 */
	public static final int HTTP_REQ_LIMIT_TIME = 15 * 1000;
	/** HTTP常量-响应限制时间 */
	public static final int HTTP_RES_LIMIT_TIME = 25 * 1000;

	// 返回码
	public static final int RES_CODE_SUCCESS = 200;
	public static final int RES_CODE_ERROR = 1;
	public static final int RES_CODE_FAIL = 2;

	public static final int ERROR_CODE_TIME_OUT = 600;
	public static final int ERROR_CODE_UNKNOW = 601;

	private int errorCode;
	private String errorMsg;

	private HttpClient client;
	private HttpResponse response;

	public HttpHelper(Context context) {
		this.context=context;
	}
	
	public static void setRequestHost(String host){
		REQUEST_HOST = host;
	}

	public HttpClient createHttpClient(){
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, HTTP_REQ_LIMIT_TIME);
		HttpConnectionParams.setSoTimeout(params,HTTP_RES_LIMIT_TIME);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
		//添加网络代理
		if(SystemUtil.getProxy(context)!=null){
			HttpHost proxy = new HttpHost("10.0.0.172", 80);
			params.setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
		}
		return new DefaultHttpClient(params);
	}

	public HttpUriRequest createHttpRequest(String url,int method){
		HttpUriRequest request=null;
		if(method == HTTP_METHOD_GET){
			request=new HttpGet(url);
		}else{
			request=new HttpPost(url);
		}
		//关闭 100-Continue 询问
		request.getParams().setBooleanParameter(
				CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
		return request;
	}

	public boolean openUrl(HttpUriRequest request){
		client=createHttpClient();
		try {
			response=client.execute(request);
			
			StatusLine statusLine=response.getStatusLine();
			if(statusLine == null)return true;
			
			int code=statusLine.getStatusCode();
			if(code == HttpStatus.SC_OK)
				return true;
			else{
				errorCode = code;
				errorMsg = statusLine.getReasonPhrase();
				return false;
			}
		}catch (ConnectTimeoutException e) {
			errorCode=ERROR_CODE_TIME_OUT;
			errorMsg="connect timeout";
		}catch (SocketTimeoutException e) {
			errorCode=ERROR_CODE_TIME_OUT;
			errorMsg="socket timeout";
		}catch (Exception e) {
			e.printStackTrace();
			errorCode=ERROR_CODE_UNKNOW;
			errorMsg="request data error "+e.getMessage();
		}
		return false;
	}

	public String getHttpResponse(){
		String result = null;
		try {
			if(response==null)return null;
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			ByteArrayOutputStream content = new ByteArrayOutputStream();
			int readBytes = 0;
			byte[] sBuffer = new byte[512];
			while ((readBytes = is.read(sBuffer)) != -1)
				content.write(sBuffer, 0, readBytes);
			result = new String(content.toByteArray());
			result = URLDecoder.decode(result, "UTF-8");
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(client!=null)
				client.getConnectionManager().shutdown();
		}
		return result;
	}

	public void request(
			final String url,final HttpUriRequest request,final HttpListener listerner) {
		new Thread() {
			@Override
			public void run() {
				boolean ret = openUrl(request);
				if(!ret){
					listerner.onError(errorCode,errorMsg);
					return;
				}
				listerner.onComplete(getHttpResponse());
			}
		}.start();
	}
}
