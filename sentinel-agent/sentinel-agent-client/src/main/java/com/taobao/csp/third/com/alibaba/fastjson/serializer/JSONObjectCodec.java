package com.taobao.csp.third.com.alibaba.fastjson.serializer;

import com.taobao.csp.third.com.alibaba.fastjson.serializer.JSONSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.MapSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.ObjectSerializer;
import com.taobao.csp.third.com.alibaba.fastjson.serializer.SerializeWriter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

public class JSONObjectCodec implements ObjectSerializer {
	public final static com.taobao.csp.third.com.alibaba.fastjson.serializer.JSONObjectCodec instance = new com.taobao.csp.third.com.alibaba.fastjson.serializer.JSONObjectCodec();

	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
			throws IOException {
		SerializeWriter out = serializer.out;
		MapSerializer mapSerializer = MapSerializer.instance;

		try {
			Field mapField = object.getClass().getDeclaredField("map");
			if (Modifier.isPrivate(mapField.getModifiers())) {
				mapField.setAccessible(true);
			}

			Object map = mapField.get(object);
			mapSerializer.write(serializer, map, fieldName, fieldType, features);

		} catch (Exception e) {
			out.writeNull();
		}
	}
}
