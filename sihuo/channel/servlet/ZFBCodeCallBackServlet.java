package com.channel.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cn.phoneposp.dao.AliTradeInfoDAO;
import cn.phoneposp.dao.RWBankCardDAO;
import cn.phoneposp.entity.AliTradeInfo;
import cn.phoneposp.entity.RWZFBConsumer;
import cn.phoneposp.entity.T0CashInfoNew;

import com.channel.util.XmlUtils;
import com.interfaces.O110101.Constant;

import dao.MerchantDao;


/**
 *  支付宝扫码支付回调接口
 */
public class ZFBCodeCallBackServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger
			.getLogger(ZFBCodeCallBackServlet.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		logger.info("=========================支付宝回调开始================================");
		
		String resString = XmlUtils.parseRequst(request);
		String respString = "error";
		try {
			if ((resString != null) && (!"".equals(resString))) {
				Map<String, String> map = XmlUtils.toMap(resString.getBytes(),"utf-8");
				String res = XmlUtils.toXml(map);
				if (map.containsKey("sign")) {
					String status = (String) map.get("status");
					if ((status != null) && ("0".equals(status))) {
						String result_code = (String) map.get("result_code");
						if ((result_code != null) && ("0".equals(result_code))) {
//							MerchantDao merchantDao = new MerchantDao();
//							String outTradeNo = (String) map.get("out_trade_no");
//							boolean flag = merchantDao.updatePNByOutTradeNo(outTradeNo, "14",
//									(String) map.get("mch_id"));
//							merchantDao.updateMerchantIsPay((String) map
//									.get("mch_id"));
//							System.out.println("数据是否操作成功?====" + flag);
//							respString = "success";
							
							 //实例化辅助类
					        AliTradeInfoDAO aliDAO = new AliTradeInfoDAO();
					        RWZFBConsumer rwZFBConsumer = new RWZFBConsumer();
					        rwZFBConsumer = aliDAO.getRWZFBConsumerByBcconOrdernum(map.get("out_trade_no"));
					        
					        if (rwZFBConsumer != null && "SUCCESS".equals(rwZFBConsumer.getBcconState())) {
					        	respString = "success";
					            response.getWriter().write(respString);
					            response.getWriter().flush();
					            response.getWriter().close();
					            return;
					        }

					        rwZFBConsumer.setBcconState("SUCCESS");
					        aliDAO.updateRWZFBConsumerState(rwZFBConsumer);

					        AliTradeInfo aliInfo = new AliTradeInfo();
					        aliInfo.setTrade_status("TRADE_SUCCESS");
					        aliInfo.setOut_trade_no(map.get("out_trade_no"));
					        aliDAO.updateAliTradeInfoWhereClose(aliInfo);

					        //插入T0表中
					        RWBankCardDAO bankDao = new RWBankCardDAO();
					        T0CashInfoNew cashInfo = new T0CashInfoNew();
					        cashInfo.setBccon_ordernum(map.get("out_trade_no"));
					        cashInfo.setSaru_lruid(rwZFBConsumer.getSaruLruid());
					        cashInfo.setCash_amount(rwZFBConsumer.getBcconSalemoney() + "");
					        cashInfo.setCash_status("AUDITING_A"); //默认初审中
					        cashInfo.setT0Setttype(2);
					        cashInfo.setSettlers(Constant.INTERFACE_CODE);
					        bankDao.updateNewT0ThingServlet(cashInfo);
					        MerchantDao merchantDao = new MerchantDao();
					        merchantDao.updateMerchantIsPay((String)map.get("mch_id"));//每笔交易成功置成Y 商户号是轮流的 
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
		logger.info("=======================兴业银行支付宝交易回调成功============================");
	}
}
