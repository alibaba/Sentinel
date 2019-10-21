/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.xml.txw2;

/**
 * @author Kohsuke Kawaguchi
 */
final class Attribute {
    final String nsUri;
    final String localName;

    /**
     * Attributes of an element form a linked list.
     */
    Attribute next;

    /**
     * Attribute value that potentially contains dummy prefixes.
     */
    final StringBuilder value = new StringBuilder();

    Attribute(String nsUri, String localName) {
        assert nsUri!=null && localName!=null;

        this.nsUri = nsUri;
        this.localName = localName;
    }

    boolean hasName( String nsUri, String localName ) {
        return this.localName.equals(localName) && this.nsUri.equals(nsUri);
    }
}
