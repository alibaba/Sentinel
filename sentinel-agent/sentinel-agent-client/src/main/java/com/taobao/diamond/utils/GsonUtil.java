package com.taobao.diamond.utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.taobao.diamond.domain.ConfigInfo4Beta;
import com.taobao.diamond.domain.RestResult;

import java.io.IOException;
import java.lang.reflect.Type;

public class GsonUtil {
    static Gson gson = new Gson();

    public static String serializeObject(Object o) {
        return gson.toJson(o);
    }

    public static Object deserializeObject(String s, Class<?> clazz)
            throws JsonSyntaxException {
        return gson.fromJson(s, clazz);
    }

    public static Object deserializeObject(String s, Type type)
            throws JsonSyntaxException {
        return gson.fromJson(s, type);
    }

    public static void main(String[] args) throws IOException {
        String json1 = "&quot;";

        String json = "{\"code\":200,\"message\":\"query beta ok\",\"data\":{\"id\":1113,\"dataId\":\"com.alibaba\",\"group\":\"tsing0\",\"content\":\"style=&quot;\",\"md5\":\"7cba9a5331136c4c45d1390945b9de4c\",\"tenant\":\"\",\"appName\":\"diamond\",\"betaIps\":\"127.0.0.1\"}}";

        RestResult<ConfigInfo4Beta> restResult = (RestResult<ConfigInfo4Beta>) GsonUtil.deserializeObject(json, new TypeToken<RestResult<ConfigInfo4Beta>>(){}.getType());
        System.out.println(restResult.getData());
        System.out.println("text = " + GsonUtil.serializeObject(json1));
        System.out.println("text = " + JSONUtils.serializeObject(json1));
    }
}


