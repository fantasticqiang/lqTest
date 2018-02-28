package com.syf.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import cn.phoneposp.dao.ConnectionSource;
import cn.phoneposp.dao.SaruInfoDao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.interfaces.O110101.util.OrderNumUtil;
import com.phoneposp.util.PropertiesUtil;
import com.syf.constant.SyfConstant;
import com.syf.dao.SyfQuickPayDao;
import com.syf.model.MerReportModel;
import com.syf.model.MerchantEnterModel;
import com.syf.util.Commons;
import com.syf.util.HttpMsg;
import com.syf.util.HttpUtil;

public class PayServletTest {

	public static void main(String[] args) throws Exception {
		testPay();
	}
	
	public static void testPay(){
		SyfQuickPayDao merchantDao = new SyfQuickPayDao();
		// 从前台获取交易参数
		String saruLruid = "6000002332";// 刷卡商户的ID
		String merId = "mi118021410542700009";// 报户生成的商户标识
		String cardNoFromApp = "6214830125502616";// 交易卡号
		String orderAmt = "10000";// 订单金额，单位:分
		String payType = "freecardcredit";//支付类型，无卡支付
		if (StringUtils.isEmpty(cardNoFromApp) || StringUtils.isEmpty(merId)) {
			return;
		}
		PropertiesUtil util = new PropertiesUtil(SaruInfoDao.class.getResource(
				"/merchantInfo.properties").getFile());
		String prefix_wk = util.getValue("prefix_wk");
		Integer id = SaruInfoDao.getSequence("SEQ_ORDER_NO");
		String orderId = "";
		orderId = OrderNumUtil.createOrderNum(prefix_wk, id);// 交易订单号
		orderId = new Date().getTime()+"";
		
		MerReportModel merReport = merchantDao.getMerReport(saruLruid);
		merId = merReport.getMerId();//商户号
		/**
		 * 失败：mi118021410542700009
		 * 成功：mi118021410542700009
		 */
		String md5Key = merReport.getKey();//秘钥
		System.out.println("商户号："+merId);
		System.out.println("商户号："+md5Key);
		/**
		 * 6240900e7f2e4bdbb81f08a695c19234
		 * 6240900e7f2e4bdbb81f08a695c19234
		 */
		/** 准备请求参数*/
		//公共字段
		String source = "M";//M-商户发起
		String version = "V1";//
		String cipherType = "AES";//加密类型
		String signType = "MD5";//签名方式
		String transCode = "T102";//交易编码
		String partnerId = SyfConstant.partnerId;//代理商ID
		String accessToken = orderId;//请求流水号
		String ciphertext = "";//请求密文
		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("source", source);
		reqMap.put("version", version);
		reqMap.put("cipherType", cipherType);
		reqMap.put("signType", signType);
		reqMap.put("transCode", transCode);
		reqMap.put("partnerId", partnerId);
		reqMap.put("merchId", merId);
		reqMap.put("accessToken", accessToken);
		reqMap.put("reqReserved", "");
		//密文字段
		String orderNo = orderId;//商户订单号
		String amount = String.format("%.2f",Double.parseDouble(orderAmt)/100d);//金额
		String subject = "线上支付";//商品主题
		String body = "线上支付";//商品内容
		String clientIp = SyfConstant.clientIp;//发起请求客户端的ip
		String notifyUrl = SyfConstant.notifyUrl;//异步通知地址
		String cardNo = cardNoFromApp;//卡号

		MerchantEnterModel merchantInfo = merchantDao.queryMerchantBysaruId(saruLruid);
		String cardName = merchantInfo.getMerName();//结算卡姓名，银行卡持卡人姓名
		String cardMobile = merchantInfo.getPhone();//手机
		String cardCertNo = merchantInfo.getIdCard();//身份证
		String unionNo = merchantInfo.getBankNo();//联行号

		String cardCvv = "123";//不知道此字段是什么？
		//String cardExpireDate = "0519";//有效期
		String bankName = merchantInfo.getBankName();//银行名称
		String returnUrl = "";//前台通知地址
		Map<String, String> reqBusiMap = new HashMap<String, String>();
		reqBusiMap.put("orderNo", orderNo);
		reqBusiMap.put("amount", amount);
		reqBusiMap.put("subject", subject);
		reqBusiMap.put("body", body);
		reqBusiMap.put("clientIp", clientIp);
		reqBusiMap.put("notifyUrl", notifyUrl);
		reqBusiMap.put("cardNo", cardNo);
		reqBusiMap.put("cardName", cardName);
		reqBusiMap.put("cardMobile", cardMobile);
		reqBusiMap.put("cardCertNo", cardCertNo);
		reqBusiMap.put("unionNo", unionNo);
		reqBusiMap.put("cardCvv", cardCvv);
		//reqBusiMap.put("cardExpireDate", cardExpireDate);
		reqBusiMap.put("bankName", bankName);
		reqBusiMap.put("returnUrl", returnUrl);
		
		try {
			Commons.sign_encode(reqBusiMap, reqMap, md5Key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String url = SyfConstant.kj_url;//请求地址
		HttpMsg httpMsg = HttpUtil.postJson(url, reqMap);
		try {
			Commons.validate_decode(httpMsg, md5Key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		HashMap<String, String> map2app = new HashMap<String,String>();
		JSONObject returnMap = JSONObject.parseObject(httpMsg.getResBusiMsg());
		if(httpMsg.isVerify()){
			System.out.println("验签通过");
		}else{
			System.out.println("验签失败");
		}
		JSONObject map = JSON.parseObject(httpMsg.getResMsg());
		map2app.put("resCode", map.getString("gateResCode"));
		map2app.put("resMsg", map.get("gateResMsg").toString());
		System.out.println("返回的结果："+JSON.toJSONString(map2app));
	}
}
