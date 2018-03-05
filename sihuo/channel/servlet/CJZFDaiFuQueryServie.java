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
import cn.phoneposp.entity.T0CashInfoNew;
import cn.phoneposp.entity.T0CashinfoGallery;

import com.channel.util.Cj;
import com.channel.util.CjSignHelper;
import com.channel.util.CjSignHelper.VerifyResult;
import com.channel.util.HttpPostBodyMethod;
import com.channel.util.S;
import com.channel.util.U;

import dao.MerchantDao;
import dao.T0CashinfoGalleryDao;


public class CJZFDaiFuQueryServie {

//	private static final long serialVersionUID = 1L;
	static Logger logger = Logger.getLogger(CJZFDaiFuQueryServie.class);


	public static void queryTransStatusOfTransProxy() {
		RWBankCardDAO bankDao = new RWBankCardDAO();
		// List<T0CashInfoNew> list = bankDao.queryNotCardWITHI();
		MerchantDao merchantDao = new MerchantDao();
		List<T0CashInfoNew> list = merchantDao.queryCJDaiFuByWX();
		T0CashinfoGalleryDao t0CashinfoGalleryDao = new T0CashinfoGalleryDao();
		T0CashinfoGallery t0CashinfoGallery = new T0CashinfoGallery();
		logger.info(new Date() + "有" + list.size() + "个出款中的T0");
		SimpleDateFormat sdfOld = new SimpleDateFormat("yyyyMMddHHmmss");
		for (int i = 0; i < list.size(); i++) {
			T0CashInfoNew cash = list.get(i);
			logger.info(new Date() + "畅捷代付查询开始执行第" + (i + 1) + "个");

			Document doc = DocumentHelper.createDocument();
			Element msgEl = doc.addElement("MESSAGE");

			Element infoEl = msgEl.addElement("INFO");
			infoEl.addElement("TRX_CODE")
					.setText(Cj.XMLMSG_TRANS_CODE_实时交易结果查询);// G20001
			infoEl.addElement("VERSION").setText(Cj.XMLMSG_VERSION_01);// 01
			infoEl.addElement("MERCHANT_ID").setText("cp2016072894706");// 商户号
																		
			infoEl.addElement("REQ_SN").setText(U.createUUID());// 32位编码
			infoEl.addElement("TIMESTAMP").setText(sdfOld.format(new Date()));
			infoEl.addElement("SIGNED_MSG").setText("");

			Element bodyEl = msgEl.addElement("BODY");
			bodyEl.addElement("QRY_REQ_SN").setText(
					cash.getBccon_ordernum() + cash.getTimes());
			t0CashinfoGallery.setBccon_order_num(cash.getBccon_ordernum() + cash.getTimes());
			String xml = Cj.formatXml_UTF8(doc);
			System.out.println("xml:" + xml);
			try {
				CjSignHelper singHelper = new CjSignHelper();
				String signMsg = singHelper.signXml$Add(xml);
				System.out.println("signMsg为:" + signMsg);
				
				HttpClient client = new HttpClient();
				client.getParams().setParameter(
						HttpMethodParams.HTTP_CONTENT_CHARSET, S.ENCODING_utf8);
				client.getParams().setSoTimeout(10 * 60 * 1000);
				

				URL url = new URL("https://cop-gateway.chanpay.com/crps-gateway/gw01/process01");
				String urlstr = url.toString();
				System.out.println("提交地址" + urlstr);
				HttpPostBodyMethod post = new HttpPostBodyMethod(urlstr,signMsg);

				int statusCode = client.executeMethod(post);
				if (statusCode != HttpStatus.SC_OK) {
					String err = "访问失败！！HTTP_STATUS=" + statusCode;
					System.out.println(err);
					System.out.println("返回内容为："+ post.getResponseBodyAsString());
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
				Element body = msgE.element("BODY");

				if ("0000".equals(infoE.elementText("RET_CODE"))) {
					if ("0001".equals(body.elementText("RET_CODE")) || "0002".equals(body.elementText("RET_CODE"))) {
						bankDao.updateT0StatusAndReasonByOrdernum(
								body.elementText("ERR_MSG"), "I",
								cash.getBccon_ordernum());
						t0CashinfoGallery.setStatus("I");
						
					} else if ("0000".equals(body.elementText("RET_CODE"))) {
						bankDao.updateT0StatusAndReasonByOrdernum(
								body.elementText("ERR_MSG"), "S",
								cash.getBccon_ordernum());
						t0CashinfoGallery.setStatus("S");
						logger.info("====rtnBODY===" + body.toString());
					} else {
						logger.info("====rtnINFO===" + infoE.toString());
						bankDao.updateT0StatusAndReasonByOrdernum(
								body.elementText("ERR_MSG"), "F",
								cash.getBccon_ordernum());
						t0CashinfoGallery.setStatus("F");
					}
				} else {
					logger.info("====rtnINFO===" + infoE.toString());
					bankDao.updateT0StatusAndReasonByOrdernum(
							infoE.elementText("ERR_MSG"), "F",
							cash.getBccon_ordernum());
					t0CashinfoGallery.setStatus("F");
				}
				t0CashinfoGalleryDao.updateT0CashinfoGallery(t0CashinfoGallery);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
