/**
 * Functionality needed for Bean introspection, required for detecting
 * accessors and mutators for Beans, as well as locating and handling
 * method annotations.
 *<p>
 * Beyond collecting annotations, additional "method annotation inheritance"
 * is also supported: whereas regular JDK classes do not add annotations
 * from overridden methods in any situation. But code in this package does.
 * Similarly class-annotations are inherited properly from interfaces, in
 * addition to abstract and concrete classes.
 */
package com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect;
