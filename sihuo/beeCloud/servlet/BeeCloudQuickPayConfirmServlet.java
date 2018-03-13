package com.beeCloud.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cn.phoneposp.dao.SaruInfoDao;
import cn.phoneposp.entity.RPscalecommission;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.beeCloud.constant.BeeCloudConstant;
import com.beeCloud.util.HttpClientUtils;
import com.beeCloud.util.Md5Util;
import com.common.util.AmountUtil;
import com.common.util.StringUtil;

/**
 * 老罗商旅快捷确认
 *
 */
public class BeeCloudQuickPayConfirmServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Logger logger = Logger.getLogger("DEFAULT-APPENDER");

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		
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
		System.out.println("请求参数："+JSON.toJSONString(map));
		String url = BeeCloudConstant.kj_url_confirm;
		String resultStr = "";
		try {
			resultStr = HttpClientUtils.postJsonParameters(url, JSON.toJSONString(map));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("返回结果："+resultStr);
		logger.info("返回结果："+resultStr);
		JSONObject resultJson = JSON.parseObject(resultStr);
		String resCode = "";
		String retMsg = "";
		Map<String,String> resultMap = new HashMap<String,String>();
		if("0".equals(resultJson.getString("result_code")) && "OK".equals(resultJson.getString("result_msg"))){
			resCode = "0000";
			retMsg = "付款提交成功";
			//进行相关业务处理
		}else{
			resCode = "0001";
			retMsg = resultJson.getString("errMsg");
		}
		resultMap.put("resCode", resCode);
		resultMap.put("resMsg", retMsg);
		PrintWriter out = response.getWriter();
		out.println(JSON.toJSONString(resultMap));
		out.flush();
		out.close();
	}
	
	/**
	 * 进行相关业务
	 */
	public void doSomeBusiness(String orderId,String saruLruid,String orderAmt,String cardNo,String payType){
		SaruInfoDao saruInfoDao = new SaruInfoDao();
		logger.info("插入water表交易记录");
		saruInfoDao.insertFreeCardWithPlace(orderId, saruLruid,
				Double.parseDouble(orderAmt), "cvv2", cardNo,
				"jwd", "china");
		Double p3 = Double.valueOf(AmountUtil.div(
				Double.valueOf(orderAmt).doubleValue(), 100.0D, 2));
		RPscalecommission scalecommission = saruInfoDao
				.querySracT0Rate(saruLruid);
		double rate = scalecommission.getFreecardvalue();
		double handFee = AmountUtil.ceiling(
				AmountUtil.mul(p3.doubleValue(), rate), 2);
		int status = 1;// 0:初始 1:已提交 2:成功 3:失败
		String linv = saruInfoDao.getLinvnum();
		if (linv == null)
			linv = "0";
		int linv2 = Integer.parseInt(linv);
		String linvnum = (linv2 + 1 + "").trim();
		linvnum = StringUtil.fillChar(linvnum, "0", "L", 6);
		logger.info("beeCloud=====更新water表交易状态为 1:已提交");
		saruInfoDao.updateFreeCard2(orderId, status, handFee);// 增加无卡支付流水water
		if ("freecardcredit".equals(payType)) {
			saruInfoDao.updateSaleAccountByWeixin("12",
					p3.doubleValue(), handFee, saruLruid);// 修改商户总额
		}
		logger.info("beCloud=====插入wuka_water表交易记录");// 插入无卡表流水号
		saruInfoDao.insertWukaWater(payType, scalecommission,
				saruLruid, cardNo, p3.doubleValue(), handFee,
				orderId);
	}
	
}
