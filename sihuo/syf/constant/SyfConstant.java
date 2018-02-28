package com.syf.constant;

public class SyfConstant {

	/** 
	 * 闪云付快捷下单请求地址
	 * 	测试：https://gray-scp-getway.9fbank.com/scp/pay
	 *  正式：https://scp-getway.9fbank.com/scp/pay
	 * */
	public static String kj_url  = "https://scp-getway.9fbank.com/scp/pay";
	/** 
	 * 闪云付快捷商户入驻请求地址
	 * 	测试：
	 *  正式：https://scp-getway.9fbank.com/merchant/createMerchantInfo
	 * */
	public static String merRegiste_url  = "https://scp-getway.9fbank.com/merchant/createMerchantInfo";
	/** 
	 * 代理商ID
	 *  测试：ORG_1505714517351
	 *  正式：ORG_1518153007521
	 * */
	public static String partnerId = "ORG_1518153007521";
	/** 
	 * 商户号
	 * 测试：m1801250001
	 * 正式：18611539891
	 * */
	public static String merchId = "ORG_1518153007521";
	/** 发起请求客户端的ip*/
	public static String clientIp = "47.104.91.166";
	/** 
	 * 异步通知地址
	 * 测试：
	 * 正式：
	 * */
	public static String notifyUrl = "http://"+clientIp+"/PhonePospInterface/SyfQuickPayNotifyServlet";
	/** 
	 * 异步通知地址
	 * 测试：
	 * 正式：
	 * */
	public static String merRegisteNotifyUrl = "http://"+clientIp+"/PhonePospInterface/SyfQuickPayMerRegisteServlet";
	/** md5Key
	 * 测试：9a9ce9b9302b4e2e9dd58156d59add4e
	 * 正式：f30b3e50d615481e990c6150ac7b41d0
	 * */
	public static String md5Key = "f30b3e50d615481e990c6150ac7b41d0";
	
	/**
	 * 提现手续费1.00元
	 */
	public static String sxf = "0.80";
}
