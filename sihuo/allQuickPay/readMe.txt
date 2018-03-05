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
	
	<!-- 总的 付款-->
	<servlet>
		<servlet-name>QuickPayServlet</servlet-name>
		<servlet-class>com.allQuickPay.servlet.QuickPayServlet</servlet-class>
	</servlet>
	<servlet-mapping>
		<servlet-name>QuickPayServlet</servlet-name>
		<url-pattern>/QuickPayServlet</url-pattern>
	</servlet-mapping> 
	
	<!-- 总的查询商户状态方法-->
	<servlet>
		<servlet-name>QuickPayQueryMerStatusServlet</servlet-name>
		<servlet-class>com.allQuickPay.servlet.QuickPayQueryMerStatusServlet</servlet-class>
	</servlet>
	<servlet-mapping>
		<servlet-name>QuickPayQueryMerStatusServlet</servlet-name>
		<url-pattern>/QuickPayQueryMerStatusServlet</url-pattern>
	</servlet-mapping> 
	
	<!-- 总的查询所有渠道的方法-->
	<servlet>
		<servlet-name>QuickPayQueryAllChannelServlet</servlet-name>
		<servlet-class>com.allQuickPay.servlet.QuickPayQueryAllChannelServlet</servlet-class>
	</servlet>
	<servlet-mapping>
		<servlet-name>QuickPayQueryAllChannelServlet</servlet-name>
		<url-pattern>/QuickPayQueryAllChannelServlet</url-pattern>
	</servlet-mapping> 
	
		2.说明
	
	(已作废：18.3.1)
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
	
	###查看商户的状态
	方法名：QuickPayQueryMerStatusServlet
	传入参数1：saruLruid （本系统商户id），必填
	返回参数：是json。
		resCode"0000"表示审核成功;"0001"审核中;"0002"审核失败;"0003"未开户
		resMsg表示审核的状态的描述
		data JSON对象中存放报户信息表的ID，商户在本系统标识ID：SARULRU_ID，上游下发的商户号MER_ID,商户状态MER_STATUS（商户状态，"I"处理中,"S"成功,"F"失败）
		
	###查看所有的渠道
	方法名：QuickPayQueryAllChannelServlet
	无传入参数
	返回json：
		resCode:"0000"表示请求成功
		resMsg : 请求结果的描述,只会返回"请求成功"
		data :各个渠道的json数组，
			CHANNEL_ID：渠道标识，
			CHANNEL_NAME：渠道名称，
			CHANNEL_TYPE：渠道类型，
			JF：是否有积分（1有积分，0无积分）
			JS_TYPE：结算类型（D0实时到账，T1第二天到账）
			DESCRIPTION：描述交易额度限制（如：100-9000元每笔、单日五万元）
			
			
	###样例
	
	###商户付款，beeCloud
	QuickPayServlet