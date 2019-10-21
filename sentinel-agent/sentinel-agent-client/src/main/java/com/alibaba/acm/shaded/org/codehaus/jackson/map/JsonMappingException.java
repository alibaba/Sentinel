package com.alibaba.acm.shaded.org.codehaus.jackson.map;

import java.io.Serializable;
import java.util.*;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;

/**
 * Checked exception used to signal fatal problems with mapping of
 * content.
 *<p>
 * One additional feature is the ability to denote relevant path
 * of references (during serialization/deserialization) to help in
 * troubleshooting.
 */
public class JsonMappingException
    extends JsonProcessingException
{
    private static final long serialVersionUID = 1L;

    /**
     * Let's limit length of reference chain, to limit damage in cases
     * of infinite recursion.
     */
    final static int MAX_REFS_TO_LIST = 1000;

    /*
    /**********************************************************
    /* Helper classes
    /**********************************************************
     */

    /**
     * Simple bean class used to contain references. References
     * can be added to indicate execution/reference path that
     * lead to the problem that caused this exception to be
     * thrown.
     */
    public static class Reference implements Serializable
    {
        private static final long serialVersionUID = 1L;

        /**
         * Object through which reference was resolved. Can be either
         * actual instance (usually the case for serialization), or
         * Class (usually the case for deserialization).
         */
        protected Object _from;

        /**
         * Name of field (for beans) or key (for Maps) that is part
         * of the reference. May be null for Collection types (which
         * generally have {@link #_index} defined), or when resolving
         * Map classes without (yet) having an instance to operate on.
         */
        protected String _fieldName;

        /**
         * Index within a {@link Collection} instance that contained
         * the reference; used if index is relevant and available.
         * If either not applicable, or not available, -1 is used to
         * denote "not known".
         */
        protected int _index = -1;

        /**
         * Default constructor for deserialization/sub-classing purposes
         */
        protected Reference() { }

        public Reference(Object from) { _from = from; }

        public Reference(Object from, String fieldName) {
            _from = from;
            if (fieldName == null) {
                throw new NullPointerException("Can not pass null fieldName");
            }
            _fieldName = fieldName;
        }

        public Reference(Object from, int index) {
            _from = from;
            _index = index;
        }

        public void setFrom(Object o) { _from = o; }
        public void setFieldName(String n) { _fieldName = n; }
        public void setIndex(int ix) { _index = ix; }

        public Object getFrom() { return _from; }
        public String getFieldName() { return _fieldName; }
        public int getIndex() { return _index; }

        @Override public String toString() {
            StringBuilder sb = new StringBuilder();
            Class<?> cls = (_from instanceof Class<?>) ?
                ((Class<?>)_from) : _from.getClass();
            /* Hmmh. Although Class.getName() is mostly ok, it does look
             * butt-ugly for arrays. So let's use getSimpleName() instead;
             * but have to prepend package name too.
             */
            Package pkg = cls.getPackage();
            if (pkg != null) {
                sb.append(pkg.getName());
                sb.append('.');
            }
            sb.append(cls.getSimpleName());
            sb.append('[');
            if (_fieldName != null) {
                sb.append('"');
                sb.append(_fieldName);
                sb.append('"');
            } else if (_index >= 0) {
                sb.append(_index);
            } else {
                sb.append('?');
            }
            sb.append(']');
            return sb.toString();
        }
    }

    /*
    /**********************************************************
    /* State/configuration
    /**********************************************************
     */

    /**
     * Path through which problem that triggering throwing of
     * this exception was reached.
     */
    protected LinkedList<Reference> _path;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public JsonMappingException(String msg)
    {
        super(msg);
    }

    public JsonMappingException(String msg, Throwable rootCause)
    {
        super(msg, rootCause);
    }

    public JsonMappingException(String msg, JsonLocation loc)
    {
        super(msg, loc);
    }

    public JsonMappingException(String msg, JsonLocation loc, Throwable rootCause)
    {
        super(msg, loc, rootCause);
    }

    public static JsonMappingException from(JsonParser jp, String msg)
    {
        return new JsonMappingException(msg, jp.getTokenLocation());
    }

    public static JsonMappingException from(JsonParser jp, String msg,
                                            Throwable problem)
    {
        return new JsonMappingException(msg, jp.getTokenLocation(), problem);
    }

    /**
     * Method that can be called to either create a new JsonMappingException
     * (if underlying exception is not a JsonMappingException), or augment
     * given exception with given path/reference information.
     *
     * This version of method is called when the reference is through a
     * non-indexed object, such as a Map or POJO/bean.
     */
    public static JsonMappingException wrapWithPath(Throwable src, Object refFrom,
                                                    String refFieldName)
    {
        return wrapWithPath(src, new Reference(refFrom, refFieldName));
    }

    /**
     * Method that can be called to either create a new JsonMappingException
     * (if underlying exception is not a JsonMappingException), or augment
     * given exception with given path/reference information.
     *
     * This version of method is called when the reference is through an
     * index, which happens with arrays and Collections.
     */
    public static JsonMappingException wrapWithPath(Throwable src, Object refFrom,
                                                    int index)
    {
        return wrapWithPath(src, new Reference(refFrom, index));
    }

    /**
     * Method that can be called to either create a new JsonMappingException
     * (if underlying exception is not a JsonMappingException), or augment
     * given exception with given path/reference information.
     */
    public static JsonMappingException wrapWithPath(Throwable src, Reference ref)
    {
        JsonMappingException jme;
        if (src instanceof JsonMappingException) {
            jme = (JsonMappingException) src;
        } else {
            String msg = src.getMessage();
            /* Related to [JACKSON-62], let's use a more meaningful placeholder
             * if all we have is null
             */
            if (msg == null || msg.length() == 0) {
                msg = "(was "+src.getClass().getName()+")";
            }
            jme = new JsonMappingException(msg, null, src);
        }
        jme.prependPath(ref);
        return jme;
    }

    /*
    /**********************************************************
    /* Accessors/mutators
    /**********************************************************
     */

    public List<Reference> getPath()
    {
        if (_path == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(_path);
    }

    /**
     * Method called to prepend a reference information in front of
     * current path
     */
    public void prependPath(Object referrer, String fieldName)
    {
        Reference ref = new Reference(referrer, fieldName);
        prependPath(ref);
    }
    /**
     * Method called to prepend a reference information in front of
     * current path
     */
    public void prependPath(Object referrer, int index)
    {
        Reference ref = new Reference(referrer, index);
        prependPath(ref);
    }

    public void prependPath(Reference r)
    {
        if (_path == null) {
            _path = new LinkedList<Reference>();
        }
        /* Also: let's not increase without bounds. Could choose either
         * head or tail; tail is easier (no need to ever remove), as
         * well as potentially more useful so let's use it:
         */
        if (_path.size() < MAX_REFS_TO_LIST) {
            _path.addFirst(r);
        }
    }

    /*
    /**********************************************************
    /* Overridden methods
    /**********************************************************
     */

    /**
     * Method is overridden so that we can properly inject description
     * of problem path, if such is defined.
     */
    @Override
    public String getMessage()
    {
        /* First: if we have no path info, let's just use parent's
         * definition as is
         */
        String msg = super.getMessage();
        if (_path == null) {
            return msg;
        }
        /* 19-Feb-2009, tatu: Null and empty messages are not very
         *   useful (plus nulls would lead to NPEs), so let's
         *   use something else
         */
        StringBuilder sb = (msg == null) ? new StringBuilder() : new StringBuilder(msg);
        /* 18-Feb-2009, tatu: initially there was a linefeed between
         *    message and path reference; but unfortunately many systems
         *   (loggers, junit) seem to assume linefeeds are only added to
         *   separate stack trace.
         */
        sb.append(" (through reference chain: ");
        _appendPathDesc(sb);
        sb.append(')');
        return sb.toString();
    }

    @Override
    public String toString()
    {
        return getClass().getName()+": "+getMessage();
    }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    protected void _appendPathDesc(StringBuilder sb)
    {
        Iterator<Reference> it = _path.iterator();
        while (it.hasNext()) {
            sb.append(it.next().toString());
            if (it.hasNext()) {
                sb.append("->");
            }
        }
    }
}
