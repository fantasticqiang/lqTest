package com.channel.servlet;

import java.io.IOException;

import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cn.phoneposp.dao.SaruInfoDao;

import com.phoneposp.util.PropertiesUtil;
import com.phoneposp.util.SequenceUtils;

public class ZFBGetCodeServlet extends HttpServlet{
	Logger logger = Logger.getLogger(ZFBGetCodeServlet.class);
	public PropertiesUtil util = new PropertiesUtil(SaruInfoDao.class.getResource("/merchantInfo.properties").getFile());
	public String prefix_pn = util.getValue("prefix_pn");
protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
	this.doPost(request, response);
}
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		logger.info("==============支付宝获取Code请求开始=============");
		StringBuffer requestUrl = new StringBuffer("https://openauth.alipay.com/oauth2/publicAppAuthorize.htm");//支付宝获取code的请求地址
		String appId = "2017030105985046";// 金联银通支付宝的appId
		String redirectUri = "http://shuaka.qingyutec.com/PhonePospInterface/zfbpnTransfer.jsp";// redirect_uri 重定向的地址
//		String redirectUri = "https://www.baidu.com";// redirect_uri
		String scope ="auth_base";//以auth_base为scope发起的网页授权，是用来获取进入页面的用户的userId的，并且是静默授权并自动跳转到回调页的
	    
		// 我自己的参数
		Integer id = new SaruInfoDao().getSequence("SEQ_ORDER_NO");
		String totalFee = request.getParameter("transAmt");// 金额,单位分
		logger.info("获取的金额是=="+totalFee);
		String saruLruid = request.getParameter("saruLruid");// 商户号
		logger.info("获取的商户号是=="+saruLruid);
		//订单号
		String orderNum = prefix_pn
                + SequenceUtils.createSequence(id, new int[] { 1, 7, 1, 6, 3, 3, 9, 8, 5, 6, 0, 5 },
                    new int[] { 2, 12 }, new int[] { 5, 9 }, new int[] { 3, 6 }, new int[] { 7, 9 });
		// 获取code
				requestUrl.append("?").append("app_id=").append(appId);
				requestUrl.append("&").append("scope=").append(scope);
				requestUrl.append("&").append("redirect_uri=").append(URLEncoder.encode(redirectUri, "utf-8"));
				requestUrl.append("&").append("state=").append(saruLruid + orderNum + totalFee);
				// 跳到 中转页面
				response.sendRedirect(requestUrl.toString());
		
	}
	public static void main(String[] args) throws Exception {
		String redirectUri = "http://shuaka.qingyutec.com/PhonePospInterface/zfbpnTransfer.jsp";// redirect_uri
       //https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=2017030105985046&scope=auth_base&redirect_uri=http%3A%2F%2Fshuaka.qingyutec.com%2FPhonePospInterface%2FzfbpnTransfer.jsp

      System.err.println(URLEncoder.encode(redirectUri, "utf-8"));
	}

}
