package com.syf.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cn.phoneposp.dao.RWBankCardDAO;
import cn.phoneposp.dao.SaruInfoDao;
import cn.phoneposp.entity.RPscalecommission;
import cn.phoneposp.entity.T0CashInfoNew;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.util.AmountUtil;
import com.common.util.StringUtil;
import com.google.gson.Gson;
import com.syf.constant.SyfConstant;
import com.syf.dao.SyfQuickPayDao;
import com.syf.model.MerReportModel;
import com.syf.util.Commons;
import com.syf.util.HttpMsg;
import com.syf.util.HttpUtil;

/**
 * 闪云付查单
 *
 */
public class SyfQuickPayQueryServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	Logger logger = Logger.getLogger("DEFAULT-APPENDER");

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//request.setCharacterEncoding("UTF-8");
		logger.info("syf cha xun jie kou 开始 --------");
		String orderId = request.getParameter("orderId");// 订单号
		logger.info("前端传过来的订单号:"+orderId);
		// 根据订单号查询merId
		SyfQuickPayDao merchantEnterDao = new SyfQuickPayDao();
		String saruLruid = merchantEnterDao.selectSaruLruidByOrderId(orderId);
		MerReportModel merReport = merchantEnterDao.getMerReport(saruLruid);
		String merId = merReport.getMerId();//商户号
		String md5Key = merReport.getKey();//秘钥
		/** 准备请求参数*/
		//公共字段
		String source = "M";//M-商户发起
		String version = "V1";//
		String cipherType = "AES";//加密类型
		String signType = "MD5";//签名方式
		String transCode = "Q101";//交易编码
		String partnerId = SyfConstant.partnerId;//代理商ID
		String merchId = merId;//商户号
		String accessToken = System.currentTimeMillis()+"";//请求流水号
		String ciphertext = "";//请求密文
		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("source", source);
		reqMap.put("version", version);
		reqMap.put("cipherType", cipherType);
		reqMap.put("signType", signType);
		reqMap.put("transCode", transCode);
		reqMap.put("partnerId", partnerId);
		reqMap.put("merchId", merchId);
		reqMap.put("accessToken", accessToken);
		reqMap.put("reqReserved", "");
		//密文字段
		String orderNo = orderId;//商户订单号
		Map<String, String> reqBusiMap = new HashMap<String, String>();
		reqBusiMap.put("orderNo", orderNo);
		try {
			Commons.sign_encode(reqBusiMap, reqMap, md5Key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String url = SyfConstant.kj_url;
		HttpMsg httpMsg = HttpUtil.postJson(url, reqMap);
		logger.info("闪云付返回报文："+httpMsg.getResMsg());
		try {
			Commons.validate_decode(httpMsg, md5Key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, String> resultMap = new HashMap<String, String>();
		if(httpMsg.isVerify()){  //验签通过
			JSONObject returnMap = JSONObject.parseObject(httpMsg.getResBusiMsg());
			HashMap<String, String> map2app = new HashMap<String,String>();
			if("S01000000".equals(returnMap.getString("resCode")) && "02".equals(returnMap.getString("status"))){
				resultMap.put("retCode", "000");
				resultMap.put("retMsg", "支付成功");

				// 处理重复回调
				SaruInfoDao saruInfo = new SaruInfoDao();
				SyfQuickPayDao merchantDao = new SyfQuickPayDao();
				String orderStatus = saruInfo.getTradeStatusByOrderNo(orderNo);
				if (orderStatus != null && "2".equals(orderStatus)) {
					logger.info("此订单已经成功，已经成功更新交易表，插入T0表");
					response.getWriter().write(new String(JSON.toJSONString(resultMap).getBytes("utf-8"),"utf-8"));
					response.getWriter().flush();
					response.getWriter().close();
					return;
				}
				// 处理业务逻辑，更新成功交易表，插入T0表,(由于无需代付，状态置为M--无卡快捷)
				//String saruLruid = "";
				//saruLruid = saruInfo.getSarulruidByOrderNo(merId);//
				String payType = "freecardcredit";
				String orderAmount = saruInfo
						.getTradeAmountByOrderNo(orderNo);
				Double p3 = Double.valueOf(AmountUtil.div(Double
						.valueOf(Double.parseDouble(orderAmount))
						.doubleValue(), 100.0D, 2));
				RPscalecommission scalecommission = saruInfo
						.querySracT0Rate(saruLruid);
				double rate = scalecommission.getFreecardvalue();
				double handFee = AmountUtil.ceiling(
						AmountUtil.mul(p3.doubleValue(), rate), 2);
				int status = 2;// 0:初始 1:已提交 2:成功 3:失败
				String linv = saruInfo.getLinvnum();
				if (linv == null)
					linv = "0";
				int linv2 = Integer.parseInt(linv);
				String linvnum = (linv2 + 1 + "").trim();
				linvnum = StringUtil.fillChar(linvnum, "0", "L", 6);
				String cardNo = merchantDao
						.selectCardNoByOrderId(orderNo);
				// 插入银行卡流水号
				logger.info("插入成功交易表");
				saruInfo.insertBankCardConsumer(payType,
						scalecommission, saruLruid, cardNo, p3,
						handFee, orderNo, linvnum);
				
				// 以下逻辑是处理成功交易表和T0出款表中的内容
				RWBankCardDAO bankDao = new RWBankCardDAO();
				T0CashInfoNew cashInfo = new T0CashInfoNew();
				cashInfo.setBccon_ordernum(orderNo);
				cashInfo.setSaru_lruid(saruLruid);
				cashInfo.setCash_amount(AmountUtil.sub(
						p3.doubleValue(), handFee) + "");// 到账金额=交易金额-手续费
				cashInfo.setPan("");
				cashInfo.setBccon_linvnum(linvnum);
				cashInfo.setCash_status("无卡快捷交易成功");// 注意：次数如果是同名卡，状态置为状态M--无需代付
				cashInfo.setT0Setttype(Integer.valueOf(30));// 无卡代付类型-3-无需代付，所以改成30
				cashInfo.setSettlers("O110101");
				bankDao.updateNewT0ThingServlet(cashInfo);
				saruInfo.updateCreditCardTradeStatus(orderNo,status);
				
			}
		}else {
			logger.info("验签失败");
			resultMap.put("retCode", "9996");
			resultMap.put("retMsg", "验签失败");
		}
		Gson gson = new Gson();
		String json = gson.toJson(resultMap);
		response.getWriter().write(json.toString());
		response.getWriter().flush();
		response.getWriter().close();
	}
}
