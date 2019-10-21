package com.taobao.diamond.utils;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.ObjectMapper;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.DeserializationConfig.Feature;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.TypeReference;


public class JSONUtils {

    static ObjectMapper mapper = new ObjectMapper();
    
    static {
    	mapper.disable(Feature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

	public static String serializeObject(Object o) throws IOException {
		return mapper.writeValueAsString(o);
	}

    public static Object deserializeObject(String s, Class<?> clazz) throws IOException {
        return mapper.readValue(s, clazz);
    }

    public static Object deserializeObject(String s, TypeReference<?> typeReference)
            throws IOException {
        return mapper.readValue(s, typeReference);
    }

    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    public static Object deserializeCollection(String s, JavaType type) throws IOException {
        return mapper.readValue(s, type);
    }

}
