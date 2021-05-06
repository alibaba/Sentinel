/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.spi;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.io.*;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple SPI loading facility (refactored since 1.8.1).
 *
 * <p>SPI is short for Service Provider Interface.</p>
 *
 * <p>
 * Service is represented by a single type, that is, a single interface or an abstract class.
 * Provider is implementations of Service, that is, some classes which implement the interface or extends the abstract class.
 * </p>
 *
 * <p>
 * For Service type:
 * Must interface or abstract class.
 * </p>
 *
 * <p>
 * For Provider class:
 * Must have a zero-argument constructor so that they can be instantiated during loading.
 * </p>
 *
 * <p>
 * For Provider configuration file:
 * 1. The file contains a list of fully-qualified binary names of concrete provider classes, one per line.
 * 2. Space and tab characters surrounding each name, as well as blank lines, are ignored.
 * 3. The comment line character is #, all characters following it are ignored.
 * </p>
 *
 *
 * <p>{@code SpiLoader} provide common functions, such as:</p>
 * <ul>
 * <li>Load all Provider instance unsorted/sorted list.</li>
 * <li>Load highest/lowest order priority instance.</li>
 * <li>Load first-found or default instance.</li>
 * <li>Load instance by alias name or provider class.</li>
 * </ul>
 *
 * @author Eric Zhao
 * @author cdfive
 * @since 1.4.0
 * @see com.alibaba.csp.sentinel.spi.Spi
 * @see java.util.ServiceLoader
 */
public final class SpiLoader<S> {

    // Default path for the folder of Provider configuration file
    private static final String SPI_FILE_PREFIX = "META-INF/services/";

    // Cache the SpiLoader instances, key: classname of Service, value: SpiLoader instance
    private static final ConcurrentHashMap<String, SpiLoader> SPI_LOADER_MAP = new ConcurrentHashMap<>();

    // Cache the classes of Provider
    private final List<Class<? extends S>> classList = Collections.synchronizedList(new ArrayList<Class<? extends S>>());

    // Cache the sorted classes of Provider
    private final List<Class<? extends S>> sortedClassList = Collections.synchronizedList(new ArrayList<Class<? extends S>>());

    /**
     * Cache the classes of Provider, key: aliasName, value: class of Provider.
     * Note: aliasName is the value of {@link Spi} when the Provider class has {@link Spi} annotation and value is not empty,
     * otherwise use classname of the Provider.
     */
    private final ConcurrentHashMap<String, Class<? extends S>> classMap = new ConcurrentHashMap<>();

    // Cache the singleton instance of Provider, key: classname of Provider, value: Provider instance
    private final ConcurrentHashMap<String, S> singletonMap = new ConcurrentHashMap<>();

    // Whether this SpiLoader has been loaded, that is, loaded the Provider configuration file
    private final AtomicBoolean loaded = new AtomicBoolean(false);

    // Default provider class
    private Class<? extends S> defaultClass = null;

    // The Service class, must be interface or abstract class
    private Class<S> service;

    /**
     * Create SpiLoader instance via Service class
     * Cached by className, and load from cache first
     *
     * @param service Service class
     * @param <T>     Service type
     * @return SpiLoader instance
     */
    public static <T> SpiLoader<T> of(Class<T> service) {
        AssertUtil.notNull(service, "SPI class cannot be null");
        AssertUtil.isTrue(service.isInterface() || Modifier.isAbstract(service.getModifiers()),
                "SPI class[" + service.getName() + "] must be interface or abstract class");

        String className = service.getName();
        SpiLoader<T> spiLoader = SPI_LOADER_MAP.get(className);
        if (spiLoader == null) {
            synchronized (SpiLoader.class) {
                spiLoader = SPI_LOADER_MAP.get(className);
                if (spiLoader == null) {
                    SPI_LOADER_MAP.putIfAbsent(className, new SpiLoader<>(service));
                    spiLoader = SPI_LOADER_MAP.get(className);
                }
            }
        }

        return spiLoader;
    }

    /**
     * Reset and clear all SpiLoader instances.
     * Package privilege, used only in test cases.
     */
    synchronized static void resetAndClearAll() {
        Set<Map.Entry<String, SpiLoader>> entries = SPI_LOADER_MAP.entrySet();
        for (Map.Entry<String, SpiLoader> entry : entries) {
            SpiLoader spiLoader = entry.getValue();
            spiLoader.resetAndClear();
        }
        SPI_LOADER_MAP.clear();
    }

    // Private access
    private SpiLoader(Class<S> service) {
        this.service = service;
    }

    /**
     * Load all Provider instances of the specified Service
     *
     * @return Provider instances list
     */
    public List<S> loadInstanceList() {
        load();

        return createInstanceList(classList);
    }

