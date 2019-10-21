/**
Contains extended support for "external" packages: things that
may or may not be present in runtime environment, but that are
commonly enough used so that explicit support can be added.
<p>
Currently supported extensions include:
<ul>
 <li>Support for Java 1.5 core XML datatypes: the reason these are
considered "external" is that some platforms that claim to be 1.5 conformant
are only partially so (Google Android, GAE) and do not included these
 types.
  </li>
 <li>Joda time. This package has superior date/time handling functionality,
and is thus supported. However, to minimize forced dependencies this
support is added as extension so that Joda is not needed by Jackson
itself: but if it is present, its core types are supported to some
degree
  </li>
</ul>

*/

package com.alibaba.acm.shaded.org.codehaus.jackson.map.ext;
