package com.alibaba.jvm.sandbox.qatest.core.util;

import com.alibaba.jvm.sandbox.api.event.Event;
import com.alibaba.jvm.sandbox.api.filter.Filter;
import com.alibaba.jvm.sandbox.api.listener.EventListener;
import com.alibaba.jvm.sandbox.api.listener.ext.AdviceListener;
import com.alibaba.jvm.sandbox.core.enhance.EventEnhancer;
import com.alibaba.jvm.sandbox.core.enhance.weaver.EventListenerHandlers;
import com.alibaba.jvm.sandbox.core.util.ObjectIDs;
import com.alibaba.jvm.sandbox.core.util.SandboxReflectUtils;
import com.alibaba.jvm.sandbox.core.util.SpyUtils;
import com.alibaba.jvm.sandbox.core.util.matcher.ExtFilterMatcher;
import com.alibaba.jvm.sandbox.core.util.matcher.MatchingResult;
import com.alibaba.jvm.sandbox.core.util.matcher.structure.ClassStructureFactory;
import com.alibaba.jvm.sandbox.qatest.core.enhance.listener.InterruptedAdviceAdapterListener;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.com.alibaba.jvm.sandbox.spy.Spy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.alibaba.jvm.sandbox.api.event.Event.Type.*;
import static com.alibaba.jvm.sandbox.api.filter.ExtFilter.ExtFilterFactory.make;
import static com.alibaba.jvm.sandbox.api.util.GaStringUtils.getJavaClassName;
import static com.alibaba.jvm.sandbox.core.CoreConfigure.toConfigure;
import static com.alibaba.jvm.sandbox.qatest.core.util.QaClassUtils.toByteArray;
import static com.alibaba.jvm.sandbox.qatest.core.util.QaClassUtils.toResourceName;
import static org.apache.commons.lang3.ArrayUtils.toArray;

/**
 * JVM帮助类，能模拟一个JVM对类的管理行为
 */
public class JvmHelper {

    private final String namespace;
    private final PrivateClassLoader classLoader
            = new PrivateClassLoader();

    public JvmHelper(final String namespace) {
        this.namespace = namespace;
        SpyUtils.init(namespace);
        toConfigure(String.format(";namespace=%s;", namespace), "");
    }

    public JvmHelper defineClass(final Class<?> clazz) throws IOException, InvocationTargetException, IllegalAccessException {
        return defineClass(
                getJavaClassName(clazz),
                toByteArray(clazz)
        );
    }

    public JvmHelper defineClass(final String javaClassName,
                                 final byte[] byteCodeArray) throws InvocationTargetException, IllegalAccessException {
        classLoader.defineClass(javaClassName, byteCodeArray);
        return this;
    }

    public static class Transformer {

        private final Filter filter;
        private final EventListener listener;
        private final Event.Type[] eventTypes;

        public Transformer(final Filter filter,
                           final EventListener listener,
                           final Event.Type... eventTypes) {
            this.filter = filter;
            this.listener = listener;
            this.eventTypes = eventTypes;
        }

        public Transformer(final Filter filter,
                           final AdviceListener listener,
                           final Event.Type... eventTypes) {
            this.filter = filter;
            this.listener = new InterruptedAdviceAdapterListener(listener);
            final List<Event.Type> eventTypeList = new ArrayList<Event.Type>();
            CollectionUtils.addAll(eventTypeList, toArray(BEFORE, RETURN, THROWS, IMMEDIATELY_THROWS, IMMEDIATELY_RETURN));
            CollectionUtils.addAll(eventTypeList, eventTypes);
            this.eventTypes = eventTypeList.toArray(new Event.Type[]{});
        }

        public byte[] transform(final String namespace,
                                final ClassLoader loader,
                                final byte[] byteCodes) {

            final MatchingResult matchingResult = new ExtFilterMatcher(make(filter))
                    .matching(ClassStructureFactory.createClassStructure(byteCodes, loader));

            final int listenerId = ObjectIDs.instance.identity(listener);
            EventListenerHandlers.getSingleton().active(
                    listenerId,
                    listener,
                    eventTypes
            );

            if (matchingResult.isMatched()) {
                return new EventEnhancer().toByteCodeArray(
                        loader,
                        byteCodes,
                        matchingResult.getBehaviorSignCodes(),
                        namespace,
                        listenerId,
                        eventTypes
                );
            } else {
                return byteCodes;
            }
        }

    }

    public JvmHelper defineClass(final Class<?> clazz,
                                 final Filter filter,
                                 final EventListener listener,
                                 final Event.Type... eventType) throws IllegalAccessException, IOException, InvocationTargetException {
        return defineClass(new Class<?>[]{clazz}, filter, listener, eventType);
    }

    public JvmHelper defineClass(final Class<?>[] classes,
                                 final Filter filter,
                                 final EventListener listener,
                                 final Event.Type... eventTypes) throws IllegalAccessException, IOException, InvocationTargetException {
        return defineClass(
                classes,
                new Transformer(filter, listener, eventTypes)
        );
    }

    public JvmHelper defineClass(final Class<?> clazz,
                                 final Transformer... transformers) throws IOException, InvocationTargetException, IllegalAccessException {
        return defineClass(new Class<?>[]{clazz}, transformers);
    }

    public JvmHelper defineClass(final Class<?>[] classes,
                                 final Transformer... transformers) throws IOException, InvocationTargetException, IllegalAccessException {
        for (final Class<?> clazz : classes) {
            final String javaClassName = getJavaClassName(clazz);
            byte[] byteCodes = toByteArray(clazz);
            for (final Transformer transformer : transformers) {
                byteCodes = transformer.transform(namespace, classLoader, byteCodes);
            }
            defineClass(javaClassName, byteCodes);
        }
        return this;
    }

    public Class<?> loadClass(String javaClassName) throws ClassNotFoundException {
        return classLoader.loadClass(javaClassName);
    }


    /**
     * 私有的ClassLoader
     */
    static class PrivateClassLoader extends ClassLoader {

        private final Map<String, byte[]> javaClassByteArrayMap
                = new HashMap<String, byte[]>();

        private final Set<Class<?>> classes = new LinkedHashSet<Class<?>>();

        public PrivateClassLoader() {
        }

        public PrivateClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

            for (final Class<?> clazz : classes) {
                if (StringUtils.equals(name, getJavaClassName(clazz))) {
                    return clazz;
                }
            }

            final Class<?> loadedClass = findLoadedClass(name);
            if (loadedClass == null) {
                try {
                    final Class<?> aClass = findClass(name);
                    if (resolve) {
                        resolveClass(aClass);
                    }
                    return aClass;
                } catch (Exception e) {
                    return super.loadClass(name, resolve);
                }
            } else {
                return loadedClass;
            }
        }

        public Class<?> defineClass(final String javaClassName,
                                    final byte[] classByteArray) throws InvocationTargetException, IllegalAccessException {
            javaClassByteArrayMap.put(toResourceName(javaClassName), classByteArray);
            final Class<?> clazz = SandboxReflectUtils.defineClass(this, javaClassName, classByteArray);
            classes.add(clazz);
            return clazz;
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            if (javaClassByteArrayMap.containsKey(name)) {
                return new ByteArrayInputStream(javaClassByteArrayMap.get(name));
            }
            return super.getResourceAsStream(name);
        }

    }


    public static JvmHelper createJvm(final String namespace) {
        return new JvmHelper(StringUtils.isBlank(namespace) ? "default" : namespace);
    }

    public static JvmHelper createJvm() {
        Spy.isSpyThrowException = true;
        return createJvm("default");
    }

}
