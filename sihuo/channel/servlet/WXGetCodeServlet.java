package com.channel.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cn.phoneposp.dao.SaruInfoDao;

import com.phoneposp.util.PropertiesUtil;
import com.phoneposp.util.SequenceUtils;

public class WXGetCodeServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	Logger logger = Logger.getLogger(WXPublicStartServlet.class);
	public PropertiesUtil util = new PropertiesUtil(SaruInfoDao.class
			.getResource("/merchantInfo.properties").getFile());
	public String prefix_pn = util.getValue("prefix_pn");

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		StringBuffer requestUrl = new StringBuffer(
				"https://open.weixin.qq.com/connect/oauth2/authorize");

		String appId = "wx473f40f2bbfa73c7";// appid

//		String appId = "wx03a7de97bd7184d1";// appid
//		String redirectUri = "http://shuaka.qingyutec.com/PhonePospInterface/pnTransfer.jsp";// redirect_uri
		String redirectUri = "http://nkbnk.cn/PhonePospInterface/pnTransfer.jsp";// redirect_uri
																							// 跳转页面，中转页面
																							// 获取code
		String responseType = "code";// response_type 固定
		String scope = "snsapi_base";// 不弹出授权页面，直接跳转，只能获取用户openid
		String wechatRedirect = "#wechat_redirect";// 末尾固定值
		Integer id = new SaruInfoDao().getSequence("SEQ_ORDER_NO");
		
		// 我自己的参数
		String totalFee = request.getParameter("transAmt");// 金额,单位分
		String saruLruid = request.getParameter("saruLruid");// 商户号
		//订单号
		String orderNum = prefix_pn
                + SequenceUtils.createSequence(id, new int[] { 1, 7, 1, 6, 3, 3, 9, 8, 5, 6, 0, 5 },
                    new int[] { 2, 12 }, new int[] { 5, 9 }, new int[] { 3, 6 }, new int[] { 7, 9 });

		// 获取code
		requestUrl.append("?").append("appid=").append(appId);
		requestUrl.append("&").append("redirect_uri=")
				.append(URLEncoder.encode(redirectUri, "utf-8"));
		requestUrl.append("&").append("response_type=").append(responseType);
		requestUrl.append("&").append("scope=").append(scope);
		requestUrl.append("&").append("state=")
				.append(saruLruid + orderNum + totalFee);
		requestUrl.append(wechatRedirect);
		// 跳到 中转页面
		response.sendRedirect(requestUrl.toString());
	}
	public static void main(String[] args) throws Exception {
		StringBuffer requestUrl = new StringBuffer(
				"https://open.weixin.qq.com/connect/oauth2/authorize");

		String appId = "wx473f40f2bbfa73c7";// appid
		String redirectUri = "http://shuaka.qingyutec.com/PhonePospInterface/pnTransfer.jsp";// redirect_uri
																							// 跳转页面，中转页面
																							// 获取code
		String responseType = "code";// response_type 固定
		String scope = "snsapi_base";// 不弹出授权页面，直接跳转，只能获取用户openid
		String wechatRedirect = "#wechat_redirect";// 末尾固定值
		Integer id = 12;
		
//		// 我自己的参数
//		String totalFee = request.getParameter("transAmt");// 金额,单位分
//		String saruLruid = request.getParameter("saruLruid");// 商户号
		//订单号
		String orderNum = "SKWX"
                + SequenceUtils.createSequence(id, new int[] { 1, 7, 1, 6, 3, 3, 9, 8, 5, 6, 0, 5 },
                    new int[] { 2, 12 }, new int[] { 5, 9 }, new int[] { 3, 6 }, new int[] { 7, 9 });

		// 获取code
		requestUrl.append("?").append("appid=").append(appId);
		requestUrl.append("&").append("redirect_uri=")
				.append(URLEncoder.encode(redirectUri, "utf-8"));
		requestUrl.append("&").append("response_type=").append(responseType);
		requestUrl.append("&").append("scope=").append(scope);
		requestUrl.append("&").append("state=")
				.append("12345" + "11111111" + "33");
		requestUrl.append(wechatRedirect);
		// 跳到 中转页面
		System.out.println(requestUrl.toString());
		
	}
}
