package org.aopalliance.intercept;

public interface Invocation extends Joinpoint {
   Object[] getArguments();
}
