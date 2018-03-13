package com.beeCloud.util;


import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;

import cn.phoneposp.dao.SaruInfoDao;

import com.alibaba.fastjson.JSON;
import com.beeCloud.constant.BeeCloudConstant;
import com.beeCloud.dao.BeeCloudPayDao;
import com.beeCloud.model.MercantEnterInfo;
import com.interfaces.O110101.util.OrderNumUtil;


public class HttpClientUtils {

    public static final int connTimeout = 30000;
    public static final int readTimeout = 30000;
    public static final String charset = "UTF-8";
    private static HttpClient client = null;

    static {
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(128);
        connManager.setDefaultMaxPerRoute(128);
        client = HttpClients.custom().setConnectionManager(connManager).build();
    }

    /**
     * post 请求
     * 
     * @param url
     * @param parameterStr
     * @return
     * @throws ConnectTimeoutException
     * @throws SocketTimeoutException
     * @throws Exception
     */
    public static String postJsonParameters(String url, String parameterStr)
            throws ConnectTimeoutException, SocketTimeoutException, Exception {
        return post(url, parameterStr, "application/json", charset, connTimeout, readTimeout);
    }

    public static String postParameters(String url, String parameterStr)
            throws ConnectTimeoutException, SocketTimeoutException, Exception {
        return post(url, parameterStr, "application/x-www-form-urlencoded", charset, connTimeout, readTimeout);
    }

    public static String postParameters(String url) throws ConnectTimeoutException, SocketTimeoutException, Exception {
        return post(url, null, "application/x-www-form-urlencoded", charset, connTimeout, readTimeout);
    }

    public static String postParameters(String url, String parameterStr, String charset, Integer connTimeout,
            Integer readTimeout) throws ConnectTimeoutException, SocketTimeoutException, Exception {
        return post(url, parameterStr, "application/x-www-form-urlencoded", charset, connTimeout, readTimeout);
    }

    public static String postParameters(String url, Map<String, String> params)
            throws ConnectTimeoutException, SocketTimeoutException, Exception {
        return postForm(url, params, null, connTimeout, readTimeout);
    }

    public static String postParameters(String url, Map<String, String> params, Integer connTimeout,
            Integer readTimeout) throws ConnectTimeoutException, SocketTimeoutException, Exception {
        return postForm(url, params, null, connTimeout, readTimeout);
    }

    public static String get(String url) throws Exception {
        return get(url, charset, null, null);
    }

    public static String get(String url, String charset) throws Exception {
        return get(url, charset, connTimeout, readTimeout);
    }

    /**
     * 发送一个 Post 请求, 使用指定的字符集编码.
     * 
     * @param url
     * @param body
     * RequestBody
     * @param mimeType
     * 例如 application/xml "application/x-www-form-urlencoded" a=1&b=2&c=3
     * @param charset
     * 编码
     * @param connTimeout
     * 建立链接超时时间,毫秒.
     * @param readTimeout
     * 响应超时时间,毫秒.
     * @return ResponseBody, 使用指定的字符集编码.
     * @throws ConnectTimeoutException
     * 建立链接超时异常
     * @throws SocketTimeoutException
     * 响应超时
     * @throws Exception
     */
    public static String post(String url, String body, String mimeType, String charset, Integer connTimeout,
            Integer readTimeout) throws ConnectTimeoutException, SocketTimeoutException, Exception {
        HttpClient client = null;
        HttpPost post = new HttpPost(url);
        String result = "";
        try {
            if (StringUtils.isNotBlank(body)) {
                HttpEntity entity = new StringEntity(body, ContentType.create(mimeType, charset));
                post.setEntity(entity);
            }
            // 设置参数
            Builder customReqConf = RequestConfig.custom();
            if (connTimeout != null) {
                customReqConf.setConnectTimeout(connTimeout);
            }
            if (readTimeout != null) {
                customReqConf.setSocketTimeout(readTimeout);
            }
            post.setConfig(customReqConf.build());

            HttpResponse res;
            if (url.startsWith("https")) {
                // 执行 Https 请求.
                client = createSSLInsecureClient();
                res = client.execute(post);
            } else {
                // 执行 Http 请求.
                client = HttpClientUtils.client;
                res = client.execute(post);
            }
            result = IOUtils.toString(res.getEntity().getContent(), charset);
        } finally {
            post.releaseConnection();
            if (url.startsWith("https") && client != null && client instanceof CloseableHttpClient) {
                ((CloseableHttpClient) client).close();
            }
        }
        return result;
    }

