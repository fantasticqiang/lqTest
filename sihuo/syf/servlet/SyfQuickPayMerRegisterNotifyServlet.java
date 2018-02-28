package com.syf.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.yufutong.t0wuka.WKUnipayServletT0Notify;

public class SyfQuickPayMerRegisterNotifyServlet {

	private static final long serialVersionUID = 1L;
	Logger logger = Logger.getLogger(SyfQuickPayMerRegisterNotifyServlet.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String respString = "SUCCESS";
		response.getWriter().write(respString);
		response.getWriter().flush();
		response.getWriter().close();
		logger.info("(闪云付)无卡---T0------消费交易回调结束");
	}
}
