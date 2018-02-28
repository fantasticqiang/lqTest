package com.allQuickPay.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.syf.servlet.SyfModifyMerStatusByManServlet;

public class QuickPayModifyMerStatusServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;
	Logger logger = Logger.getLogger("DEFAULT-APPENDER");

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String channelID = request.getParameter("channelID");
		if("SYF".equals(channelID)){
			new SyfModifyMerStatusByManServlet().doPost(request, response);
		}
	}
}
