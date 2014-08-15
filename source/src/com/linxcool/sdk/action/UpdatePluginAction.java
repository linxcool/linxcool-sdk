package com.linxcool.sdk.action;

import java.util.Observable;
import java.util.Observer;

import org.json.JSONObject;

import com.linxcool.sdk.support.PluginInfo;
import com.linxcool.sdk.support.PluginInfoManager;
import com.linxcool.sdk.support.PluginDownloader;
import com.linxcool.sdk.support.PluginDownloader.OnPluginDownloadListener;
import com.linxcool.sdk.util.ResourceUtil;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

/**
 * 更新插件操作
 * <P>
 * <STRONG>Time：</STRONG>2014年6月1日 下午10:20:13
 * </P>
 * @author 胡昌海(linxcool.hu)
 */
public class UpdatePluginAction extends ActionSupport implements OnPluginDownloadListener, Observer{

	private static final String TAG = "UpdatePluginAction";
	
	private String plugName;
	private int plugVer;
	
	public UpdatePluginAction(Context context) {
		super(context);
		httpHelper.setMethod(HttpHelper.HTTP_METHOD_GET);
		addObserver(this);
	}

	@Override
	public void putReqData(Object... datas) {
		plugName = String.valueOf(datas[0]);
		plugVer = (Integer) datas[1];
		gContent.put("plgname", plugName);
		gContent.put("ver", String.valueOf(plugVer));
	}

	@Override
	protected String getURL() {
		return HttpHelper.REQUEST_HOST + "plg";
	}

	@Override
	protected void onSuccess(JSONObject obj) throws Exception {
		String url = obj.getString("plg_url");
		plugVer = Integer.parseInt(obj.getString("ver"));
		
		PluginInfo info = new PluginInfo(plugName, plugVer);
		info.fileFolder = ResourceUtil.getPluginsFolder(context, plugName);
		info.updateUrl = url;
		info.fileName = PluginInfoManager.getPluginFileName(plugName, plugVer, ".jar");
		
		if(context instanceof Activity)
			PluginDownloader.start(context, info, this);
		else{
			Looper.prepare();
			PluginDownloader.start(context, info, this);
			Looper.loop();
		}
	}
	
	@Override
	public void onComplete(PluginInfo pluginInfo) {
		Log.i(TAG, "plugin download success " + plugVer);
	}

	@Override
	public void onDError(int code, String msg) {
		Log.e(TAG, code + " | " + msg);
	}

	@Override
	public void update(Observable observable, Object data) {
		switch ((Integer)data) {
		case ERROR:
		case FAILURE:
			Log.e(TAG, errorCode + " | " + errorMsg);
			break;
		default:
			break;
		}
	}

}
