package com.syf.util;

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class Commons {
	/**
	 * 加密加签
	 * 
	 * @param reqBusiMap
	 * @param reqMap
	 * @param md5Key
	 * @throws Exception
	 */
	public static void sign_encode(Map<String, String> reqBusiMap, Map<String, String> reqMap, String md5Key) throws Exception {
		String plaintext = JSON.toJSONString(reqBusiMap);
		String ciphertext = AesUtil.encode(md5Key, plaintext);// 加密
		String signData = "ciphertext=" + ciphertext + "&key=" + md5Key;
		String sign = Md5Util.string2MD5(signData);// 签名
		System.out.println(plaintext);
		//
		reqMap.put("ciphertext", ciphertext);
		reqMap.put("sign", sign);
	}

	/**
	 * 解密验签
	 * 
	 * @param httpMsg
	 * @param key
	 * @throws Exception
	 */
	public static void validate_decode(HttpMsg httpMsg, String key) throws Exception {
		String plaintext;
		String ciphertext;
		String signData;
		String sign;
		if (200 == httpMsg.getStatus()) {
			JSONObject resJson = JSONObject.parseObject(httpMsg.getResMsg());
			if ("E00000".equals(resJson.getString("gateResCode"))) {
				ciphertext = resJson.getString("ciphertext");
				signData = "ciphertext=" + ciphertext + "&key=" + key;
				sign = Md5Util.string2MD5(signData);
				if (sign.equals(resJson.getString("sign"))) {// 验签
					plaintext = AesUtil.decode(key, ciphertext);// 解密
					httpMsg.setResBusiMsg(plaintext);
					httpMsg.setVerify(true);
				}
			}
		}
	}
}
