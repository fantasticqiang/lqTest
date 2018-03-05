/**
 * 文件名： U.java
 * 建立时间： 2012-3-1 下午04:03:55
 * 创建人： SongCheng
 */
package com.channel.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.map.ObjectMapper;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * 常用方法工具类
 *
 * @author SongCheng
 */
public class U {
	public static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(U.class);

	/** 计数器，避免名称重复 */
	public static final AtomicLong COUNTER = new AtomicLong();

	public static final ObjectMapper JSON_objectMapper = new ObjectMapper();

	/**
	 * 根据指定类型和属性参数新建类
	 * @param cls	要新建的类型 
	 * @param args BeanPorperty 属性列表
	 * @return
	 */
	public static <T> T createWithProperties(Class<T> cls, Object... args) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			for (int i = 0; i < args.length; i++) {
				String key = (String) args[i];
				Object val = args[++i];
				map.put(key, val);
			}
			T t = cls.newInstance();
			BeanUtils.populate(t, map);
			return t;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}//method

	public static Map<String, String> convertStringToMapBySeparator(String s, String separator) {
		Map<String, String> map = new HashMap<String, String>();
		String[] strs = s.split(separator);
		for (String string : strs) {
			map.put(string, string);
		}
		return map;
	}

	/**
	 * 从源对象复制指定属性到目标对象，主要用于相同类型对象之间的数值复制。<br>
	 * 不同类型请使用 {@link #populateProperty(Object, Object, String...)}
	 * @param src	源对象
	 * @param dest	目标对象
	 * @param pros	指定属性列表
	 */
	public static void copyProperties(Object src, Object dest, String... pros) {
		PropertyUtilsBean pub = new PropertyUtilsBean();
		for (String pro : pros) {
			try {
				Object val = pub.getProperty(src, pro);
				pub.setProperty(dest, pro, val);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}//for
	}//method

	/**
	 * 生成多行类描述字串
	 */
	public static String build2StringMultiLineStyle(Object obj) {
		String ts = null;
		ts = ToStringBuilder.reflectionToString(obj, ToStringStyle.MULTI_LINE_STYLE);
		return ts;
	}

	/**
	 * 生成单行类描述字串
	 */
	public static String build2StringShortStyle(Object obj) {
		String ts = null;
		ts = ToStringBuilder.reflectionToString(obj, ToStringStyle.SHORT_PREFIX_STYLE);
		return ts;
	}

	/**
	 * 根据指定属性，生成单行类描述字串
	 */
	public static String build2StringShortStyle(Object obj, String... pnames) {
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (String name : pnames) {
			try {
				String val = BeanUtils.getProperty(obj, name);
				map.put(name, val);
			} catch (Exception e) {
			}
		}//for
		Class<?> cls = obj.getClass();
		return cls.getSimpleName() + map.toString();
	}

	public static void touchProperty(Object obj, String propertyPath) {
		if (obj instanceof Collection<?>) {
			Collection<?> coll = (Collection<?>) obj;
			for (Object it : coll)
				touchProperty(it, propertyPath);
		} else if (obj instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) obj;
			touchProperty(map.values(), propertyPath);
		} else {
			try {
				BeanUtils.getProperty(obj, propertyPath);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}//if
	}//method

	/**
	 * 重新解码客户端字符串
	 */
	public static String decodeISO88591ToGBK(String str) {
		try {
			byte[] bs = str.getBytes("iso-8859-1");
			String nn = new String(bs, "gbk");
			return nn;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}//method

	/**
	 * 去除对象所有非空String属性对象，两端的空白字符。
	 * @param bean
	 */
	public static void trim(Object bean) {
		try {
			Map<String, String> map = BeanUtils.describe(bean);
			for (String name : map.keySet()) {
				if (String.class != PropertyUtils.getPropertyType(bean, name))
					continue;

				String val = (String) PropertyUtils.getProperty(bean, name);
				if (StringUtils.isBlank(val))
					continue;

				val = val.trim();
				PropertyUtils.setProperty(bean, name, val);
			}//for
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public static BeanUtilsBean createBeanUtilsBean() {
		BeanUtilsBean bu = BeanUtilsBean.getInstance();
		ConvertUtilsBean convu = bu.getConvertUtils();

		//日期转换器
		DateConverter dateConv = new DateConverter(null);
		dateConv.setPatterns(new String[] { "yyyy/MM/dd", "yyyy-MM-dd", "yyyy/MM/dd HH:mm:ss" });
		convu.register(dateConv, Date.class);
		return bu;
	}

	/**
	 * 主要用于将不同类型对象，相同属性名但不同类型的数据复制，如：前台传来的String类型到其他类型的转换。<br>
	 * 将 TDO类中的数据转存到实体类中<br>
	 * 注意：String 数值可能因无法转换而是用 NULL 值，如：<br>
	 * <ul>
	 * 		<li>"999999999999999" 到 int 型，无法转换设成NULL</li>
	 * 		<li>NULL 到 Date 型，无法转换设成NULL</li>
	 * 		<li>"abc无效日期" 到 Date 型，无法转换设成NULL</li>
	 * </ul>
	 * 
	 * @param src TDO类，其属性全是其对应实体类的String类型数据
	 * @param dest 目标实体类
	 * @param pros 要装配的属性名称列表，为空时，装配全部属性
	 */
	public static void populateProperty(Object src, Object dest, String... props) {
		try {
			BeanUtilsBean bu = createBeanUtilsBean();
			Map<String, String> map = bu.describe(src);
			Collection<String> names = null;

			if (props == null || props.length == 0) {
				names = map.keySet();
			} else {
				names = Arrays.asList(props);
			}

			for (String name : names) {
				Object val = map.get(name);
				bu.setProperty(dest, name, val);
			}//for

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}//method

	/**
	 * 组建Map方便类
	 * @param vals 必须由 [String,Object,String,Object ... String,Object] 类型的数据组成
	 * @return 返回 {@link HashMap}
	 */
	public static Map<String, Object> asMap(Object... vals) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (vals.length == 0) {
			return map;
		}

		for (int i = 0; i < vals.length; i++) {
			Object k = vals[i];
			Object v = vals[++i];
			map.put(k == null ? null : k.toString(), v);
		}
		return map;
	}

	public static <T> T[] toArray(List<T> lst) {
		Object[] arr = lst.toArray();
		return (T[]) arr;
	}

	public static <T> List<T> asList(T... vals) {
		return Arrays.asList(vals);
	}

	/**
	 * 连接集合中的元素成字符串
	 * @param src
	 * @param separator 元素间分隔符
	 * @param wraper 元素两边的包装字符，如：[wraper]String[wraper]
	 * @return 返回形如：[wraper]String[wraper][separator][wraper]String[wraper][separator]...[separator][wraper]String[wraper]
	 */
	public static String join(List<?> src, String separator, String wraper) {
		wraper = wraper == null ? "" : wraper;
		separator = separator == null ? "" : separator;

		StringBuilder out = new StringBuilder();
		int len = src.size();
		for (int i = 0; i < len; i++) {
			Object obj = src.get(i);
			out.append(wraper);
			out.append(obj == null ? "" : obj.toString());
			out.append(wraper);
			if (i + 1 < len)
				out.append(separator);
		}
		return out.toString();
	}

	public static boolean out(Object msg) {
		String val = null;
		if (msg == null) {
			val = "";
		} else {
			val = msg.toString();
		}
		System.out.println(val);
		return true;
	}

	public static String nvl(String str) {
		return nvl(str, "");
	}//method

	public static String nvl(Long val) {
		return val == null ? "" : val + "";
	}//method

	public static String nvl(Integer val) {
		return val == null ? "" : val + "";
	}//method

	public static String nvl(String str, String def) {
		if (StringUtils.isBlank(str)) {
			return def;
		}
		return str.trim();
	}//method

	/** 用于拼装“|”分割的文本文件 */
	public static String nvl_txt(Number val) {
		return val == null ? "" : val + "";
	}//method

	/** 用于拼装“|”分割的文本文件 */
	public static String nvl_txt(Object val) {
		String empty = " ";
		if (val == null)
			return empty;

		String s = val.toString();
		if (s == null)
			return empty;
		s = s.trim();
		if (s.length() == 0)
			return empty;

		return s;
	}//method

	public static String formatXml(String xml) {
		try {
			return formatXml(DocumentHelper.parseText(xml));
		} catch (Exception e) {
			LOG.error(e, e);
			throw new RuntimeException(e);
		}
	}//method

	public static String formatXml(Document doc) throws IOException {
		OutputFormat format = OutputFormat.createPrettyPrint();
		//format.setEncoding("GBK");
		StringWriter out = new StringWriter();
		XMLWriter writer = new XMLWriter(out, format);
		writer.write(doc);
		writer.close();
		return out.toString();
	}//method

	/**
	 * 解析 XML 字符串为 Document 对象
	 */
	public static Document parseXml(String xml) {
		Document doc = null;
		if (StringUtils.isBlank(xml))
			return doc;

		try {
			doc = DocumentHelper.parseText(xml);
		} catch (DocumentException e) {
			throw new RuntimeException(e);
		}
		return doc;
	}//method

	/**
	 * 从Map集合中去除 keys 序列中不包括的键
	 * @param source
	 * @param keys
	 * @return
	 */
	public static Map<String, Object> removeNotContainKey(Map source, String... keys) {
		Map<String, Object> tar = new HashMap<String, Object>();
		for (String key : keys) {
			tar.put(key, source.get(key));
		}

		return tar;
	}//method

	public static Timestamp currentTimestamp() {
		return new Timestamp(System.currentTimeMillis());
	}//method

	/**
	 * 打印错误堆栈信息
	 */
	public static String printStackTrace(Exception ex) {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		ex.printStackTrace(out);
		out.flush();
		return writer.toString();
	}//method

	/**
	 * 将对象转换为Map结构
	 */
	public static Map<String, String> convertBeanToMap(Object obj) {
		Map<String, String> map = Collections.EMPTY_MAP;
		try {
			map = BeanUtils.describe(obj);
		} catch (Exception e) {
			LOG.error(e, e);
		}
		return map;
	}//method

	/**
	 * 将给定对象转换为 JSON 格式的字符串
	 */
	public static String toJsonString(Object target) {
		String json = null;
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			JSON_objectMapper.setDateFormat(dateFormat);
			json = JSON_objectMapper.writeValueAsString(target);
		} catch (Exception e) {
			LOG.error(e, e);
			throw new RuntimeException(e);
		}
		return json;
	}//method

	/**
	 * 对银行卡号进行掩码操作，生成格式如：**********XXXXXX
	 */
	public static String maskBankAcctNo(String acct) {
		String prefix = "**********";
		if (StringUtils.isBlank(acct)) {
			return prefix;
		}

		//看见长度
		int len = 5;
		acct = acct.trim();
		if (acct.length() <= len) {
			return prefix;
		}

		String str = prefix + acct.substring(acct.length() - len);

		return str;
	}//method

	public static Timestamp timestamp(Date date) {
		return new Timestamp(date.getTime());
	}//method

	public static java.sql.Date currentDate() {
		return new java.sql.Date(System.currentTimeMillis());
	}

	/**
	 * 将请求参数构建成 XML 报文格式
	 */
	public static String httpReuquestMapToXml(Map<String, ?> parametersMap) {
		/*
		<parameters>
			<version>v1.0</version>
			<corpId>ddd</corpId>
		</parameters>
		 */

		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding(S.ENCODING_utf8);
		Element root = doc.addElement("parameters");

		for (String key : parametersMap.keySet()) {
			Object val = parametersMap.get(key);
			if (val == null)
				continue;

			Collection<String> vals = null;

			if (val instanceof String[]) {
				vals = U.asList((String[]) val);
			} else if (val instanceof Collection) {
				vals = (Collection<String>) val;
			} else {
				vals = U.asList(val.toString());
			}

			for (String it : vals)
				root.addElement(key).setText(it);
		}//for

		return doc.asXML();
	}//method

	public static String memoryUsage() {
		int k = 1024;
		Runtime rt = Runtime.getRuntime();
		StringBuilder out = new StringBuilder();
		out.append("usage[ ").append((rt.totalMemory() - rt.freeMemory()) / k).append(" ]\b");
		out.append("free[ ").append(rt.freeMemory() / k).append(" ]\b");
		out.append("total[ ").append(rt.totalMemory() / k).append(" ]\b");
		out.append("max[ ").append(rt.maxMemory() / k).append(" ]\b");

		return out.toString();
	}//method

	public static String createUUID() {
		UUID id = UUID.randomUUID();
		return id.toString().replace("-", "");
	}//method

	/**
	 * 安照GBK编码截取计算字符串实际的字节长度，主要用于存储如数据库时，限定字符长度使用。<br>
	 * varchar2(2000)的字段，只能存储1000个中文字符
	 * @param src 原始字符串
	 * @param len 指定的长度
	 * @return 
	 */
	public static String substringByByte(String src, final int len) {
		if (src.length() < (len / 2))
			return src;

		try {
			byte[] bs = src.getBytes(S.ENCODING_gbk);

			//字符长度小于给定长度
			if (bs.length <= len) {
				return src;
			}

			//如果没有双字节字
			if (bs.length == src.length()) {
				return src.substring(0, len);
			}

			//处理双字节情况
			StringBuilder sb = new StringBuilder();
			int size = 0;
			int cnt = 0;
			for (Character ch : src.toCharArray()) {
				cnt = Character.toString(ch).getBytes(S.ENCODING_gbk).length;
				size += cnt;
				if (size <= len) {
					sb.append(ch);
				}
			}
			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}//method

	/**
	 * 去除xml中的特殊字符
	 */
	public static String removeSpecialXMLChar(String src) {
		if (StringUtils.isBlank(src)) {
			return src;
		}//if

		String rs = src.replace("<![CDATA[", "{");
		rs = rs.replace("]]>", "}");
		rs = rs.replace(">", "》");
		rs = rs.replace("<", "《");
		rs = rs.replace("&", "$");
		rs = rs.replace("|", ":");
		return rs;
	}//method

	/**
	 * 得到string类型的系统当前时间
	 * 
	 * @return
	 */
	public static String getCurrentTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		String createTime = format.format(calendar.getTime());
		return createTime;
	}
	
	public static String getCurrentTimestamp() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar calendar = Calendar.getInstance();
		String createTime = format.format(calendar.getTime());
		return createTime;
	}

	/** 元；格式化 */
	private static final DecimalFormat FMT_YUAN = new DecimalFormat("#.00");
	/** 分；格式化 */
	private static final DecimalFormat FMT_CENT = new DecimalFormat("#");

	/**
	 * 钱：单位元（XX.XX）转变为 单位分（XXXX）
	 */
	public static BigDecimal money_yuan2cent(String yuan) {
		BigDecimal bd = StringUtils.isBlank(yuan) ? BigDecimal.ZERO : new BigDecimal(yuan);
		return money_yuan2cent(bd);
	}//method

	/**
	 * 钱：单位元（XX.XX）转变为 单位分（XXXX）
	 */
	public static BigDecimal money_yuan2cent(BigDecimal yuan) {
		BigDecimal n = yuan.multiply(S.BIG_DECIMAL_100);
		String cent = FMT_CENT.format(n.doubleValue());
		return new BigDecimal(cent);
	}//method

	/**
	 * 钱：单位分（XXXX）转变为 单位元（XX.XX）
	 */
	public static BigDecimal money_cent2yuan(String cent) {
		BigDecimal bd = StringUtils.isBlank(cent) ? BigDecimal.ZERO : new BigDecimal(cent);
		return money_cent2yuan(bd);
	}//method

	/**
	 * 钱：单位分（XXXX）转变为 单位元（XX.XX）
	 */
	public static BigDecimal money_cent2yuan(BigDecimal cent) {
		BigDecimal n = cent.divide(S.BIG_DECIMAL_100);
		String yuan = FMT_YUAN.format(n.doubleValue());
		return new BigDecimal(yuan);
	}//method

}//class