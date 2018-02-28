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