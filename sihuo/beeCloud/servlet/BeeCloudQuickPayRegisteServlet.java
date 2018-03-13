package com.beeCloud.servlet;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.beeCloud.constant.BeeCloudConstant;
import com.beeCloud.dao.BeeCloudPayDao;
import com.beeCloud.model.MercantEnterInfo;
import com.syf.constant.SyfConstant;
import com.syf.dao.SyfQuickPayDao;
import com.syf.model.MerchantEnterModel;

/**
 * 
 *老罗商旅 报户
 */
public class BeeCloudQuickPayRegisteServlet extends HttpServlet{

	/**
	 * 商户报户接口
	 */
	private static final long serialVersionUID = 1L;
	Logger logger = Logger.getLogger("DEFAULT-APPENDER");

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=utf-8");
		logger.info("============(老罗商旅BeeCloud)----商户报户开始==============");
		// 获取App前台参数
		String saruLruid = request.getParameter("saruLruid");// 商户号
		String reservedPhone = request.getParameter("reservedPhone"); // 结算卡绑定的手机号
		String idCard = request.getParameter("idCard"); // 身份证号
		String cardNo = request.getParameter("cardNo");//结算卡卡号
		
		BeeCloudPayDao merchantDao = new BeeCloudPayDao();
		MercantEnterInfo merchantInfo = merchantDao.queryMerchantBysaruId(saruLruid);
		String contactorNm = merchantInfo.getMerName();//结算人姓名
		String acctBankname = merchantInfo.getBankName();//结算卡银行名称
		String bankNo = merchantInfo.getBankNo();//联行号
		String t0drawFee = BeeCloudConstant.sxf;//t0提现手续费
		String t0tradeRate = merchantDao.selectWKT0feeRate(saruLruid.substring(saruLruid.length() - 4));//t0交易费率
		String retCode = "0001";
		String retMsg = "报户失败";
		HashMap<String, String> resultMap = new HashMap<String, String>();
		try {
			String merStatus = "S";//(I-正在处理，F-开户失败，S-开户成功)
			//设置渠道标识：SYF
			String channelID = "BeeCloud";
			String regionId = merchantDao.selectAreaCode(idCard.substring(0, 6));
			retCode = "0000";
			retMsg = "报户成功";
			merchantDao.insertMerchantAllMeaasge(saruLruid, contactorNm,
					"", reservedPhone, merStatus, idCard,
					contactorNm, cardNo, acctBankname, reservedPhone,
					bankNo, "1", "1", regionId, t0drawFee,
					t0tradeRate, retCode, retMsg,"",channelID);
			resultMap.put("resCode", retCode);
			resultMap.put("resMsg", retMsg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String json = JSON.toJSONString(resultMap);
		response.getWriter().write(json.toString());
		response.getWriter().flush();
		response.getWriter().close();
		logger.info("============(老罗商旅BeeCloud)----商户报户结束==============");
	}
		
}
