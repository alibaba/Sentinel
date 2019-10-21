package com.alibaba.acm.shaded.org.codehaus.jackson.util;

import java.io.*;
import java.util.Arrays;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.impl.Indenter;

/**
 * Default {@link PrettyPrinter} implementation that uses 2-space
 * indentation with platform-default linefeeds.
 * Usually this class is not instantiated directly, but instead
 * method {@link JsonGenerator#useDefaultPrettyPrinter} is
 * used, which will use an instance of this class for operation.
 */
public class DefaultPrettyPrinter
    implements PrettyPrinter
{
    // // // Config, indentation

    /**
     * By default, let's use only spaces to separate array values.
     */
    protected Indenter _arrayIndenter = new FixedSpaceIndenter();

    /**
     * By default, let's use linefeed-adding indenter for separate
     * object entries. We'll further configure indenter to use
     * system-specific linefeeds, and 2 spaces per level (as opposed to,
     * say, single tabs)
     */
    protected Indenter _objectIndenter = new Lf2SpacesIndenter();

    // // // Config, other white space configuration

    /**
     * By default we will add spaces around colons used to
     * separate object fields and values.
     * If disabled, will not use spaces around colon.
     */
    protected boolean _spacesInObjectEntries = true;

    // // // State:

    /**
     * Number of open levels of nesting. Used to determine amount of
     * indentation to use.
     */
    protected int _nesting = 0;

    /*
    /**********************************************************
    /* Life-cycle (construct, configure)
    /**********************************************************
    */

    public DefaultPrettyPrinter() { }

    public void indentArraysWith(Indenter i)
    {
        _arrayIndenter = (i == null) ? new NopIndenter() : i;
    }

    public void indentObjectsWith(Indenter i)
    {
        _objectIndenter = (i == null) ? new NopIndenter() : i;
    }

    public void spacesInObjectEntries(boolean b) { _spacesInObjectEntries = b; }

    /*
    /**********************************************************
    /* PrettyPrinter impl
    /**********************************************************
     */

    @Override
    public void writeRootValueSeparator(JsonGenerator jg)
        throws IOException, JsonGenerationException
    {
        jg.writeRaw(' ');
    }

    @Override
    public void writeStartObject(JsonGenerator jg)
        throws IOException, JsonGenerationException
    {
        jg.writeRaw('{');
        if (!_objectIndenter.isInline()) {
            ++_nesting;
        }
    }

    @Override
    public void beforeObjectEntries(JsonGenerator jg)
        throws IOException, JsonGenerationException
    {
        _objectIndenter.writeIndentation(jg, _nesting);
    }

    /**
     * Method called after an object field has been output, but
     * before the value is output.
     *<p>
     * Default handling (without pretty-printing) will output a single
     * colon to separate the two. Pretty-printer is
     * to output a colon as well, but can surround that with other
     * (white-space) decoration.
     */
    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator jg)
        throws IOException, JsonGenerationException
    {
        if (_spacesInObjectEntries) {
            jg.writeRaw(" : ");
        } else {
            jg.writeRaw(':');
        }
    }

    /**
     * Method called after an object entry (field:value) has been completely
     * output, and before another value is to be output.
     *<p>
     * Default handling (without pretty-printing) will output a single
     * comma to separate the two. Pretty-printer is
     * to output a comma as well, but can surround that with other
     * (white-space) decoration.
     */
    @Override
    public void writeObjectEntrySeparator(JsonGenerator jg)
        throws IOException, JsonGenerationException
    {
        jg.writeRaw(',');
        _objectIndenter.writeIndentation(jg, _nesting);
    }

    @Override
    public void writeEndObject(JsonGenerator jg, int nrOfEntries)
        throws IOException, JsonGenerationException
    {
        if (!_objectIndenter.isInline()) {
            --_nesting;
        }
        if (nrOfEntries > 0) {
            _objectIndenter.writeIndentation(jg, _nesting);
        } else {
            jg.writeRaw(' ');
        }
        jg.writeRaw('}');
    }

    @Override
    public void writeStartArray(JsonGenerator jg)
        throws IOException, JsonGenerationException
    {
        if (!_arrayIndenter.isInline()) {
            ++_nesting;
        }
        jg.writeRaw('[');
    }

    @Override
    public void beforeArrayValues(JsonGenerator jg)
        throws IOException, JsonGenerationException
    {
        _arrayIndenter.writeIndentation(jg, _nesting);
    }

    /**
     * Method called after an array value has been completely
     * output, and before another value is to be output.
     *<p>
     * Default handling (without pretty-printing) will output a single
     * comma to separate the two. Pretty-printer is
     * to output a comma as well, but can surround that with other
     * (white-space) decoration.
     */
    @Override
    public void writeArrayValueSeparator(JsonGenerator jg)
        throws IOException, JsonGenerationException
    {
        jg.writeRaw(',');
        _arrayIndenter.writeIndentation(jg, _nesting);
    }

    @Override
    public void writeEndArray(JsonGenerator jg, int nrOfValues)
        throws IOException, JsonGenerationException
    {
        if (!_arrayIndenter.isInline()) {
            --_nesting;
        }
        if (nrOfValues > 0) {
            _arrayIndenter.writeIndentation(jg, _nesting);
        } else {
            jg.writeRaw(' ');
        }
        jg.writeRaw(']');
    }

    /*
    /**********************************************************
    /* Helper classes
    /**********************************************************
     */

    /**
     * Dummy implementation that adds no indentation whatsoever
     */
    public static class NopIndenter
        implements Indenter
    {
        public NopIndenter() { }
        @Override
        public void writeIndentation(JsonGenerator jg, int level) { }
        @Override
        public boolean isInline() { return true; }
    }

    /**
     * This is a very simple indenter that only every adds a
     * single space for indentation. It is used as the default
     * indenter for array values.
     */
    public static class FixedSpaceIndenter
        implements Indenter
    {
        public FixedSpaceIndenter() { }

        @Override
        public void writeIndentation(JsonGenerator jg, int level)
            throws IOException, JsonGenerationException
        {
            jg.writeRaw(' ');
        }

        @Override
        public boolean isInline() { return true; }
    }

    /**
     * Default linefeed-based indenter uses system-specific linefeeds and
     * 2 spaces for indentation per level.
     */
    public static class Lf2SpacesIndenter
        implements Indenter
    {
        final static String SYSTEM_LINE_SEPARATOR;
        static {
            String lf = null;
            try {
                lf = System.getProperty("line.separator");
            } catch (Throwable t) { } // access exception?
            SYSTEM_LINE_SEPARATOR = (lf == null) ? "\n" : lf;
        }

        final static int SPACE_COUNT = 64;
        final static char[] SPACES = new char[SPACE_COUNT];
        static {
            Arrays.fill(SPACES, ' ');
        }

        public Lf2SpacesIndenter() { }

        @Override
        public boolean isInline() { return false; }

        @Override
        public void writeIndentation(JsonGenerator jg, int level)
            throws IOException, JsonGenerationException
        {
            jg.writeRaw(SYSTEM_LINE_SEPARATOR);
            level += level; // 2 spaces per level
            while (level > SPACE_COUNT) { // should never happen but...
                jg.writeRaw(SPACES, 0, SPACE_COUNT); 
                level -= SPACES.length;
            }
            jg.writeRaw(SPACES, 0, level);
        }
    }
}
