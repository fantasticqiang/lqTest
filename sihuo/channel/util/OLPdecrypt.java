package com.channel.util;
// 中信银行(CNCB)外联平台(OLP)解密来自合作方(partner,PTNR)的请求或回应并验证签名类

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import com.lsy.baselib.crypto.algorithm.DESede;
import com.lsy.baselib.crypto.algorithm.RSA;
import com.lsy.baselib.crypto.protocol.PKCS7Signature;
import com.lsy.baselib.crypto.util.Base64;
import com.lsy.baselib.crypto.util.CryptUtil;
import com.lsy.baselib.crypto.util.FileUtil;

public class OLPdecrypt
{
	public static byte[] LinkByteArrays(byte[] arr1, byte[] arr2)
	{
		int n1 = arr1.length;
		int n2 = arr2.length;
		byte[] newArr = new byte[n1 + n2];
		System.arraycopy(arr1, 0, newArr, 0, n1);
		System.arraycopy(arr2, 0, newArr, n1, n2);
		return newArr;
	}
	//解密
	public static byte[] decrypt_verify(byte[] byte_接收的报文头和密文报文体, String 接收方私钥文件密码, String 接收方私钥文件名, String 发送方cer证书文件名)
	{
		try
		{
			long beginTime = System.currentTimeMillis();

			byte[] byte_MsgHead1 = new byte[6]; // 6字节报文头，后续信息长度
			byte[] byte_MsgHead2 = new byte[10]; // 10字节报文头，标识位(P=明文，E=密文)和保留域
			System.arraycopy(byte_接收的报文头和密文报文体, 0, byte_MsgHead1, 0, 6);
			System.arraycopy(byte_接收的报文头和密文报文体, 6, byte_MsgHead2, 0, 10);

			int bodyLen = Integer.valueOf(new String(byte_MsgHead1)) - 10;
			byte[] byte_报文密文 = new byte[bodyLen];
			System.arraycopy(byte_接收的报文头和密文报文体, 16, byte_报文密文, 0, bodyLen);

			byte[] byte_解密后数据 = null;
			if (byte_MsgHead2[0] != 'E')
			{
				byte_解密后数据 = byte_报文密文; // 明文
			}
			else
			{
				String sMsg = new String(byte_报文密文);
				String sKey = null;
				String openTag = "<sessionkey>";
				String closeTag = "</sessionkey>";
				byte[] byte_密文数据 = null;

				int start = sMsg.indexOf(openTag);
				if (start != -1)
				{
					int end = sMsg.indexOf(closeTag, start + openTag.length());
					if (end != -1)
					{
						sKey = sMsg.substring(start + openTag.length(), end);
						byte_密文数据 = new byte[byte_报文密文.length - end - closeTag.length()];
						System.arraycopy(byte_报文密文, end + closeTag.length(), byte_密文数据, 0, byte_报文密文.length - end - closeTag.length());
						System.out.println("------ sKey=" + sKey);
						System.out.println("------ byte_密文数据=" + new String(byte_密文数据));
					}
					else
					{
						System.out.println("?????? 找不到标签" + closeTag);
						return null;
					}
				}
				else
				{
					System.out.println("?????? 找不到标签" + openTag);
					return null;
				}

				char[] recverPrivKeyPass = new String(接收方私钥文件密码).toCharArray();
				byte[] base64EncodedPrivatekey = FileUtil.read4file(接收方私钥文件名);
				PrivateKey recverPrivKey = CryptUtil.decryptPrivateKey(Base64.decode(base64EncodedPrivatekey), recverPrivKeyPass); // 接收方私钥
				byte[] byte_会话密钥明文 = RSA.decrypt(Base64.decode(sKey.getBytes()), recverPrivKey.getEncoded());
				byte[] iv = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
				byte_解密后数据 = DESede.decrypt(byte_密文数据, byte_会话密钥明文, iv);
			}
			
			String sData = new String(byte_解密后数据);
			System.out.println("解密后的数据sData" +sData );//Test
			
			String b64StrSign = null;
			String openTag2 = "<signature>";
			String closeTag2 = "</signature>";
			byte[] byte_解密后报文 = null;

			int start2 = sData.indexOf(openTag2);
			if (start2 != -1)
			{
				int end2 = sData.indexOf(closeTag2, start2 + openTag2.length());
				if (end2 != -1)
				{
					b64StrSign = sData.substring(start2 + openTag2.length(), end2);
					byte_解密后报文 = new byte[byte_解密后数据.length - end2 - closeTag2.length()];
					System.arraycopy(byte_解密后数据, end2 + closeTag2.length(), byte_解密后报文, 0, byte_解密后数据.length - end2 - closeTag2.length());
				}
				else
				{
					System.out.println("?????? 找不到标签" + closeTag2);
					return null;
				}
			}
			else
			{
				System.out.println("?????? 找不到标签" + openTag2 + ", 无签名信息，无法验证签名！");
				// return null;
			}

			if (start2 != -1)
			{
				byte[] base64EncodedSenderCert = FileUtil.read4file(发送方cer证书文件名);
				X509Certificate signerCertificate = CryptUtil.generateX509Certificate(Base64.decode(base64EncodedSenderCert));

				PublicKey senderPubKey = signerCertificate.getPublicKey();

				boolean 验签结果 = PKCS7Signature.verifyDetachedSignature(byte_解密后报文, Base64.decode(b64StrSign.getBytes()), senderPubKey);
				System.out.println("------ byte_解密后报文 =【" + new String(byte_解密后报文) + "】");
				System.out.println("------ 验签结果 = " + 验签结果);
			}
			else
			{
				byte_解密后报文 = byte_解密后数据;
			}
			
			long endTime = System.currentTimeMillis();
			System.out.println("------ 接收方报文解密、验签处理耗时: " + (endTime - beginTime) + " ms.");
			return byte_解密后报文;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	 //验签
	public static Boolean verifySign(byte[] msg,String sign, String 发送方cer证书文件名)
	{
				byte[] base64EncodedSenderCert=null;
				try {
					base64EncodedSenderCert = FileUtil.read4file(发送方cer证书文件名);
				} catch (Exception e) {
					e.printStackTrace();
				}
				X509Certificate signerCertificate=null;
				try {
					signerCertificate = CryptUtil.generateX509Certificate(Base64.decode(base64EncodedSenderCert));
				} catch (Exception e) {
					e.printStackTrace();
				}
				PublicKey senderPubKey = signerCertificate.getPublicKey();
				return PKCS7Signature.verifyDetachedSignature(msg, Base64.decode(sign.getBytes()), senderPubKey);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub

	}

}
