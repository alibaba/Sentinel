package com.taobao.diamond.utils;

import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPUtil {

	public static boolean isIPV4(String addr) {
		if (null == addr) {
			return false;
		}
		String rexp = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";

		Pattern pat = Pattern.compile(rexp);

		Matcher mat = pat.matcher(addr);

		boolean ipAddress = mat.find();
		return ipAddress;
	}

	public static boolean isIPV6(String addr) {
		if (null == addr) {
			return false;
		}
		String rexp = "^([\\da-fA-F]{1,4}:){7}[\\da-fA-F]{1,4}$";

		Pattern pat = Pattern.compile(rexp);

		Matcher mat = pat.matcher(addr);

		boolean ipAddress = mat.find();
		return ipAddress;
	}

	/**
	 * 把IP地址转化为int
	 * @param ip
	 * @return int
	 */
	public static int ipToInt(String ip) {
		try {
			return bytesToInt(ipToBytesByInet(ip));
		} catch (Exception e) {
			throw new IllegalArgumentException(ip + " is invalid IP");
		}
	}

	/**
	 * 根据位运算把 byte[] -> int
	 * @param bytes
	 * @return int
	 */
	public static int bytesToInt(byte[] bytes) {
		int addr = bytes[3] & 0xFF;
		addr |= ((bytes[2] << 8) & 0xFF00);
		addr |= ((bytes[1] << 16) & 0xFF0000);
		addr |= ((bytes[0] << 24) & 0xFF000000);
		return addr;
	}

	/**
	 * 把IP地址转化为字节数组
	 * @param ip
	 * @return byte[]
	 */
	public static byte[] ipToBytesByInet(String ip) {
		try {
			return InetAddress.getByName(ip).getAddress();
		} catch (Exception e) {
			throw new IllegalArgumentException(ip + " is invalid IP");
		}
	}
}
