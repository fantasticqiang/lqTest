package com.syf.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.common.util.AmountUtil;
import com.common.util.StringUtil;
import com.syf.constant.SyfConstant;
import com.syf.dao.SyfQuickPayDao;
import com.syf.model.MerReportModel;
import com.syf.util.AesUtil;
import com.syf.util.Md5Util;
import com.yufutong.dao.MerchantEnterDao;
import com.yufutong.t0wuka.WKUnipayServletT0Notify;

import cn.phoneposp.dao.RWBankCardDAO;
import cn.phoneposp.dao.SaruInfoDao;
import cn.phoneposp.entity.RPscalecommission;
import cn.phoneposp.entity.T0CashInfoNew;

/**
 * 
 *作用：闪云付接收异步通知servlet
 */
public class SyfQuickPayNotifyServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;
	Logger logger = Logger.getLogger(SyfQuickPayNotifyServlet.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		logger.info("(syf) notify kai shi ");
		String payType = "freecardcredit";
		String respString = "ERROR";
		try {
			JSONObject resJson = getJsonString(request);
			/*
			 * {"signType":"MD5","partnerId":"V1","ciphertext":"1nPn6r6YYuuuYTjR1Jl5hmFzJuWnbHiFPy0P0qyzceBIm3lOXjvVXziIl7HEU4NlDu5fNmT7xVKEaEaCeXFHjvbtlMMljdYegaCCkfkjjBr4oJSaqO7GEsA0tBeojhv/u14VGClAnlnzWIZBtyynTv/BrGqolqKiWqfx7AMPPRjua8nnYpuK4OXzKeqxt133u3OnusIoZNg/wCBh8UtwLnPOxCE5JngLkloiIZUhl0diFuGLQcx3F14MlmKYKGKVCd0G49O7xNXDloURMfqiIota4+t7mdDMECBpD0I2NQnUmoELL30hwZFB5IvyIs15nPKYBUwd/4grcfA8f++3TQ==","exTxnId":"10002001200287","resMsg":"支付成功","platOrderNo":"de180227095348yJEGs6t","version":"V1","payOrderNo":"IG18W1L005EJNT00001","amount":"107.00","cipherType":"AES","signInfo":"ccafcae76095ee149a68428990799047","source":"SM","resCode":"0000","receiveTime":"20180227095349","serviceCode":"10003","merOrderNo":"SKWK153641309317","completeTime":"20180227095605","merchId":"mi118021410542700009"}
			 */
			//获取验签的key
			SyfQuickPayDao merchantDao = new SyfQuickPayDao();
			String merchId = resJson.getString("merchId"); //上游下发的商户号
			if(null == merchId || "".equals(merchId)){
				logger.info("syf's baowen is null");
				return ;
			}
			MerReportModel keyBySyfMerchId = merchantDao.getKeyBySyfMerchId(merchId);
			String md5Key = keyBySyfMerchId.getKey();
			//验签
			String ciphertext;
			String signData;
			String sign;
			ciphertext = resJson.getString("ciphertext");
			signData = "ciphertext=" + ciphertext + "&key=" + md5Key;
			sign = Md5Util.string2MD5(signData);
			if(!sign.equals(resJson.getString("signInfo"))){  //验签失败
				System.out.println("闪云付异步通知验签失败");
				logger.info("syf notify failed");
				return;
			}
			String merOrderNo = "";
			merOrderNo = resJson.getString("merOrderNo");
			if("".equals(merOrderNo)||merOrderNo == null){
				return;
			}

			SaruInfoDao saruInfo = new SaruInfoDao();
			
			if(resJson.containsKey("resCode")&&"0000".equals(resJson.getString("resCode"))){ //标志成功
				// 处理重复回调
				String orderStatus = saruInfo
						.getTradeStatusByOrderNo(merOrderNo);
				if (orderStatus != null && "2".equals(orderStatus)) {
					respString = "SUCCESS";
					response.getWriter().write(respString);
					response.getWriter().flush();
					response.getWriter().close();
					return;
				}
				// 处理业务逻辑，更新成功交易表，插入T0表,(由于无需代付，状态置为M--无卡快捷)
				String saruLruid = "";
				saruLruid = saruInfo.getSarulruidByOrderNo(merOrderNo);
				String orderAmount = saruInfo
						.getTradeAmountByOrderNo(merOrderNo);
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
						.selectCardNoByOrderId(merOrderNo);
				// 插入银行卡流水号
				logger.info("插入成功交易表");
				saruInfo.insertBankCardConsumer(payType,
						scalecommission, saruLruid, cardNo, p3,
						handFee, merOrderNo, linvnum);
				
				// 以下逻辑是处理成功交易表和T0出款表中的内容
				RWBankCardDAO bankDao = new RWBankCardDAO();
				T0CashInfoNew cashInfo = new T0CashInfoNew();
				cashInfo.setBccon_ordernum(merOrderNo);
				cashInfo.setSaru_lruid(saruLruid);
				cashInfo.setCash_amount(AmountUtil.sub(
						p3.doubleValue(), handFee) + "");// 到账金额=交易金额-手续费
				cashInfo.setPan("");
				cashInfo.setBccon_linvnum(linvnum);
				cashInfo.setCash_status("无卡快捷交易成功");// 注意：次数如果是同名卡，状态置为状态M--无需代付
				cashInfo.setT0Setttype(Integer.valueOf(30));// 无卡代付类型-3-无需代付，所以改成30
				cashInfo.setSettlers("O110101");
				bankDao.updateNewT0ThingServlet(cashInfo);
				saruInfo.updateCreditCardTradeStatus(merOrderNo,
						status);
				respString = "SUCCESS";
			}else{
				response.setContentType("text/html");
				PrintWriter out = response.getWriter();
				out.println("验签失败");
				out.flush();
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.getWriter().write(respString);
		response.getWriter().flush();
		response.getWriter().close();
		logger.info("(闪云付)无卡---T0------消费交易回调结束");
	}
	
/*	public static void main(String[] args) {
		String key = "df9e9c0e38b74d2a9c447693091dd04b";
		String response = "";
		response ="{\"signType\":\"MD5\",\"partnerId\":\"V1\",\"ciphertext\":\"/trgfjMUpWqOdX9k0GgnWnXbt0gHx68xoqwfALdIdV0owYA8omXc+FH6ntADpJB6jTlqbZmKRTAgEOjKrpN1HJNs84NRzPE5ValtUMiLv6se6E3yl3QaAH8BvEY+gdD1QOcUILxG7zS/QlBYg4kcjxN9i+aKQ9ceuOX/WyWYlz/Q9T/yqsQX/11L6b4aFVJmGn1dJLgvdSyT/5Rb/Kvi5KUB/mgcYIuZbIlwG70h5J4MJfHZMEpaWeC091ojZUJ0lboqUBDwdVcpq8yFCHlDrknCoUXkMQ2/dyEQR7co/oFgOqs6HwvoFmFnI2KTbB4x\",\"exTxnId\":null,\"resMsg\":\"支付成功\",\"platOrderNo\":\"de180117112430bgZvNoQ\",\"version\":\"V1\",\"payOrderNo\":\"IG18CCA7IICJ9V00004\",\"amount\":\"100.00\",\"cipherType\":\"AES\",\"signInfo\":\"2cab2e5529e9fb5eb8923eb4eb9c2f82\",\"source\":\"S\",\"resCode\":\"0000\",\"receiveTime\":\"20180117112430\",\"serviceCode\":\"10003\",\"merOrderNo\":\"S118011711242600000100000\",\"completeTime\":\"20180117112644\",\"merchId\":\"m1801140001\"}";
		JSONObject resJson = JSONObject.parseObject(response);
		String ciphertext = resJson.getString("ciphertext");
		try {
			ciphertext = AesUtil.decode(key, ciphertext);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(ciphertext);
	}*/
	
	/**
	 * 获取回调结果
	 * @return
	 * @throws Exception 
	 */
	public JSONObject getJsonString(HttpServletRequest request) throws Exception{
/*		String source = request.getParameter("source");
		String version = request.getParameter("version");
		String cipherType = request.getParameter("cipherType");
		String signType = request.getParameter("signType");
		String transCode = request.getParameter("transCode");
		String partnerId = request.getParameter("partnerId");
		String merchId = request.getParameter("merchId");
*/		
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		String response = "";
		request.setCharacterEncoding("utf-8");
		ServletInputStream inputStream = request.getInputStream();
		inputStreamReader = new InputStreamReader(inputStream);
		bufferedReader = new BufferedReader(inputStreamReader);
		String lines;
		while ((lines = bufferedReader.readLine()) != null) {
			lines = new String(lines.getBytes(), "utf-8");
			response += lines;
		}
		JSONObject resJson = JSONObject.parseObject(response);
/*		String ciphertext = resJson.getString("ciphertext");
		try {
			ciphertext = AesUtil.decode(SyfConstant.md5Key, ciphertext);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		return resJson;
	}
}
