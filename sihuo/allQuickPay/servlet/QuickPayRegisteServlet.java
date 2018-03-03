package com.allQuickPay.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.beeCloud.servlet.BeeCloudQuickPayRegisteServlet;
import com.syf.servlet.SyfQuickPayMerRegisteServlet;

public class QuickPayRegisteServlet extends HttpServlet{
	/**
	 * 商户总的  报户接口
	 */
	private static final long serialVersionUID = 1L;
	Logger logger = Logger.getLogger(QuickPayRegisteServlet.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String channelID = request.getParameter("channelID");
		if("SYF".equals(channelID)){
			new SyfQuickPayMerRegisteServlet().doPost(request, response);
		}else if("BeeCloud".equals(channelID)){
			new BeeCloudQuickPayRegisteServlet().doPost(request, response);
		}
	}
}
