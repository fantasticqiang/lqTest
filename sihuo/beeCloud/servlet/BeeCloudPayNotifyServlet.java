package com.beeCloud.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import com.beeCloud.constant.BeeCloudConstant;
import com.beeCloud.dao.BeeCloudPayDao;
import com.common.util.AmountUtil;
import com.common.util.StringUtil;

import cn.beecloud.BCCache;
import cn.beecloud.BeeCloud;
import cn.phoneposp.dao.RWBankCardDAO;
import cn.phoneposp.dao.SaruInfoDao;
import cn.phoneposp.entity.RPscalecommission;
import cn.phoneposp.entity.T0CashInfoNew;

public class BeeCloudPayNotifyServlet extends HttpServlet{

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
		String appID=BeeCloudConstant.app_id;
	    String testSecret=BeeCloudConstant.app_secret;
	    String appSecret=BeeCloudConstant.app_secret;
	    String masterSecret="39a7a518-9ac8-4a9e-87bc-7885f33cf18c";
	    BeeCloud.registerApp(appID,testSecret, appSecret, masterSecret);
	    StringBuffer json = new StringBuffer();
	    String line = null;


	    try {
	        request.setCharacterEncoding("utf-8");
	        BufferedReader reader = request.getReader();
	        while ((line = reader.readLine()) != null) {
	            json.append(line);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    JSONObject jsonObj = JSONObject.fromObject(json.toString());
	    System.out.println("接受到异步通知："+ json.toString());
	    logger.info("接受到异步通知："+ json.toString());

	    String signature = jsonObj.getString("signature");
	    String transactionId=jsonObj.getString("transaction_id");
	    String transactionType=jsonObj.getString("transaction_type");
	    String channelType=jsonObj.getString("channel_type");
	    String transactionFee=jsonObj.getString("transaction_fee");

	    StringBuffer toSign = new StringBuffer();
	    toSign.append(BCCache.getAppID()).append(transactionId)
	            .append(transactionType).append(channelType)
	            .append(transactionFee);
	   boolean status = verifySign(toSign.toString(),masterSecret,signature);
	   PrintWriter out = response.getWriter();
	    if (status) { //验证成功
	        out.println("success"); //请不要修改或删除

	        // 此处需要验证购买的产品与订单金额是否匹配:
	        // 验证购买的产品与订单金额是否匹配的目的在于防止黑客反编译了iOS或者Android app的代码，
	        // 将本来比如100元的订单金额改成了1分钱，开发者应该识别这种情况，避免误以为用户已经足额支付。
	        // Webhook传入的消息里面应该以某种形式包含此次购买的商品信息，比如title或者optional里面的某个参数说明此次购买的产品是一部iPhone手机，
	        // 开发者需要在客户服务端去查询自己内部的数据库看看iPhone的金额是否与该Webhook的订单金额一致，仅有一致的情况下，才继续走正常的业务逻辑。
	        // 如果发现不一致的情况，排除程序bug外，需要去查明原因，防止不法分子对你的app进行二次打包，对你的客户的利益构成潜在威胁。
	        // 如果发现这样的情况，请及时与我们联系，我们会与客户一起与这些不法分子做斗争。而且即使有这样极端的情况发生，
	        // 只要按照前述要求做了购买的产品与订单金额的匹配性验证，在你的后端服务器不被入侵的前提下，你就不会有任何经济损失。
	        logger.info("beeCloud通知成功");
	        String respString = "";
	        SaruInfoDao saruInfo = new SaruInfoDao();
	        String merOrderNo = transactionId;
	        String orderStatus = saruInfo
					.getTradeStatusByOrderNo(merOrderNo);
			if (orderStatus != null && "2".equals(orderStatus)) {
				logger.info("beeCloud异步通知处理重复通知，订单号："+merOrderNo);
				respString = "SUCCESS";
				response.getWriter().write(respString);
				response.getWriter().flush();
				response.getWriter().close();
				return;
			}
	        // 处理业务逻辑
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
			int status2 = 2;// 0:初始 1:已提交 2:成功 3:失败
			String linv = saruInfo.getLinvnum();
			if (linv == null)
				linv = "0";
			int linv2 = Integer.parseInt(linv);
			String linvnum = (linv2 + 1 + "").trim();
			linvnum = StringUtil.fillChar(linvnum, "0", "L", 6);
			BeeCloudPayDao merchantDao = new BeeCloudPayDao();
			String cardNo = merchantDao
					.selectCardNoByOrderId(merOrderNo);
			// 插入银行卡流水号
			logger.info("插入成功交易表");
			String payType = "freecardcredit";
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
					status2);
	    } else { //验证失败
	        out.println("fail");
	    }
	}
	
	boolean verifySign(String text,String masterKey,String signature) {
        boolean isVerified = verify(text, signature, masterKey, "UTF-8");
        if (!isVerified) {
            return false;
        }
        return true;
    }


     boolean verify(String text, String sign, String key, String inputCharset) {
        text = text + key;
        String mysign = DigestUtils.md5Hex(getContentBytes(text, inputCharset));
        return mysign.equals(sign);
    }

    byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }
}
