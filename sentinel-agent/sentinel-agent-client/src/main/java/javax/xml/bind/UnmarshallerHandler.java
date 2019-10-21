/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

import org.xml.sax.ContentHandler;

/**
 * Unmarshaller implemented as SAX ContentHandler.
 * 
 * <p>
 * Applications can use this interface to use their JAXB provider as a component 
 * in an XML pipeline.  For example:
 * 
 * <pre>
 *       JAXBContext context = JAXBContext.newInstance( "org.acme.foo" );
 *
 *       Unmarshaller unmarshaller = context.createUnmarshaller();
 * 
 *       UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();
 *
 *       SAXParserFactory spf = SAXParserFactory.newInstance();
 *       spf.setNamespaceAware( true );
 * 
 *       XMLReader xmlReader = spf.newSAXParser().getXMLReader();
 *       xmlReader.setContentHandler( unmarshallerHandler );
 *       xmlReader.parse(new InputSource( new FileInputStream( XML_FILE ) ) );
 *
 *       MyObject myObject= (MyObject)unmarshallerHandler.getResult();                          
 * </pre>
 * 
 * <p>
 * This interface is reusable: even if the user fails to unmarshal
 * an object, s/he can still start a new round of unmarshalling.
 * 
 * @author <ul><li>Kohsuke KAWAGUCHI, Sun Microsystems, Inc.</li></ul>
 * @version $Revision: 1.2 $ $Date: 2006/03/08 16:55:17 $
 * @see Unmarshaller#getUnmarshallerHandler()
 * @since JAXB1.0
 */
public interface UnmarshallerHandler extends ContentHandler
{
    /**
     * Obtains the unmarshalled result.
     * 
     * This method can be called only after this handler
     * receives the endDocument SAX event.
     * 
     * @exception IllegalStateException
     *      if this method is called before this handler
     *      receives the endDocument event.
     * 
     * @exception JAXBException
     *      if there is any unmarshalling error.
     *      Note that the implementation is allowed to throw SAXException
     *      during the parsing when it finds an error.
     * 
     * @return
     *      always return a non-null valid object which was unmarshalled.
     */
    Object getResult() throws JAXBException, IllegalStateException;
}
