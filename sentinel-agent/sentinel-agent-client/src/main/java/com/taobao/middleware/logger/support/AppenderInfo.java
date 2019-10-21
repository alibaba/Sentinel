package com.taobao.middleware.logger.support;

import java.util.HashMap;

/**
 * @author zhuyong on 2017/6/30.
 */
public class AppenderInfo extends HashMap {

    private static String name = "name";
    private static String type = "type";
    private static String file = "file";

    public String getName() {
        return (String) get(AppenderInfo.name);
    }

    public void setName(String name) {
        put(AppenderInfo.name, name);
    }

    public void setType(String type) {
        put(AppenderInfo.type, type);
    }

    public void setFile(String file) {
        put(AppenderInfo.file, file);
    }

    public void withDetail(String name, Object value) {
        put(name, value);
    }
}
