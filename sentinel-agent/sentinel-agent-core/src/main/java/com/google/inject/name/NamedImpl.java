package com.google.inject.name;

import com.google.common.base.Preconditions;
import com.google.inject.internal.Annotations;

import java.io.Serializable;
import java.lang.annotation.Annotation;

class NamedImpl implements Named, Serializable {
   private final String value;
   private static final long serialVersionUID = 0L;

   public NamedImpl(String value) {
      this.value = (String)Preconditions.checkNotNull(value, "name");
   }

   public String value() {
      return this.value;
   }

   public int hashCode() {
      return 127 * "value".hashCode() ^ this.value.hashCode();
   }

   public boolean equals(Object o) {
      if (!(o instanceof Named)) {
         return false;
      } else {
         Named other = (Named)o;
         return this.value.equals(other.value());
      }
   }

   public String toString() {
      return "@" + Named.class.getName() + "(value=" + Annotations.memberValueString(this.value) + ")";
   }

   public Class<? extends Annotation> annotationType() {
      return Named.class;
   }
}
