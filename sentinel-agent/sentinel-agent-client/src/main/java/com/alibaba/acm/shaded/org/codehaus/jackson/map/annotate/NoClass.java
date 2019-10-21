package com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate;

/**
 * Marker class used with annotations to indicate "no class". This is
 * a silly but necessary work-around -- annotations can not take nulls
 * as either default or explicit values. Hence for class values we must
 * explicitly use a bogus placeholder to denote equivalent of
 * "no class" (for which 'null' is usually the natural choice).
 *<p>
 * Note before version 1.4, this marker class was under
 * "com.alibaba.acm.shaded.org.codehaus.jackson.annotate". However, since it is only used
 * by annotations in "com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate" (and not externally
 * exposed), it was moved to that package as of version 1.5.
 */
public final class NoClass
{
    private NoClass() { }
}