    /**
     * Load all Provider instances of the specified Service, sorted by order value in class's {@link Spi} annotation
     *
     * @return Sorted Provider instances list
     */
    public List<S> loadInstanceListSorted() {
        load();

        return createInstanceList(sortedClassList);
    }

    /**
     * Load highest order priority instance, order value is defined in class's {@link Spi} annotation
     *
     * @return Provider instance of highest order priority
     */
    public S loadHighestPriorityInstance() {
        load();

        if (sortedClassList.size() == 0) {
            return null;
        }

        Class<? extends S> highestClass = sortedClassList.get(0);
        return createInstance(highestClass);
    }

    /**
     * Load lowest order priority instance, order value is defined in class's {@link Spi} annotation
     *
     * @return Provider instance of lowest order priority
     */
    public S loadLowestPriorityInstance() {
        load();

        if (sortedClassList.size() == 0) {
            return null;
        }

        Class<? extends S> lowestClass = sortedClassList.get(sortedClassList.size() - 1);
        return createInstance(lowestClass);
    }

    /**
     * Load the first-found Provider instance
     *
     * @return Provider instance of first-found specific
     */
    public S loadFirstInstance() {
        load();

        if (classList.size() == 0) {
            return null;
        }

        Class<? extends S> serviceClass = classList.get(0);
        S instance = createInstance(serviceClass);
        return instance;
    }

    /**
     * Load the first-found Provider instance,if not found, return default Provider instance
     *
     * @return Provider instance
     */
    public S loadFirstInstanceOrDefault() {
        load();

        for (Class<? extends S> clazz : classList) {
            if (defaultClass == null || clazz != defaultClass) {
                return createInstance(clazz);
            }
        }

        return loadDefaultInstance();
    }

    /**
     * Load default Provider instance
     * Provider class with @Spi(isDefault = true)
     *
     * @return default Provider instance
     */
    public S loadDefaultInstance() {
        load();

        if (defaultClass == null) {
            return null;
        }

        return createInstance(defaultClass);
    }

    /**
     * Load instance by specific class type
     *
     * @param clazz class type
     * @return Provider instance
     */
    public S loadInstance(Class<? extends S> clazz) {
        AssertUtil.notNull(clazz, "SPI class cannot be null");

        if (clazz.equals(service)) {
            fail(clazz.getName() + " is not subtype of " + service.getName());
        }

        load();

        if (!classMap.containsValue(clazz)) {
            fail(clazz.getName() + " is not Provider class of " + service.getName() + ",check if it is in the SPI configuration file?");
        }

        return createInstance(clazz);
    }

    /**
     * Load instance by aliasName of Provider class
     *
     * @param aliasName aliasName of Provider class
     * @return Provider instance
     */
    public S loadInstance(String aliasName) {
        AssertUtil.notEmpty(aliasName, "aliasName cannot be empty");

        load();

        Class<? extends S> clazz = classMap.get(aliasName);
        if (clazz == null) {
            fail("no Provider class's aliasName is " + aliasName);
        }

        return createInstance(clazz);
    }

    /**
     * Reset and clear all fields of current SpiLoader instance and remove instance in SPI_LOADER_MAP
     */
    synchronized void resetAndClear() {
        SPI_LOADER_MAP.remove(service.getName());
        classList.clear();
        sortedClassList.clear();
        classMap.clear();
        singletonMap.clear();
        defaultClass = null;
        loaded.set(false);
    }

