package com.alibaba.middleware.tls;

import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import com.alibaba.middleware.tls.log.TlsLogger;

/**
 * 自定义HostnameVerifier,可以允许ip https访问
 * 
 * @author shenyu.yxl
 *
 */
public class SelfHostnameVerifier implements HostnameVerifier {

	private static final int IPV4_LENGTH = 4;
	private final HostnameVerifier hv;

	private static ConcurrentHashMap<String, Boolean> hosts =
			new ConcurrentHashMap<String, Boolean>();

	public SelfHostnameVerifier(HostnameVerifier hv) {
		this.hv = hv;
	}

	@Override
	public boolean verify(String hostname, SSLSession session) {

		if (isIPv4(hostname)) {
			return true;
		}

		return hv.verify(hostname, session);
	}

	private static boolean isIPv4(String host) {

		if (host == null || host.isEmpty()) {
			TlsLogger.warn("host is empty, isIPv4 = false");
			return false;
		}

		Boolean isIp = hosts.get(host);
		if (isIp != null) {
			return isIp;
		}

		String[] sections = host.split("\\.");
		isIp = verifySections(sections);

		hosts.putIfAbsent(host, isIp);

		return isIp;
	}

	private static boolean verifySections(String[] sections) {
		if (sections.length != IPV4_LENGTH) {
			String msg = "invalid section length, isIPv4 = false len(section): "
                    + sections.length;

			TlsLogger.warn(msg);
		}

		for (String section : sections) {
			int value = Integer.MAX_VALUE;

			try {
				value = Integer.parseInt(section);
			} catch (NumberFormatException e) {
			}

			if (value < 0 || value > 256) {
				String msg = "invalid ipv4 digit, isIPv4 = false section: " +
						section;
				TlsLogger.warn(msg);

				return false;
			}
		}

		return true;
	}
}