    /**
     * 提交form表单
     * 
     * @param url
     * @param params
     * @param connTimeout
     * @param readTimeout
     * @return
     * @throws ConnectTimeoutException
     * @throws SocketTimeoutException
     * @throws Exception
     */
    public static String postForm(String url, Map<String, String> params, Map<String, String> headers,
            Integer connTimeout, Integer readTimeout)
            throws ConnectTimeoutException, SocketTimeoutException, Exception {

        HttpClient client = null;
        HttpPost post = new HttpPost(url);
        try {
            if (params != null && !params.isEmpty()) {
                List<NameValuePair> formParams = new ArrayList<org.apache.http.NameValuePair>();
                Set<Entry<String, String>> entrySet = params.entrySet();
                for (Entry<String, String> entry : entrySet) {
                    formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
                post.setEntity(entity);
            }

            if (headers != null && !headers.isEmpty()) {
                for (Entry<String, String> entry : headers.entrySet()) {
                    post.addHeader(entry.getKey(), entry.getValue());
                }
            }
            // 设置参数
            Builder customReqConf = RequestConfig.custom();
            if (connTimeout != null) {
                customReqConf.setConnectTimeout(connTimeout);
            }
            if (readTimeout != null) {
                customReqConf.setSocketTimeout(readTimeout);
            }
            post.setConfig(customReqConf.build());
            HttpResponse res = null;
            if (url.startsWith("https")) {
                // 执行 Https 请求.
                client = createSSLInsecureClient();
                res = client.execute(post);
            } else {
                // 执行 Http 请求.
                client = HttpClientUtils.client;
                res = client.execute(post);
            }
            return IOUtils.toString(res.getEntity().getContent(), "UTF-8");
        } finally {
            post.releaseConnection();
            if (url.startsWith("https") && client != null && client instanceof CloseableHttpClient) {
                ((CloseableHttpClient) client).close();
            }
        }
    }

    /**
     * 发送一个 GET 请求
     * 
     * @param url
     * @param charset
     * @param connTimeout
     * 建立链接超时时间,毫秒.
     * @param readTimeout
     * 响应超时时间,毫秒.
     * @return
     * @throws ConnectTimeoutException
     * 建立链接超时
     * @throws SocketTimeoutException
     * 响应超时
     * @throws Exception
     */
    public static String get(String url, String charset, Integer connTimeout, Integer readTimeout)
            throws ConnectTimeoutException, SocketTimeoutException, Exception {

        HttpClient client = null;
        HttpGet get = new HttpGet(url);
        String result = "";
        try {
            // 设置参数
            Builder customReqConf = RequestConfig.custom();
            if (connTimeout != null) {
                customReqConf.setConnectTimeout(connTimeout);
            }
            if (readTimeout != null) {
                customReqConf.setSocketTimeout(readTimeout);
            }
            get.setConfig(customReqConf.build());

            HttpResponse res = null;

            if (url.startsWith("https")) {
                // 执行 Https 请求.
                client = createSSLInsecureClient();
                res = client.execute(get);
            } else {
                // 执行 Http 请求.
                client = HttpClientUtils.client;
                res = client.execute(get);
            }
            result = IOUtils.toString(res.getEntity().getContent(), charset);
        } finally {
            get.releaseConnection();
            if (url.startsWith("https") && client != null && client instanceof CloseableHttpClient) {
                ((CloseableHttpClient) client).close();
            }
        }
        return result;
    }

    /**
     * 创建 SSL连接
     * 
     * @return
     * @throws GeneralSecurityException
     */
    private static CloseableHttpClient createSSLInsecureClient() throws GeneralSecurityException {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {

                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }

                @Override
                public void verify(String host, SSLSocket ssl) throws IOException {}

                @Override
                public void verify(String host, X509Certificate cert) throws SSLException {}

                @Override
                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {}

            });

