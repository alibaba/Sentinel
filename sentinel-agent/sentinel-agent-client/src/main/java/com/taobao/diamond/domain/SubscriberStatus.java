package com.taobao.diamond.domain;

/**
 * taobao.com Inc. Copyright (c) 1998-2101 All Rights Reserved.
 * <p/>
 * Project: diamond-utils
 * User: qiaoyi.dingqy
 * Date: 13-10-22
 * Time: 下午4:11
 */
public class SubscriberStatus {
    String groupKey;
    String md5;
    Long lastTime;
    Boolean status;
    String serverIp;

    public SubscriberStatus(){}

    public SubscriberStatus(String groupKey, Boolean status, String md5, Long lastTime) {
        this.groupKey = groupKey;
        this.md5 = md5;
        this.lastTime = lastTime;
        this.status = status;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public Long getLastTime() {
        return lastTime;
    }

    public void setLastTime(Long lastTime) {
        this.lastTime = lastTime;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getGroupKey() {

        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }
}
