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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.phoneposp.dao.ConnectionSource;

public class QuickPayQueryMerStatusServlet extends HttpServlet{

	/**
	 * 商户总的  查询商户状态接口
	 */
	private static final long serialVersionUID = 1L;
	Logger logger = Logger.getLogger(QuickPayQueryServlet.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 从前台获取交易参数
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		String saruLruid = request.getParameter("saruLruid");// 用户id
		
		String sql = "select ID,SARULRU_ID,MER_ID,MER_STATUS from MERCHANT_REPORT where SARULRU_ID = ?";

		Connection con = null;
		PreparedStatement state = null;
		ResultSet res = null;
		JSONArray jsonArray = new JSONArray();
		try {
			con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1, saruLruid);
			res = state.executeQuery();
			/*
			 * con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1, SARULRU_ID);
			res = state.executeQuery();
			 */
			JSONObject jsonObject = new JSONObject();
			while(res.next()){
				String ID = res.getString(1);
				String SARULRU_ID = res.getString(2);
				String MER_ID = res.getString(3);
				String MER_STATUS = res.getString(4);
				jsonObject.put("ID", ID);
				jsonObject.put("SARULRU_ID", SARULRU_ID);
				jsonObject.put("MER_ID", MER_ID);
				jsonObject.put("MER_STATUS", MER_STATUS);
			}
			JSONObject result = new JSONObject();
			if("S".equals(jsonObject.getString("MER_STATUS"))){
				result.put("resCode", "0000");
				result.put("resMsg", "审核成功");
			}else if("I".equals(jsonObject.getString("MER_STATUS"))){
				result.put("resCode", "0001");
				result.put("resMsg", "审核中");
			}else if("F".equals(jsonObject.getString("MER_STATUS"))){
				result.put("resCode", "0002");
				result.put("resMsg", "审核失败");
			}else{
				result.put("resCode", "0003");
				result.put("resMsg", "未开户");
			}
			jsonArray.add(jsonObject);
			result.put("data", jsonObject);
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
