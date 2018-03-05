/**
 * 文件名： HttpPostBodyMethod.java
 * 建立时间： 2013-7-26 上午11:41:17
 * 创建人： SongCheng
 */
package com.channel.util;

import org.apache.commons.httpclient.methods.EntityEnclosingMethod;

/**
 * 使用POST方法，发送字符主体
 *
 * @author SongCheng
 */
public class HttpPostBodyMethod extends EntityEnclosingMethod {
	public static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HttpPostBodyMethod.class);

	private String body;

	public HttpPostBodyMethod(String url, String body) {
		super(url);
		this.body = body;

	}

	public String getName() {
		return "POST";
	}

	protected boolean hasRequestContent() {
		return true;
	}

	protected byte[] generateRequestBody() {
		/*
		String content = EncodingUtil.formUrlEncode(getParameters(), getRequestCharSet());
		byte[] bs = EncodingUtil.getAsciiBytes(body);
		 */
		byte[] bs = {};
		try {
			bs = body.getBytes(S.ENCODING_utf8);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return bs;
	}

}//class
