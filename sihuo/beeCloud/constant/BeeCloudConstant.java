package com.beeCloud.constant;

public class BeeCloudConstant {

	/**
	 * 快捷签约地址
	 */
	public static String kj_url_registe = "https://api.beecloud.cn/2/rest/bill"; // 快捷签约地址
	
	/**
	 * 上游平台下发的唯一标识
	 * 测试：0950c062-5e41-44e3-8f52-f89d8cf2b6eb
	 */
	public static String app_id = "0950c062-5e41-44e3-8f52-f89d8cf2b6eb";
	
	/**
	 * 上游下发的密码
	 * 
	 */
	public static String app_secret = "";
	
	/**
	 * 交易订单号前缀
	 */
	public static String pre_fix = "BeeCloud";
	
	/** 
	 * 异步通知地址
	 * 测试：
	 * 正式：
	 * */
	public static String notif_yUrl = "http://47.104.91.166/PhonePospInterface/BeeCloudPayNotifyServlet";
	
	/**
	 * 手续费
	 */
	public static String sxf = "80";
	
}
