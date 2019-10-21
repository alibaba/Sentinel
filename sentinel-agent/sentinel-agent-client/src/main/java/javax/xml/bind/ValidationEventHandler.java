/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.xml.bind;

/**
 * A basic event handler interface for validation errors.
 *
 * <p>
 * If an application needs to implement customized event handling, it must
 * implement this interface and then register it with either the 
 * {@link Unmarshaller#setEventHandler(ValidationEventHandler) Unmarshaller}, 
 * the {@link Validator#setEventHandler(ValidationEventHandler) Validator}, or 
 * the {@link Marshaller#setEventHandler(ValidationEventHandler) Marshaller}.  
 * The JAXB Provider will then report validation errors and warnings encountered
 * during the unmarshal, marshal, and validate operations to these event 
 * handlers.
 *
 * <p>
 * If the <tt>handleEvent</tt> method throws an unchecked runtime exception,
 * the JAXB Provider must treat that as if the method returned false, effectively
 * terminating whatever operation was in progress at the time (unmarshal, 
 * validate, or marshal).
 * 
 * <p>
 * Modifying the Java content tree within your event handler is undefined
 * by the specification and may result in unexpected behaviour.
 *
 * <p>
 * Failing to return false from the <tt>handleEvent</tt> method after 
 * encountering a fatal error is undefined by the specification and may result 
 * in unexpected behavior.
 *
 * <p>
 * <b>Default Event Handler</b>
 * <blockquote>
 *    See: <a href="Validator.html#defaulthandler">Validator javadocs</a>
 * </blockquote>
 *
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li><li>Kohsuke Kawaguchi, Sun Microsystems, Inc.</li><li>Joe Fialli, Sun Microsystems, Inc.</li></ul> 
 * @version $Revision: 1.1 $
 * @see Unmarshaller
 * @see Validator
 * @see Marshaller
 * @see ValidationEvent
 * @see javax.xml.bind.util.ValidationEventCollector
 * @since JAXB1.0
 */
public interface ValidationEventHandler {
    /**
     * Receive notification of a validation warning or error.  
     * 
     * The ValidationEvent will have a 
     * {@link ValidationEventLocator ValidationEventLocator} embedded in it that 
     * indicates where the error or warning occurred.
     *
     * <p>
     * If an unchecked runtime exception is thrown from this method, the JAXB
     * provider will treat it as if the method returned false and interrupt
     * the current unmarshal, validate, or marshal operation.
     * 
     * @param event the encapsulated validation event information.  It is a 
     * provider error if this parameter is null.
     * @return true if the JAXB Provider should attempt to continue the current
     *         unmarshal, validate, or marshal operation after handling this 
     *         warning/error, false if the provider should terminate the current 
     *         operation with the appropriate <tt>UnmarshalException</tt>, 
     *         <tt>ValidationException</tt>, or <tt>MarshalException</tt>.
     * @throws IllegalArgumentException if the event object is null.
     */
    public boolean handleEvent( ValidationEvent event );
    
}

