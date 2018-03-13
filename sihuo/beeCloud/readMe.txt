beeCloud快捷

流程介绍：
	1.用户注册，需要如下四个参数
			// 获取App前台参数
		String channelID = request.getParameter("channelID");
		String saruLruid = request.getParameter("saruLruid");// 商户号
		String reservedPhone = request.getParameter("reservedPhone"); // 结算卡绑定的手机号
		String idCard = request.getParameter("idCard"); // 身份证号
		String cardNo = request.getParameter("cardNo");//结算卡卡号
		
		返回：
		一、
			retCode = "0000";
			retMsg = "报户成功";
		二、	String retCode = "0001";
			String retMsg = "报户失败";
			
	2.商户付款
	请求参数：
			// 从前台获取交易参数
		String channelID = request.getParameter("channelID");
		String saruLruid = request.getParameter("saruLruid");// 刷卡商户的ID
		String id = request.getParameter("id");// 信用卡表的id
		String merId = request.getParameter("merId");// 报户生成的商户标识
		String cardNoFromApp = request.getParameter("cardNo");// 交易卡号
		String orderAmt = request.getParameter("orderAmt");// 订单金额，单位:分
	返回三个参数：
		resultMap.put("resCode", resCode);//返回码"0000"下单成功，用户手机号就可以接收到短信验证码；"0001"下单失败
		resultMap.put("retMsg", retMsg);//返回码描述
		resultMap.put("syid", syid);//上游下发的id，如果下单成功id就会有值，下单失败为空字符串
		
	返回json样例：
		{"resCode":"0000","retMsg":"下单成功","syid":"d559f497-a685-4541-8317-a723c04a92d5"}
		
	3.商户确认
	
		