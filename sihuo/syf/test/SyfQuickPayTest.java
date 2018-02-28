package com.syf.test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import cn.phoneposp.dao.SaruInfoDao;

import com.alibaba.fastjson.JSONObject;
import com.interfaces.O110101.util.OrderNumUtil;
import com.phoneposp.util.PropertiesUtil;
import com.syf.constant.SyfConstant;
import com.syf.dao.SyfQuickPayDao;
import com.syf.model.MerReportModel;
import com.syf.model.MerchantEnterModel;
import com.syf.util.Commons;
import com.syf.util.HttpMsg;
import com.syf.util.HttpUtil;
import com.syf.util.Md5Util;

/**
 * 闪云付快捷测试类
 */
public class SyfQuickPayTest {
	
	static Logger logger = Logger.getLogger(SyfQuickPayTest.class);

	public static void main(String[] args) throws Exception{
/*		SyfQuickPayDao merchantDao = new SyfQuickPayDao();
		MerReportModel merReport = merchantDao.getMerReport("6000002332");
		String merId = merReport.getMerId();//商户号
		System.out.println(merId);*/
		
		testPay();
		
		//testQuery();
		
		//testNotify();
		
		//merRegisteTest();//测试报户
	}
	
	/**
	 * 闪云付测试快捷下单
	 * 
	 * {"amount":"100.00","bankName":"招商银行股份有限公司上海淮中支行","body":"线上支付","cardCertNo":"411503198706030755","cardCvv":"123","cardMobile":"15021312607","cardName":"秦原伟","cardNo":"6222020409032244853","clientIp":"127.0.0.1","notifyUrl":"http://47.94.193.202:8080/Test/kft.jsp","orderNo":"1518055426549","returnUrl":"","subject":"线上支付","unionNo":"308290003378"}
https://gray-scp-getway.9fbank.com/scp/pay
{"accessToken":"1518055426549","cipherType":"AES","ciphertext":"4fiKWLHKYnMud8gmbV8N5lGuzfJXxEbk0AMDazkPo2h2msaBwK6+/FbUmzxAXsfvncofA5gVkqe0\r\nH9ZgUciWyRPCwKxA0KQYVhYh/Vz+4TltisyORsFO/cythl+JrQF8NPyxKjBFL/uV/ijXthWhnVop\r\nAR+0G3bXrckhHU9XKkau0JVfdgycNtMZsCcXA+8v4IfzCTOCxdY5L8J4aOpxpcySEqZlTY9RgTOJ\r\n5IWewxO11zTp7oMNfe5oYKo1035ZcY8qNLLoP1nsiiqNcd9xildZUZoSebMAR7ZudW0GrnZThs+5\r\nuuWq+abBzlXa6G4nxWK55j9rpO0m6PoRHiQfkemSS69dI45ZCOz4hBdhnPyIFm30sXnBMhEvRl3L\r\nlW1DbUZZDozi0kZwNfz/E3Br21XDKD8Z3j/hx+ZNLGV5O150OmV7t7pnEOGiK9DXGEiPjSRHtjN1\r\nKYVMvMx6HaduEpPG79s+CN3ImgpOD3OAODH/uoNPdAehrq6jDcD5OjibfXwMXvnw7jsbivPHcUEd\r\nd6mPmbojOWEAICK1S3fRwXo=","merchId":"m1801250001","partnerId":"ORG_1505714517351","reqReserved":"","sign":"1d0ed4dc7fb11e2107bb6e38cf343259","signType":"MD5","source":"M","transCode":"T102","version":"V1"}
829
200
{"gateResCode":"E00000","gateResMsg":"成功","ciphertext":"aW8SH9GZfS8dqxy9t35OgAq5eM8iLsg0dOyyu7nDKwnBUtsWHVSbxB+bf9bn2ZCTjYp1S4wvdCq5/a/58lSlLbDEDSal8xFWjEn70WiQ6NQ=","sign":"088ca969cdf21370a99a3e4605370894"}
{"orderNo":"1518055426549","resCode":"S01000000","resMsg":"成功"}
true
	 */
	public static void testPay() {
		String orderId = new Date().getTime()+"";
		String orderAmt = "10000";
		String cardNoFromApp = "6222020409032244853";
		/** 准备请求参数*/
		//公共字段
		String source = "M";//M-商户发起
		String version = "V1";//
		String cipherType = "AES";//加密类型
		String signType = "MD5";//签名方式
		String transCode = "T102";//交易编码
		String partnerId = SyfConstant.partnerId;//代理商ID
		String merchId = "mi118021410542700009";//商户号
		String accessToken = orderId;//请求流水号
		String ciphertext = "";//请求密文
		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("source", source);
		reqMap.put("version", version);
		reqMap.put("cipherType", cipherType);
		reqMap.put("signType", signType);
		reqMap.put("transCode", transCode);
		reqMap.put("partnerId", partnerId);
		reqMap.put("merchId", merchId);
		reqMap.put("accessToken", accessToken);
		reqMap.put("reqReserved", "");
		//密文字段
		String orderNo = orderId;//商户订单号
		String amount = String.format("%.2f",Double.parseDouble(orderAmt)/100d);//金额
		String subject = "线上支付";//商品主题
		String body = "线上支付";//商品内容
		String clientIp = SyfConstant.clientIp;//发起请求客户端的ip
		String notifyUrl = SyfConstant.notifyUrl;//异步通知地址
		String cardNo = cardNoFromApp;//卡号
		SyfQuickPayDao merchantDao = new SyfQuickPayDao();
		MerchantEnterModel merchantInfo = merchantDao.queryMerchantBysaruId("6000002332");
		String cardName = merchantInfo.getMerName();//结算卡姓名，银行卡持卡人姓名
		String cardMobile = merchantInfo.getPhone();//手机
		String cardCertNo = merchantInfo.getIdCard();//身份证
		String unionNo = merchantInfo.getBankNo();//联行号
		String cardCvv = "123";//不知道此字段是什么？
		//String cardExpireDate = "0519";//有效期
		String bankName = merchantInfo.getBankName();//银行名称
		String returnUrl = "";//前台通知地址
		Map<String, String> reqBusiMap = new HashMap<String, String>();
		reqBusiMap.put("orderNo", orderNo);
		reqBusiMap.put("amount", amount);
		reqBusiMap.put("subject", subject);
		reqBusiMap.put("body", body);
		reqBusiMap.put("clientIp", clientIp);
		reqBusiMap.put("notifyUrl", notifyUrl);
		reqBusiMap.put("cardNo", cardNo);
		reqBusiMap.put("cardName", cardName);
		reqBusiMap.put("cardMobile", cardMobile);
		reqBusiMap.put("cardCertNo", cardCertNo);
		reqBusiMap.put("unionNo", unionNo);
		reqBusiMap.put("cardCvv", cardCvv);
		//reqBusiMap.put("cardExpireDate", cardExpireDate);
		reqBusiMap.put("bankName", bankName);
		reqBusiMap.put("returnUrl", returnUrl);
		String md5Key = "6240900e7f2e4bdbb81f08a695c19234";
		try {
			Commons.sign_encode(reqBusiMap, reqMap, md5Key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String url = "https://scp-getway.9fbank.com/scp/pay";//请求地址
		HttpMsg httpMsg = HttpUtil.postJson(url, reqMap);
		try {
			Commons.validate_decode(httpMsg, md5Key);
			System.out.println("是否验签通过："+httpMsg.isVerify());
			System.out.println("返回消息："+httpMsg.getResBusiMsg());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testPay2(){
		SyfQuickPayDao merchantDao = new SyfQuickPayDao();
		
		String saruLruid = "6000002332";// 刷卡商户的ID
		String merId = "mi118021410542700009";// 报户生成的商户标识
		String cardNoFromApp = "6214830125502616";// 交易卡号
		String orderAmt = "10000";// 订单金额，单位:分
		String payType = "freecardcredit";//支付类型，无卡支付
		if (StringUtils.isEmpty(cardNoFromApp) || StringUtils.isEmpty(merId)) {
			logger.info("T0交易接口传入的交易卡号或者商户标识为空,程序结束");
			return;
		}
		PropertiesUtil util = new PropertiesUtil(SaruInfoDao.class.getResource(
				"/merchantInfo.properties").getFile());
		String prefix_wk = util.getValue("prefix_wk");
		Integer id = SaruInfoDao.getSequence("SEQ_ORDER_NO");
		String orderId = "";
		orderId = OrderNumUtil.createOrderNum(prefix_wk, id);// 交易订单号
		
/*		MerReportModel merReport = merchantDao.getMerReport(saruLruid);
		merId = merReport.getMerId();//商户号
		String md5Key = merReport.getKey();//秘钥
*/		
		String md5Key = "6240900e7f2e4bdbb81f08a695c19234";
		/** 准备请求参数*/
		//公共字段
		String source = "M";//M-商户发起
		String version = "V1";//
		String cipherType = "AES";//加密类型
		String signType = "MD5";//签名方式
		String transCode = "T102";//交易编码
		String partnerId = SyfConstant.partnerId;//代理商ID
		String merchId = "";//商户号
		String accessToken = orderId;//请求流水号
		String ciphertext = "";//请求密文
		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("source", source);
		reqMap.put("version", version);
		reqMap.put("cipherType", cipherType);
		reqMap.put("signType", signType);
		reqMap.put("transCode", transCode);
		reqMap.put("partnerId", partnerId);
		reqMap.put("merchId", merchId);
		reqMap.put("accessToken", accessToken);
		reqMap.put("reqReserved", "");
		//密文字段
		String orderNo = orderId;//商户订单号
		String amount = String.format("%.2f",Double.parseDouble(orderAmt)/100d);//金额
		String subject = "线上支付";//商品主题
		String body = "线上支付";//商品内容
		String clientIp = SyfConstant.clientIp;//发起请求客户端的ip
		String notifyUrl = SyfConstant.notifyUrl;//异步通知地址
		String cardNo = cardNoFromApp;//卡号

		MerchantEnterModel merchantInfo = merchantDao.queryMerchantBysaruId(saruLruid);
		String cardName = merchantInfo.getMerName();//结算卡姓名，银行卡持卡人姓名
		String cardMobile = merchantInfo.getPhone();//手机
		String cardCertNo = merchantInfo.getIdCard();//身份证
		String unionNo = merchantInfo.getBankNo();//联行号

		String cardCvv = "123";//不知道此字段是什么？
		String cardExpireDate = "0519";//有效期
		String bankName = merchantInfo.getBankName();//银行名称
		String returnUrl = "";//前台通知地址
		Map<String, String> reqBusiMap = new HashMap<String, String>();
		reqBusiMap.put("orderNo", orderNo);
		reqBusiMap.put("amount", amount);
		reqBusiMap.put("subject", subject);
		reqBusiMap.put("body", body);
		reqBusiMap.put("clientIp", clientIp);
		reqBusiMap.put("notifyUrl", notifyUrl);
		reqBusiMap.put("cardNo", cardNo);
		reqBusiMap.put("cardName", cardName);
		reqBusiMap.put("cardMobile", cardMobile);
		reqBusiMap.put("cardCertNo", cardCertNo);
		reqBusiMap.put("unionNo", unionNo);
		reqBusiMap.put("cardCvv", cardCvv);
		reqBusiMap.put("cardExpireDate", cardExpireDate);
		reqBusiMap.put("bankName", bankName);
		reqBusiMap.put("returnUrl", returnUrl);
		
		try {
			Commons.sign_encode(reqBusiMap, reqMap, md5Key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String url = SyfConstant.kj_url;//请求地址
		HttpMsg httpMsg = HttpUtil.postJson(url, reqMap);
		try {
			Commons.validate_decode(httpMsg, md5Key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(httpMsg.isVerify() ) {
			System.out.println("验签成功");
			System.out.println(httpMsg.getResBusiMsg());
		}
	}
	
	/**
	 * 测试查单
	 */
	public static void testQuery(){
		//公共字段
				String source = "M";//M-商户发起
				String version = "V1";//
				String cipherType = "AES";//加密类型
				String signType = "MD5";//签名方式
				String transCode = "Q101";//交易编码
				String partnerId = "ORG_1505714517351";//代理商ID
				String merchId = "m1801140001";//商户号
				String accessToken = System.currentTimeMillis()+"";//请求流水号
				String ciphertext = "";//请求密文
				Map<String, String> reqMap = new HashMap<String, String>();
				reqMap.put("source", source);
				reqMap.put("version", version);
				reqMap.put("cipherType", cipherType);
				reqMap.put("signType", signType);
				reqMap.put("transCode", transCode);
				reqMap.put("partnerId", partnerId);
				reqMap.put("merchId", merchId);
				reqMap.put("accessToken", accessToken);
				reqMap.put("reqReserved", "");
				//密文字段
				String orderNo = "S118011711242600000100000";//商户订单号
				Map<String, String> reqBusiMap = new HashMap<String, String>();
				reqBusiMap.put("orderNo", orderNo);
				String md5Key = "df9e9c0e38b74d2a9c447693091dd04b";
				try {
					Commons.sign_encode(reqBusiMap, reqMap, md5Key);
				} catch (Exception e) {
					e.printStackTrace();
				}
				String url = SyfConstant.kj_url;
				HttpMsg httpMsg = HttpUtil.postJson(url, reqMap);
				try {
					Commons.validate_decode(httpMsg, md5Key);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(httpMsg.isVerify()){  //验签通过
					JSONObject returnMap = JSONObject.parseObject(httpMsg.getResBusiMsg());
					HashMap<String, String> map2app = new HashMap<String,String>();
					
				}
				System.out.println("返回结果："+httpMsg.getResBusiMsg());
				System.out.println("验签结果："+httpMsg.isVerify());
	}
	
	/**
	 * 测试异步通知
	 * @throws Exception 
	 */
	public static void testNotify() throws Exception{
		String key = "8BB418FCA8A480BC3E00365AE14148A2";
		String response = "";
		response ="{\"signType\":\"MD5\",\"partnerId\":\"V1\",\"ciphertext\":\"/trgfjMUpWqOdX9k0GgnWnXbt0gHx68xoqwfALdIdV0owYA8omXc+FH6ntADpJB6jTlqbZmKRTAgEOjKrpN1HJNs84NRzPE5ValtUMiLv6se6E3yl3QaAH8BvEY+gdD1QOcUILxG7zS/QlBYg4kcjxN9i+aKQ9ceuOX/WyWYlz/Q9T/yqsQX/11L6b4aFVJmGn1dJLgvdSyT/5Rb/Kvi5KUB/mgcYIuZbIlwG70h5J4MJfHZMEpaWeC091ojZUJ0lboqUBDwdVcpq8yFCHlDrknCoUXkMQ2/dyEQR7co/oFgOqs6HwvoFmFnI2KTbB4x\",\"exTxnId\":null,\"resMsg\":\"支付成功\",\"platOrderNo\":\"de180117112430bgZvNoQ\",\"version\":\"V1\",\"payOrderNo\":\"IG18CCA7IICJ9V00004\",\"amount\":\"100.00\",\"cipherType\":\"AES\",\"signInfo\":\"2cab2e5529e9fb5eb8923eb4eb9c2f82\",\"source\":\"S\",\"resCode\":\"0000\",\"receiveTime\":\"20180117112430\",\"serviceCode\":\"10003\",\"merOrderNo\":\"S118011711242600000100000\",\"completeTime\":\"20180117112644\",\"merchId\":\"m1801140001\"}";

		JSONObject resJson = JSONObject.parseObject(response);
		//验签
		String ciphertext;
		String signData;
		String sign;
		ciphertext = resJson.getString("ciphertext");
		signData = "ciphertext=" + ciphertext + "&key=" + key;
		sign = Md5Util.string2MD5(signData);
		if(!sign.equals(resJson.getString("signInfo"))){  //验签失败
			System.out.println("闪云付异步通知验签失败");
			return;
		}
		String merId = "";
		merId = resJson.getString("merOrderNo");
		if("".equals(merId)){
			return;
		}

		SaruInfoDao saruInfo = new SaruInfoDao();
		SyfQuickPayDao merchantDao = new SyfQuickPayDao();
		if(resJson.containsKey("resCode")&&"0000".equals(resJson.getString("resCode"))){ //标志成功
			System.out.println("订单成功");
		}
	}
	
	/**
	 * 商户报户测试
	 */
	public static void merRegisteTest(){
		/** 请求参数如下*/
		String source = "A";//A-代理商发起、M-商户发起
		String version = "V1";//版本号
		String cipherType = "AES";//加密类型
		String partnerId = "ORG_1517213955772";//代理商id
		String accessToken = new Date().getTime()+"";//每次访问的唯一标示
		String signType = "MD5";//RSA或MD5
		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("source", source);
		reqMap.put("version", version);
		reqMap.put("cipherType", cipherType);
		reqMap.put("signType", signType);
		reqMap.put("partnerId", partnerId);
		reqMap.put("accessToken", accessToken);
		reqMap.put("reqReserved", "");
		//获取密文字段
		String outerMerchId = "123456";//代理商端商户号
		String merchEmail = "tianbiao@scloudpay.com";//商户邮箱
		String merchName = "甜甜圈test1";//商户名称
		String corpName = "甜甜圈test2";//公司名称
		String merchClassify = "10";//商户类型，10个人，20个体工商，30企业
		String merchAddress = "商户地址";//商户地址
		String corporationId = "532501100006302";//营业执照号
		String receiveSmsPhone = "133";//接受通知手机号
		String merchantCityCode = "4610";//城市编码
		String merchantProvinceCode = "370";//省编码
		String businessCode = "210";//微信行业类目编码
		String businessCodeZFB = "2015050700000010";//支付宝行业类目编码
		String cmNotifyUrl = "https://gray-scp-getway.9fbank.com//test/testMerNotify";//商户审核通过后会向该地址发送通知，详情请参照商户入驻回调接口
		String idNumber = "130182199310022533";//身份证号
		String acctNo = "6222020409031145882";//银行卡号
		String accountType = "2";//结算类型，1：对公，2：对私
		String contactorNm = "刘先生";//结算人姓名
		String contactorCell = "18330564498";//结算人手机
		String contactorMail = "123@qq.com";//联系人邮箱
		String acctBankname = "中国银行";//银行名称
		String bankNo = "308100005192";//支行号
		String t0drawFee = "1.00";//t0提现手续费
		String t0tradeRate = "0.0040";//t0交易费率
		String t1drawFee = "1.00";//t1提现手续费
		String t1tradeRate = "0.0040";//t1交易费率
		String balanceType = "SMZF_ZHHQ_HD_T1";//结算类型，SMZF_ZHHQ_HD_T0、SMZF_ZHHQ_HD_T1
		String tradeFeeMax = "20.01";//交易封顶费
		
		Map<String, String> reqBusiMap = new HashMap<String, String>();
		reqBusiMap.put("outerMerchId", outerMerchId);
		reqBusiMap.put("merchEmail", merchEmail);
		reqBusiMap.put("merchName", merchName);
		reqBusiMap.put("corpName", corpName);
		reqBusiMap.put("merchClassify", merchClassify);
		reqBusiMap.put("merchAddress", merchAddress);
		reqBusiMap.put("corporationId", corporationId);
		reqBusiMap.put("receiveSmsPhone", receiveSmsPhone);
		reqBusiMap.put("merchantCityCode", merchantCityCode);
		reqBusiMap.put("merchantProvinceCode", merchantProvinceCode);
		reqBusiMap.put("businessCode", businessCode);
		reqBusiMap.put("businessCodeZFB", businessCodeZFB);
		reqBusiMap.put("cmNotifyUrl", cmNotifyUrl);
		reqBusiMap.put("idNumber", idNumber);
		reqBusiMap.put("acctNo", acctNo);
		reqBusiMap.put("accountType", accountType);
		reqBusiMap.put("contactorNm", contactorNm);
		reqBusiMap.put("contactorCell", contactorCell);
		reqBusiMap.put("contactorMail", contactorMail);
		reqBusiMap.put("acctBankname", acctBankname);
		reqBusiMap.put("bankNo", bankNo);
		reqBusiMap.put("t0drawFee", t0drawFee);
		reqBusiMap.put("t0tradeRate", t0tradeRate);
		reqBusiMap.put("t1drawFee", t1drawFee);
		reqBusiMap.put("t1tradeRate", t1tradeRate);
		reqBusiMap.put("balanceType", balanceType);
		reqBusiMap.put("tradeFeeMax", tradeFeeMax);
		
		String md5Key = "378a60ef7b1d4ec592e31a087fcf2e1a";
		try {
			Commons.sign_encode(reqBusiMap, reqMap, md5Key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String url = SyfConstant.kj_url;
		url = "https://gray-scp-getway.9fbank.com/merchant/createMerchantInfo";//测试请求地址
		HttpMsg httpMsg = HttpUtil.postJson(url, reqMap);
		try {
			Commons.validate_decode(httpMsg, md5Key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("返回结果："+httpMsg.getResBusiMsg());
		System.out.println("验签结果："+httpMsg.isVerify());
		
	}
}
