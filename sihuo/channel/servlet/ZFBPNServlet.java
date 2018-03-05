package com.channel.servlet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import cn.phoneposp.dao.SaruInfoDao;
import cn.phoneposp.dao.WXTradeInfoDAO;
import cn.phoneposp.entity.Merchant;
import cn.phoneposp.entity.OneCodePayTradeInfo;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.channel.util.MD5;
import com.channel.util.SignUtils;
import com.channel.util.XmlUtils;
import com.phoneposp.util.PropertiesUtil;
import com.phoneposp.util.SequenceUtils;

import dao.ZFBPNDao;

/**
 * 
 *@author:侯磊
 *@date：2017-6-9下午1:06:55
 *@version 1.0
 *
 */
public class ZFBPNServlet extends HttpServlet{
	Logger logger = Logger.getLogger(ZFBPNServlet.class);
	public PropertiesUtil util = new PropertiesUtil(SaruInfoDao.class
			.getResource("/merchantInfo.properties").getFile());
	public String prefix_pn = util.getValue("prefix_pn");
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doPost(request, response);
	}
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		logger.info("==============支付宝一码付请求开始=============");
		ZFBPNDao zfbPNDao = new ZFBPNDao();//支付宝一码付的Dao类
		List<Merchant> merchantList = zfbPNDao.getZFBPNMerchant();//获取支付宝的商户号  商户号的支付类型为0
		Map<String, String> reqmap = new HashMap<String, String>();
		String reqUrl ="https://pay.swiftpass.cn/pay/gateway";//支付宝的请求路径
		// 我自己的参数
//		String totalFee = request.getParameter("transAmt");// 金额,单位分
//		logger.info("获取的金额======="+totalFee);
//		System.out.println("获取的金额======="+totalFee);
//		String saruLruid = request.getParameter("saruLruid");// 商户号
//		logger.info("获取的商户号======="+saruLruid);
//		System.out.println("获取的商户号======="+saruLruid);
		String privateKey="MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCy6sjUMHn5SeAGmSm05KJa/GQz0ttj17Y7LmE0CPdZMp3HYys4BS4fFGX2q++k7RmYgcmyime6D8F/tFxMikxevncomHCcn1WWWTLKiYsao3Fp2aOVdr1ngHgZkAZz152S3pJjhkBTqJ8EFScGIbRUUVOlMDMPLscjaDvn3UmvJrEagQ4Yz9Czw8UAcPfoW41p6XBfdege9YRr9wRSQucqVuTcaDeOAa2oOCVnV3hN36u7zuJ3ehGwEe+bu0yGDZP0ChsKgPO1snWGTzmfzHu26CQCPSfCiW7rMxKezuQ2B3Hs6fDUX8F+YQfVpCdyBoYS4QVB7C1tcVa+Jtqa0Vm7AgMBAAECggEAHtt9bZM6xjLmj1zFI6y3D6guA/4e1nZyuHxESXwuZNTk/2lsH8pM7RFQkJ2QxEocOwBPcBqMzzs+bS8HKDBGAaUp2feInIqicT7LEsFsG1kLC2182B9VhU9T51y+sCYEyDrY1o9l8sfHPSYEx/ryJgSsv3WzqK+xqrqkclXpCmsAs2Wq9bnFNSSdxKRhXb3L3zSJOYZuBTQKeVFmiRmg5pZ4AC5ORcgWYbizpkGnVlUin7ZD8BishIbQ4veOx3GlbQnTWqjb2XwFa4gqR49fSL0/JwsrbJ3xXzH92SKhL7fAAIcmaRRZJhTxaXPtHVsi26WdnKy5m9Vk0RH3g2myuQKBgQDtQjlaOmcMTzzz7Y7MuilUdNOaK8cmlTpF5dMQv5gTiUkWoZwRX1qwq68Mt49LmllH6bVV1rM6ZuqR5uow8MP0V4GocJ+9RJAP8JwDVWHuA8xQVFwoZiAEpyTqr8cxHoko6sTfGw/3wZPHQxHf/qnsp7ZxpPZgVupoBIZysNOtRwKBgQDBDMtf/b0c7dP18LP2/qcN1Xsc1590p0lrHIgbpmnTDtjicL12YT3fBr+O+4kViCRGrNKB/138Shjztu+NtfAHDlCtYvORwRxC2f7oqBmKxoG5/SSAzdQ0Z1tMCHoARW023D/0bwdlqkXebRRzrW19kMCv9oGPj6Fk8Q2gzKIZ7QKBgQCRCmsFPa7BV69rb05c2XFqsACrvxd//rohAN6G3pmZV9PWgfVmqWRHnKzmmVCAZ1stGzI/x4DJsyCzEPcZ9BXWyaf50N/WVJO9Kto1L30uhCPi969fAw+PyJ5I2ixrWr9+xAaWXXTlgPFWXyoORSCcC+r8jlwPiRLDhACtHGxLAQKBgBq1VXHLXh2hfcC583wJkT0luumrn0D5X/v0r5P7uwpbNIBnjp7RJ0ky2s/CVx3mfvZUNK+NkAg2jCsztQLLGBEx1tGWLhKNQAazuEOh9h6wBgSgVCuJzoENh6EGTzK4aDq90A9WSRS0sQPr3Xd/l/VWUIJHvXcKLQ8VFoiXpxmlAoGAcnUKIcPmAf3NKTGPTtoRi/++80qA/oU9u1OojCWgaaDavIKFmddu0lYxSPcKfSv6y+7uS7pJtkztrE+t5eBeeRi/zCpeNjPj68Yd8YjY418jF4LapywYAjmsl3kBmdoXHxowRR123tqKzk5RG+BSFsB++ijqzkLD1TQdTJ9iq3I=";
