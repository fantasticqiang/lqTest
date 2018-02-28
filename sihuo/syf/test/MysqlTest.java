package com.syf.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.phoneposp.dao.ConnectionSource;

import com.alibaba.fastjson.JSON;
import com.syf.model.MerReportModel;

public class MysqlTest {

	public static void main(String[] args) throws Exception{
		 testMerchantReportSql();
	}
	
	public static void testMerchantReportSql() throws Exception{
		Class.forName("com.mysql.jdbc.Driver");
		String url = "jdbc:mysql://rm-bp1wcvyi069986t0xno.mysql.rds.aliyuncs.com:3306/beimite";
		String user = "root";
		String password = "Admin666";
		
		String id = "6000002332";
		String sql = "select MER_ID,BUSINESS from MERCHANT_REPORT where SARULRU_ID = ?";
		Connection con = null;
		
		con = DriverManager.getConnection(url, user, password);
		
		PreparedStatement state = null;
		ResultSet res = null;
		MerReportModel merReportModel = null;
		try {
			//con = ConnectionSource.getConnection();
			state = con.prepareStatement(sql);
			state.setString(1, id);
			res = state.executeQuery();
			merReportModel = new MerReportModel();
			while (res.next()) {
				String merId = res.getString(1);
				String key = res.getString(2);
				merReportModel.setKey(key);
				merReportModel.setMerId(merId);
			}
			System.out.println(merReportModel.getMerId());
			System.out.println(merReportModel.getKey());
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectionSource.close(con, res, state);
		}
	}
}
