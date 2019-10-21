package com.google.inject.internal.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

public final class SourceProvider {
   public static final Object UNKNOWN_SOURCE = "[unknown source]";
   private final SourceProvider parent;
   private final ImmutableSet<String> classNamesToSkip;
   public static final SourceProvider DEFAULT_INSTANCE = new SourceProvider(ImmutableSet.of(SourceProvider.class.getName()));

   private SourceProvider(Iterable<String> classesToSkip) {
      this((SourceProvider)null, classesToSkip);
   }

   private SourceProvider(SourceProvider parent, Iterable<String> classesToSkip) {
      this.parent = parent;
      Builder<String> classNamesToSkipBuilder = ImmutableSet.builder();
      Iterator i$ = classesToSkip.iterator();

      while(true) {
         String classToSkip;
         do {
            if (!i$.hasNext()) {
               this.classNamesToSkip = classNamesToSkipBuilder.build();
               return;
            }

            classToSkip = (String)i$.next();
         } while(parent != null && parent.shouldBeSkipped(classToSkip));

         classNamesToSkipBuilder.add(classToSkip);
      }
   }

   public SourceProvider plusSkippedClasses(Class... moreClassesToSkip) {
      return new SourceProvider(this, asStrings(moreClassesToSkip));
   }

   private boolean shouldBeSkipped(String className) {
      return this.parent != null && this.parent.shouldBeSkipped(className) || this.classNamesToSkip.contains(className);
   }

   private static List<String> asStrings(Class... classes) {
      List<String> strings = Lists.newArrayList();
      Class[] arr$ = classes;
      int len$ = classes.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Class c = arr$[i$];
         strings.add(c.getName());
      }

      return strings;
   }

   public StackTraceElement get(StackTraceElement[] stackTraceElements) {
      Preconditions.checkNotNull(stackTraceElements, "The stack trace elements cannot be null.");
      StackTraceElement[] arr$ = stackTraceElements;
      int len$ = stackTraceElements.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         StackTraceElement element = arr$[i$];
         String className = element.getClassName();
         if (!this.shouldBeSkipped(className)) {
            return element;
         }
      }

      throw new AssertionError();
   }

   public Object getFromClassNames(List<String> moduleClassNames) {
      Preconditions.checkNotNull(moduleClassNames, "The list of module class names cannot be null.");
      Iterator i$ = moduleClassNames.iterator();

      String moduleClassName;
      do {
         if (!i$.hasNext()) {
            return UNKNOWN_SOURCE;
         }

         moduleClassName = (String)i$.next();
      } while(this.shouldBeSkipped(moduleClassName));

      return new StackTraceElement(moduleClassName, "configure", (String)null, -1);
   }
}
