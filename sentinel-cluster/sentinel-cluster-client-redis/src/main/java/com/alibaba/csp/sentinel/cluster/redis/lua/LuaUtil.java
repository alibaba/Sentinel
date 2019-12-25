package com.alibaba.csp.sentinel.cluster.redis.lua;

import com.alibaba.csp.sentinel.util.function.Function;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class LuaUtil {
    public static final String FLOW_CHECKER_LUA = "flow_checker";
    private static Map<String, String> luaCodeMapper = new HashMap<>();
    private static Map<String, String> luaShaMapper;

    public static String loadLuaCodeIfNeed(String luaSign) {
        String lua = luaCodeMapper.get(luaSign);
        if(lua == null) {
            synchronized (luaCodeMapper) {
                lua = luaCodeMapper.get(luaSign);
                if(lua == null) {
                    lua = loadLua(luaSign);
                    luaCodeMapper.put(luaSign, lua);
                }
            }
        }
        return lua;
    }

    private static String loadLua(String luaId)  {
        try (InputStream input = LuaUtil.class.getResourceAsStream("/lua/" + luaId + ".lua")) {
            StringBuilder out = new StringBuilder();
            byte[] b = new byte[1024];
            for (int n; (n = input.read(b)) != -1; ) {
                out.append(new String(b, 0, n));
            }
            return out.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("cannot load luaCode:" + luaId,e);
        }
    }

    public static void resetLuaSha() {
        synchronized (LuaUtil.class) {
            luaShaMapper = new HashMap<>();
        }
    }

    public static String loadLuaShaIfNeed(String luaCode, Function<String, String> loadFunc) {
        String sha = luaShaMapper.get(luaCode);
        if(sha == null) {
            synchronized (luaShaMapper) {
                sha = luaShaMapper.get(luaCode);
                if(sha == null) {
                    sha = loadFunc.apply(luaCode);
                    luaShaMapper.put(luaCode, sha);
                }
            }
        }
        return sha;
    }

    public static String toLuaParam(Object val, Object slotKey) {
        return  val + "{" + slotKey + "}" ;
    }
}

