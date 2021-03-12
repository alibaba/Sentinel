package com.alibaba.csp.sentinel.util;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Operates on classes without using reflection.
 * This class handles invalid null inputs as best it can. Each method documents its behavior in more detail.
 * The notion of a canonical name includes the human readable name for the type, for example int[]. The non-canonical method variants work with the JVM names, such as [I.
 * Since:
 * 2.0
 *
 * @author masonma
 */
public class ClassUtils {
    ClassUtils() {
    }

    public static final char PACKAGE_SEPARATOR_CHAR = '.';

    /**
     * The inner class separator character: <code>'$' == {@value}</code>.
     */
    public static final char INNER_CLASS_SEPARATOR_CHAR = '$';

    private static final Map<String, Class<?>> namePrimitiveMap = new HashMap<>();

    static {
        namePrimitiveMap.put("boolean", Boolean.TYPE);
        namePrimitiveMap.put("byte", Byte.TYPE);
        namePrimitiveMap.put("char", Character.TYPE);
        namePrimitiveMap.put("short", Short.TYPE);
        namePrimitiveMap.put("int", Integer.TYPE);
        namePrimitiveMap.put("long", Long.TYPE);
        namePrimitiveMap.put("double", Double.TYPE);
        namePrimitiveMap.put("float", Float.TYPE);
        namePrimitiveMap.put("void", Void.TYPE);
    }

    private static final Map<String, String> abbreviationMap;

    /**
     * Feed abbreviation maps
     */
    static {
        final Map<String, String> m = new HashMap<>();
        m.put("int", "I");
        m.put("boolean", "Z");
        m.put("float", "F");
        m.put("long", "J");
        m.put("short", "S");
        m.put("byte", "B");
        m.put("double", "D");
        m.put("char", "C");
        abbreviationMap = Collections.unmodifiableMap(m);
    }

    public static Class<?> getClass(final String className) throws ClassNotFoundException {
        return getClass(className, true);
    }

    public static Class<?> getClass(final String className, final boolean initialize) throws ClassNotFoundException {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final ClassLoader loader = contextClassLoader == null ? ClassUtils.class.getClassLoader() : contextClassLoader;
        return getClass(loader, className, initialize);
    }

    public static Class<?> getClass(
            final ClassLoader classLoader, final String className, final boolean initialize) throws ClassNotFoundException {
        try {
            Class<?> clazz;
            if (namePrimitiveMap.containsKey(className)) {
                clazz = namePrimitiveMap.get(className);
            } else {
                clazz = Class.forName(toCanonicalName(className), initialize, classLoader);
            }
            return clazz;
        } catch (final ClassNotFoundException ex) {
            // allow path separators (.) as inner class name separators
            final int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);

            if (lastDotIndex != -1) {
                try {
                    return getClass(classLoader, className.substring(0, lastDotIndex) +
                                    INNER_CLASS_SEPARATOR_CHAR + className.substring(lastDotIndex + 1),
                            initialize);
                } catch (final ClassNotFoundException ex2) { // NOPMD
                    // ignore exception
                }
            }

            throw ex;
        }
    }

    private static String toCanonicalName(String className) {
        className = deleteWhitespace(className);
        AssertUtil.notNull(className, "className must not be null.");
        if (className.endsWith("[]")) {
            final StringBuilder classNameBuffer = new StringBuilder();
            while (className.endsWith("[]")) {
                className = className.substring(0, className.length() - 2);
                classNameBuffer.append("[");
            }
            final String abbreviation = abbreviationMap.get(className);
            if (abbreviation != null) {
                classNameBuffer.append(abbreviation);
            } else {
                classNameBuffer.append("L").append(className).append(";");
            }
            className = classNameBuffer.toString();
        }
        return className;
    }

    public static String deleteWhitespace(final String str) {
        if (StringUtil.isEmpty(str)) {
            return str;
        }
        final int sz = str.length();
        final char[] chs = new char[sz];
        int count = 0;
        for (int i = 0; i < sz; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                chs[count++] = str.charAt(i);
            }
        }
        if (count == sz) {
            return str;
        }
        return new String(chs, 0, count);
    }
}
