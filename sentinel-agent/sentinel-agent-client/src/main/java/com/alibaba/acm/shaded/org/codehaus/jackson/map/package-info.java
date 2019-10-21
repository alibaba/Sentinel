/**
Contains basic mapper (conversion) functionality that
allows for converting between regular streaming json content and
Java objects (beans or Tree Model: support for both is via
{@link com.alibaba.acm.shaded.org.codehaus.jackson.map.ObjectMapper} class, as well
as convenience methods included in
{@link com.alibaba.acm.shaded.org.codehaus.jackson.JsonParser}
<p>
Object mapper will convert Json content to ant from
basic Java wrapper types (Integer, Boolean, Double),
Collection types (List, Map), Java Beans,
Strings and nulls.
<p>
Tree mapper builds dynamically typed tree of <code>JsonNode</code>s
from Json content (and writes such trees as Json),
similar to how DOM model works with xml.
Main benefits over Object mapping are:
<ul>
 <li>No null checks are needed (dummy
nodes are created as necessary to represent "missing" Object fields
and Array elements)
  </li>
 <li>No type casts are usually needed: all public access methods are defined
in basic JsonNode class, and when "incompatible" method (such as Array
element access on, say, Boolean node) is used, returned node is
virtual "missing" node.
  </li>
</ul>
Because of its dynamic nature, Tree mapping is often convenient
for basic path access and tree navigation, where structure of
the resulting tree is known in advance.
*/

package com.alibaba.acm.shaded.org.codehaus.jackson.map;
