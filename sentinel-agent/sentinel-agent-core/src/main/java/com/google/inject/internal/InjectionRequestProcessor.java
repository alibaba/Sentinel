package com.google.inject.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Binding;
import com.google.inject.ConfigurationException;
import com.google.inject.Stage;
import com.google.inject.spi.InjectionRequest;
import com.google.inject.spi.StaticInjectionRequest;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

final class InjectionRequestProcessor extends AbstractProcessor {
   private final List<StaticInjection> staticInjections = Lists.newArrayList();
   private final Initializer initializer;

   InjectionRequestProcessor(Errors errors, Initializer initializer) {
      super(errors);
      this.initializer = initializer;
   }

   public Boolean visit(StaticInjectionRequest request) {
      this.staticInjections.add(new StaticInjection(this.injector, request));
      return true;
   }

   public Boolean visit(InjectionRequest<?> request) {
      Set injectionPoints;
      try {
         injectionPoints = request.getInjectionPoints();
      } catch (ConfigurationException var4) {
         this.errors.merge(var4.getErrorMessages());
         injectionPoints = (Set)var4.getPartialValue();
      }

      this.initializer.requestInjection(this.injector, request.getInstance(), (Binding)null, request.getSource(), injectionPoints);
      return true;
   }

   void validate() {
      Iterator i$ = this.staticInjections.iterator();

      while(i$.hasNext()) {
         StaticInjection staticInjection = (StaticInjection)i$.next();
         staticInjection.validate();
      }

   }

   void injectMembers() {
      Iterator i$ = this.staticInjections.iterator();

      while(i$.hasNext()) {
         StaticInjection staticInjection = (StaticInjection)i$.next();
         staticInjection.injectMembers();
      }

   }

   private class StaticInjection {
      final InjectorImpl injector;
      final Object source;
      final StaticInjectionRequest request;
      ImmutableList<SingleMemberInjector> memberInjectors;

      public StaticInjection(InjectorImpl injector, StaticInjectionRequest request) {
         this.injector = injector;
         this.source = request.getSource();
         this.request = request;
      }

      void validate() {
         Errors errorsForMember = InjectionRequestProcessor.this.errors.withSource(this.source);

         Set injectionPoints;
         try {
            injectionPoints = this.request.getInjectionPoints();
         } catch (ConfigurationException var4) {
            errorsForMember.merge(var4.getErrorMessages());
            injectionPoints = (Set)var4.getPartialValue();
         }

         if (injectionPoints != null) {
            this.memberInjectors = this.injector.membersInjectorStore.getInjectors(injectionPoints, errorsForMember);
         } else {
            this.memberInjectors = ImmutableList.of();
         }

         InjectionRequestProcessor.this.errors.merge(errorsForMember);
      }

      void injectMembers() {
         InternalContext context = this.injector.enterContext();

         try {
            boolean isStageTool = this.injector.options.stage == Stage.TOOL;
            Iterator i$ = this.memberInjectors.iterator();

            while(true) {
               SingleMemberInjector memberInjector;
               do {
                  if (!i$.hasNext()) {
                     return;
                  }

                  memberInjector = (SingleMemberInjector)i$.next();
               } while(isStageTool && !memberInjector.getInjectionPoint().isToolable());

               try {
                  memberInjector.inject(context, (Object)null);
               } catch (InternalProvisionException var9) {
                  InjectionRequestProcessor.this.errors.merge(var9);
               }
            }
         } finally {
            context.close();
         }
      }
   }
}
