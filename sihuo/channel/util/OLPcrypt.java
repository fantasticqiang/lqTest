package com.channel.util;

//中信银行(CNCB)外联平台(OLP)请求或回应报文加密/加签名类，以便将加密结果发送给合作方(partner,PTNR)

import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import org.slf4j.LoggerFactory;

import com.lsy.baselib.crypto.algorithm.DESede;
import com.lsy.baselib.crypto.algorithm.RSA;
import com.lsy.baselib.crypto.protocol.PKCS7Signature;
import com.lsy.baselib.crypto.util.Base64;
import com.lsy.baselib.crypto.util.CryptUtil;
import com.lsy.baselib.crypto.util.FileUtil;

public class OLPcrypt {
	public static byte[] LinkByteArrays(byte[] arr1, byte[] arr2) {
		int n1 = arr1.length;
		int n2 = arr2.length;
		byte[] newArr = new byte[n1 + n2];
		System.arraycopy(arr1, 0, newArr, 0, n1);
		System.arraycopy(arr2, 0, newArr, n1, n2);
		return newArr;
	}

	public static byte[] LinkByteArrays(byte[] arr1, byte[] arr2, byte[] arr3) {
		int n1 = arr1.length;
		int n2 = arr2.length;
		int n3 = arr3.length;
		byte[] newArr = new byte[n1 + n2 + n3];
		System.arraycopy(arr1, 0, newArr, 0, n1);
		System.arraycopy(arr2, 0, newArr, n1, n2);
		System.arraycopy(arr3, 0, newArr, n1 + n2, n3);
		return newArr;
	}

	public static byte[] LinkByteArrays(byte[] arr1, byte[] arr2, byte[] arr3,
			byte[] arr4) {
		int n1 = arr1.length;
		int n2 = arr2.length;
		int n3 = arr3.length;
		int n4 = arr4.length;
		byte[] newArr = new byte[n1 + n2 + n3 + n4];
		System.arraycopy(arr1, 0, newArr, 0, n1);
		System.arraycopy(arr2, 0, newArr, n1, n2);
		System.arraycopy(arr3, 0, newArr, n1 + n2, n3);
		System.arraycopy(arr4, 0, newArr, n1 + n2 + n3, n4);
		return newArr;
	}

	// 加签加密
	public static byte[] sign_crypt(byte[] byte_明文报文, String 发送方私钥文件密码,
			String 发送方私钥文件名, String 发送方cer证书文件名, String 接收方cer证书文件名) {
		try {
			long beginTime = System.currentTimeMillis();

			char[] keyPassword = new String(发送方私钥文件密码).toCharArray();
			byte[] base64EncodedPrivatekey = FileUtil.read4file(发送方私钥文件名);
			PrivateKey signerPrivatekey = CryptUtil.decryptPrivateKey(
					Base64.decode(base64EncodedPrivatekey), keyPassword);

			byte[] base64EncodedCert = FileUtil.read4file(发送方cer证书文件名);
			X509Certificate signerCertificate = CryptUtil
					.generateX509Certificate(Base64.decode(base64EncodedCert));

			byte[] signature = PKCS7Signature.sign(byte_明文报文, signerPrivatekey,
					signerCertificate, null, false); // 签名中不附带原文
			String b64StrSignature = new String(Base64.encode(signature));
			byte[] signedMsg = LinkByteArrays(
					("<signature>" + b64StrSignature + "</signature>")
							.getBytes(),
					byte_明文报文); // 带签名的报文

			byte[] sessionKey = DESede.createKey(DESede.DESEDE_KEY_112_BIT); // 产生随机会话密钥明文
			byte[] iv = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
			byte[] cryptMsg = DESede.encrypt(signedMsg, sessionKey, iv); // 签名报文再加密

			byte[] base64EncodedCert2 = FileUtil.read4file(接收方cer证书文件名);
			X509Certificate cert2 = CryptUtil.generateX509Certificate(Base64
					.decode(base64EncodedCert2));
			PublicKey pubKey2 = cert2.getPublicKey();
			byte[] sessionKeyCipher = RSA.encrypt(sessionKey,
					pubKey2.getEncoded()); // 随机会话密钥密文
			byte[] msgBody = LinkByteArrays("<sessionkey>".getBytes(),
					Base64.encode(sessionKeyCipher),
					"</sessionkey>".getBytes(), cryptMsg); // 完整报文体
			byte[] msgHead = String.format("%06dE123456789",
					10 + msgBody.length).getBytes(); // 报文头,后续信息长度+标识位(P=明文，E=密文)和保留域

			long endTime = System.currentTimeMillis();
			System.out.println("------ 请求或回应报文加密/加签名示例处理耗时: "
					+ (endTime - beginTime) + " ms.");
			return LinkByteArrays(msgHead, msgBody);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// 加签
	public static String sign(byte[] string, String 发送方私钥文件密码,
			String 发送方私钥文件名, String 发送方cer证书文件名) {
		try {
			char[] keyPassword = new String(发送方私钥文件密码).toCharArray();
			byte[] base64EncodedPrivatekey = FileUtil.read4file(发送方私钥文件名);
			PrivateKey signerPrivatekey = CryptUtil.decryptPrivateKey(
					Base64.decode(base64EncodedPrivatekey), keyPassword);

			byte[] base64EncodedCert = FileUtil.read4file(发送方cer证书文件名);
			X509Certificate signerCertificate = CryptUtil
					.generateX509Certificate(Base64.decode(base64EncodedCert));

			byte[] signature = PKCS7Signature.sign(string, signerPrivatekey,
					signerCertificate, null, false); // 签名中不附带原文
			String b64StrSignature = new String(Base64.encode(signature));

			return b64StrSignature;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws UnsupportedEncodingException,
			FileNotFoundException {
		Socket socket = null;
		OutputStream out = null;
		InputStream in = null;
		try {

			String encode = "GBK";
			String xmlStr = "<?xml version=\"1.0\" encoding=\"GBK\"?><ROOT><merid>010790060121001</merid><trancode>0100ACP2</trancode><orderid>20160905010787244065</orderid><txntime>20160905161957</txntime></ROOT>";
			byte[] strByte = xmlStr.getBytes("GBK");
			byte[] msg = xmlStr.getBytes(encode);
			// 加签
			String signByte = OLPcrypt.sign(msg, "Mima001",
					"D:/changecre/testcer001/PTNRtest.key",
					"D:/changecre/testcer001/PTNRtest.cer");
			byte[] signedMsg = LinkByteArrays(
					("<signature>" + signByte + "</signature>").getBytes("GBK"),
					strByte); // 带签名的报文
			System.out.println("===========>>>  "
					+ new String(signedMsg, "GBK"));
			saveTestFile("D:\\changecre\\baowen\\request.txt",signedMsg);

			LoggerFactory.getLogger(OLPcrypt.class).info(signedMsg+"");
			// 验签
			Boolean decStr = OLPdecrypt.verifySign(strByte, signByte,
					"D:/changecre/testcer001/PTNRtest.cer");
			System.out.println("++++：  " + decStr);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public static void saveTestFile(String fileName, byte[] msg)
			throws IOException {
		File outFile = new File(fileName);
		if (!outFile.exists()) {
			outFile.createNewFile();
		}
		FileOutputStream out = new FileOutputStream(outFile);
		out.write(msg);
		out.flush();
		out.close();
	}
}
