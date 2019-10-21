/**
 * Contains public standard implementations of abstraction that
 * Jackson uses. This means that they are not merely implementation
 * details, but part of semi-public interface where project
 * tries to maintain backwards compatibility at higher level
 * than for 'impl' types (although less so than with fully
 * public interfaces).
 *<p>
 * Note that since this package was only added relatively late
 * in development cycle, not all classes that belong here are
 * included. Plan is to move more classes over time.
 * 
 * @since 1.9
 */
package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std;
