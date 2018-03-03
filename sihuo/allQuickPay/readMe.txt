1.web.xml中新增该servlet
	<!-- ==========配置总的商户入驻servlet==================== -->
	<servlet>
		<servlet-name>QuickPayRegisteServlet</servlet-name>
		<servlet-class>com.allQuickPay.servlet.QuickPayRegisteServlet</servlet-class>
	</servlet>
	<servlet-mapping>
		<servlet-name>QuickPayRegisteServlet</servlet-name>
		<url-pattern>/QuickPayRegisteServlet</url-pattern>
	</servlet-mapping> 
	<!-- 总的 查单-->
	<servlet>
		<servlet-name>QuickPayQueryServlet</servlet-name>
		<servlet-class>com.allQuickPay.servlet.QuickPayQueryServlet</servlet-class>
	</servlet>
	<servlet-mapping>
		<servlet-name>QuickPayQueryServlet</servlet-name>
		<url-pattern>/QuickPayQueryServlet</url-pattern>
	</servlet-mapping> 
	<!-- 总的 手动修改订单状态-->
	<servlet>
		<servlet-name>QuickPayModifyMerStatusServlet</servlet-name>
		<servlet-class>com.allQuickPay.servlet.QuickPayModifyMerStatusServlet</servlet-class>
	</servlet>
	<servlet-mapping>
		<servlet-name>QuickPayModifyMerStatusServlet</servlet-name>
		<url-pattern>/QuickPayModifyMerStatusServlet</url-pattern>
	</servlet-mapping> 
	<!-- 总的 付款-->
	<servlet>
		<servlet-name>QuickPayServlet</servlet-name>
		<servlet-class>com.allQuickPay.servlet.QuickPayServlet</servlet-class>
	</servlet>
	<servlet-mapping>
		<servlet-name>QuickPayServlet</servlet-name>
		<url-pattern>/QuickPayServlet</url-pattern>
	</servlet-mapping> 
	
	2.说明
	
	###手动修改商户状态--
	方法名：QuickPayModifyMerStatusServlet
	传入参数：channelID   （渠道ID,闪云付渠道ID:SYF）
	传入参数：upMerID （上游分配的商户id，闪云付入驻商户后下发的id）
	传入参数：status （商户状态，"I"处理中,"S"成功,"F"失败）
	
	###商户入驻
	方法名：QuickPayRegisteServlet
	传入参数：跟之前一样，但是需要多传一个参数，channelID   （渠道ID,闪云付渠道ID:SYF）
	
	###商户付款
	方法名：QuickPayServlet
	传入参数：跟之前一样，但是需要多传一个参数，channelID   （渠道ID,闪云付渠道ID:SYF）
	
	###查看某笔订单状态
	方法名：QuickPayQueryServlet
	传入参数：跟之前一样，但是需要多传一个参数，channelID   （渠道ID,闪云付渠道ID:SYF）