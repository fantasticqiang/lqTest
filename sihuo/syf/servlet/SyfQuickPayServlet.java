package com.syf.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import cn.phoneposp.dao.SaruInfoDao;
import cn.phoneposp.entity.RPscalecommission;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.util.AmountUtil;
import com.common.util.StringUtil;
import com.interfaces.O110101.util.OrderNumUtil;
import com.phoneposp.util.PropertiesUtil;
import com.syf.constant.SyfConstant;
import com.syf.dao.SyfQuickPayDao;
import com.syf.model.MerReportModel;
import com.syf.model.MerchantEnterModel;
import com.syf.util.Commons;
import com.syf.util.HttpMsg;
import com.syf.util.HttpUtil;

/**
 * 闪云付快捷下单servlet
 *
 */
public class SyfQuickPayServlet extends HttpServlet{

	Logger logger = Logger.getLogger("DEFAULT-APPENDER");

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		
		SyfQuickPayDao merchantDao = new SyfQuickPayDao();
		logger.info("(闪云付)无卡---T0------消费交易开始");
		// 从前台获取交易参数
		String saruLruid = request.getParameter("saruLruid");// 刷卡商户的ID
		String merId = request.getParameter("merId");// 报户生成的商户标识
		String cardNoFromApp = request.getParameter("cardNo");// 交易卡号
		String orderAmt = request.getParameter("orderAmt");// 订单金额，单位:分
		String payType = "freecardcredit";//支付类型，无卡支付
		if (StringUtils.isEmpty(cardNoFromApp) || StringUtils.isEmpty(merId)) {
			logger.info("T0交易接口传入的交易卡号或者商户标识为空,程序结束");
			return;
		}
		PropertiesUtil util = new PropertiesUtil(SaruInfoDao.class.getResource(
				"/merchantInfo.properties").getFile());
		String prefix_wk = util.getValue("prefix_wk");
		Integer id = SaruInfoDao.getSequence("SEQ_ORDER_NO");
		String orderId = "";
		orderId = OrderNumUtil.createOrderNum(prefix_wk, id);// 交易订单号
		
		MerReportModel merReport = merchantDao.getMerReport(saruLruid);
		merId = merReport.getMerId();//商户号
		String md5Key = merReport.getKey();//秘钥
		logger.info("商户号："+merId);
		logger.info("秘钥："+md5Key);
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
		String cardExpireDate = "0519";//有效期
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
		reqBusiMap.put("cardExpireDate", cardExpireDate);
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
		if(httpMsg.isVerify() ) {
			//System.out.println(httpMsg.getResBusiMsg());
			//下单成功的时候
			if("S01000000".equals(returnMap.getString("resCode")) ){ //判断下单成功与否？
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
				logger.info("闪云付=====更新water表交易状态为 1:已提交");
				saruInfoDao.updateFreeCard2(orderId, status, handFee);// 增加无卡支付流水water
				if ("freecardcredit".equals(payType)) {
					saruInfoDao.updateSaleAccountByWeixin("12",
							p3.doubleValue(), handFee, saruLruid);// 修改商户总额
				}
				logger.info("闪云付=====插入wuka_water表交易记录");// 插入无卡表流水号
				saruInfoDao.insertWukaWater(payType, scalecommission,
						saruLruid, cardNo, p3.doubleValue(), handFee,
						orderId);
				
				// 返回给前台-银联在线95516页面
				response.setContentType("text/html");
				PrintWriter out = response.getWriter();
				
				//返回页面
				String payPage = returnMap.getString("payPage");
				map2app.put("orderNo", orderId);
				map2app.put("resCode", "000");
				map2app.put("resMsg", "下单成功！");
				map2app.put("resPage", payPage);
				out.println(JSON.toJSONString(map2app));
				out.flush();
				out.close();
			}else {
				response.setContentType("text/html");
				PrintWriter out = response.getWriter();
				map2app.put("resCode", "001");
				map2app.put("orderNo", orderId);
				if (returnMap.containsKey("retMsg")) {
					map2app.put("resMsg", returnMap.get("retMsg").toString());
					out.println(JSON.toJSONString(map2app));
				}
				out.flush();
				out.close();
			}
		}else {
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			JSONObject map = JSON.parseObject(httpMsg.getResMsg());
			map2app.put("resCode", map.getString("gateResCode"));
			map2app.put("resMsg", map.get("gateResMsg").toString());
			out.println(JSON.toJSONString(map2app));
			out.flush();
			out.close();
		}
		logger.info("(闪云付)无卡---T0------消费交易结束");
	}
}
