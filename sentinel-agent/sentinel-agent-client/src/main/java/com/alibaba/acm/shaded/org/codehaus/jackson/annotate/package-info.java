/**
 * Public core annotations, most of which are used to configure how
 * Data Mapping/Binding works. Annotations in this package can only
 * have dependencies to non-annotation classes in Core package;
 * annotations that have dependencies to Mapper classes are included
 * in Mapper module (under <code>com.alibaba.acm.shaded.org.codehaus.jackson.map.annotate</code>).
 * Also contains parameter types (mostly enums) needed by annotations.
 *<p>
 * In future (version 2.0?), this package will probably be split off
 * as a separate jar/module, to allow use of annotations without
 * including core module. This would be useful for third party value
 * classes that themselves do not depend on Jackson, but may want to
 * be annotated to be automatically and conveniently serializable by
 * Jackson.
 */
package com.alibaba.acm.shaded.org.codehaus.jackson.annotate;
