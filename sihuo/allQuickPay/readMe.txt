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