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

import javax.xml.namespace.QName;

/**
 * Pluggable datatype writer.
 *
 * @author Kohsuke Kawaguchi
 */
public interface DatatypeWriter<DT> {

    /**
     * Gets the Java class that this writer can write.
     *
     * @return
     *      must not be null. Must be the same value always.
     */
    Class<DT> getType();

    /**
     * Prints the given datatype object and appends that result
     * into the given buffer.
     *
     * @param dt
     *      the datatype object to be printed.
     * @param resolver
     *      allows the converter to declare additional namespace prefixes.
     */
    void print(DT dt, NamespaceResolver resolver, StringBuilder buf);


    static final DatatypeWriter<?>[] BUILDIN = new DatatypeWriter<?>[] {
        new DatatypeWriter<String>() {
            public Class<String> getType() {
                return String.class;
            }
            public void print(String s, NamespaceResolver resolver, StringBuilder buf) {
                buf.append(s);
            }
        },
        new DatatypeWriter<Integer>() {
            public Class<Integer> getType() {
                return Integer.class;
            }
            public void print(Integer i, NamespaceResolver resolver, StringBuilder buf) {
                buf.append(i);
            }
        },
        new DatatypeWriter<Float>() {
            public Class<Float> getType() {
                return Float.class;
            }
            public void print(Float f, NamespaceResolver resolver, StringBuilder buf) {
                buf.append(f);
            }
        },
        new DatatypeWriter<Double>() {
            public Class<Double> getType() {
                return Double.class;
            }
            public void print(Double d, NamespaceResolver resolver, StringBuilder buf) {
                buf.append(d);
            }
        },
        new DatatypeWriter<QName>() {
            public Class<QName> getType() {
                return QName.class;
            }
            public void print(QName qn, NamespaceResolver resolver, StringBuilder buf) {
                String p = resolver.getPrefix(qn.getNamespaceURI());
                if(p.length()!=0)
                    buf.append(p).append(':');
                buf.append(qn.getLocalPart());
            }
        }
    };
}
