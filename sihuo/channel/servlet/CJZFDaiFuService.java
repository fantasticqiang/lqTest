package com.channel.servlet;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import cn.phoneposp.dao.RWBankCardDAO;
import cn.phoneposp.entity.MerchantInfo;
import cn.phoneposp.entity.ParentBank;
import cn.phoneposp.entity.T0CashInfoNew;

import com.channel.util.Cj;
import com.channel.util.CjSignHelper;
import com.channel.util.CjSignHelper.VerifyResult;
import com.channel.util.HttpPostBodyMethod;
import com.channel.util.S;
import com.channel.util.U;
import com.common.util.AmountUtil;

import dao.CJZFDaiFuDao;
import dao.MerchantDao;
//畅捷进行代付 
public class CJZFDaiFuService {
	static Logger logger = Logger.getLogger(CJZFDaiFuService.class);
	public static void main(String[] args) {
		wxDaiFu();
	}
	public static void wxDaiFu() {
		MerchantDao merchantDao = new MerchantDao();
		RWBankCardDAO bankDao = new RWBankCardDAO();
		List<T0CashInfoNew> list = merchantDao.queryCJDaiFuWithWX();// 查询需要代付的数据
		logger.info(new Date() + "有" + list.size() + "个需要代付");
		Date date = new Date();
		SimpleDateFormat sdfOld = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		double total = 0;
		for (int i = 0; i < list.size(); i++) {
			T0CashInfoNew cash = list.get(i);
			logger.info(new Date() + "畅捷代付开始执行第" + (i + 1) + "个");
			total = AmountUtil.div((double) (AmountUtil.sub(
					(int) (AmountUtil.mul(
							Double.parseDouble(cash.getCash_amount()), 100)),
					(int) (AmountUtil.mul(
							Double.parseDouble(cash.getCash_fee()), 10000)))),
					100);//需要代付的金额
			 
			if (total <= 0) {
				logger.error("金额过低！");
				continue;
			}
			CJZFDaiFuDao cjzfDaiFuDao = new CJZFDaiFuDao();
			MerchantInfo merInfo = bankDao.getBankInfo(cash.getSaru_lruid());
			ParentBank parentBank = cjzfDaiFuDao.getParentBank(merInfo.getSaruBankLinked());
			Document doc = DocumentHelper.createDocument();
			Element msgEl = doc.addElement("MESSAGE");
			Element infoEl = msgEl.addElement("INFO");
			infoEl.addElement("TRX_CODE").setText(Cj.XMLMSG_TRANS_CODE_单笔实时付款);// G10002
			infoEl.addElement("VERSION").setText(Cj.XMLMSG_VERSION_01);// 01
			infoEl.addElement("MERCHANT_ID").setText("cp2016072894706");// 商户号
			infoEl.addElement("REQ_SN").setText(cash.getBccon_ordernum() + (cash.getTimes()));// 32位编码，该号码是通过算法得来的 是唯一的
			infoEl.addElement("TIMESTAMP").setText(U.getCurrentTimestamp());
			infoEl.addElement("SIGNED_MSG").setText("");
			Element bodyEl = msgEl.addElement("BODY");
			bodyEl.addElement("BUSINESS_CODE").setText("09900");// 业务代码
			
			bodyEl.addElement("CORP_ACCT_NO").setText("110919558810604");
			
			bodyEl.addElement("PRODUCT_CODE").setText("60020002");// 产品编码
		
			bodyEl.addElement("ACCOUNT_PROP").setText("0");// 对私 0  对公 1
			
			bodyEl.addElement("SUB_MERCHANT_ID").setText("YS0001");// 可选
			bodyEl.addElement("BANK_GENERAL_NAME").setText(
					parentBank.getParentBankName());
			bodyEl.addElement("ACCOUNT_TYPE").setText("00");//
			bodyEl.addElement("ACCOUNT_NO").setText(merInfo.getSaruBankCard());// 银行卡或存折号码
			bodyEl.addElement("ACCOUNT_NAME").setText(
					merInfo.getSaruAccountName());// 银行卡或存折上的所有人姓名
			bodyEl.addElement("BANK_NAME").setText(merInfo.getSaurBankName());// 开户行名称
			bodyEl.addElement("BANK_CODE").setText(merInfo.getSaruBankLinked());// 对方开户行号，对方账号对应的支行行号
			bodyEl.addElement("DRCT_BANK_CODE").setText(
					parentBank.getParentBankNo());// 对方开户行对应的清算行总行行号
			bodyEl.addElement("CURRENCY").setText("CNY");
			bodyEl.addElement("AMOUNT").setText(
					(int) AmountUtil.mul(total, 100) + "");// 金额
			// bodyEl.addElement("ID_TYPE").setText("0");//可选 证件类型
			// bodyEl.addElement("ID").setText("1234567890");//证件号
			String xml = Cj.formatXml_UTF8(doc);
			System.out.println("xml:" + xml);
			
			try {

				CjSignHelper singHelper = new CjSignHelper();
				String signMsg = singHelper.signXml$Add(xml);//对xml进行加密
				System.out.println("signMsg为:" + signMsg);
                //通过httpclient发送请求
				HttpClient client = new HttpClient();
				client.getParams().setParameter(
						HttpMethodParams.HTTP_CONTENT_CHARSET, S.ENCODING_utf8);
				client.getParams().setSoTimeout(10 * 60 * 1000);

				URL url = new URL(
						"https://cop-gateway.chanpay.com/crps-gateway/gw01/process01");
				String urlstr = url.toString();
				System.out.println("提交地址" + urlstr);
				HttpPostBodyMethod post = new HttpPostBodyMethod(urlstr,signMsg);

				int statusCode = client.executeMethod(post);
				if (statusCode != HttpStatus.SC_OK) {
					String err = "访问失败！！HTTP_STATUS=" + statusCode;
					System.out.println(err);
					System.out.println("返回内容为："
							+ post.getResponseBodyAsString());
					throw new HttpException(err);
				}
				String respData = post.getResponseBodyAsString();
				// 验证签名x
				VerifyResult verifyResult = singHelper
						.verifyCjServerXml(respData);
				if (!verifyResult.result) {
					throw new Exception("验证CJ返回数据的签名失败！" + verifyResult.errMsg);
				}
				Document reqDoc = DocumentHelper.parseText(respData);

				Element msgE = reqDoc.getRootElement();
				Element infoE = msgE.element("INFO");
                 String str =infoE.elementText("RET_CODE");
                 System.out.println(str);
				if ("0000".equals(infoE.elementText("RET_CODE"))) {
					bankDao.updateT0CashNEWByHuanShangTong("I", cash
							.getBccon_ordernum(), cash.getFile_path(), cash
							.getLng_lat(), cash.getPlace_name(), sdfOld
							.format(date), infoE.elementText("ERR_MSG")
							.toString(), cash.getTimes());
				} else {
					logger.info("====rtncode===");
//					bankDao.updateT0CashNEWByHuanShangTong(
//							"F",
//							cash.getBccon_ordernum(),
//							cash.getFile_path(),
//							cash.getLng_lat(),
//							cash.getPlace_name(),
//							sdfOld.format(date),
//							infoE.elementText("ERR_MSG").toString()
//									+ infoE.elementText("RET_CODE"),
//							cash.getTimes()+1);
					//为了解决重复请求 所修改
					bankDao.updateT0CashNEWByHuanShangTong(
							"F",
							cash.getBccon_ordernum(),
							cash.getFile_path(),
							cash.getLng_lat(),
							cash.getPlace_name(),
							sdfOld.format(date),
							infoE.elementText("ERR_MSG").toString()
									+ infoE.elementText("RET_CODE"),
							cash.getTimes());
				}

			} catch (Exception e) {
				e.printStackTrace();
				logger.info("异常是-------"+e.getMessage());
			}
		}

	}
	
}
