package com.syf.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.syf.dao.SyfQuickPayDao;

public class SyfModifyMerStatusByManServlet {
	private static final long serialVersionUID = 1L;
	Logger logger = Logger.getLogger("DEFAULT-APPENDER");

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		// 从前台获取交易参数
		String upMerID = request.getParameter("upMerID");// 上游分配的商户号
		String status = request.getParameter("status");// 上游分配的商户号
		String [] statusArray= {"I","S","F"};
		HashMap<String, String> resultMap = new HashMap<String, String>();
		if(!Arrays.asList(statusArray).contains(status)){
			resultMap.put("resCode", "001");
			resultMap.put("resMsg", "状态非法");
		}
		SyfQuickPayDao syfQuickPayDao = new SyfQuickPayDao();
		syfQuickPayDao.modifyMerStatusByMan(upMerID, status);
		resultMap.put("resCode", "000");
		resultMap.put("resMsg", "修改状态为："+status+"成功");
		PrintWriter out = response.getWriter();
		out.write(JSON.toJSONString(resultMap));
		out.flush();
		out.close();
	}
}
