/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.xml.bind;

/**
 * This exception indicates that an error has occurred while performing
 * an unmarshal operation that prevents the JAXB Provider from completing
 * the operation.
 * 
 * <p>
 * The <tt>ValidationEventHandler</tt> can cause this exception to be thrown
 * during the unmarshal operations.  See 
 * {@link ValidationEventHandler#handleEvent(ValidationEvent)
 * ValidationEventHandler.handleEvent(ValidationEvent)}.
 * 
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li></ul>
 * @version $Revision: 1.1 $
 * @see JAXBException
 * @see Unmarshaller
 * @see ValidationEventHandler
 * @since JAXB1.0
 */
public class UnmarshalException extends JAXBException {

    /** 
     * Construct an UnmarshalException with the specified detail message.  The 
     * errorCode and linkedException will default to null.
     *
     * @param message a description of the exception
     */
    public UnmarshalException( String message ) {
        this( message, null, null );
    }

    /** 
     * Construct an UnmarshalException with the specified detail message and vendor 
     * specific errorCode.  The linkedException will default to null.
     *
     * @param message a description of the exception
     * @param errorCode a string specifying the vendor specific error code
     */
    public UnmarshalException( String message, String errorCode ) {
        this( message, errorCode, null );
    }

    /** 
     * Construct an UnmarshalException with a linkedException.  The detail message and
     * vendor specific errorCode will default to null.
     *
     * @param exception the linked exception
     */
    public UnmarshalException( Throwable exception ) {
        this( null, null, exception );
    }
    
    /** 
     * Construct an UnmarshalException with the specified detail message and 
     * linkedException.  The errorCode will default to null.
     *
     * @param message a description of the exception
     * @param exception the linked exception
     */
    public UnmarshalException( String message, Throwable exception ) {
        this( message, null, exception );
    }
    
    /** 
     * Construct an UnmarshalException with the specified detail message, vendor 
     * specific errorCode, and linkedException.
     *
     * @param message a description of the exception
     * @param errorCode a string specifying the vendor specific error code
     * @param exception the linked exception
     */
    public UnmarshalException( String message, String errorCode, Throwable exception ) {
        super( message, errorCode, exception );
    }

}


