package org.aopalliance.intercept;

import java.lang.reflect.Constructor;

public interface ConstructorInvocation extends Invocation {
   Constructor getConstructor();
}
