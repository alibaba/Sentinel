/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.xml.bind.annotation;

/**
 * Enumeration of XML Schema namespace qualifications. 
 *
 * <p>See "Package Specification" in javax.xml.bind.package javadoc for
 * additional common information.</p>
 *
 * <p><b>Usage</b>  
 * <p>
 * The namespace qualification values are used in the annotations
 * defined in this packge. The enumeration values are mapped as follows:
 *
 * <p>
 * <table border="1" cellpadding="4" cellspacing="3">
 *   <tbody>
 *     <tr>
 *       <td><b>Enum Value<b></td>
 *       <td><b>XML Schema Value<b></td>
 *     </tr>
 * 
 *     <tr valign="top">
 *       <td>UNQUALIFIED</td>
 *       <td>unqualified</td>
 *     </tr>
 *     <tr valign="top">
 *       <td>QUALIFIED</td>
 *       <td>qualified</td>
 *     </tr>
 *     <tr valign="top">
 *       <td>UNSET</td>
 *       <td>namespace qualification attribute is absent from the
 *           XML Schema fragment</td>
 *     </tr>
 *   </tbody>
 * </table>
 * 
 * @author Sekhar Vajjhala, Sun Microsystems, Inc.
 * @since JAXB2.0
 * @version $Revision: 1.2 $
 */
public enum XmlNsForm {UNQUALIFIED, QUALIFIED, UNSET}



