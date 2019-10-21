package com.google.inject.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.logging.Logger;

public class InternalFlags {
   private static final Logger logger = Logger.getLogger(InternalFlags.class.getName());
   private static final IncludeStackTraceOption INCLUDE_STACK_TRACES = parseIncludeStackTraceOption();
   private static final CustomClassLoadingOption CUSTOM_CLASS_LOADING = parseCustomClassLoadingOption();
   private static final NullableProvidesOption NULLABLE_PROVIDES;

   public static IncludeStackTraceOption getIncludeStackTraceOption() {
      return INCLUDE_STACK_TRACES;
   }

   public static CustomClassLoadingOption getCustomClassLoadingOption() {
      return CUSTOM_CLASS_LOADING;
   }

   public static NullableProvidesOption getNullableProvidesOption() {
      return NULLABLE_PROVIDES;
   }

   private static IncludeStackTraceOption parseIncludeStackTraceOption() {
      return (IncludeStackTraceOption)getSystemOption("guice_include_stack_traces", IncludeStackTraceOption.ONLY_FOR_DECLARING_SOURCE);
   }

   private static CustomClassLoadingOption parseCustomClassLoadingOption() {
      return (CustomClassLoadingOption)getSystemOption("guice_custom_class_loading", CustomClassLoadingOption.BRIDGE, CustomClassLoadingOption.OFF);
   }

   private static NullableProvidesOption parseNullableProvidesOption(NullableProvidesOption defaultValue) {
      return (NullableProvidesOption)getSystemOption("guice_check_nullable_provides_params", defaultValue);
   }

   private static <T extends Enum<T>> T getSystemOption(String name, T defaultValue) {
      return getSystemOption(name, defaultValue, defaultValue);
   }

   private static <T extends Enum<T>> T getSystemOption(final String name, T defaultValue, T secureValue) {
      Class<T> enumType = defaultValue.getDeclaringClass();
      String value = null;

      try {
         value = (String)AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
               return System.getProperty(name);
            }
         });
         return value != null && value.length() > 0 ? Enum.valueOf(enumType, value) : defaultValue;
      } catch (SecurityException var6) {
         return secureValue;
      } catch (IllegalArgumentException var7) {
         logger.warning(value + " is not a valid flag value for " + name + ". " + " Values must be one of " + Arrays.asList(enumType.getEnumConstants()));
         return defaultValue;
      }
   }

   static {
      NULLABLE_PROVIDES = parseNullableProvidesOption(NullableProvidesOption.ERROR);
   }

   public static enum NullableProvidesOption {
      IGNORE,
      WARN,
      ERROR;
   }

   public static enum CustomClassLoadingOption {
      OFF,
      BRIDGE;
   }

   public static enum IncludeStackTraceOption {
      OFF,
      ONLY_FOR_DECLARING_SOURCE,
      COMPLETE;
   }
}
