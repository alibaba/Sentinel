/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.xml.bind.helpers;

import javax.xml.bind.ParseConversionEvent;
import javax.xml.bind.ValidationEventLocator;

/**
 * Default implementation of the ParseConversionEvent interface.
 * 
 * <p>
 * JAXB providers are allowed to use whatever class that implements
 * the ValidationEvent interface. This class is just provided for a
 * convenience.
 *
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li></ul> 
 * @version $Revision: 1.1 $
 * @see javax.xml.bind.ParseConversionEvent
 * @see javax.xml.bind.Validator
 * @see javax.xml.bind.ValidationEventHandler
 * @see javax.xml.bind.ValidationEvent
 * @see javax.xml.bind.ValidationEventLocator
 * @since JAXB1.0
 */
public class ParseConversionEventImpl
    extends ValidationEventImpl
    implements ParseConversionEvent {

    /**
     * Create a new ParseConversionEventImpl.
     * 
     * @param _severity The severity value for this event.  Must be one of
     * ValidationEvent.WARNING, ValidationEvent.ERROR, or 
     * ValidationEvent.FATAL_ERROR
     * @param _message The text message for this event - may be null.
     * @param _locator The locator object for this event - may be null.
     * @throws IllegalArgumentException if an illegal severity field is supplied
     */
    public ParseConversionEventImpl( int _severity, String _message,
                                      ValidationEventLocator _locator) {
            
        super(_severity, _message, _locator);
    }

    /**
     * Create a new ParseConversionEventImpl.
     * 
     * @param _severity The severity value for this event.  Must be one of
     * ValidationEvent.WARNING, ValidationEvent.ERROR, or 
     * ValidationEvent.FATAL_ERROR
     * @param _message The text message for this event - may be null.
     * @param _locator The locator object for this event - may be null.
     * @param _linkedException An optional linked exception that may provide
     * additional information about the event - may be null.
     * @throws IllegalArgumentException if an illegal severity field is supplied
     */
    public ParseConversionEventImpl( int _severity, String _message,
                                      ValidationEventLocator _locator,
                                      Throwable _linkedException) {
            
        super(_severity, _message, _locator, _linkedException);
    }

}
