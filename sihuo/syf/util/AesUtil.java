package com.syf.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


public class AesUtil {

	public static void main(String[] args) throws Exception {
		String key = "9a9ce9b9302b4e2e9dd58156d59add4e";
		String content = "4fiKWLHKYnMud8gmbV8N5lGuzfJXxEbk0AMDazkPo2h2msaBwK6+/FbUmzxAXsfvncofA5gVkqe0\r\nH9ZgUciWyRPCwKxA0KQYVhYh/Vz+4TltisyORsFO/cythl+JrQF8NPyxKjBFL/uV/ijXthWhnVop\r\nAR+0G3bXrckhHU9XKkau0JVfdgycNtMZsCcXA+8v4IfzCTOCxdY5L8J4aOpxpcySEqZlTY9RgTOJ\r\n5IWewxO11zTp7oMNfe5oYKo1035ZcY8qNLLoP1nsiiqNcd9xildZUZoSebMAR7ZudW0GrnZThs+5\r\nuuWq+abBzlXa6G4nxWK55j9rpO0m6PoRHiQfkemSS69dI45ZCOz4hBdhnPyIFm30sXnBMhEvRl3L\r\nlW1DbUZZDozi0kZwNfz/E3Br21XDKD8Z3j/hx+ZNLGV5O17ND4FBOv4pn3+HtJk4f35BGSB3g0/2\r\nXV9Is/Ykf4eJi+jFfMheeB5wUjzC7+Yd6/gfwWgqBBOpvWPWxV5pgBhAnHrATSmHTXqY/INAzuFJ\r\nXC7LCIdmwGq7K9VAEkc4WFU=";
		String ciphertext = "";

//		ciphertext = encode(key, content);
//		System.out.println(ciphertext);

		ciphertext = decode(key, content);
		System.out.println(ciphertext);
	}

	public static String encode(String encodeRules, String content) throws Exception {
		// 取前16位
		String key = encodeRules.substring(0, 16);
		String key2 = encodeRules.substring(0, 16);
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			int blockSize = cipher.getBlockSize();
			byte[] dataBytes = content.getBytes("utf-8");
			int plaintextLength = dataBytes.length;
			if (plaintextLength % blockSize != 0) {
				plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
			}
			byte[] plaintext = new byte[plaintextLength];
			System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

			SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
			IvParameterSpec ivspec = new IvParameterSpec(key2.getBytes());
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			byte[] encrypted = cipher.doFinal(plaintext);
			/*
			 * 这里修改了
			 */
			return new BASE64Encoder().encode(encrypted);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String decode(String encodeRules, String content) throws Exception {
		try {
			String key = encodeRules.substring(0, 16);
			String key2 = encodeRules.substring(0, 16);

			/*
			 * 这里修改了
			 * Base64.decode(content)
			 */
			byte[] encrypted1 = new BASE64Decoder().decodeBuffer(content);

			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
			IvParameterSpec ivspec = new IvParameterSpec(key2.getBytes());
			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
			byte[] original = cipher.doFinal(encrypted1);
			String originalString = new String(original, "utf-8");
			if (originalString != null) {
				originalString = originalString.trim();
			}
			return originalString;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