            return HttpClients.custom().setSSLSocketFactory(sslsf).build();

        } catch (GeneralSecurityException e) {
            throw e;
        }
    }

    public static void main(String[] args) throws Exception {
    	/*
    	 * {"app_id":"04538d30-5f17-478b-b4bd-f335a2cdbe51","app_sign":"425aa2d90a63bb7b0bee776f49c6262d","bill_no":"BeeCloud193641309347","card_no":"6225758212103831","channel":"BC_EXPRESS","notify_url":"http://47.104.91.166/PhonePospInterface/BeeCloudPayNotifyServlet","optional":{"fc_card_no":"6214830125502616","in_bank_code":"308","pay_bank_code":"308","pay_bank_cvv2":"709","pay_bank_expiry_date":"0322","user_cert_no":"110101198702201514","user_fee":"20","user_mobile":"18330219446","user_name":"董海龙","user_rate":"0.0060"},"return_url":"https://www.baidu.com","timestamp":1520227504598,"title":"线上支付","total_fee":300000}
    	 */
    	//testPay();
    	testPay2();
    	/*
    	 * {"app_id":"04538d30-5f17-478b-b4bd-f335a2cdbe51","app_sign":"5aa75dc3cfed763df4c702532ababf76","bill_no":"BeeCloud193641309347","card_no":"6225758212103831","channel":"BC_EXPRESS","notify_url":"http://47.104.91.166/PhonePospInterface/BeeCloudPayNotifyServlet","optional":{"fc_card_no":"6214830125502616","in_bank_code":"308","pay_bank_code":"308","pay_bank_cvv2":"709","pay_bank_expiry_date":"0322","user_cert_no":"110101198702201514","user_fee":"80","user_mobile":"15601205173","user_name":"董海龙","user_rate":"0.0060"},"return_url":"https://www.baidu.com","timestamp":1520227360356,"title":"线上支付","total_fee":300000}
    	 */
    }
    
    
    public static void testPay(){
    	String app_id = "04538d30-5f17-478b-b4bd-f335a2cdbe51";
		String app_secret = "061f84f5-f966-4b16-bc5e-d24c80d5fed4";
		String url = "https://api.beecloud.cn/2/rest/bill";
		String res = null;
    try {
        	Map<String, Object> map = new HashMap<String, Object>();
    		Date now = new Date();
    		map.put("app_id", app_id);
    		long time = now.getTime();
    		map.put("timestamp", time);
    		String str2sign = BeeCloudConstant.app_id+time+BeeCloudConstant.app_secret; 
    		String string2md5 = Md5Util.string2MD5(str2sign);
    		map.put("app_sign", string2md5);
    		map.put("channel", "BC_EXPRESS"); // ALI_WEB BC_EXPRESS BC_GATEWAY BC_JD_QRCODE
    		map.put("bill_no", "BeeCloud193641309347"); // 20171130145830 yyyyMMddHHmmssSSS
    		map.put("total_fee", 300000);
    		map.put("title", "线上支付");
    		map.put("return_url", "https://www.baidu.com");
    		map.put("notify_url", "http://47.104.91.166/PhonePospInterface/BeeCloudPayNotifyServlet");
    		map.put("card_no", "6225758212103831"); // 支付卡号,信用卡
    		
    		Map<String, Object> optional = new HashMap<String, Object>();
    		optional.put("user_name", "董海龙");
    		optional.put("user_cert_no", "110101198702201514");
    		optional.put("user_mobile", "15601205173");
    		optional.put("fc_card_no", "6214830125502616");//储蓄卡
    		optional.put("pay_bank_code", "308");
    		optional.put("in_bank_code", "308");
    		optional.put("pay_bank_expiry_date", "1121");
    		optional.put("pay_bank_cvv2", "532");
    		optional.put("user_rate", "0.0060");
    		optional.put("user_fee", "20");
    		map.put("optional", optional);
    	/*
    	 * {"app_id":"04538d30-5f17-478b-b4bd-f335a2cdbe51","app_sign":"14703ee41603bb3749d7f3fb14744299","bill_no":"BeeCloud193641309337","card_no":"6214830125502616","channel":"BC_EXPRESS","notify_url":"http://47.104.91.166/PhonePospInterface/BeeCloudPayNotifyServlet","optional":{"fc_card_no":"6214830125502616","in_bank_code":"308","pay_bank_code":"308","pay_bank_cvv2":"567","pay_bank_expiry_date":"0733","user_cert_no":"110101198702201514","user_fee":"80","user_mobile":"15601205173","user_name":"董海龙","user_rate":"0.0060"},"timestamp":1520221903131,"title":"线上支付","total_fee":10000}
    	 * {"app_id":"04538d30-5f17-478b-b4bd-f335a2cdbe51","app_sign":"a4ada3e0762cd482b51b3072f82bd089","bill_no":"20180305173850001","card_no":"6258101647020165","channel":"BC_EXPRESS","optional":{"fc_card_no":"6214830217872612","in_bank_code":"308","pay_bank_code":"306","pay_bank_cvv2":"709","pay_bank_expiry_date":"0322","user_cert_no":"321281199007300010","user_fee":"20","user_mobile":"13916385054","user_name":"吴佳杰","user_rate":"0.0039"},"return_url":"https://www.baidu.com","timestamp":1520221244024,"title":"测试支付","total_fee":300000}
    	 */
    	System.out.println("请求参数："+JSON.toJSONString(map));
        res = HttpClientUtils.postJsonParameters(url, JSON.toJSONString(map));
    } catch (Exception e) {
        e.printStackTrace();
    }
    System.out.println(res);
    }
    
    public static void testPay2(){
    	String app_id = "04538d30-5f17-478b-b4bd-f335a2cdbe51";
		String app_secret = "061f84f5-f966-4b16-bc5e-d24c80d5fed4";
		String url = "https://api.beecloud.cn/2/rest/bill";
		String res = null;
		
		try {	
			Map<String, Object> map = new HashMap<String, Object>();
    		Date now = new Date();
    		map.put("app_id", app_id);
    		long time = now.getTime();
    		map.put("timestamp", time);
    		String str2sign = BeeCloudConstant.app_id+time+BeeCloudConstant.app_secret; 
    		String string2md5 = Md5Util.string2MD5(str2sign);
    		map.put("app_sign", string2md5);
    		map.put("channel", "BC_EXPRESS"); // ALI_WEB BC_EXPRESS BC_GATEWAY BC_JD_QRCODE
    		map.put("bill_no", "BeeCloud193641309347"); // 20171130145830 yyyyMMddHHmmssSSS
    		map.put("total_fee", 300000);
    		map.put("title", "线上支付");
    		map.put("return_url", "https://www.baidu.com");
    		map.put("notify_url", "http://47.104.91.166/PhonePospInterface/BeeCloudPayNotifyServlet");
    		map.put("card_no", "6221682295594274"); // 支付卡号
    		
    		Map<String, Object> optional = new HashMap<String, Object>();
    		optional.put("user_name", "沈日月");
    		optional.put("user_cert_no", "210421198707024217");
    		optional.put("user_mobile", "18611539891");
    		optional.put("fc_card_no", "6217710708401146");//储蓄卡
    		optional.put("pay_bank_code", "105");
    		optional.put("in_bank_code", "302");
    		optional.put("pay_bank_expiry_date", "0623");
    		optional.put("pay_bank_cvv2", "193");
    		optional.put("user_rate", "0.0060");
    		optional.put("user_fee", "20");
    		map.put("optional", optional);
    		System.out.println("请求参数："+JSON.toJSONString(map));
            res = HttpClientUtils.postJsonParameters(url, JSON.toJSONString(map));
            System.out.println("返回结果："+res);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
    	
    }
    
    public static void testFinal(){
   		String app_id = "04538d30-5f17-478b-b4bd-f335a2cdbe51";
		String app_secret = "061f84f5-f966-4b16-bc5e-d24c80d5fed4";
		String url = "https://api.beecloud.cn/2/rest/bill";
		String res = null;
    try {
        	Map<String, Object> map = new HashMap<String, Object>();
    		Date now = new Date();
    		map.put("app_id", app_id);
    		long time = now.getTime();
    		map.put("timestamp", time);
    		String str2sign = BeeCloudConstant.app_id+time+BeeCloudConstant.app_secret; 
    		String string2md5 = Md5Util.string2MD5(str2sign);
    		map.put("app_sign", string2md5);
    		map.put("channel", "BC_EXPRESS"); // ALI_WEB BC_EXPRESS BC_GATEWAY BC_JD_QRCODE
    		map.put("bill_no", "20180305173850001"); // 20171130145830 yyyyMMddHHmmssSSS
    		map.put("total_fee", 300000);
    		map.put("title", "测试支付");
    		map.put("return_url", "https://www.baidu.com");
    		map.put("card_no", "6258101647020165"); // 支付卡号
    		
    		Map<String, Object> optional = new HashMap<String, Object>();
    		optional.put("user_name", "吴佳杰");
    		optional.put("user_cert_no", "321281199007300010");
    		optional.put("user_mobile", "13916385054");
    		optional.put("fc_card_no", "6214830217872612");
    		optional.put("pay_bank_code", "306");
    		optional.put("in_bank_code", "308");
    		optional.put("pay_bank_expiry_date", "0322");
    		optional.put("pay_bank_cvv2", "709");
    		optional.put("user_rate", "0.0039");
    		optional.put("user_fee", "20");
    		map.put("optional", optional);
    	/*
    	 * {"app_id":"04538d30-5f17-478b-b4bd-f335a2cdbe51","app_sign":"14703ee41603bb3749d7f3fb14744299","bill_no":"BeeCloud193641309337","card_no":"6214830125502616","channel":"BC_EXPRESS","notify_url":"http://47.104.91.166/PhonePospInterface/BeeCloudPayNotifyServlet","optional":{"fc_card_no":"6214830125502616","in_bank_code":"308","pay_bank_code":"308","pay_bank_cvv2":"567","pay_bank_expiry_date":"0733","user_cert_no":"110101198702201514","user_fee":"80","user_mobile":"15601205173","user_name":"董海龙","user_rate":"0.0060"},"timestamp":1520221903131,"title":"线上支付","total_fee":10000}
    	 * {"app_id":"04538d30-5f17-478b-b4bd-f335a2cdbe51","app_sign":"a4ada3e0762cd482b51b3072f82bd089","bill_no":"20180305173850001","card_no":"6258101647020165","channel":"BC_EXPRESS","optional":{"fc_card_no":"6214830217872612","in_bank_code":"308","pay_bank_code":"306","pay_bank_cvv2":"709","pay_bank_expiry_date":"0322","user_cert_no":"321281199007300010","user_fee":"20","user_mobile":"13916385054","user_name":"吴佳杰","user_rate":"0.0039"},"return_url":"https://www.baidu.com","timestamp":1520221244024,"title":"测试支付","total_fee":300000}
    	 */
    	System.out.println("请求参数："+JSON.toJSONString(map));
        res = HttpClientUtils.postJsonParameters(url, JSON.toJSONString(map));
    } catch (Exception e) {
        e.printStackTrace();
    }
    System.out.println(res);
    }

}
