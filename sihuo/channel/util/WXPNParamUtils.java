package com.channel.util;

import java.util.Map;



import com.channel.servlet.HttpRequestor;


import net.sf.json.JSONObject;

public class WXPNParamUtils {

	public String getOpenId(String requestUrl) {
		String openid = "";
		String oppid = "";
		try {
			
			oppid = new HttpRequestor().doGet(requestUrl);
//			JSONObject oppidObj =JSONObject.fromObject(oppid);
//			String access_token = (String) oppidObj.get("access_token");
//			openid = (String) oppidObj.get("openid");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return oppid;
	}
	
	public String getOpenId(String requestUrl, Map map) {
		String openid = "";
		String oppid = "";
		try {
			oppid = new HttpRequestor().doPost(requestUrl, map);
			JSONObject oppidObj =JSONObject.fromObject(oppid);
			String access_token = (String) oppidObj.get("access_token");
			openid = (String) oppidObj.get("openid");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return openid;
	}
	
}
