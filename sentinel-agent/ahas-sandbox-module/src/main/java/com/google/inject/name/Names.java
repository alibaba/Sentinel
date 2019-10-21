package com.google.inject.name;

import com.google.inject.Binder;
import com.google.inject.Key;
import java.lang.annotation.Annotation;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

public class Names {
   private Names() {
   }

   public static Named named(String name) {
      return new NamedImpl(name);
   }

   public static void bindProperties(Binder binder, Map<String, String> properties) {
      binder = binder.skipSources(Names.class);
      Iterator i$ = properties.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, String> entry = (Entry)i$.next();
         String key = (String)entry.getKey();
         String value = (String)entry.getValue();
         binder.bind(Key.get((Class)String.class, (Annotation)(new NamedImpl(key)))).toInstance(value);
      }

   }

   public static void bindProperties(Binder binder, Properties properties) {
      binder = binder.skipSources(Names.class);
      Enumeration e = properties.propertyNames();

      while(e.hasMoreElements()) {
         String propertyName = (String)e.nextElement();
         String value = properties.getProperty(propertyName);
         binder.bind(Key.get((Class)String.class, (Annotation)(new NamedImpl(propertyName)))).toInstance(value);
      }

   }
}
