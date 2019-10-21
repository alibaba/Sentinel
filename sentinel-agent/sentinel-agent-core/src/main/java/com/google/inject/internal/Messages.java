package com.google.inject.internal;

import com.google.common.base.Equivalence;
import com.google.common.base.Equivalence.Wrapper;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.util.Classes;
import com.google.inject.internal.util.StackTraceElements;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.ElementSource;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.Message;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.*;

public final class Messages {
   private static final Collection<Converter<?>> converters = ImmutableList.of(new Converter<Class>(Class.class) {
      public String toString(Class c) {
         return c.getName();
      }
   }, new Converter<Member>(Member.class) {
      public String toString(Member member) {
         return Classes.toString(member);
      }
   }, new Converter<Key>(Key.class) {
      public String toString(Key key) {
         return key.getAnnotationType() != null ? key.getTypeLiteral() + " annotated with " + (key.getAnnotation() != null ? key.getAnnotation() : key.getAnnotationType()) : key.getTypeLiteral().toString();
      }
   });

   private Messages() {
   }

   static Message mergeSources(List<Object> sources, Message message) {
      List<Object> messageSources = message.getSources();
      if (!sources.isEmpty() && !messageSources.isEmpty() && Objects.equal(messageSources.get(0), sources.get(sources.size() - 1))) {
         messageSources = messageSources.subList(1, messageSources.size());
      }

      return new Message(ImmutableList.builder().addAll(sources).addAll(messageSources).build(), message.getMessage(), message.getCause());
   }

   public static String format(String messageFormat, Object... arguments) {
      for(int i = 0; i < arguments.length; ++i) {
         arguments[i] = convert(arguments[i]);
      }

      return String.format(messageFormat, arguments);
   }

   public static String formatMessages(String heading, Collection<Message> errorMessages) {
      Formatter fmt = (new Formatter()).format(heading).format(":%n%n");
      int index = 1;
      boolean displayCauses = getOnlyCause(errorMessages) == null;
      Map<Wrapper<Throwable>, Integer> causes = Maps.newHashMap();

      for(Iterator i$ = errorMessages.iterator(); i$.hasNext(); fmt.format("%n")) {
         Message errorMessage = (Message)i$.next();
         int thisIdx = index++;
         fmt.format("%s) %s%n", thisIdx, errorMessage.getMessage());
         List<Object> dependencies = errorMessage.getSources();

         for(int i = dependencies.size() - 1; i >= 0; --i) {
            Object source = dependencies.get(i);
            formatSource(fmt, source);
         }

         Throwable cause = errorMessage.getCause();
         if (displayCauses && cause != null) {
            Wrapper<Throwable> causeEquivalence = ThrowableEquivalence.INSTANCE.wrap(cause);
            if (!causes.containsKey(causeEquivalence)) {
               causes.put(causeEquivalence, thisIdx);
               fmt.format("Caused by: %s", Throwables.getStackTraceAsString(cause));
            } else {
               int causeIdx = (Integer)causes.get(causeEquivalence);
               fmt.format("Caused by: %s (same stack trace as error #%s)", cause.getClass().getName(), causeIdx);
            }
         }
      }

      if (errorMessages.size() == 1) {
         fmt.format("1 error");
      } else {
         fmt.format("%s errors", errorMessages.size());
      }

      return fmt.toString();
   }

   public static Message create(String messageFormat, Object... arguments) {
      return create((Throwable)null, messageFormat, arguments);
   }

   public static Message create(Throwable cause, String messageFormat, Object... arguments) {
      return create(cause, ImmutableList.of(), messageFormat, arguments);
   }

   public static Message create(Throwable cause, List<Object> sources, String messageFormat, Object... arguments) {
      String message = format(messageFormat, arguments);
      return new Message(sources, message, cause);
   }

   static Object convert(Object o) {
      ElementSource source = null;
      if (o instanceof ElementSource) {
         source = (ElementSource)o;
         o = source.getDeclaringSource();
      }

      return convert(o, source);
   }

   static Object convert(Object o, ElementSource source) {
      Iterator i$ = converters.iterator();

      Converter converter;
      do {
         if (!i$.hasNext()) {
            return appendModules(o, source);
         }

         converter = (Converter)i$.next();
      } while(!converter.appliesTo(o));

      return appendModules(converter.convert(o), source);
   }

   private static Object appendModules(Object source, ElementSource elementSource) {
      String modules = moduleSourceString(elementSource);
      return modules.length() == 0 ? source : source + modules;
   }

   private static String moduleSourceString(ElementSource elementSource) {
      if (elementSource == null) {
         return "";
      } else {
         ArrayList modules = Lists.newArrayList(elementSource.getModuleClassNames());

         while(elementSource.getOriginalElementSource() != null) {
            elementSource = elementSource.getOriginalElementSource();
            modules.addAll(0, elementSource.getModuleClassNames());
         }

         if (modules.size() <= 1) {
            return "";
         } else {
            StringBuilder builder = new StringBuilder(" (via modules: ");

            for(int i = modules.size() - 1; i >= 0; --i) {
               builder.append((String)modules.get(i));
               if (i != 0) {
                  builder.append(" -> ");
               }
            }

            builder.append(")");
            return builder.toString();
         }
      }
   }