//		                   MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCy6sjUMHn5SeAGmSm05KJa/GQz0ttj17Y7LmE0CPdZMp3HYys4BS4fFGX2q++k7RmYgcmyime6D8F/tFxMikxevncomHCcn1WWWTLKiYsao3Fp2aOVdr1ngHgZkAZz152S3pJjhkBTqJ8EFScGIbRUUVOlMDMPLscjaDvn3UmvJrEagQ4Yz9Czw8UAcPfoW41p6XBfdege9YRr9wRSQucqVuTcaDeOAa2oOCVnV3hN36u7zuJ3ehGwEe+bu0yGDZP0ChsKgPO1snWGTzmfzHu26CQCPSfCiW7rMxKezuQ2B3Hs6fDUX8F+YQfVpCdyBoYS4QVB7C1tcVa+Jtqa0Vm7AgMBAAECggEAHtt9bZM6xjLmj1zFI6y3D6guA/4e1nZyuHxESXwuZNTk/2lsH8pM7RFQkJ2QxEocOwBPcBqMzzs+bS8HKDBGAaUp2feInIqicT7LEsFsG1kLC2182B9VhU9T51y+sCYEyDrY1o9l8sfHPSYEx/ryJgSsv3WzqK+xqrqkclXpCmsAs2Wq9bnFNSSdxKRhXb3L3zSJOYZuBTQKeVFmiRmg5pZ4AC5ORcgWYbizpkGnVlUin7ZD8BishIbQ4veOx3GlbQnTWqjb2XwFa4gqR49fSL0/JwsrbJ3xXzH92SKhL7fAAIcmaRRZJhTxaXPtHVsi26WdnKy5m9Vk0RH3g2myuQKBgQDtQjlaOmcMTzzz7Y7MuilUdNOaK8cmlTpF5dMQv5gTiUkWoZwRX1qwq68Mt49LmllH6bVV1rM6ZuqR5uow8MP0V4GocJ+9RJAP8JwDVWHuA8xQVFwoZiAEpyTqr8cxHoko6sTfGw/3wZPHQxHf/qnsp7ZxpPZgVupoBIZysNOtRwKBgQDBDMtf/b0c7dP18LP2/qcN1Xsc1590p0lrHIgbpmnTDtjicL12YT3fBr+O+4kViCRGrNKB/138Shjztu+NtfAHDlCtYvORwRxC2f7oqBmKxoG5/SSAzdQ0Z1tMCHoARW023D/0bwdlqkXebRRzrW19kMCv9oGPj6Fk8Q2gzKIZ7QKBgQCRCmsFPa7BV69rb05c2XFqsACrvxd//rohAN6G3pmZV9PWgfVmqWRHnKzmmVCAZ1stGzI/x4DJsyCzEPcZ9BXWyaf50N/WVJO9Kto1L30uhCPi969fAw+PyJ5I2ixrWr9+xAaWXXTlgPFWXyoORSCcC+r8jlwPiRLDhACtHGxLAQKBgBq1VXHLXh2hfcC583wJkT0luumrn0D5X/v0r5P7uwpbNIBnjp7RJ0ky2s/CVx3mfvZUNK+NkAg2jCsztQLLGBEx1tGWLhKNQAazuEOh9h6wBgSgVCuJzoENh6EGTzK4aDq90A9WSRS0sQPr3Xd/l/VWUIJHvXcKLQ8VFoiXpxmlAoGAcnUKIcPmAf3NKTGPTtoRi/++80qA/oU9u1OojCWgaaDavIKFmddu0lYxSPcKfSv6y+7uS7pJtkztrE+t5eBeeRi/zCpeNjPj68Yd8YjY418jF4LapywYAjmsl3kBmdoXHxowRR123tqKzk5RG+BSFsB++ijqzkLD1TQdTJ9iq3I=
		String publicKey="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzh6QNwZcPM1h9bFDorjDPpfoxNQTlH1J77H7A5TbCmsET+wRKaN9100ohVcMDsHCvjzqf4MZiRbvtgCBKPpS/83S5ZmT9Yzyg7WbY1kkjs30tyqeYmJHIbGK7iLNc1c/BvOaMb3swhWyQhsgvDpRI0DpT8oOGnKWlM6f9/EGXZF0Ff62gYjmJXpiy2/cSfCZVFl9bm09AWbBo+QgFBmGSbAZSZBdx4d9/nsxqq3DpvPXlnt0hOKNm323tqflSTn416NDgYN1g7eNPm9921gfPmYjEs4nqPuM/FQF6n1rjsht9UtmxZXNDXecR5NlGcKOHGy3gglopEB5J5YbYQZOnwIDAQAB";
		
		String code = request.getParameter("code").trim();
		logger.info("支付宝公众号获取的code "+code);
		String state = request.getParameter("state").trim();
		logger.info("支付宝公众号获取的state "+state);
		String saruLruid = state.substring(0, 10);
		logger.info("获取的商户号==="+saruLruid);
		String orderNum = state.substring(10, 26);
		logger.info("获取的订单号"+orderNum);
		String totalFee = state.substring(26);
		logger.info("获取的金额 "+totalFee);
		// 获取userid请求的路径
		String userId="";
