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
abstract class Content {
    private Content next;

    /**
     * Returns null if the next token has not decided yet.
     */
    final Content getNext() {
        return next;
    }

    /**
     *
     * @param doc
     *      A {@link Content} object is so light-weight that
     *      it doesn't even remember what document it belongs to.
     *      So the caller needs to "remind" a {@link Content}
     *      who its owner is.
     */
    final void setNext(Document doc,Content next) {
        assert next!=null;
        assert this.next==null : "next of "+this+" is already set to "+this.next;
        this.next = next;
        doc.run();
    }

    /**
     * Returns true if this content is ready to be committed.
     */
    boolean isReadyToCommit() {
        return true;
    }

    /**
     * Returns true if this {@link Content} can guarantee that
     * no more new namespace decls is necessary for the currently
     * pending start tag.
     */
    abstract boolean concludesPendingStartTag();

    /**
     * Accepts a visitor.
     */
    abstract void accept(ContentVisitor visitor);

    /**
     * Called when this content is written to the output.
     */
    public void written() {
    }
}
