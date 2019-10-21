package com.alibaba.acm.shaded.org.codehaus.jackson.map.jsontype.impl;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.type.TypeFactory;
import com.alibaba.acm.shaded.org.codehaus.jackson.type.JavaType;
import com.alibaba.acm.shaded.org.codehaus.jackson.annotate.JsonTypeInfo;

public class MinimalClassNameIdResolver
    extends ClassNameIdResolver
{
    /**
     * Package name of the base class, to be used for determining common
     * prefix that can be omitted from included type id.
     * Does not include the trailing dot.
     */
    protected final String _basePackageName;

    /**
     * Same as {@link #_basePackageName}, but includes trailing dot.
     */
    protected final String _basePackagePrefix;
    
    protected MinimalClassNameIdResolver(JavaType baseType, TypeFactory typeFactory)
    {
        super(baseType, typeFactory);
        String base = baseType.getRawClass().getName();
        int ix = base.lastIndexOf('.');
        if (ix < 0) { // can this ever occur?
            _basePackageName = "";
            _basePackagePrefix = ".";
        } else {
            _basePackagePrefix = base.substring(0, ix+1);
            _basePackageName = base.substring(0, ix);
        }
    }

    @Override
    public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.MINIMAL_CLASS; }
    
    @Override
    public String idFromValue(Object value)
    {
        String n = value.getClass().getName();
        if (n.startsWith(_basePackagePrefix)) {
            // note: we will leave the leading dot in there
            return n.substring(_basePackagePrefix.length()-1);
        }
        return n;
    }

    @Override
    public JavaType typeFromId(String id)
    {
        if (id.startsWith(".")) {
            StringBuilder sb = new StringBuilder(id.length() + _basePackageName.length());
            if  (_basePackageName.length() == 0) {
                // no package; must remove leading '.' from id
                sb.append(id.substring(1));
            } else {
                // otherwise just concatenate package, with leading-dot-partial name
                sb.append(_basePackageName).append(id);
            }
            id = sb.toString();
        }
        return super.typeFromId(id);
    }
}
