package com.taobao.csp.sentinel.dashboard.rule.datasource.zookeeper;

import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * 类名称：ZookeeperConfigUtil
 * 类描述：
 * 开发人：朱水平【Tank】
 * 创建时间：2018/12/21.10:17
 * 修改备注：
 *
 * @version 1.0.0
 */
public class ZookeeperConfigUtil {

    public static final String GROUP_ID = "/sentinel";
    public static final String FLOW_RULES = "/rules/flow-rules";
    public static final String DEGRADE_RULES="/rules/degrade-rules";


    public static String getPath(String groupId, String dataId) {
        String path = ZookeeperConfigUtil.GROUP_ID;
        if (groupId.startsWith("/")) {
            path += groupId;
        } else {
            path += "/" + groupId;
        }
        if (StringUtil.isNotBlank(dataId)){
            if (dataId.startsWith("/")) {
                path += dataId;
            } else {
                path += "/" + dataId;
            }
        }
        return path;
    }

    private ZookeeperConfigUtil() {}

}
