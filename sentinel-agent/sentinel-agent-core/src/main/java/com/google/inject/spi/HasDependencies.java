package com.google.inject.spi;

import java.util.Set;

public interface HasDependencies {
   Set<Dependency<?>> getDependencies();
}
