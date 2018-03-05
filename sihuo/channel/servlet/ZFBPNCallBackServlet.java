package com.channel.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.channel.util.XmlUtils;

import dao.MerchantDao;
import dao.ZFBPNDao;

public class ZFBPNCallBackServlet extends HttpServlet{
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
		logger.info("===================支付宝一码付回调开始=====================");
		String resString = XmlUtils.parseRequst(request);
		logger.info("支付宝一码付回调的参数"+resString);
		String respString = "error";
		try {
			
			if(resString != null && !"".equals(resString)){
				Map<String,String> map = XmlUtils.toMap(resString.getBytes(),"utf-8");
				if (map.containsKey("sign")) {

					String status = map.get("status");
					if (status != null && "0".equals(status)) {
						String result_code = map.get("result_code");
						if (result_code != null && "0".equals(result_code)) {
							MerchantDao merchantDao = new MerchantDao();
							ZFBPNDao   zfbPNDao = new ZFBPNDao();
							String outTradeNo = map.get("out_trade_no");
							boolean flag = zfbPNDao.updatePNByOutTradeNo(outTradeNo, "14", map.get("mch_id"));//更新支付宝一码付的交易表
							merchantDao.updateMerchantIsPay(map.get("mch_id"));
							System.out.println("数据是否操作成功?====" + flag);
							respString = "success";
						}
					}
				}
			}
		} catch (Exception e) {
			logger.info("操作失败，原因：", e);
		}
		
		response.getWriter().write(respString);
		response.getWriter().flush();
		response.getWriter().close();
	}
}
