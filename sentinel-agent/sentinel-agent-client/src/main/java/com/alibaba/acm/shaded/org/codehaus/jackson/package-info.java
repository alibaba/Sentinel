/**
 * Main public API classes of the core streaming JSON
 * processor: most importantly {@link com.alibaba.acm.shaded.org.codehaus.jackson.JsonFactory}
 * used for constructing
 * JSON parser ({@link com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser})
 * and generator
 * ({@link com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser})
 * instances.
 * <p>
 * Public API of the higher-level mapping interfaces ("Mapping API")
 * is found from
 * under {@link com.alibaba.acm.shaded.org.codehaus.jackson.map} and not included here,
 * except for following base interfaces:
 * <ul>
 *<li>{@link com.alibaba.acm.shaded.org.codehaus.jackson.JsonNode} is included
 *within Streaming API to support integration of the Tree Model
 *(which is based on <code>JsonNode</code>) with the basic
 *parsers and generators (iff using mapping-supporting factory: which
 *is part of Mapping API, not core)
 *  </li>
 *<li>{@link com.alibaba.acm.shaded.org.codehaus.jackson.ObjectCodec} is included so that
 *  reference to the object capable of serializing/deserializing
 *  Objects to/from JSON (usually, {@link com.alibaba.acm.shaded.org.codehaus.jackson.map.ObjectMapper})
 *  can be exposed, without adding direct dependency to implementation.
 *  </li>
 *</ul>
 * </ul>
 */

package com.alibaba.acm.shaded.org.codehaus.jackson;
