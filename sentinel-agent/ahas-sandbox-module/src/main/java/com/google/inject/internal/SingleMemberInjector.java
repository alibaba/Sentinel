package com.google.inject.internal;

import com.google.inject.spi.InjectionPoint;

interface SingleMemberInjector {
   void inject(InternalContext var1, Object var2) throws InternalProvisionException;

   InjectionPoint getInjectionPoint();
}
