package com.alibaba.acm.shaded.org.codehaus.jackson.map.util;

import java.lang.annotation.Annotation;

/**
 * Interface that defines interface for collection of annotations.
 *<p>
 * Standard mutable implementation is {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotationMap}
 * 
 * @since 1.7
 */
public interface Annotations
{
    /**
     * Main access method used to find value for given annotation.
     */
    public <A extends Annotation> A get(Class<A> cls);

    /**
     * Returns number of annotation entries in this collection.
     */
    public int size();
}
