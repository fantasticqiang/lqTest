package com.beeCloud.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.beeCloud.constant.BeeCloudConstant;
import com.beeCloud.util.HttpClientUtils;
import com.beeCloud.util.Md5Util;

/**
 * 老罗商旅快捷确认
 *
 */
public class BeeCloudQuickPayConfirmServlet extends HttpServlet{

	Logger logger = Logger.getLogger("DEFAULT-APPENDER");

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String bc_bill_id = request.getParameter("syid");
		String verify_code = request.getParameter("verify_code");
		
		String app_id = BeeCloudConstant.app_id;//上游平台下发的唯一标识
		long timestamp = new Date().getTime();//时间戳
		String str2sign = BeeCloudConstant.app_id+timestamp+BeeCloudConstant.app_secret; 
		String app_sign = "";
		try {
			app_sign = Md5Util.string2MD5(str2sign);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("app_id", app_id);
		map.put("timestamp", timestamp);
		map.put("app_sign", app_sign);
		map.put("bc_bill_id", bc_bill_id);
		map.put("verify_code", verify_code);
		
		logger.info("请求参数："+JSON.toJSONString(map));
		String url = BeeCloudConstant.kj_url_registe;
		String resultStr = "";
		try {
			resultStr = HttpClientUtils.postJsonParameters(url, JSON.toJSONString(map));
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("返回结果："+resultStr);
		JSONObject resultJson = JSON.parseObject(resultStr);
		String resCode = "";
		String retMsg = "";
		Map<String,String> resultMap = new HashMap<String,String>();
		if("0".equals(resultJson.getString("result_code")) && "0".equals(resultJson.getString("result_msg"))){
			resCode = "0000";
			retMsg = "付款成功";
		}else{
			resCode = "0001";
			retMsg = resultJson.getString("errMsg");
		}
		resultMap.put("resCode", resCode);
		resultMap.put("retMsg", retMsg);
		
	}
}
