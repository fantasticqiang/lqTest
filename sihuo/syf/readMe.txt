文档说明：
1.本目录及以下是闪云付快捷
2.目录介绍：
	syf-constant-SyfConstant.java             (此快捷用到的常量定义，例如：下单地址url)
	   -dao-SyfQuickPayDao.java               (业务相关的数据库操作)
	   -servlet-SyfQuickPayServlet.java       (和app交互的servlet)
	   -test                                  (测试相关)
	   -util                                  (上游渠道提供的工具类，例如：http post,encode,md5,aes)
	   

#######################

对外app提供了三个方法：

1.SyfQuickPayServlet（闪云付下单）
接收四个参数：		
		// 从前台获取交易参数
		String saruLruid = request.getParameter("saruLruid");// 刷卡商户的ID
		String merId = request.getParameter("merId");// 报户生成的商户标识
		String cardNoFromApp = request.getParameter("cardNo");// 交易卡号
		String orderAmt = request.getParameter("orderAmt");// 订单金额，单位:分
返回参数（json格式）：
		map2app.put("orderNo", orderId);//下单订单号
		map2app.put("resCode", "000");//订单状态
		map2app.put("resMsg", "下单成功！");//消息说明
		map2app.put("resPage", payPage);//用此页面点确定，完成支付
		
2.SyfQuickPayQueryServlet（闪云付查单）
接收参数：
		String orderId = request.getParameter("orderId");// 订单号
返回参数：
		resultMap.put("retCode", "000");//返回码
		resultMap.put("retMsg", "支付成功");//返回码说明
		只有这种状态的时候表示，订单成功

3.SyfQuickPayMerRegisteServlet（闪云付商户入驻）
接受参数：
		String saruLruid = request.getParameter("saruLruid");// 商户号
		String reservedPhone = request.getParameter("reservedPhone"); // 结算卡绑定的手机号
		String idCard = request.getParameter("idCard"); // 身份证号
		String provinceArgs =  request.getParameter("province");; // 省份
		String cityArgs =  request.getParameter("city"); // 城市
返回参数：
		resultMap.put("retCode", resCode);//返回码
		resultMap.put("retMsg", retMsg);//返回信息
		resultMap.put("merId", merId);//返回的商户号
		请注意：只需要看retCode这个字段，如果为"000"表示，商户入驻成功
#########################

web.xml添加配置：

	<!-- ==================云闪付配置开始========================= -->
	<servlet>
		<servlet-name>SyfQuickPayNotifyServlet</servlet-name>
		<servlet-class>com.syf.servlet.SyfQuickPayNotifyServlet</servlet-class>
	</servlet>
	<servlet-mapping>
		<servlet-name>SyfQuickPayNotifyServlet</servlet-name>
		<url-pattern>/SyfQuickPayNotifyServlet</url-pattern>
	</servlet-mapping>
	<servlet>
		<servlet-name>SyfQuickPayQueryServlet</servlet-name>
		<servlet-class>com.syf.servlet.SyfQuickPayQueryServlet</servlet-class>
	</servlet>
	<servlet-mapping>
		<servlet-name>SyfQuickPayQueryServlet</servlet-name>
		<url-pattern>/SyfQuickPayQueryServlet</url-pattern>
	</servlet-mapping>
	<servlet>
		<servlet-name>SyfQuickPayServlet</servlet-name>
		<servlet-class>com.syf.servlet.SyfQuickPayServlet</servlet-class>
	</servlet>
	<servlet-mapping>
		<servlet-name>SyfQuickPayServlet</servlet-name>
		<url-pattern>/SyfQuickPayServlet</url-pattern>
	</servlet-mapping>
	<servlet>
		<servlet-name>SyfQuickPayMerRegisteServlet</servlet-name>
		<servlet-class>com.syf.servlet.SyfQuickPayMerRegisteServlet</servlet-class>
	</servlet>
	<servlet-mapping>
		<servlet-name>SyfQuickPayMerRegisteServlet</servlet-name>
		<url-pattern>/SyfQuickPayMerRegisteServlet</url-pattern>
	</servlet-mapping>
	
	<servlet>
		<servlet-name>SyfQuickPayMerRegisterNotifyServlet</servlet-name>
		<servlet-class>com.syf.servlet.SyfQuickPayMerRegisterNotifyServlet</servlet-class>
	</servlet>
	<servlet-mapping>
		<servlet-name>SyfQuickPayMerRegisterNotifyServlet</servlet-name>
		<url-pattern>/SyfQuickPayMerRegisterNotifyServlet</url-pattern>
	</servlet-mapping>
	<!-- ==================云闪付配置结束========================= -->
	
	
	##################
营业执照号：91110105MA00D0DE5B
