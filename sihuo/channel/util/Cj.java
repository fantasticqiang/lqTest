package com.channel.util;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * 畅捷参数定义类
 * @author SongCheng
 * 2015年9月17日下午4:15:15
 */
public class Cj {
	public static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Cj.class);

	/** CJ_PARAMETER 参数表键名 */
	public static final String CJPARAM_mertid;
	public static final String CJPARAM_businessCode;
	public static final String CJPARAM_cjGatewayUrl;
	public static final String CJPARAM_cjFtpPath;
	public static final String CJPARAM_cjFtpIp;
	public static final String CJPARAM_cjFtpPort;
	public static final String CJPARAM_cjFtpUsername;
	public static final String CJPARAM_cjFtpPassword;
	public static final String CJPARAM_submertid_huaxia;
	public static final String CJPARAM_ysPfxPath;
	public static final String CJPARAM_ysPfxPasswd;
	public static final String CJPARAM_cjCertPath;

	static {
		Map<String, String> g = S.INI.getGroup("crps-demo");
		
		CJPARAM_mertid = g.get("MERCHANT_ID");
		CJPARAM_submertid_huaxia = g.get("SUBMERTID_HUAXIA");
		CJPARAM_businessCode = g.get("BUSINESS_CODE");
		CJPARAM_cjGatewayUrl = g.get("CJ_GATEWAY_URL");
		CJPARAM_cjFtpPath = g.get("CJ_FTP_PATH");
		CJPARAM_cjFtpIp = g.get("CJ_FTP_IP");
		CJPARAM_cjFtpPort = g.get("CJ_FTP_PORT");
		CJPARAM_cjFtpUsername = g.get("CJ_FTP_USERNAME");
		CJPARAM_cjFtpPassword = g.get("CJ_FTP_PASSWORD");
		CJPARAM_ysPfxPath = g.get("YS_PFX_PATH");
		CJPARAM_ysPfxPasswd = g.get("YS_PFX_PASSWD");
		CJPARAM_cjCertPath = g.get("CJ_CERT_PATH");
	}//static

	public static final SimpleDateFormat FMT_yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss");
	public static final SimpleDateFormat FMT_MMddyyyy = new SimpleDateFormat("MM/dd/yyyy");

	/** CJ报文编码 */
	public static final String XMLMSG_TRANS_CODE_单笔实时收款 = "G10001";
	public static final String XMLMSG_TRANS_CODE_单笔实时付款 = "G10002";
	public static final String XMLMSG_TRANS_CODE_批量收款 = "G10003";
	public static final String XMLMSG_TRANS_CODE_批量付款 = "G10004";
	public static final String XMLMSG_TRANS_CODE_批量收款文件提交 = "G10005";
	public static final String XMLMSG_TRANS_CODE_批量付款文件提交 = "G10006";
	public static final String XMLMSG_TRANS_CODE_虚拟账户单笔付款 = "G10007";
	public static final String XMLMSG_TRANS_CODE_虚拟账户批量付款文件提交 = "G10008";
	public static final String XMLMSG_TRANS_CODE_虚拟账户充值 = "G10009";
	public static final String XMLMSG_TRANS_CODE_消费贷付款 = "G10010";
	public static final String XMLMSG_TRANS_CODE_消费贷收款 = "G10011";

	/** 查询类报文 G20000 */
	public static final String XMLMSG_TRANS_CODE_实时交易结果查询 = "G20001";
	public static final String XMLMSG_TRANS_CODE_批量交易结果查询 = "G20002";
	public static final String XMLMSG_TRANS_CODE_交易明细查询 = "G20004";
	public static final String XMLMSG_TRANS_CODE_批量文件提回 = "G20005";
	public static final String XMLMSG_TRANS_CODE_虚拟账户单笔付款結果查询 = "G20007";
	public static final String XMLMSG_TRANS_CODE_批量交易结果查询NC = "G20008";
	public static final String XMLMSG_TRANS_CODE_虚拟账户批量付款文件结果提回 = "G20009";

	/** 功能类报文 G30000 */
	public static final String XMLMSG_TRANS_CODE_虚拟账户余额查询 = "G30001";
	public static final String XMLMSG_TRANS_CODE_虚拟账户内部转账 = "G30002";
	public static final String XMLMSG_TRANS_CODE_订单刷卡通知 = "G30003";

	/** 上传下载类报文 G40000 */
	public static final String XMLMSG_TRANS_CODE_行名行号下载 = "G40001";
	public static final String XMLMSG_TRANS_CODE_电子回单下载 = "G40002";
	public static final String XMLMSG_TRANS_CODE_对账文件下载 = "G40003";
	public static final String XMLMSG_TRANS_CODE_特色对账文件下载 = "G40004";
	public static final String XMLMSG_TRANS_CODE_垫资还款文件上传 = "G40005";

	/** 其他类报文 G50000 */
	public static final String XMLMSG_TRANS_CODE_银行余额查询 = "G50001";
	public static final String XMLMSG_TRANS_CODE_银行明细查询 = "G50002";
	
	/** 协议认证类报文 G60000 */
	public static final String XMLMSG_TRANS_CODE_实名认证 = "G60001";
	public static final String XMLMSG_TRANS_CODE_实名认证结果查询 = "G60002";
	public static final String XMLMSG_TRANS_CODE_协议签约 = "G60003";
	public static final String XMLMSG_TRANS_CODE_协议签约结果查询 = "G60004";

	/** 版本 */
	public static final String XMLMSG_VERSION_01 = "01";
	/** 数据格式 */
	public static final String XMLMSG_DATATYPE_XML = "2";
	/** 优先级 */
	public static final String XMLMSG_LEVEL_5 = "5";

	/** 属性常量：返回码 */
	public static final String PROP_NAME_RET_CODE = "RET_CODE";
	/** 属性常量：返回描述 */
	public static final String PROP_NAME_ERR_MSG = "ERR_MSG";

	/** 产品代码 */
	public static final String XMLMSG_PRODUCT_CODE_对公实时付款 = "50010001";
	public static final String XMLMSG_PRODUCT_CODE_对公标准付款 = "50110001";
	public static final String XMLMSG_PRODUCT_CODE_对公定时付款 = "50220001";
	public static final String XMLMSG_PRODUCT_CODE_对私实时付款 = "50010002";
	public static final String XMLMSG_PRODUCT_CODE_对私标准付款 = "50110002";
	public static final String XMLMSG_PRODUCT_CODE_对私定时付款 = "50220012";
	public static final String XMLMSG_PRODUCT_CODE_工资网报标准付款 = "50120002";
	public static final String XMLMSG_PRODUCT_CODE_工资网报T_1付款 = "50220002";
	public static final String XMLMSG_PRODUCT_CODE_虚拟账户垫资付款 = "60000001";
	public static final String XMLMSG_PRODUCT_CODE_虚拟账户提现 = "60000002";
	public static final List<String> XMLMSG_PRODUCT_CODE_LIST = Arrays.asList(XMLMSG_PRODUCT_CODE_对公实时付款, XMLMSG_PRODUCT_CODE_对公标准付款,
			XMLMSG_PRODUCT_CODE_对公定时付款, XMLMSG_PRODUCT_CODE_对私实时付款, XMLMSG_PRODUCT_CODE_对私标准付款, XMLMSG_PRODUCT_CODE_对私定时付款, XMLMSG_PRODUCT_CODE_工资网报标准付款,
			XMLMSG_PRODUCT_CODE_工资网报T_1付款);

	/** 支付时效类型 */
	public static final String XMLMSG_PAY_TIMELINESS_加急实时 = "0";
	public static final String XMLMSG_PAY_TIMELINESS_普通T加0 = "5";
	public static final String XMLMSG_PAY_TIMELINESS_预约T加N = "9";
	public static final List<String> XMLMSG_PAY_TIMELINESS_LIST = Arrays.asList(XMLMSG_PAY_TIMELINESS_加急实时, XMLMSG_PAY_TIMELINESS_普通T加0,
			XMLMSG_PAY_TIMELINESS_预约T加N);

	/** 账户类型 */
	public static final String ACCOUNT_TYPE_借记卡 = "00";
	public static final String ACCOUNT_TYPE_存折 = "01";
	public static final String ACCOUNT_TYPE_信用卡 = "02";
	public static final List<String> ACCOUNT_TYPES = Arrays.asList(ACCOUNT_TYPE_借记卡, ACCOUNT_TYPE_存折, ACCOUNT_TYPE_信用卡);

	/** 账户对公、对私 */
	public static final String ACCOUNT_PROP_对私 = "0";
	public static final String ACCOUNT_PROP_对公 = "1";
	public static final List<String> ACCOUNT_PROPS = Arrays.asList(ACCOUNT_PROP_对私, ACCOUNT_PROP_对公);

	/** 币种 */
	public static final String CURRENCY_人民币 = "CNY";
	public static final String CURRENCY_港元 = "HKD";
	public static final String CURRENCY_美元 = "USD";

	/** 身份证件类型 */
	public static final String CERT_ID_TYPE_身份证 = "0";
	public static final String CERT_ID_TYPE_户口薄 = "1";
	public static final String CERT_ID_TYPE_护照 = "2";
	public static final String CERT_ID_TYPE_军官证 = "3";
	public static final String CERT_ID_TYPE_士兵证 = "4";
	public static final String CERT_ID_TYPE_港澳通行证 = "5";
	public static final String CERT_ID_TYPE_台湾通行证 = "6";
	public static final String CERT_ID_TYPE_临时身份证 = "7";
	public static final String CERT_ID_TYPE_外国居住证 = "8";
	public static final String CERT_ID_TYPE_警官证 = "9";
	public static final String CERT_ID_TYPE_其他 = "X";
	public static final List<String> CERT_ID_TYPES = Arrays.asList(CERT_ID_TYPE_身份证, CERT_ID_TYPE_户口薄, CERT_ID_TYPE_护照, CERT_ID_TYPE_军官证, CERT_ID_TYPE_士兵证,
			CERT_ID_TYPE_港澳通行证, CERT_ID_TYPE_台湾通行证, CERT_ID_TYPE_临时身份证, CERT_ID_TYPE_外国居住证, CERT_ID_TYPE_警官证, CERT_ID_TYPE_其他);

	/** 交易查询：指定状态 */
	public static final String QUERY_TRANS_STATUS_成功 = "0";
	public static final String QUERY_TRANS_STATUS_失败 = "1";
	public static final String QUERY_TRANS_STATUS_全部 = "2";
	public static final String QUERY_TRANS_STATUS_退票 = "3";
	public static final String QUERY_TRANS_STATUS_代付失败退款 = "4";
	public static final String QUERY_TRANS_STATUS_代付退票 = "5";
	public static final String QUERY_TRANS_STATUS_委托代扣 = "6";
	public static final List<String> QUERY_TRANS_STATUS = Arrays.asList(QUERY_TRANS_STATUS_成功, QUERY_TRANS_STATUS_失败, QUERY_TRANS_STATUS_全部);

	/** 交易查询：查询时间类型 */
	public static final String QUERY_TIME_TYPE_完成日期 = "0";
	/** 1.按提交日期，默认为1 */
	public static final String QUERY_TIME_TYPE_提交日期 = "1";

	/** 交易方向类型 */
	public static final String TRANS_DIR_TYPE_付 = "0";
	public static final String TRANS_DIR_TYPE_收 = "1";

	/** crps-ext 文件记录表，文件类型 */
	public static final int ExtBillFileType_每日特色交易对账文件 = 0;
	public static final int ExtBillFileType_每日垫资还款文件 = 1;

	/** 返回当前交易时间，格式：yyyyMMddHHmmss */
	public static String currTransDatetime_string() {
		return FMT_yyyyMMddHHmmss.format(new Date());
	}//method

	/** 返回当前交易时间，格式：yyyyMMdd */
	public static String currTransDate_string() {
		return currTransDatetime_string().substring(0, 8);
	}

	/** 返回当前交易时间，格式：HHmmss */
	public static String currTransTime_string() {
		return currTransDatetime_string().substring(8);
	}

	public static String formatDatetime_string(Date date) {
		return FMT_yyyyMMddHHmmss.format(date);
	}//method

	public static String formatDate_string(Date date) {
		return formatDatetime_string(date).substring(0, 8);
	}
	
	public static String formatMonthDate_string(Date date) {
		return formatDatetime_string(date).substring(0, 6);
	}
	
	public static Date parseDatetime(String str) {
		try {
			Date dd = FMT_yyyyMMddHHmmss.parse(str);
			return dd;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Date parseDate(String str) {
		try {
			Date dd = FMT_MMddyyyy.parse(str);
			return dd;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String formatXml_UTF8(Document doc) {
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();//OutputFormat用于输出xml时的格式控制
			format.setEncoding(S.ENCODING_utf8);//设置编码格式
			format.setIndent(false);//设置是否缩进
			format.setNewlines(false);//设置是否换行
			format.setNewLineAfterDeclaration(false);//声明后是否有换行
			/*
			format.setLineSeparator("\n");
			 */
			StringWriter out = new StringWriter();
			XMLWriter writer = new XMLWriter(out, format);
			writer.write(doc);
			writer.close();
			return out.toString();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}//method
	
	

}//class
