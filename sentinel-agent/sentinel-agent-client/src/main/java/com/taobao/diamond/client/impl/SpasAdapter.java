package com.taobao.diamond.client.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import com.taobao.diamond.identify.Base64;
import com.taobao.diamond.identify.CredentialService;
import com.taobao.diamond.utils.StringUtils;
/**
 * ����spas�ӿ�
 * @author Diamond
 *
 */
public class SpasAdapter {
	
	public static List<String> getSignHeaders(String resource, String secretKey) {
		List<String> header = new ArrayList<String>();
		String timeStamp = String.valueOf(System.currentTimeMillis());
		header.add("timeStamp");
		header.add(timeStamp);
		if (secretKey != null) {
			header.add("Spas-Signature");
			String signature = "";
			if (StringUtils.isBlank(resource)) {
				signature = signWithhmacSHA1Encrypt(timeStamp, secretKey);
			} else {
				signature = signWithhmacSHA1Encrypt(resource + "+" + timeStamp, secretKey);
			}
			header.add(signature);
		}
		return header;
	}
	
	
	public static List<String> getSignHeaders(List<String> paramValues, String secretKey) {
		if (null == paramValues) {
			return null;
		}
		Map<String, String> signMap = new HashMap<String, String>();
		for (Iterator<String> iter = paramValues.iterator(); iter.hasNext();) {
			String key = iter.next();
			if ("tenant".equals(key) || "group".equals(key)) {
				signMap.put(key, iter.next());
			} else {
				iter.next();
			}
		}
		String resource = "";
		if (signMap.size() > 1) {
			resource = signMap.get("tenant") + "+" + signMap.get("group");
		} else if (!StringUtils.isBlank(signMap.get("group"))) {
			resource = signMap.get("group");
		} else {
			resource = signMap.get("tenant");
		}
		return getSignHeaders(resource, secretKey);
	}

	public static String getSk() {
		return CredentialService.getInstance().getCredential().getSecretKey();
	}

	public static String getAk() {
		return CredentialService.getInstance().getCredential().getAccessKey();
	}
	
	public static String signWithhmacSHA1Encrypt(String encryptText, String encryptKey) {
		try {
			byte[] data = encryptKey.getBytes("UTF-8");
			// ��ݸ���ֽ����鹹��һ����Կ,�ڶ�����ָ��һ����Կ�㷨�����
			SecretKey secretKey = new SecretKeySpec(data, "HmacSHA1");
			// ���һ��ָ�� Mac �㷨 �� Mac ����
			Mac mac = Mac.getInstance("HmacSHA1");
			// �ø���Կ��ʼ�� Mac ����
			mac.init(secretKey);
			byte[] text = encryptText.getBytes("UTF-8");
			byte[] textFinal = mac.doFinal(text);
			// ��� Mac ����, base64���룬��byte����ת��Ϊ�ַ�
			return new String(Base64.encodeBase64(textFinal));
		} catch (Exception e) {
			throw new RuntimeException("signWithhmacSHA1Encrypt fail", e);
		}
	}
}
