package com.alibaba.acm.shaded.org.codehaus.jackson.io;

import java.io.*;

/**
 * Handler class that can be used to decorate output destinations.
 * Typical use is to use a filter abstraction (filtered output stream,
 * writer) around original output destination, and apply additional
 * processing during write operations.
 * 
 * @since 1.8
 */
public abstract class OutputDecorator
{
    /**
     * Method called by {@link com.alibaba.acm.shaded.org.codehaus.jackson.JsonFactory} instance when
     * creating generator for given {@link OutputStream}, when this decorator
     * has been registered.
     * 
     * @param ctxt IO context in use (provides access to declared encoding)
     * @param out Original output destination
     * 
     * @return OutputStream to use; either passed in argument, or something that
     *   calls it
     */
    public abstract OutputStream decorate(IOContext ctxt, OutputStream out)
        throws IOException;

    /**
     * Method called by {@link com.alibaba.acm.shaded.org.codehaus.jackson.JsonFactory} instance when
     * creating generator for given {@link Writer}, when this decorator
     * has been registered.
     * 
     * @param ctxt IO context in use (provides access to declared encoding)
     * @param w Original output writer
     * 
     * @return Writer to use; either passed in argument, or something that calls it
     */
    public abstract Writer decorate(IOContext ctxt, Writer w) throws IOException;
}
