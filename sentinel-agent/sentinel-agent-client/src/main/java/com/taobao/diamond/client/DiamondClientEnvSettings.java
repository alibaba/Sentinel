package com.taobao.diamond.client;

public class DiamondClientEnvSettings {

	public static final String ADDRESS_SERVER_DOMAIN = "address.server.domain";
	public static final String ADDRESS_SERVER_PORT = "address.server.port";
	public static final String DIAMOND_CLIENT_LOG_PATH = "JM.LOG.PATH";
	public static final String MAX_LOG_FILE_RETAIN_COUNT = "JM.LOG.RETAIN.COUNT";
	public static final String JM_LOG_FILE_SIZE = "JM.LOG.FILE.SIZE";
	public static final String DIAMOND_CLIENT_SNAPSHOT_PATH = "JM.SNAPSHOT.PATH";
	public static final String DIAMOND_CONNECT_TIMEOUT_IN_MILLS = "DIAMOND.CONNECT.TIMEOUT";


	public static void setSystemProperties(String key, String value) {
		System.setProperty(key, value);
	}

}
