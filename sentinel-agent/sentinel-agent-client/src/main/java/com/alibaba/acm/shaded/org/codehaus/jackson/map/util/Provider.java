package com.alibaba.acm.shaded.org.codehaus.jackson.map.util;

import java.util.*;

/**
* Simple helper class used for decoupling instantiation of
* optionally loaded handlers, like deserializers and deserializers
* for libraries that are only present on some platforms.
 * 
 * @author tatu
 *
 * @param <T> Type of objects provided
 */
public interface Provider<T>
{
    /**
     * Method used to request provider to provide entries it has
     */
    public Collection<T> provide();
}

