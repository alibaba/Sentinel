package com.alibaba.acm.shaded.org.codehaus.jackson;

import java.io.IOException;

/**
 * Interface for objects that implement pretty printer functionality, such
 * as indentation.
 * Pretty printers are used to add white space in output JSON content,
 * to make results more human readable. Usually this means things like adding
 * linefeeds and indentation.
 */
public interface PrettyPrinter
{
    /*
    /**********************************************************
    /* First methods that act both as events, and expect
    /* output for correct functioning (i.e something gets
    /* output even when not pretty-printing)
    /**********************************************************
     */

    // // // Root-level handling:

    /**
     * Method called after a root-level value has been completely
     * output, and before another value is to be output.
     *<p>
     * Default
     * handling (without pretty-printing) will output a space, to
     * allow values to be parsed correctly. Pretty-printer is
     * to output some other suitable and nice-looking separator
     * (tab(s), space(s), linefeed(s) or any combination thereof).
     */
    public void writeRootValueSeparator(JsonGenerator jg)
        throws IOException, JsonGenerationException;

    // // Object handling

    /**
     * Method called when an Object value is to be output, before
     * any fields are output.
     *<p>
     * Default handling (without pretty-printing) will output
     * the opening curly bracket.
     * Pretty-printer is
     * to output a curly bracket as well, but can surround that
     * with other (white-space) decoration.
     */
    public void writeStartObject(JsonGenerator jg)
        throws IOException, JsonGenerationException;

    /**
     * Method called after an Object value has been completely output
     * (minus closing curly bracket).
     *<p>
     * Default handling (without pretty-printing) will output
     * the closing curly bracket.
     * Pretty-printer is
     * to output a curly bracket as well, but can surround that
     * with other (white-space) decoration.
     *
     * @param nrOfEntries Number of direct members of the array that
     *   have been output
     */
    public void writeEndObject(JsonGenerator jg, int nrOfEntries)
        throws IOException, JsonGenerationException;

    /**
     * Method called after an object entry (field:value) has been completely
     * output, and before another value is to be output.
     *<p>
     * Default handling (without pretty-printing) will output a single
     * comma to separate the two. Pretty-printer is
     * to output a comma as well, but can surround that with other
     * (white-space) decoration.
     */
    public void writeObjectEntrySeparator(JsonGenerator jg)
        throws IOException, JsonGenerationException;

    /**
     * Method called after an object field has been output, but
     * before the value is output.
     *<p>
     * Default handling (without pretty-printing) will output a single
     * colon to separate the two. Pretty-printer is
     * to output a colon as well, but can surround that with other
     * (white-space) decoration.
     */
    public void writeObjectFieldValueSeparator(JsonGenerator jg)
        throws IOException, JsonGenerationException;

    // // // Array handling

    /**
     * Method called when an Array value is to be output, before
     * any member/child values are output.
     *<p>
     * Default handling (without pretty-printing) will output
     * the opening bracket.
     * Pretty-printer is
     * to output a bracket as well, but can surround that
     * with other (white-space) decoration.
     */
    public void writeStartArray(JsonGenerator jg)
        throws IOException, JsonGenerationException;

    /**
     * Method called after an Array value has been completely output
     * (minus closing bracket).
     *<p>
     * Default handling (without pretty-printing) will output
     * the closing bracket.
     * Pretty-printer is
     * to output a bracket as well, but can surround that
     * with other (white-space) decoration.
     *
     * @param nrOfValues Number of direct members of the array that
     *   have been output
     */
    public void writeEndArray(JsonGenerator jg, int nrOfValues)
        throws IOException, JsonGenerationException;

    /**
     * Method called after an array value has been completely
     * output, and before another value is to be output.
     *<p>
     * Default handling (without pretty-printing) will output a single
     * comma to separate the two. Pretty-printer is
     * to output a comma as well, but can surround that with other
     * (white-space) decoration.
     */
    public void writeArrayValueSeparator(JsonGenerator jg)
        throws IOException, JsonGenerationException;

    /*
    /**********************************************************
    /* Then events that by default do not produce any output
    /* but that are often overridden to add white space
    /* in pretty-printing mode
    /**********************************************************
     */

    /**
     * Method called after array start marker has been output,
     * and right before the first value is to be output.
     * It is <b>not</b> called for arrays with no values.
     *<p>
     * Default handling does not output anything, but pretty-printer
     * is free to add any white space decoration.
     */
    public void beforeArrayValues(JsonGenerator jg)
        throws IOException, JsonGenerationException;

    /**
     * Method called after object start marker has been output,
     * and right before the field name of the first entry is
     * to be output.
     * It is <b>not</b> called for objects without entries.
     *<p>
     * Default handling does not output anything, but pretty-printer
     * is free to add any white space decoration.
     */
    public void beforeObjectEntries(JsonGenerator jg)
        throws IOException, JsonGenerationException;
}

