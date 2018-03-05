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
import cn.phoneposp.entity.WXtradeInfo;
import cn.phoneposp.entity.WXtradeResSusInfo;
import cn.phoneposp.entity.WXtradeRetSusInfo;

import com.channel.util.MD5;
import com.channel.util.SignUtils;
import com.channel.util.XmlUtils;
import com.phoneposp.util.PropertiesUtil;
import com.phoneposp.util.SequenceUtils;

import dao.MerchantDao;


public class WXCodePyServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	Logger logger = Logger.getLogger(WXCodePyServlet.class);
	public PropertiesUtil util = new PropertiesUtil(SaruInfoDao.class
			.getResource("/merchantInfo.properties").getFile());
	public String prefix_wx = util.getValue("prefix_wx");
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		this.doPost(request, response);
	}
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		logger.info("==============生成二维码开始=============");
		Map<String, String> reqmap = new HashMap<String, String>();
		SortedMap<String, String> map = new TreeMap<String, String>();
		MerchantDao merchantDao = new MerchantDao();
		List<Merchant> merchantList = merchantDao.getWXMerchant();
		if (merchantList.size() == 1) {
			merchantDao.updateWXMerchant();
		}
		boolean bl = true;
		for (int i = 0; i < merchantList.size(); i++) {
			Merchant merchant = merchantList.get(i);
			if (merchantDao.queryByMerchantNo(merchant.getMerchantNo())) {
				bl = false;
				String mch_id = merchant.getMerchantNo();
                String notify_url = "http://shuaka.qingyutec.com/PhonePospInterface/WXCodeCallBackServlet";
				String key = merchant.getMerchantSecretKey();
				String saruLruid = request.getParameter("saruLruid"); // 数据库商户号
				String type = request.getParameter("type"); // 判断是正常交易还是升级订单 w upOrder
				Integer id = new SaruInfoDao().getSequence("SEQ_ORDER_NO");
                String total_fee = request.getParameter("transAmt");// 金额
				String mchIp = getIp(request);
				// 订单号
				String orderNum = prefix_wx+ SequenceUtils.createSequence(id, new int[] { 1, 7, 1, 6, 3,
								3, 9, 8, 5, 6, 0, 5 }, new int[] { 2, 12 }, new int[] {
								5, 9 }, new int[] { 3, 6 }, new int[] { 7, 9 });
				map.put("service", "pay.weixin.native");
				map.put("mch_id", mch_id);
				map.put("out_trade_no", orderNum);//---
				map.put("body", "微信支付");// 商品信息 ---
				map.put("total_fee", total_fee); //----
				map.put("notify_url", notify_url);// 通知地址
				map.put("mch_create_ip", mchIp);
				map.put("nonce_str", String.valueOf(new Date().getTime()));
				// 处理字符串
				Map<String, String> params = SignUtils.paraFilter(map);
				StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
				// 将字符串以&分隔
				SignUtils.buildPayParams(buf, params, false);
				String preStr = buf.toString();
				// 将字符串转换成md5
				String sign = MD5.sign(preStr, "&key=" + key, "utf-8");
				map.put("sign", sign);
				String reqUrl = "https://pay.swiftpass.cn/pay/gateway";
				System.out.println("reqUrl：" + reqUrl);

				// 生成xml
				System.out.println("reqParams:" + XmlUtils.parseXML(map));
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
						Map<String, String> resultMap = XmlUtils.toMap(
								EntityUtils.toByteArray(resp.getEntity()), "utf-8");
						res = XmlUtils.toXml(resultMap);
						System.out.println("请求结果：" + res);

						if ("0".equals(resultMap.get("status"))
								&& "0".equals(resultMap.get("result_code"))) {
							WXtradeRetSusInfo retInfo = new WXtradeRetSusInfo();
							WXTradeInfoDAO tradeinfoDao = new WXTradeInfoDAO(); // 微信支付交易返回信息表
							// 微信支付交易返回信息表 // WXtradeRetSusInfo
							retInfo = new WXtradeRetSusInfo();
							retInfo.setTrade_id(orderNum);
							retInfo.setAppid("");
							retInfo.setSub_appid("");
							retInfo.setMch_id("");
							retInfo.setSub_mch_id("");
							retInfo.setDevice_info(saruLruid);
							retInfo.setNonce_str("");
							retInfo.setSign("");
							retInfo.setResult_code("");
							retInfo.setError_code("");
							retInfo.setError_code_des("");
							tradeinfoDao.insertWXtradeRetSusInfo(retInfo);//请求成功插入WX_TRADE_RETCOD_SUCC_INFO

							// 微信支付交易流水信息表
							WXtradeInfo tradeInfo = new WXtradeInfo();
							tradeInfo.setTrade_id(orderNum);
							tradeInfo.setAppid("");
							tradeInfo.setMch_id("");
							tradeInfo.setDevice_info(saruLruid);
							tradeInfo.setNonce_str("");
							tradeInfo.setSign("");
							tradeInfo.setBody("");
							tradeInfo.setOut_trade_no(orderNum);
							tradeInfo.setTotal_fee(total_fee);
							tradeInfo.setFee_type("CNY");
							tradeInfo.setSpbill_create_ip(mchIp);
							tradeInfo.setReturn_code("");
							tradeInfo.setReturn_msg("");
							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
							tradeInfo.setTrade_time(sdf.format(new Date()));
							tradeInfo.setPrepay_id("");
							tradeInfo.setAuth_code("");
							tradeInfo.setYu37("");
							if (type.equals("upOrder")) {
								tradeInfo.setType("2");
								tradeinfoDao.insertWXtradeInfoOfUporder(tradeInfo);
							} else {
								tradeinfoDao.insertWXtradeInfo(tradeInfo);//请求成功 插入WX_TRADE_INFO
							}

							// 微信支付交易支付结果信息表
							WXtradeResSusInfo resInfo = new WXtradeResSusInfo();
							resInfo.setTrade_id(orderNum);
							resInfo.setTrade_state("NOTPAY");
							resInfo.setTrade_state_desc("未支付");
							tradeinfoDao.insertWXtradeResSusInfo(resInfo, saruLruid);//更新微信交易成功信息表 包含微信的交易成功流水表

							reqmap.put("resultCode", "0");
							reqmap.put("requestNo", orderNum);
							reqmap.put("code_url",resultMap.get("code_url"));
							reqmap.put("errorMsg", "获取二维码成功");

						} else {
							map.put("resultCode", "-99996");
							map.put("errorMsg", "获取二维码失败," + resultMap.get("message"));
						}
					} else {
						map.put("resultCode", "-99999");
						map.put("errorMsg", "获取二维码失败");
					}
				} catch (Exception e) {
					e.printStackTrace();
					reqmap.put("resultCode", "-99999");
					reqmap.put("errorMsg", "获取二维码失败");
				}
				break;
			}
		}
		if (bl) {
			reqmap.put("resultCode", "-99997");
			reqmap.put("errorMsg", "当天交易额度已满");
		}
		
		JSONObject json = JSONObject.fromObject(reqmap);
		response.getWriter().write(json.toString());
		response.getWriter().flush();
		response.getWriter().close();
		logger.info("==============生成二维码结束=============");
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