   static void formatSource(Formatter formatter, Object source) {
      ElementSource elementSource = null;
      if (source instanceof ElementSource) {
         elementSource = (ElementSource)source;
         source = elementSource.getDeclaringSource();
      }

      formatSource(formatter, source, elementSource);
   }

   static void formatSource(Formatter formatter, Object source, ElementSource elementSource) {
      String modules = moduleSourceString(elementSource);
      if (source instanceof Dependency) {
         Dependency<?> dependency = (Dependency)source;
         InjectionPoint injectionPoint = dependency.getInjectionPoint();
         if (injectionPoint != null) {
            formatInjectionPoint(formatter, dependency, injectionPoint, elementSource);
         } else {
            formatSource(formatter, dependency.getKey(), elementSource);
         }
      } else if (source instanceof InjectionPoint) {
         formatInjectionPoint(formatter, (Dependency)null, (InjectionPoint)source, elementSource);
      } else if (source instanceof Class) {
         formatter.format("  at %s%s%n", StackTraceElements.forType((Class)source), modules);
      } else if (source instanceof Member) {
         formatter.format("  at %s%s%n", StackTraceElements.forMember((Member)source), modules);
      } else if (source instanceof TypeLiteral) {
         formatter.format("  while locating %s%s%n", source, modules);
      } else if (source instanceof Key) {
         Key<?> key = (Key)source;
         formatter.format("  while locating %s%n", convert(key, elementSource));
      } else if (source instanceof Thread) {
         formatter.format("  in thread %s%n", source);
      } else {
         formatter.format("  at %s%s%n", source, modules);
      }

   }

   private static void formatInjectionPoint(Formatter formatter, Dependency<?> dependency, InjectionPoint injectionPoint, ElementSource elementSource) {
      Member member = injectionPoint.getMember();
      Class<? extends Member> memberType = Classes.memberType(member);
      if (memberType == Field.class) {
         dependency = (Dependency)injectionPoint.getDependencies().get(0);
         formatter.format("  while locating %s%n", convert(dependency.getKey(), elementSource));
         formatter.format("    for field at %s%n", StackTraceElements.forMember(member));
      } else if (dependency != null) {
         formatter.format("  while locating %s%n", convert(dependency.getKey(), elementSource));
         formatter.format("    for %s%n", formatParameter(dependency));
      } else {
         formatSource(formatter, injectionPoint.getMember());
      }

   }

   static String formatParameter(Dependency<?> dependency) {
      int ordinal = dependency.getParameterIndex() + 1;
      return String.format("the %s%s parameter of %s", ordinal, getOrdinalSuffix(ordinal), StackTraceElements.forMember(dependency.getInjectionPoint().getMember()));
   }

   private static String getOrdinalSuffix(int ordinal) {
      Preconditions.checkArgument(ordinal >= 0);
      if (ordinal / 10 % 10 == 1) {
         return "th";
      } else {
         switch(ordinal % 10) {
         case 1:
            return "st";
         case 2:
            return "nd";
         case 3:
            return "rd";
         default:
            return "th";
         }
      }
   }

   public static Throwable getOnlyCause(Collection<Message> messages) {
      Throwable onlyCause = null;
      Iterator i$ = messages.iterator();

      while(i$.hasNext()) {
         Message message = (Message)i$.next();
         Throwable messageCause = message.getCause();
         if (messageCause != null) {
            if (onlyCause != null && !ThrowableEquivalence.INSTANCE.equivalent(onlyCause, messageCause)) {
               return null;
            }

            onlyCause = messageCause;
         }
      }

      return onlyCause;
   }

   private static final class ThrowableEquivalence extends Equivalence<Throwable> {
      static final ThrowableEquivalence INSTANCE = new ThrowableEquivalence();

      protected boolean doEquivalent(Throwable a, Throwable b) {
         return a.getClass().equals(b.getClass()) && Objects.equal(a.getMessage(), b.getMessage()) && Arrays.equals(a.getStackTrace(), b.getStackTrace()) && this.equivalent(a.getCause(), b.getCause());
      }

      protected int doHash(Throwable t) {
         return Objects.hashCode(new Object[]{t.getClass().hashCode(), t.getMessage(), this.hash(t.getCause())});
      }
   }

   private abstract static class Converter<T> {
      final Class<T> type;

      Converter(Class<T> type) {
         this.type = type;
      }

      boolean appliesTo(Object o) {
         return o != null && this.type.isAssignableFrom(o.getClass());
      }

      String convert(Object o) {
         return this.toString(this.type.cast(o));
      }

      abstract String toString(T var1);
   }
}
