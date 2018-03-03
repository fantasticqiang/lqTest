package com.beeCloud.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Md5Util {
	public static String createLinkString(Map<String, ?> params, String md5Key) {
		String prestr = "";
		//
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);
		//
		String key = "";
		Object valObj = null;
		String value = null;
		for (int i = 0; i < keys.size(); i++) {
			key = keys.get(i);
			valObj = params.get(key);
			if (valObj == null) {
				value = "";
			} else {
				value = valObj.toString();
			}
			//
			if (i == keys.size() - 1) {
				prestr = prestr + key + "=" + value;
			} else {
				prestr = prestr + key + "=" + value + "&";
			}
		}
		prestr = prestr + "key=" + md5Key;
		return prestr;
	}

	public static String string2MD5(String input) throws Exception {
		String result = input;
		if (input != null) {
			MessageDigest md = MessageDigest.getInstance("MD5"); // or "SHA-1"
			md.update(input.getBytes("UTF-8"));
			BigInteger hash = new BigInteger(1, md.digest());
			result = hash.toString(16);
			while (result.length() < 32) {
				result = "0" + result;
			}
		}
		return result;
	}

	public static String md52(String inputStr) throws Exception {
		String md5Str = inputStr;
		if (inputStr != null) {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(inputStr.getBytes("UTF-8"));
			BigInteger hash = new BigInteger(1, md.digest());
			md5Str = hash.toString(16);
			if ((md5Str.length() % 2) != 0) {
				md5Str = "0" + md5Str;
			}
		}
		return md5Str;
	}

	public static void main(String[] args) {
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("amount", "100.00");
			map.put("signInfo", "c3128959b82f60d5a7aed4ecf12c22b0");
			map.put("signType", "NONE");
			map.put("resCode", "0000");
			map.put("receiveTime", "20171130105732");
			map.put("serviceCode", "10003");
			map.put("exTxnId", "123");
			map.put("resMsg", "支付成功");
			map.put("merOrderNo", "S11711301057240000100000");
			map.put("platOrderNo", "de1711301057314DDUKyV");
			map.put("completeTime", "20171130105850");
			map.put("merchId", "m1709150001");
			map.put("payOrderNo", "IG18FJP3WLAJW500014");
			String str = createLinkString(map, "md5key");

			System.out.println(str);
			System.out.println(string2MD5(str));

			str = "amount=0&exTxnId=4200000036201711308029625016&merOrderNo=20171129013855130&merchId=m1709150001&platOrderNo=wx171130142210CjnEYFP&resCode=0000&resMsg=支付成功&signType=NONE&key=8BB418FCA8A480BC3E00365AE14148A2";
			System.out.println(string2MD5(str));
			System.out.println("6a59eb423b983abec515c6287a40d65d");

			System.out.println(10001L / 100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