    /**
     * Load the Provider class from Provider configuration file
     */
    public void load() {
        if (!loaded.compareAndSet(false, true)) {
            return;
        }

        String fullFileName = SPI_FILE_PREFIX + service.getName();
        ClassLoader classLoader;
        if (SentinelConfig.shouldUseContextClassloader()) {
            classLoader = Thread.currentThread().getContextClassLoader();
        } else {
            classLoader = service.getClassLoader();
        }
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        Enumeration<URL> urls = null;
        try {
            urls = classLoader.getResources(fullFileName);
        } catch (IOException e) {
            fail("Error locating SPI configuration file, filename=" + fullFileName + ", classloader=" + classLoader, e);
        }

        if (urls == null || !urls.hasMoreElements()) {
            RecordLog.warn("No SPI configuration file, filename=" + fullFileName + ", classloader=" + classLoader);
            return;
        }

        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();

            InputStream in = null;
            BufferedReader br = null;
            try {
                in = url.openStream();
                br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                String line;
                while ((line = br.readLine()) != null) {
                    if (StringUtil.isBlank(line)) {
                        // Skip blank line
                        continue;
                    }

                    line = line.trim();
                    int commentIndex = line.indexOf("#");
                    if (commentIndex == 0) {
                        // Skip comment line
                        continue;
                    }

                    if (commentIndex > 0) {
                        line = line.substring(0, commentIndex);
                    }
                    line = line.trim();

                    Class<S> clazz = null;
                    try {
                        clazz = (Class<S>) Class.forName(line, false, classLoader);
                    } catch (ClassNotFoundException e) {
                        fail("class " + line + " not found", e);
                    }

                    if (!service.isAssignableFrom(clazz)) {
                        fail("class " + clazz.getName() + "is not subtype of " + service.getName() + ",SPI configuration file=" + fullFileName);
                    }

                    classList.add(clazz);
                    Spi spi = clazz.getAnnotation(Spi.class);
                    String aliasName = spi == null || "".equals(spi.value()) ? clazz.getName() : spi.value();
                    if (classMap.containsKey(aliasName)) {
                        Class<? extends S> existClass = classMap.get(aliasName);
                        fail("Found repeat alias name for " + clazz.getName() + " and "
                                + existClass.getName() + ",SPI configuration file=" + fullFileName);
                    }
                    classMap.put(aliasName, clazz);

                    if (spi != null && spi.isDefault()) {
                        if (defaultClass != null) {
                            fail("Found more than one default Provider, SPI configuration file=" + fullFileName);
                        }
                        defaultClass = clazz;
                    }

                    RecordLog.info("[SpiLoader] Found SPI implementation for SPI {}, provider={}, aliasName={}"
                            + ", isSingleton={}, isDefault={}, order={}",
                        service.getName(), line, aliasName
                            , spi == null ? true : spi.isSingleton()
                            , spi == null ? false : spi.isDefault()
                            , spi == null ? 0 : spi.order());
                }
            } catch (IOException e) {
                fail("error reading SPI configuration file", e);
            } finally {
                closeResources(in, br);
            }
        }

        sortedClassList.addAll(classList);
        Collections.sort(sortedClassList, new Comparator<Class<? extends S>>() {
            @Override
            public int compare(Class<? extends S> o1, Class<? extends S> o2) {
                Spi spi1 = o1.getAnnotation(Spi.class);
                int order1 = spi1 == null ? 0 : spi1.order();

                Spi spi2 = o2.getAnnotation(Spi.class);
                int order2 = spi2 == null ? 0 : spi2.order();

                return Integer.compare(order1, order2);
            }
        });
    }

    @Override
    public String toString() {
        return "com.alibaba.csp.sentinel.spi.SpiLoader[" + service.getName() + "]";
    }

    /**
     * Create Provider instance list
     *
     * @param clazzList class types of Providers
     * @return Provider instance list
     */
    private List<S> createInstanceList(List<Class<? extends S>> clazzList) {
        if (clazzList == null || clazzList.size() == 0) {
            return Collections.emptyList();
        }

        List<S> instances = new ArrayList<>(clazzList.size());
        for (Class<? extends S> clazz : clazzList) {
            S instance = createInstance(clazz);
            instances.add(instance);
        }
        return instances;
    }

    /**
     * Create Provider instance
     *
     * @param clazz class type of Provider
     * @return Provider class
     */
    private S createInstance(Class<? extends S> clazz) {
        Spi spi = clazz.getAnnotation(Spi.class);
        boolean singleton = true;
        if (spi != null) {
            singleton = spi.isSingleton();
        }
        return createInstance(clazz, singleton);
    }

    /**
     * Create Provider instance
     *
     * @param clazz     class type of Provider
     * @param singleton if instance is singleton or prototype
     * @return Provider instance
     */
    private S createInstance(Class<? extends S> clazz, boolean singleton) {
        S instance = null;
        try {
            if (singleton) {
                instance = singletonMap.get(clazz.getName());
                if (instance == null) {
                    synchronized (this) {
                        instance = singletonMap.get(clazz.getName());
                        if (instance == null) {
                            instance = service.cast(clazz.newInstance());
                            singletonMap.put(clazz.getName(), instance);
                        }
                    }
                }
            } else {
                instance = service.cast(clazz.newInstance());
            }
        } catch (Throwable e) {
            fail(clazz.getName() + " could not be instantiated");
        }
        return instance;
    }

    /**
     * Close all resources
     *
     * @param closeables {@link Closeable} resources
     */
    private void closeResources(Closeable... closeables) {
        if (closeables == null || closeables.length == 0) {
            return;
        }

        Exception firstException = null;
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (Exception e) {
                if (firstException != null) {
                    firstException = e;
                }
            }
        }
        if (firstException != null) {
            fail("error closing resources", firstException);
        }
    }

    /**
     * Throw {@link SpiLoaderException} with message
     *
     * @param msg error message
     */
    private void fail(String msg) {
        RecordLog.error(msg);
        throw new SpiLoaderException("[" + service.getName() + "]" + msg);
    }

    /**
     * Throw {@link SpiLoaderException} with message and Throwable
     *
     * @param msg error message
     */
    private void fail(String msg, Throwable e) {
        RecordLog.error(msg, e);
        throw new SpiLoaderException("[" + service.getName() + "]" + msg, e);
    }
}
