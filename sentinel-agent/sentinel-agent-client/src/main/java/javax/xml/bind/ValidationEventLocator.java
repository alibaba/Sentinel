/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.xml.bind;

/**
 * Encapsulate the location of a ValidationEvent.
 *
 * <p>
 * The <tt>ValidationEventLocator</tt> indicates where the <tt>ValidationEvent
 * </tt> occurred.  Different fields will be set depending on the type of 
 * validation that was being performed when the error or warning was detected.  
 * For example, on-demand validation would produce locators that contained 
 * references to objects in the Java content tree while unmarshal-time 
 * validation would produce locators containing information appropriate to the 
 * source of the XML data (file, url, Node, etc).
 *
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li><li>Kohsuke Kawaguchi, Sun Microsystems, Inc.</li><li>Joe Fialli, Sun Microsystems, Inc.</li></ul> 
 * @version $Revision: 1.1 $
 * @see Validator
 * @see ValidationEvent
 * @since JAXB1.0
 */
public interface ValidationEventLocator {

    /**
     * Return the name of the XML source as a URL if available
     *
     * @return the name of the XML source as a URL or null if unavailable
     */
    public java.net.URL getURL();
    
    /**
     * Return the byte offset if available
     *
     * @return the byte offset into the input source or -1 if unavailable
     */
    public int getOffset();
    
    /**
     * Return the line number if available
     *
     * @return the line number or -1 if unavailable 
     */
    public int getLineNumber();
    
    /**
     * Return the column number if available
     *
     * @return the column number or -1 if unavailable
     */
    public int getColumnNumber();
    
    /**
     * Return a reference to the object in the Java content tree if available
     *
     * @return a reference to the object in the Java content tree or null if
     *         unavailable
     */
    public java.lang.Object getObject();
    
    /**
     * Return a reference to the DOM Node if available
     *
     * @return a reference to the DOM Node or null if unavailable 
     */
    public org.w3c.dom.Node getNode();
    
}
