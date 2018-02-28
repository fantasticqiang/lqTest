package com.syf.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.alibaba.fastjson.JSON;

public class HttpUtil {

	static String[] resColumns = { "ciphertext", "sign" };

	static String encoding = "utf-8";

	public static void download(String url, Map<String, String> reqMap, Map<String, String> resMap, String targetPath, HttpMsg httpMsg) {
		File srcFile = new File(targetPath);
		URL httpUrl = null;
		HttpURLConnection conn = null;
		InputStream inputStream = null;
		FileOutputStream fileOutputStream = null;
		try {
			httpUrl = new URL(url);
			conn = (HttpURLConnection) httpUrl.openConnection();
			// 请求参数
			for (String key : reqMap.keySet()) {
				conn.setRequestProperty(key, reqMap.get(key));
			}
			conn.setConnectTimeout(180000);
			conn.setReadTimeout(180000);
			httpMsg.setStatus(conn.getResponseCode());
			if (httpMsg.getStatus() == HttpURLConnection.HTTP_OK) {
				// 返回参数
				for (String column : resColumns) {
					resMap.put(column, conn.getHeaderField(column));
				}
				// 解密
				// 写文件
				inputStream = conn.getInputStream();
				fileOutputStream = new FileOutputStream(srcFile);
				byte[] bytes = new byte[1024];
				int length = 0;
				while ((length = inputStream.read(bytes)) != -1) {
					fileOutputStream.write(bytes, 0, length);
				}
				fileOutputStream.flush();
				System.out.println(srcFile.length());
				httpMsg.setVerify(true);
				httpMsg.setResMsg("SUCCESS");
			}
		} catch (Exception e) {
			e.printStackTrace();
			httpMsg.setVerify(false);
			httpMsg.setResMsg(e.getMessage());
		} finally {
			try {
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
				if (conn != null) {
					conn.disconnect();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static HttpMsg postJson(String url, Map<String, String> params) {
		return postJson(url, JSON.toJSONString(params));
	}

	public static HttpMsg postJson(String url, String params) {
		HttpMsg httpMsg = new HttpMsg();

		OutputStream outputStream = null;
		DataOutputStream outputStreamWriter = null;
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		String response = "";
		try {
			URL httpUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json;charset=" + encoding);
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Accept-Charset", encoding);
			conn.setUseCaches(false);// 设置不要缓存
			conn.setInstanceFollowRedirects(true);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setConnectTimeout(60000);
			conn.setReadTimeout(60000);
			conn.connect();
			// POST请求
			outputStream = conn.getOutputStream();
			outputStreamWriter = new DataOutputStream(outputStream);
			outputStreamWriter.write(params.getBytes());
			outputStreamWriter.flush();
			httpMsg.setStatus(conn.getResponseCode());
			// 读取响应
			inputStream = conn.getInputStream();
			inputStreamReader = new InputStreamReader(inputStream);
			bufferedReader = new BufferedReader(inputStreamReader);
			String lines;
			while ((lines = bufferedReader.readLine()) != null) {
				lines = new String(lines.getBytes(), "utf-8");
				response += lines;
			}
			httpMsg.setResMsg(response);
			bufferedReader.close();
			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			httpMsg.setStatus(-1);
			httpMsg.setReqBusiMsg(e.getMessage());
		} finally {
			System.out.println(url);
			System.out.println(params);
			System.out.println(params.length());
			//
			System.out.println(httpMsg.getStatus());
			System.out.println(httpMsg.getResMsg());
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
				if (inputStreamReader != null) {
					inputStreamReader.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
				if (outputStreamWriter != null) {
					outputStreamWriter.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return httpMsg;
	}

	public static HttpMsg httpGet(String url, String params) {
		HttpMsg httpMsg = new HttpMsg();

		OutputStream outputStream = null;
		DataOutputStream outputStreamWriter = null;
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		String response = "";
		try {
			URL httpUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json;charset=" + encoding);
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Accept-Charset", encoding);
			conn.setUseCaches(false);// 设置不要缓存
			conn.setInstanceFollowRedirects(true);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setConnectTimeout(60000);
			conn.setReadTimeout(60000);
			conn.connect();
			// POST请求
			outputStream = conn.getOutputStream();
			outputStreamWriter = new DataOutputStream(outputStream);
			// outputStreamWriter.write(params.getBytes());
			outputStreamWriter.flush();
			httpMsg.setStatus(conn.getResponseCode());
			// 读取响应
			inputStream = conn.getInputStream();
			inputStreamReader = new InputStreamReader(inputStream);
			bufferedReader = new BufferedReader(inputStreamReader);
			String lines;
			while ((lines = bufferedReader.readLine()) != null) {
				lines = new String(lines.getBytes(), "utf-8");
				response += lines;
			}
			httpMsg.setResMsg(response);
			bufferedReader.close();
			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			httpMsg.setStatus(-1);
			httpMsg.setReqBusiMsg(e.getMessage());
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
				if (inputStreamReader != null) {
					inputStreamReader.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
				if (outputStreamWriter != null) {
					outputStreamWriter.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		//
		if (200 == httpMsg.getStatus()) {
			try {
				// httpMsg.setResBusiMap(JSON.parseObject(httpMsg.getResMsg(), httpMsg.getResMap().getClass()));
				httpMsg.setResMsg(httpMsg.getResMsg());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return httpMsg;
	}

	public static void uploadFile(String reqUrl, String fileName, Map<String, String> reqMap) {
		try {
			final String newLine = "\r\n";
			final String boundaryPrefix = "--";
			String BOUNDARY = "========7d4a6d158c9";
			//
			URL url = new URL(reqUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("Charsert", "UTF-8");
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

			OutputStream out = new DataOutputStream(conn.getOutputStream());
			// 上传文件
			File file = new File(fileName);
			StringBuilder sb = new StringBuilder();
			sb.append(boundaryPrefix);
			sb.append(BOUNDARY);
			sb.append(newLine);
			// 文件参数,photo参数名可以随意修改
			sb.append("Content-Disposition: form-data;name=\"photo\";filename=\"" + fileName + "\"" + newLine);
			sb.append("Content-Type:application/octet-stream");
			// 参数头设置完以后需要两个换行，然后才是参数内容
			sb.append(newLine);
			sb.append(newLine);
			// 将参数头的数据写入到输出流中
			out.write(sb.toString().getBytes());
			// 数据输入流,用于读取文件数据
			DataInputStream in = new DataInputStream(new FileInputStream(file));
			byte[] bufferOut = new byte[1024];
			int bytes = 0;
			// 每次读1KB数据,并且将文件数据写入到输出流中
			while ((bytes = in.read(bufferOut)) != -1) {
				out.write(bufferOut, 0, bytes);
			}
			// 最后添加换行
			out.write(newLine.getBytes());
			in.close();
			// 定义最后数据分隔线，即--加上BOUNDARY再加上--。
			byte[] end_data = (newLine + boundaryPrefix + BOUNDARY + boundaryPrefix + newLine).getBytes();
			// 写上结尾标识
			out.write(end_data);
			out.flush();
			out.close();
			// 定义BufferedReader输入流来读取URL的响应
			// BufferedReader reader = new BufferedReader(new InputStreamReader(
			// conn.getInputStream()));
			// String line = null;
			// while ((line = reader.readLine()) != null) {
			// System.out.println(line);
			// }
		} catch (Exception e) {
			System.out.println("发送POST请求出现异常！" + e);
			e.printStackTrace();
		}
	}
}