//		Map<String,String> reqMap = new HashMap<String,String>();
//		SortedMap<String, String> reqMap = new TreeMap<String, String>();
		String requestUrl = "https://openapi.alipay.com/gateway.do";
		String appId="2017030105985046";//开发者账号的appID
//		String timeStr=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		
//		reqMap.put("app_id", appId);//开发者账号的appId
//		reqMap.put("charset", "utf-8");//字符集
//		reqMap.put("code", code);//跳转页面获取的code
//		reqMap.put("grant_type", "authorization_code");//值为authorization_code时，代表用code换取
//		reqMap.put("method", "alipay.system.oauth.token");//auth_code换取access_token与user_id  获取授权访问令牌的接口
//		reqMap.put("sign_type", "RSA2");//RSA2  商户生成签名字符串所使用的签名算法类型
//		reqMap.put("timestamp", timeStr);//发送请求的时间
//		reqMap.put("version", "1.0");//版本号
//		String decode = AlipaySignature.rsaSign(reqMap, privateKey, "utf-8");
		try {
			AlipayClient alipayClient = new DefaultAlipayClient(requestUrl, appId,privateKey,"json","utf-8",publicKey,"RSA2"); 
			AlipaySystemOauthTokenRequest userIdRequest = new AlipaySystemOauthTokenRequest();
			userIdRequest.setCode(code);
			userIdRequest.setGrantType("authorization_code");
			try {
			    AlipaySystemOauthTokenResponse oauthTokenResponse = alipayClient.execute(userIdRequest);
			    userId=oauthTokenResponse.getUserId();
			} catch (AlipayApiException e) {
			    //处理异常
			    e.printStackTrace();
			}
//			logger.info("请求参数加密后的字符串 "+decode);
//			reqMap.put("sign", decode);//用RSA2加密的所有的请求参数
//			 userId=getUserId(requestUrl,reqMap);//获取用户的userId
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		logger.info("获取用户的userId"+userId);
		if(merchantList.size()==1){
			zfbPNDao.updateZFBPNMerchant();//更改支付的类型  公众号的类型为0
		}
		for (int i = 0; i < merchantList.size(); i++) {
			Integer id = SaruInfoDao.getSequence("SEQ_ORDER_NO");
			String mchCreateIp=getIp(request);//获取商户的终端IP
		
			Merchant merchant = merchantList.get(i);
			String mchId=merchant.getMerchantNo();
			String key = merchant.getMerchantSecretKey();
			SortedMap<String, String> map = new TreeMap<String, String>();
			map.put("service", "pay.alipay.jspay");//支付类型
			map.put("mch_id", mchId);//商户号
			map.put("out_trade_no", orderNum);//订单号
			map.put("body", "一码付");//商品描述
			map.put("total_fee", totalFee);//金额 单位为分
			map.put("mch_create_ip", mchCreateIp);//客户的终端IP
			map.put("notify_url", "http://shuaka.qingyutec.com/PhonePospInterface/ZFBPNCallBackServlet");//支付宝公众号的回调地址
			map.put("nonce_str", String.valueOf(new Date().getTime()));//随机字符串
			map.put("buyer_id", userId);//支付宝用户的ID
			// 处理字符串
			Map<String, String> params = SignUtils.paraFilter(map);
			StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
			// 将字符串以&分隔
			SignUtils.buildPayParams(buf, params, false);
			String preStr = buf.toString();
			// 将字符串转换成md5
			String sign = MD5.sign(preStr, "&key=" + key, "utf-8");//用商户号秘钥进行MD5加密
			map.put("sign", sign);
			// 生成xml
			logger.info("支付宝一码付请求的参数:" + XmlUtils.parseXML(map));//查看请求的所有参数
			System.out.println("支付宝一码付请求的参数:" + XmlUtils.parseXML(map));//查看请求的所有参数
			CloseableHttpResponse resp = null;
			CloseableHttpClient client = null;
			String res = null;
			try {
				HttpPost httpPost = new HttpPost(reqUrl);
				StringEntity entityParams = new StringEntity(
						XmlUtils.parseXML(map), "utf-8");
				httpPost.setEntity(entityParams);
				httpPost.setHeader("Content-Type", "text/xml;charset=ISO-8859-1");
				client = HttpClients.createDefault();
				resp = client.execute(httpPost);
				if (resp != null && resp.getEntity() != null) {
				Map<String, String> resultMap = XmlUtils.toMap(EntityUtils.toByteArray(resp.getEntity()), "utf-8");
				res = XmlUtils.toXml(resultMap);
				System.out.println("支付宝一码付的同步请求的响应" + res);
				logger.info("支付宝一码付的同步请求的响应" + res);
					SimpleDateFormat sdf2 = new SimpleDateFormat(
							"yyyyMMddHHmmss");
					OneCodePayTradeInfo oneCode = new OneCodePayTradeInfo();
					oneCode.setOutTradeNo(orderNum);
					oneCode.setBody("");
					oneCode.setMchCreateIp(getIp(request));
					oneCode.setMchId("100530013937");
					oneCode.setNonceStr("");
					oneCode.setNotifyUrl("http://shuaka.qingyutec.com/PhonePospInterface/WXJSCallBackServlet");
					oneCode.setServiceType("WEBPAY");
					oneCode.setTotalFee(Double.parseDouble(totalFee) / 100);
					oneCode.setSaruLruid(saruLruid);
					oneCode.setStatus("WAIT_FOR_PAY");
					oneCode.setKey("");
					oneCode.setTimeStart(sdf2.format(new Date()));
					oneCode.setSign("");
					oneCode.setSettleType("4");
					WXTradeInfoDAO wxTradeInfo = new WXTradeInfoDAO();
					Integer in = wxTradeInfo
							.insertOneCodeTradeInfo(oneCode);
					logger.info("是否保存成功：" + in);

				String payInfo = resultMap.get("pay_info");
				String payUrl = resultMap.get("pay_url");
				logger.info("支付宝公众号自发调用的url"+payUrl);
				JSONObject jsonStr= JSONObject.fromObject(payInfo);
				String trade_NO = jsonStr.optString("tradeNO");
				logger.info("支付宝公众号获取的tradeNO"+trade_NO);
				if("0".equals(resultMap.get("status")) && "0".equals(resultMap.get("result_code"))){
					StringBuilder backUrl = new StringBuilder("http://shuaka.qingyutec.com/PhonePospInterface/wftAL.jsp");
					backUrl.append("?").append("tradeNO=" + trade_NO);
					backUrl.append("&").append(
							"out_trade_no=" + orderNum);
					backUrl.append("&").append(
							"total_fee=" + totalFee);
					backUrl.append("&").append("body=一码付");
					logger.info("支付宝重定向的url"+backUrl.toString());
					response.sendRedirect(backUrl.toString());//重定向到确认支付的页面
				}else{
					reqmap.put("resultCode", "-99996");
					reqmap.put("errorMsg", "获取二维码失败," + resultMap.get("message"));
				}
				
				}else{
					reqmap.put("resultCode", "-99997");
					reqmap.put("errorMsg","获取支付宝公众号的二维码失败");
				}
			} catch (Exception e) {
				reqmap.put("resultCode", "-99999");
				reqmap.put("errorMsg", "获取二维码失败");
			}
			
			break;
		}
		
	}
	
	public static void main(String[] args) {
//		StringBuilder backUrl = new StringBuilder("http://shuaka.qingyutec.com/PhonePospInterface/wftAL.jsp");
//		backUrl.append("?").append("tradeNO =" + "123");
//		backUrl.append("&").append(
//				"out_trade_no=" + "11111111111");
//		backUrl.append("&").append(
//				"total_fee=" + "300");
//		backUrl.append("&").append("body=一码付");
//      System.out.println(backUrl.toString());
		String requestUrl = "https://openapi.alipay.com/gateway.do";
		
		String privateKey="MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCy6sjUMHn5SeAGmSm05KJa/GQz0ttj17Y7LmE0CPdZMp3HYys4BS4fFGX2q++k7RmYgcmyime6D8F/tFxMikxevncomHCcn1WWWTLKiYsao3Fp2aOVdr1ngHgZkAZz152S3pJjhkBTqJ8EFScGIbRUUVOlMDMPLscjaDvn3UmvJrEagQ4Yz9Czw8UAcPfoW41p6XBfdege9YRr9wRSQucqVuTcaDeOAa2oOCVnV3hN36u7zuJ3ehGwEe+bu0yGDZP0ChsKgPO1snWGTzmfzHu26CQCPSfCiW7rMxKezuQ2B3Hs6fDUX8F+YQfVpCdyBoYS4QVB7C1tcVa+Jtqa0Vm7AgMBAAECggEAHtt9bZM6xjLmj1zFI6y3D6guA/4e1nZyuHxESXwuZNTk/2lsH8pM7RFQkJ2QxEocOwBPcBqMzzs+bS8HKDBGAaUp2feInIqicT7LEsFsG1kLC2182B9VhU9T51y+sCYEyDrY1o9l8sfHPSYEx/ryJgSsv3WzqK+xqrqkclXpCmsAs2Wq9bnFNSSdxKRhXb3L3zSJOYZuBTQKeVFmiRmg5pZ4AC5ORcgWYbizpkGnVlUin7ZD8BishIbQ4veOx3GlbQnTWqjb2XwFa4gqR49fSL0/JwsrbJ3xXzH92SKhL7fAAIcmaRRZJhTxaXPtHVsi26WdnKy5m9Vk0RH3g2myuQKBgQDtQjlaOmcMTzzz7Y7MuilUdNOaK8cmlTpF5dMQv5gTiUkWoZwRX1qwq68Mt49LmllH6bVV1rM6ZuqR5uow8MP0V4GocJ+9RJAP8JwDVWHuA8xQVFwoZiAEpyTqr8cxHoko6sTfGw/3wZPHQxHf/qnsp7ZxpPZgVupoBIZysNOtRwKBgQDBDMtf/b0c7dP18LP2/qcN1Xsc1590p0lrHIgbpmnTDtjicL12YT3fBr+O+4kViCRGrNKB/138Shjztu+NtfAHDlCtYvORwRxC2f7oqBmKxoG5/SSAzdQ0Z1tMCHoARW023D/0bwdlqkXebRRzrW19kMCv9oGPj6Fk8Q2gzKIZ7QKBgQCRCmsFPa7BV69rb05c2XFqsACrvxd//rohAN6G3pmZV9PWgfVmqWRHnKzmmVCAZ1stGzI/x4DJsyCzEPcZ9BXWyaf50N/WVJO9Kto1L30uhCPi969fAw+PyJ5I2ixrWr9+xAaWXXTlgPFWXyoORSCcC+r8jlwPiRLDhACtHGxLAQKBgBq1VXHLXh2hfcC583wJkT0luumrn0D5X/v0r5P7uwpbNIBnjp7RJ0ky2s/CVx3mfvZUNK+NkAg2jCsztQLLGBEx1tGWLhKNQAazuEOh9h6wBgSgVCuJzoENh6EGTzK4aDq90A9WSRS0sQPr3Xd/l/VWUIJHvXcKLQ8VFoiXpxmlAoGAcnUKIcPmAf3NKTGPTtoRi/++80qA/oU9u1OojCWgaaDavIKFmddu0lYxSPcKfSv6y+7uS7pJtkztrE+t5eBeeRi/zCpeNjPj68Yd8YjY418jF4LapywYAjmsl3kBmdoXHxowRR123tqKzk5RG+BSFsB++ijqzkLD1TQdTJ9iq3I=";
//        MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCy6sjUMHn5SeAGmSm05KJa/GQz0ttj17Y7LmE0CPdZMp3HYys4BS4fFGX2q++k7RmYgcmyime6D8F/tFxMikxevncomHCcn1WWWTLKiYsao3Fp2aOVdr1ngHgZkAZz152S3pJjhkBTqJ8EFScGIbRUUVOlMDMPLscjaDvn3UmvJrEagQ4Yz9Czw8UAcPfoW41p6XBfdege9YRr9wRSQucqVuTcaDeOAa2oOCVnV3hN36u7zuJ3ehGwEe+bu0yGDZP0ChsKgPO1snWGTzmfzHu26CQCPSfCiW7rMxKezuQ2B3Hs6fDUX8F+YQfVpCdyBoYS4QVB7C1tcVa+Jtqa0Vm7AgMBAAECggEAHtt9bZM6xjLmj1zFI6y3D6guA/4e1nZyuHxESXwuZNTk/2lsH8pM7RFQkJ2QxEocOwBPcBqMzzs+bS8HKDBGAaUp2feInIqicT7LEsFsG1kLC2182B9VhU9T51y+sCYEyDrY1o9l8sfHPSYEx/ryJgSsv3WzqK+xqrqkclXpCmsAs2Wq9bnFNSSdxKRhXb3L3zSJOYZuBTQKeVFmiRmg5pZ4AC5ORcgWYbizpkGnVlUin7ZD8BishIbQ4veOx3GlbQnTWqjb2XwFa4gqR49fSL0/JwsrbJ3xXzH92SKhL7fAAIcmaRRZJhTxaXPtHVsi26WdnKy5m9Vk0RH3g2myuQKBgQDtQjlaOmcMTzzz7Y7MuilUdNOaK8cmlTpF5dMQv5gTiUkWoZwRX1qwq68Mt49LmllH6bVV1rM6ZuqR5uow8MP0V4GocJ+9RJAP8JwDVWHuA8xQVFwoZiAEpyTqr8cxHoko6sTfGw/3wZPHQxHf/qnsp7ZxpPZgVupoBIZysNOtRwKBgQDBDMtf/b0c7dP18LP2/qcN1Xsc1590p0lrHIgbpmnTDtjicL12YT3fBr+O+4kViCRGrNKB/138Shjztu+NtfAHDlCtYvORwRxC2f7oqBmKxoG5/SSAzdQ0Z1tMCHoARW023D/0bwdlqkXebRRzrW19kMCv9oGPj6Fk8Q2gzKIZ7QKBgQCRCmsFPa7BV69rb05c2XFqsACrvxd//rohAN6G3pmZV9PWgfVmqWRHnKzmmVCAZ1stGzI/x4DJsyCzEPcZ9BXWyaf50N/WVJO9Kto1L30uhCPi969fAw+PyJ5I2ixrWr9+xAaWXXTlgPFWXyoORSCcC+r8jlwPiRLDhACtHGxLAQKBgBq1VXHLXh2hfcC583wJkT0luumrn0D5X/v0r5P7uwpbNIBnjp7RJ0ky2s/CVx3mfvZUNK+NkAg2jCsztQLLGBEx1tGWLhKNQAazuEOh9h6wBgSgVCuJzoENh6EGTzK4aDq90A9WSRS0sQPr3Xd/l/VWUIJHvXcKLQ8VFoiXpxmlAoGAcnUKIcPmAf3NKTGPTtoRi/++80qA/oU9u1OojCWgaaDavIKFmddu0lYxSPcKfSv6y+7uS7pJtkztrE+t5eBeeRi/zCpeNjPj68Yd8YjY418jF4LapywYAjmsl3kBmdoXHxowRR123tqKzk5RG+BSFsB++ijqzkLD1TQdTJ9iq3I=
        String publicKey="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsurI1DB5+UngBpkptOSiWvxkM9LbY9e2Oy5hNAj3WTKdx2MrOAUuHxRl9qvvpO0ZmIHJsopnug/Bf7RcTIpMXr53KJhwnJ9VllkyyomLGqNxadmjlXa9Z4B4GZAGc9edkt6SY4ZAU6ifBBUnBiG0VFFTpTAzDy7HI2g7591JryaxGoEOGM/Qs8PFAHD36FuNaelwX3XoHvWEa/cEUkLnKlbk3Gg3jgGtqDglZ1d4Td+ru87id3oRsBHvm7tMhg2T9AobCoDztbJ1hk85n8x7tugkAj0nwolu6zMSns7kNgdx7Onw1F/BfmEH1aQncgaGEuEFQewtbXFWvibamtFZuwIDAQAB";
		
		String appId="2017030105985046";//开发者账号的appID

		
		AlipayClient alipayClient = new DefaultAlipayClient(requestUrl, appId,privateKey,"json","utf-8",publicKey,"RSA2"); 
		AlipaySystemOauthTokenRequest userIdRequest = new AlipaySystemOauthTokenRequest();
		userIdRequest.setCode("4a44ca60b85c4cb6956cdc5d933bXX37");
		userIdRequest.setGrantType("authorization_code");
		try {
		    AlipaySystemOauthTokenResponse oauthTokenResponse = alipayClient.execute(userIdRequest);
		    String userId=oauthTokenResponse.getUserId();
		    System.out.println("用户的ID==="+userId);
		} catch (AlipayApiException e) {
		    //处理异常
		    e.printStackTrace();
		    
		}
		
		
		
	}
	
	public String getUserId(String requestUrl, Map map) {
		String userId = "";
		String oppid = "";
		try {
			oppid = new HttpRequestor().doPost(requestUrl, map);
			logger.info("获取支付宝一码付同步的响应 "+oppid);
			JSONObject oppidObj =JSONObject.fromObject(oppid);
			userId = (String) oppidObj.get("user_id");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return userId;
	}
	
	// 获取终端Ip
		private String getIp(HttpServletRequest request) {
			String ipAddress = null;
			ipAddress = request.getHeader("x-forwarded-for");
			if (ipAddress == null || ipAddress.length() == 0
					|| "unknown".equalsIgnoreCase(ipAddress)) {
				ipAddress = request.getHeader("Proxy-Client-IP");
			}
			if (ipAddress == null || ipAddress.length() == 0
					|| "unknown".equalsIgnoreCase(ipAddress)) {
				ipAddress = request.getHeader("WL-Proxy-Client-IP");
			}
			if (ipAddress == null || ipAddress.length() == 0
					|| "unknown".equalsIgnoreCase(ipAddress)) {
				ipAddress = request.getRemoteAddr();
				if (ipAddress.equals("127.0.0.1")) {
					// 根据网卡取本机配置的IP
					InetAddress inet = null;
					try {
						inet = InetAddress.getLocalHost();
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
					ipAddress = inet.getHostAddress();
				}
			}
			// 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
			if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
																// = 15
				if (ipAddress.indexOf(",") > 0) {
					ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
				}
			}
			return ipAddress;
		}

}
