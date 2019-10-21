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
 * Namespace declarations.
 *
 * @author Kohsuke Kawaguchi
 */
final class NamespaceDecl {
    final String uri;

    boolean requirePrefix;

    /**
     * Dummy prefix assigned for this namespace decl.
     */
    final String dummyPrefix;

    final char uniqueId;

    /**
     * Set to the real prefix once that's computed.
     */
    String prefix;

    /**
     * Used temporarily inside {@link Document#finalizeStartTag()}.
     * true if this prefix is declared on the new element.
     */
    boolean declared;

    /**
     * Namespace declarations form a linked list.
     */
    NamespaceDecl next;

    NamespaceDecl(char uniqueId, String uri, String prefix, boolean requirePrefix ) {
        this.dummyPrefix = new StringBuilder(2).append(Document.MAGIC).append(uniqueId).toString();
        this.uri = uri;
        this.prefix = prefix;
        this.requirePrefix = requirePrefix;
        this.uniqueId = uniqueId;
    }
}
