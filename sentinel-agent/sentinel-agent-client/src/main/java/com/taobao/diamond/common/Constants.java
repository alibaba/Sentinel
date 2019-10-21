package com.taobao.diamond.common;

public class Constants {

    public static final String CLIENT_VERSION_HEADER = "Client-Version";

    public static final String CLIENT_VERSION = "3.0.0";

    public static int DATA_IN_BODY_VERSION = 204;
    
    public static final String DEFAULT_GROUP = "DEFAULT_GROUP";
    
    // server端配置文件基目录
    public static final String BASE_DIR = "config-data";
    
    // server端配置文件备份目录
    // FIAME , user.home
    public static final String CONFIG_BAK_DIR = System.getProperty("user.home", "/home/admin") + "/diamond/bak_data";

    public static final String APPNAME = "AppName";
    
    public static final String UNKNOWN_APP = "UnknownApp";

    public static final String DEFAULT_DOMAINNAME = "commonconfig.config-host.taobao.com";

    public static final String DAILY_DOMAINNAME = "commonconfig.taobao.net";

    public static final int DEFAULT_PORT = 8080;

    public static final String NULL = "";

    public static final String DATAID = "dataId";

    public static final String GROUP = "group";

    public static final String LAST_MODIFIED = "Last-Modified";

    public static final String ACCEPT_ENCODING = "Accept-Encoding";

    public static final String CONTENT_ENCODING = "Content-Encoding";

    public static final String PROBE_MODIFY_REQUEST = "Probe-Modify-Request";

    public static final String PROBE_MODIFY_RESPONSE = "Probe-Modify-Response";

    public static final String PROBE_MODIFY_RESPONSE_NEW = "Probe-Modify-Response-New";

    public static final String USE_ZIP = "true";

    public static final String CONTENT_MD5 = "Content-MD5";

    public static final String CONFIG_VERSION = "Config-Version";

    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    public static final String SPACING_INTERVAL = "client-spacing-interval";

    public static final int ASYNC_UPDATE_ADDRESS_INTERVAL = 300; // 秒

    public static final int POLLING_INTERVAL_TIME = 15;// 秒

    public static final int ONCE_TIMEOUT = 2000;// 毫秒

    public static final int CONN_TIMEOUT = 2000;// 毫秒

    public static final int SO_TIMEOUT = 60000; // 毫秒

    public static final int RECV_WAIT_TIMEOUT = ONCE_TIMEOUT * 5;// 毫秒

    public static final String HTTP_URI_FILE = "/diamond-server/config.co";

    public static final String CONFIG_HTTP_URI_FILE = "/diamond-server/serveraddress";

    public static final String HTTP_URI_LOGIN = "/diamond-server/login.do";

    public static final String BASESTONE_POST_URI = "/diamond-server/basestone.do";
    public static final String DATUM_POST_URI = "/diamond-server/datum.do";

    public static final String HTTP_URI_ACK = "/diamond-server/ack.do";

    public static final String ENCODE = "GBK";

    public static final String ENCODE_UTF_8 = "UTF-8";

    public static final String MAP_FILE = "map-file.js";

    public static final int FLOW_CONTROL_THRESHOLD = 20;

    public static final int FLOW_CONTROL_SLOT = 10;

    public static final int FLOW_CONTROL_INTERVAL = 1000;

    public static final String LINE_SEPARATOR = Character.toString((char) 1);

    public static final String WORD_SEPARATOR = Character.toString((char) 2);

    public static final String DEFAULT_DIAMOND_CLUSTER = "diamond";

    public static final String DEFAULT_BASESTONE_CLUSTER = "basestone";

    public static final String BASE_URI = "/diamond-server";

    public static final String DEFAULT_USERNAME = "admin";

    public static final String DEFAULT_PASSWORD = "admin";

    public static final String DIAMOND_LINE_SEPARATOR = "\r\n";

    // 从网络获取数据的总时间, 当超过此时间, 不再从网络获取数据, 单位ms
    public static final long TOTALTIME_FROM_SERVER = 10000;
    // 从网络获取数据的总时间的失效时间, 单位ms
    public static final long TOTALTIME_INVALID_THRESHOLD = 60000;

    /*
     * 批量操作时, 单条数据的状态码
     */
    // 发生异常
    public static final int BATCH_OP_ERROR = -1;
    public static final String BATCH_OP_ERROR_IO_MSG = "get config dump error";
    public static final String BATCH_OP_ERROR_CONFLICT_MSG = "config get conflicts";
    // 查询成功, 数据存在
    public static final int BATCH_QUERY_EXISTS = 1;
    public static final String BATCH_QUERY_EXISTS_MSG = "config exits";
    // 查询成功, 数据不存在
    public static final int BATCH_QUERY_NONEXISTS = 2;
    public static final String BATCH_QUERY_NONEEXISTS_MSG = "config not exits";
    // 新增成功
    public static final int BATCH_ADD_SUCCESS = 3;
    // 更新成功
    public static final int BATCH_UPDATE_SUCCESS = 4;

    public static final int MAX_UPDATE_FAIL_COUNT = 5;
    public static final int MAX_UPDATEALL_FAIL_COUNT = 5;
    public static final int MAX_REMOVE_FAIL_COUNT = 5;
    public static final int MAX_REMOVEALL_FAIL_COUNT = 5;
    public static final int MAX_NOTIFY_COUNT = 5;
    public static final int MAX_ADDACK_COUNT = 5;

    // 数据的初始版本号
    public static final int FIRST_VERSION = 1;
    // 数据被删除的标识版本号
    public static final int POISON_VERSION = -1;
    // 写磁盘文件时, 临时版本号
    public static final int TEMP_VERSION = 0;
    
    public static final int GETCONFIG_LOCAL_SERVER_SNAPSHOT = 1;//获取数据的顺序：容灾文件-> 服务器 -> 本地缓存
    public static final int GETCONFIG_LOCAL_SNAPSHOT_SERVER = 2;//获取数据的顺序：容灾文件-> 本地缓存 -> 服务器
    public static final int GETCONFIG_ONLY_SERVER           = 3;//获取数据的顺序：容灾文件-> 服务器

    public static final String CLIENT_APPNAME_HEADER = "Diamond-Client-AppName";
    public static final String CLIENT_REQUEST_TS_HEADER = "Diamond-Client-RequestTS";
    public static final String CLIENT_REQUEST_TOKEN_HEADER = "Diamond-Client-RequestToken";

    public static final String REQUEST_IDENTITY = "Diamond-Request-Identity";  // diamond-client, diamond-sdk请求server服务的身份
    public static final String ACL_RESPONSE = "Diamond-ACL-Response";  // 鉴权结果信息
    
    public static final int ATOMIC_MAX_SIZE = 1000;

    public static final String CIPHER_PREFIX = "cipher-";

    public static final String CIPHER_KMS_AES_128_PREFIX = "cipher-kms-aes-128-";

    public static final String KMS_KEY_SPEC_AES_128 = "AES_128";
}
