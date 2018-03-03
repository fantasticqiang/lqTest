package com.allQuickPay.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cn.phoneposp.dao.ConnectionSource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class QuickPayQueryAllChannelServlet extends HttpServlet{

	/**
	 * 查询所有快捷通道
	 */
	private static final long serialVersionUID = 1L;
	Logger logger = Logger.getLogger("DEFAULT-APPENDER");

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 从前台获取交易参数
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		String sql = "select ID,CHANNEL_ID,CHANNEL_NAME,CHANNEL_TYPE,JF,JS_TYPE,DESCRIPTION from CHANNEL where 1=1 order by ID desc";

		Connection con = null;
		PreparedStatement state = null;
		ResultSet res = null;
		JSONArray jsonArray = new JSONArray();
		try {
			con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			res = state.executeQuery();
			/*
			 * con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1, SARULRU_ID);
			res = state.executeQuery();
			 */
			while(res.next()){
				String ID = res.getString(1);
				String CHANNEL_ID = res.getString(2);
				String CHANNEL_NAME = res.getString(3);
				String CHANNEL_TYPE = res.getString(4);
				String JF = res.getString(5);
				String JS_TYPE = res.getString(6);
				String DESCRIPTION = res.getString(7);
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("ID", ID);
				jsonObject.put("CHANNEL_ID", CHANNEL_ID);
				jsonObject.put("CHANNEL_NAME", CHANNEL_NAME);
				jsonObject.put("CHANNEL_TYPE", CHANNEL_TYPE);
				jsonObject.put("JF", JF);
				jsonObject.put("JS_TYPE", JS_TYPE);
				jsonObject.put("DESCRIPTION", DESCRIPTION);
				jsonArray.add(jsonObject);
			}
			JSONObject result = new JSONObject();
			result.put("data", jsonArray);
			result.put("resCode", "0000");
			result.put("resMsg", "请求成功");
			String resultJsonStr = JSON.toJSONString(result);
			System.out.println(resultJsonStr);
			logger.info("查询商户状态返回："+resultJsonStr);
			PrintWriter out = response.getWriter();
			out.write(resultJsonStr);
			out.flush();
			out.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, res, state);
		}		
	}
}
