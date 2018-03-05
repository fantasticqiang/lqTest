/**
 * 文件名： InIFile.java
 * 建立时间： 2012-3-25 下午08:03:03
 * 创建人： SongCheng
 */
package com.channel.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * 解析读取 *.ini 格式的文档。
 * 指定文件路径时：以 “classpath:”开头的地址表示从类路径中查找文件，否则从文件系统中加载文件。
 *
 * @author SongCheng
 */
public class InIFile {
	public static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(InIFile.class);

	private static final Pattern REGEX_group = Pattern.compile("^\\[(.*)\\]");
	private static final Pattern REGEX_variable = Pattern.compile("\\$\\{(.+?)\\}");
	private static final String CLASSPATH_PREFIX = "classpath:";
	private static final String DEFAULT_GROUP = "default";

	private Map<String, Map<String, String>> gmap = new HashMap<String, Map<String, String>>();

	public InIFile(String path) {
		try {
			InputStream ins = null;
			path = path.trim();

			if (path.startsWith(CLASSPATH_PREFIX)) {
				ins = InIFile.class.getResourceAsStream(path.substring(CLASSPATH_PREFIX.length()).trim());
			} else {
				ins = new FileInputStream(path);
			}

			if (ins == null) {
				throw new Exception("找不到文件：" + path);
			}

			init(ins);
			parseVariable();
		} catch (Exception e) {
			LOG.error(e, e);
			throw new RuntimeException(e);
		}
	}//constructor

	private void init(InputStream ins) throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(ins, S.ENCODING_utf8));

		Matcher matcher = null;
		int idx;
		String key = "", value = "";
		String line = null;
		String currGroupName = DEFAULT_GROUP;
		Map<String, String> pmap = new HashMap<String, String>();
		gmap.put(currGroupName, pmap);

		while ((line = in.readLine()) != null) {
			if (StringUtils.isBlank(line)) {
				continue;
			}
			line = line.trim();

			matcher = REGEX_group.matcher(line);
			if (matcher.find()) {
				currGroupName = matcher.group(1);
				pmap = new HashMap<String, String>();
				gmap.put(currGroupName, pmap);
				continue;
			}

			if (line.startsWith("#")) {
				LOG.info("注释信息：" + line);
				continue;
			}

			idx = line.indexOf('=');
			if (idx == -1) {
				key = line;
				value = "";
			} else if (idx == 0) {
				key = "";
				value = line.substring(1);
			} else if (idx == line.length() - 1) {
				key = line.substring(0, line.length() - 1);
				value = "";
			} else {
				key = line.substring(0, idx);
				value = line.substring(idx + 1);
			}
			key = key.trim();
			value = value.trim();

			pmap = gmap.get(currGroupName);
			pmap.put(key, value);
			LOG.info("读取配置信息[" + currGroupName + "]: " + key + "=" + value);
		}//while

		in.close();
		ins.close();

	}//method

	/**
	 * 对同组中的 ${} 变量，替换为正确的数值
	 */
	private void parseVariable() {
		for (String gname : gmap.keySet()) {
			Map<String, String> pmap = gmap.get(gname);
			for (String pname : new ArrayList<String>(pmap.keySet())) {
				String val = pmap.get(pname);
				Matcher m = REGEX_variable.matcher(val);
				while (m.find()) {
					String el = m.group(1);
					String realValue = findVariableValue(el, pmap);
					val = val.replace(m.group(), realValue);
				}
				pmap.put(pname, val);
			}//for
		}//for
	}//method

	/**
	 * 在数组中查找给定KEY的实际值。
	 * 递归处理所有 ${} 变量
	 * @param key
	 * @param pmap
	 */
	private String findVariableValue(String key, Map<String, String> pmap) {
		LOG.info("查找变量：" + key);
		String val = pmap.get(key);
		//是否包含变量
		Matcher m = REGEX_variable.matcher(val);
		while (m.find()) {
			String el = m.group(1);
			String realValue = findVariableValue(el, pmap);
			val = val.replace(m.group(), realValue);
		}//while
		return val;
	}//method

	public String get(String key) {
		Map<String, String> pmap = gmap.get(DEFAULT_GROUP);
		return pmap.get(key);
	}

	public String get(String group, String key) {
		Map<String, String> pmap = gmap.get(group);
		return pmap.get(key);
	}

	public Map<String, String> getGroup(String group) {
		Map<String, String> pmap = gmap.get(group);
		return pmap == null ? Collections.EMPTY_MAP : pmap;
	}

	public Set<String> getGroupNames() {
		Set<String> keys = gmap.keySet();
		return keys;
	}
	public static void main(String[] args) {
		Map<String,String> map =S.INI.getGroup("crps-demo");
		System.out.println(map.get("CJ_FTP_PASSWORD"));
	}

}//class
