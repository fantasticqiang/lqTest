package com.channel.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cn.phoneposp.dao.WXTradeInfoDAO;

import com.channel.util.SignUtils;
import com.channel.util.XmlUtils;
import com.google.gson.Gson;

import dao.MerchantDao;

public class WXJSCallBackServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger
			.getLogger(WXJSCallBackServlet.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		logger.info("===================微信一码付回调开始=====================");
		String resString = XmlUtils.parseRequst(request);
		String respString = "error";
		try {

			if (resString != null && !"".equals(resString)) {
				Map<String, String> map = XmlUtils.toMap(resString.getBytes(),
						"utf-8");
				String res = XmlUtils.toXml(map);
				if (map.containsKey("sign")) {

					String status = map.get("status");
					if (status != null && "0".equals(status)) {
						String result_code = map.get("result_code");
						if (result_code != null && "0".equals(result_code)) {
							MerchantDao merchantDao = new MerchantDao();
							String outTradeNo = map.get("out_trade_no");
							logger.info("微信一码付回调的订单号"+outTradeNo);
							boolean flag = merchantDao.updatePNByOutTradeNo(
									outTradeNo, "14", map.get("mch_id"));
							merchantDao.updateMerchantIsPay(map.get("mch_id"));
							logger.info("微信一码付所要更新的商户号"+map.get("mch_id"));
							System.out.println("数据是否操作成功?====" + flag);
							respString = "success";
						}
					}
				}
			}
			response.getWriter().write(respString);
		} catch (Exception e) {
			logger.error("操作失败，原因：", e);
		}
		response.getWriter().write(respString);
		response.getWriter().flush();
		response.getWriter().close();
		logger.info("===================微信一码付回调结束=====================");

	}

}
