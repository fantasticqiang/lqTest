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

import cn.phoneposp.dao.WXTradeInfoDAO;
import cn.phoneposp.entity.Merchant;
import cn.phoneposp.entity.OneCodePayTradeInfo;

import com.channel.util.MD5;
import com.channel.util.SignUtils;
import com.channel.util.WXPNParamUtils;
import com.channel.util.XmlUtils;

import dao.DulDao;
import dao.MerchantDao;

public class WXPublicPayServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	Logger logger = Logger.getLogger(WXPublicPayServlet.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		logger.info("==============微信公众号开始=============");
		Map<String, String> reqmap = new HashMap<String, String>();
		MerchantDao merchantDao = new MerchantDao();
		List<Merchant> merchantList = merchantDao.getPNMerchant();
		boolean bl = true;
		if (merchantList.size() == 1) {
			merchantDao.updatePNMerchant();
		}
		for (int i = 0; i < merchantList.size(); i++) {
			Merchant merchant = merchantList.get(i);
			if (merchantDao.queryByMerchantNo(merchant.getMerchantNo())) {
				bl = false;
				// 获取openid请求的路径
				String requestUrl = "https://api.weixin.qq.com/sns/oauth2/access_token";
				String appId = "wx473f40f2bbfa73c7";// 公众号id
//				String appId = "wx03a7de97bd7184d1";// 公众号id
				String appSecret = "2d2428494d26e804d81048e67cfeaa1e";// 公众号密钥
//				String appSecret = "a4a54ec5b51f9dc8b9cd36e929286706";// 公众号密钥
				String grantType = "authorization_code";// 固定
				String code = request.getParameter("code").trim();
				logger.info("获取的code==="+code);
				String mch_id = merchant.getMerchantNo();
				String key = merchant.getMerchantSecretKey();

				Date date = new Date();
				String type = "weixin";
				// 获取中转参数 openId
				String state = request.getParameter("state").trim();
				String saruLruid = state.substring(0, 10);
				String orderNum = state.substring(10, 26);
				String totalFee = state.substring(26);
				if(DulDao.getInfo(orderNum)){
				      return;
			    }
				SortedMap<String, String> mapO = new TreeMap<String, String>();
				mapO.put("appid", appId);
				mapO.put("secret", appSecret);
				mapO.put("code", code);
				mapO.put("grant_type", grantType);
				// 获取openId
				String openId = new WXPNParamUtils()
						.getOpenId(requestUrl, mapO);
				logger.info("获取的openId==="+openId);
				System.out.println(openId);
				String url = "https://pay.swiftpass.cn/pay/gateway";

				SortedMap<String, String> map = new TreeMap<String, String>();
				String body = "一码付";// 商品描述
				map.put("service", "pay.weixin.jspay");
				map.put("version", "1.1");
				map.put("charset", "UTF-8");
				map.put("sign_type", "MD5");
				map.put("mch_id", mch_id);
				map.put("out_trade_no", orderNum);
				map.put("body", body);
				map.put("sub_openid", openId);
				map.put("total_fee", totalFee);
				map.put("notify_url",
						"http://shuaka.qingyutec.com/PhonePospInterface/WXJSCallBackServlet");
				map.put("nonce_str", String.valueOf(new Date().getTime()));
				map.put("mch_create_ip", getIp(request));
				map.put("is_raw", "1");// 原生js公众号必带

				Map<String, String> params = SignUtils.paraFilter(map);
				StringBuilder builder = new StringBuilder(
						(params.size() + 1) * 10);
				SignUtils.buildPayParams(builder, params, false);
				String preStr = builder.toString();
				// 进行md5加密 sign
				String sign = MD5.sign(preStr, "&key=" + key,	 "utf-8");
				map.put("sign", sign);
				System.out.println("reqUrl：" + url);
				System.out.println("reqParams:" + XmlUtils.parseXML(map));
				CloseableHttpResponse resp = null;
				CloseableHttpClient client = null;
				String res = null;
				try {
					WXTradeInfoDAO wxTradeInfo = new WXTradeInfoDAO();
					HttpPost httpPost = new HttpPost(url);
					StringEntity entityParams = new StringEntity(
							XmlUtils.parseXML(map), "utf-8");// 设定StringEntiey对象存放请求参数
					httpPost.setEntity(entityParams);// 设定http请求参数
					httpPost.setHeader("Content-Type",
							"text/xml;charset=ISO-8859-1");
					client = HttpClients.createDefault();// 创建client对象
					resp = client.execute(httpPost);// 发送请求，返回结果
					System.out.println("resp==============" + resp);
					if (resp != null && resp.getEntity() != null) { // 返回结果不为空或有值
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
						oneCode.setTimeStart(sdf2.format(date));
						oneCode.setSign("");
						if (type.equals("weixin")) {
							oneCode.setSettleType("3");
						} else {
							oneCode.setSettleType("4");
						}
//						if(DulDao.getInfo(orderNum)){
//							return;
//						}
						Integer in = wxTradeInfo
								.insertOneCodeTradeInfo(oneCode);
						System.out.println("是否保存成功：" + in);
						if (in == 1) {
							Map<String, String> resultMap = XmlUtils.toMap(
									EntityUtils.toByteArray(resp.getEntity()),
									"utf-8");
							res = XmlUtils.toXml(resultMap);
							System.out.println("返回结果"+res);

							if (resultMap.containsKey("sign")) {
								if ("0".equals(resultMap.get("status"))
										&& "0".equals(resultMap
												.get("result_code"))) {

									// 请求成功 后面将参数传过去跳到一个中转页面调用wx内部js发起支付
									String payInfo = resultMap.get("pay_info");
									JSONObject jsStr = JSONObject
											.fromObject(payInfo);
									String appIdStr = jsStr.getString("appId");
									String timeStamp = jsStr
											.getString("timeStamp");
									String nonStr = jsStr.getString("nonceStr");
									String package1 = jsStr
											.getString("package");
									String signTypeStr = jsStr
											.getString("signType");
									String paySign = jsStr.getString("paySign");
									// String tokenId =
									// resultMap.get("token_id");

									StringBuilder backUrl = new StringBuilder(
											"http://shuaka.qingyutec.com/PhonePospInterface/wftMK.jsp");
									backUrl.append("?").append(
											"showwxpaytitle=1");
									backUrl.append("&").append(
											"appId=" + appIdStr);
									backUrl.append("&").append(
											"timeStamp=" + timeStamp);
									backUrl.append("&").append(
											"nonceStr=" + nonStr);
									backUrl.append("&").append(
											"package1=" + package1);
									backUrl.append("&").append(
											"signType=" + signTypeStr);
									backUrl.append("&").append(
											"paySign=" + paySign);
									backUrl.append("&").append(
											"out_trade_no=" + orderNum);
									backUrl.append("&").append(
											"total_fee=" + totalFee);
									backUrl.append("&").append("body=" + body);
									response.sendRedirect(backUrl.toString());
								} else {
									reqmap.put("resultCode", "-99997");
									reqmap.put("errorMsg",
											resultMap.get("message"));
								}
							} else {
								reqmap.put("resultCode", "-99997");
								reqmap.put("errorMsg", resultMap.get("message"));
							}
						} else {
							// 数据保存失败
							reqmap.put("resultCode", "-99998");
							reqmap.put("errorMsg", "数据异常");
						}
					} else {
						reqmap.put("resultCode", "-99999");
						reqmap.put("errorMsg", "操作失败");
					}
				} catch (Exception e) {
					// 请求失败
					e.printStackTrace();
					reqmap.put("resultCode", "-99999");
					reqmap.put("errorMsg", "系统异常");
				} 
				break;
			}
		}
		System.out.println("========跳出循环==========");
		if (bl) {
			reqmap.put("resultCode", "-99997");
			reqmap.put("errorMsg", "当天交易额度已满");
		}
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
