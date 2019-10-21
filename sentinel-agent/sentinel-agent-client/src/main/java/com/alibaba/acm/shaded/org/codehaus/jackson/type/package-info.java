/**
 * Contains classes needed for type introspection, mostly used by data binding
 * functionality. Most of this functionality is needed to properly handled
 * generic types, and to simplify and unify processing of things Jackson needs
 * to determine how contained types (of {@link java.util.Collection} and
 * {@link java.util.Map} classes) are to be handled.
 */
package com.alibaba.acm.shaded.org.codehaus.jackson.type;
