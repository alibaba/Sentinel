package com.alibaba.csp.sentinel.fallback;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.util.ClassUtils;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName: FallbackRule
 * @Author Mason.MA
 * @Package com.alibaba.csp.sentinel.fallback
 * @Date 2021/3/10 14:22
 * @Version 1.0
 */
public class FallbackRule extends AbstractRule {

    private static ConcurrentHashMap<String, Class<?>> referenceClassMap = new ConcurrentHashMap<>(64);

    public FallbackRule() {
        super();
        setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
    }

    public FallbackRule(String resourceName) {
        super();
        setResource(resourceName);
        setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
    }

    /**
     * fallback string, json for good
     */
    private String fallback;
    /**
     * class of fallback string, if configured , use this class serialize json string
     */
    private String clazzReference = "";

    @Override
    public String toString() {
        return "FallbackRule{" +
                "resource='" + getResource() + '\'' +
                ", fallback='" + fallback + '\'' +
                ", clazzReference='" + clazzReference + '\'' +
                '}';
    }

    public String getFallback() {
        return fallback;
    }

    public FallbackRule setFallback(String fallback) {
        this.fallback = fallback;
        return this;
    }

    public String getClazzReference() {
        return clazzReference;
    }

    public FallbackRule setClazzReference(String clazzReference) {
        this.clazzReference = clazzReference;
        return this;
    }


    /**
     * get Class by name
     *
     * @param clazzReference example java.lang.String
     * @return Class
     */
    public static Class<?> getClass(String clazzReference) {
        try {
            if (null != referenceClassMap.get(clazzReference)) {
                return referenceClassMap.get(clazzReference);
            } else {
                Class<?> clazz = ClassUtils.getClass(clazzReference, true);
                referenceClassMap.put(clazzReference, clazz);
                return clazz;
            }
        } catch (ClassNotFoundException e) {
            RecordLog.warn(clazzReference + " ClassNotFoundException in current Classloader");
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        FallbackRule that = (FallbackRule) o;
        return Objects.equals(fallback, that.fallback) && Objects.equals(clazzReference, that.clazzReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fallback, clazzReference);
    }
}
