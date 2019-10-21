package com.alibaba.middleware.tls.util;

public class TlsConstants {
	
	
	
	/**
	 * 加密测试模式，此场景下证书不需要进行解密
	 */
	public static final String TLS_TEST = "tls.test";

    /**
     * 是否开启流量加密，可选为 true | false, 通过 Java 启动参数 -Dtls.enable 设置
     * <br/>
     *
     * 其中：<br/>
     * <i>如果参数为 true 时，将开启中间件各个组件的流量加密</i>
     * <i>如果参数为 false 时，将关闭中间件各个组件的流量加密</i>
     */
	public static final String TLS_ENABLE = "tls.enable";
	
	/**
	 * server 配置该参数，对客户端认证，开启双向认证
	 */
	public static final String TWO_WAY_AUTH = "tls.twoway.auth";

    /**
     * 客户端是否服务端证书进行严格校验
     *
     */
	public static final String CLIENT_AUTH = "tls.client.authServer";

    /**
     * 客户端ROOT证书或者中间证书路径
     */
	public static final String CLIENT_TRUST_CERT = "tls.client.trustCertPath";

    /**
     * 服务端是否对客户端证书进行严格校验
     */
	public static final String SERVER_AUTH = "tls.server.authClient";

    /**
     * 客户端ROOT证书或者中间件证书路径
     */
	public static final String SERVER_TRUST_CERT = "tls.server.trustCertPath";

    /**
     * 客户端端签发的证书存放路径
     */
	public static final String CLIENT_CERTPATH = "tls.client.certPath";

    /**
     * 客户端私钥存放路径
     */
	public static final String CLIENT_KEYPATH = "tls.client.keyPath";

    /**
     * 服务端签发的证书存放路径
     */
	public static final String SERVER_CERTPATH = "tls.server.certPath";

    /**
     * 服务端私钥存放路径
     */
	public static final String SERVER_KEYPATH = "tls.server.keyPath";

    /**
     *日志level default INFO
     */
	public static final String ENV_LOG_LEVEL = "tls.log.level";

    /**
     *日志备份大小 default 250M
     */
	public static final String ENV_LOG_SIZE = "tls.log.size";

    /**
     *日志备份数量 default 20个
     */
	public static final String ENV_LOG_BACKUP = "tls.log.backup";
	
	/**
	 * 加密协议TLS版本
	 */
	public static final String PROTOCOL = "TLSv1.2";
}
