package com.alibaba.acm.shaded.org.codehaus.jackson.map.util;

import com.alibaba.acm.shaded.org.codehaus.jackson.io.SerializedString;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.AnnotationIntrospector;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.MapperConfig;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedClass;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.BasicBeanDescription;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.ClassKey;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;

/**
 * Helper class for caching resolved root names.
 */
public class RootNameLookup
{
    /**
     * For efficient operation, let's try to minimize number of times we
     * need to introspect root element name to use.
     */
    protected LRUMap<ClassKey,SerializedString> _rootNames;

    public RootNameLookup() { }

    public SerializedString findRootName(JavaType rootType, MapperConfig<?> config)
    {
        return findRootName(rootType.getRawClass(), config);
    }

    public synchronized SerializedString findRootName(Class<?> rootType, MapperConfig<?> config)
    {
        ClassKey key = new ClassKey(rootType);

        if (_rootNames == null) {
            _rootNames = new LRUMap<ClassKey,SerializedString>(20, 200);
        } else {
            SerializedString name = _rootNames.get(key);
            if (name != null) {
                return name;
            }
        }
        BasicBeanDescription beanDesc = (BasicBeanDescription) config.introspectClassAnnotations(rootType);
        AnnotationIntrospector intr = config.getAnnotationIntrospector();
        AnnotatedClass ac = beanDesc.getClassInfo();
        String nameStr = intr.findRootName(ac);
        // No answer so far? Let's just default to using simple class name
        if (nameStr == null) {
            // Should we strip out enclosing class tho? For now, nope:
            nameStr = rootType.getSimpleName();
        }
        SerializedString name = new SerializedString(nameStr);
        _rootNames.put(key, name);
        return name;
    }
}
