package com.taobao.csp.third.com.alibaba.fastjson.parser;

import java.lang.reflect.Type;

public class ParseContext {

    public Object             object;
    public final com.taobao.csp.third.com.alibaba.fastjson.parser.ParseContext parent;
    public final Object       fieldName;
    public Type               type;
    private transient String  path;

    public ParseContext(com.taobao.csp.third.com.alibaba.fastjson.parser.ParseContext parent, Object object, Object fieldName){
        this.parent = parent;
        this.object = object;
        this.fieldName = fieldName;
    }

    public String toString() {
        if (path == null) {
            if (parent == null) {
                path = "$";
            } else {
                if (fieldName instanceof Integer) {
                    path = parent.toString() + "[" + fieldName + "]";
                } else {
                    path = parent.toString() + "." + fieldName;
                }
            }
        }

        return path;
    }
}
