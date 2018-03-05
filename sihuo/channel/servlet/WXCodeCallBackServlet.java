package com.channel.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cn.phoneposp.dao.RWBankCardDAO;
import cn.phoneposp.dao.SaruInfoDao;
import cn.phoneposp.dao.WXTradeInfoDAO;
import cn.phoneposp.entity.RPscalecommission;
import cn.phoneposp.entity.T0CashInfoNew;
import cn.phoneposp.entity.WXtradeResSusInfo;
import cn.phoneposp.entity.WXtradeRetSusInfo;

import com.channel.util.XmlUtils;
import com.common.util.AmountUtil;

import dao.MerchantDao;

/**
 * @author rujingyu 扫码支付返回接口
 * 
 */
public class WXCodeCallBackServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger
			.getLogger(WXCodeCallBackServlet.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		logger.info("============微信扫码回调接口开始==============");

		String resString = XmlUtils.parseRequst(request);
		System.out.println("通知内容：" + resString);
		String respString = "error";
		try {
			if (resString != null && !"".equals(resString)) {

				Map<String, String> map = XmlUtils.toMap(resString.getBytes(),
						"utf-8");
				String res = XmlUtils.toXml(map);
				System.out.println("通知内容：" + res);
				if (map.containsKey("sign")) {
					
						String status = map.get("status");
						if (status != null && "0".equals(status)) {
							String result_code = map.get("result_code");
							if (result_code != null && "0".equals(result_code)) {
								String orderNum = map.get("out_trade_no");
								String totalFee = map.get("total_fee");
								SaruInfoDao saruInfoDao = new SaruInfoDao();
								SimpleDateFormat sdf = new SimpleDateFormat(
										"yyyyMMddHHmmss");
								WXTradeInfoDAO tradeinfoDao = new WXTradeInfoDAO();

								String state = tradeinfoDao
										.getWxResStatusByTradeId(orderNum);
								String saruLruid = tradeinfoDao
										.getSaruLruidByTradeId(orderNum);
								String type = tradeinfoDao
										.getTypeByTradeId(orderNum);
								if (state.equals("SUCCESS")) {
									respString = "SUCCESS";  //如果交易已经成功，返回success给上游
									response.getWriter().write(respString);
									response.getWriter().flush();
									response.getWriter().close();
									return;
								}

								WXtradeRetSusInfo retInfo = new WXtradeRetSusInfo();
								retInfo.setTrade_id(orderNum);
								retInfo.setAppid("");
								retInfo.setSub_appid("");
								retInfo.setMch_id("");
								retInfo.setSub_mch_id("");
								retInfo.setDevice_info(saruLruid);
								retInfo.setNonce_str("");
								retInfo.setSign("");
								retInfo.setResult_code("SUCCESS");
								retInfo.setError_code("");
								retInfo.setError_code_des("");
								tradeinfoDao.updateWXtradeRetSusInfo(retInfo);//更新微信支付信息表WX_TRADE_RETCOD_SUCC_INFO

								WXtradeResSusInfo resInfo = new WXtradeResSusInfo();
								resInfo.setTrade_id(orderNum);
								resInfo.setOpenid("");
								resInfo.setSub_openid("");
								resInfo.setIs_subscibe("");
								resInfo.setSub_is_subscribe("");
								resInfo.setTrade_type("");
								resInfo.setBank_tye("");
								resInfo.setFee_type("");
								resInfo.setTotal_fee(totalFee);
								resInfo.setCash_fee_type("");
								resInfo.setCash_fee(null);
								resInfo.setCoupon_fee(null);
								resInfo.setTransaction_id("");
								resInfo.setOut_trade_id(orderNum);
								resInfo.setAttach("");
								resInfo.setTrade_state("SUCCESS");
								resInfo.setTrade_state_desc("");
								resInfo.setType(type);
								MerchantDao merchantDao = new MerchantDao();
					            merchantDao.updateMerchantIsPay((String)map.get("mch_id"));//每笔交易成功置成Y 商户号是轮流的 
					            merchantDao.insertWXtradeResSusInfoOfHST(resInfo, saruLruid,map.get("mch_id"));

								// /////////////////////////////////////////////////////////////////////////////////////
								if (type.equals("1")) {

									SaruInfoDao infoDao = new SaruInfoDao();
									RPscalecommission scalecommission = infoDao
											.querySracT0Rate(saruLruid);
									String linvnum = infoDao
											.getLinvnumByBcconOrdernum(orderNum);
									double rate = scalecommission
											.getWeixinvalue();
									double handFee = AmountUtil.mul(AmountUtil
											.div(Double.parseDouble(totalFee),100), rate); // 手续费
									double cashAmt = AmountUtil.sub(AmountUtil.div(Double.parseDouble(totalFee),100), handFee);

									RWBankCardDAO bankDao = new RWBankCardDAO();
									T0CashInfoNew cashInfo = new T0CashInfoNew();
									cashInfo.setBccon_ordernum(orderNum);
									cashInfo.setSaru_lruid(saruLruid);
									cashInfo.setCash_amount(cashAmt + "");
									cashInfo.setFile_path("");
									cashInfo.setPan("");
									cashInfo.setBccon_linvnum(linvnum);
									cashInfo.setLng_lat("");
									cashInfo.setPlace_name("");
									cashInfo.setCash_status("AUDITING_A"); // 默认初审中
									cashInfo.setT0Setttype(1);
									boolean ifUpdate = bankDao.updateNotCardT0ThingServlet(cashInfo);
								} else {
									// 修改商户等级和费率
									saruInfoDao.updateSaruToNextLevel(saruLruid);
								}
								respString = "success";
							}
						}
					}
					respString = "success";
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.getWriter().write(respString);
		response.getWriter().flush();
		response.getWriter().close();
		// logger.info("============威富通回调接口结束==============");

	}
}
