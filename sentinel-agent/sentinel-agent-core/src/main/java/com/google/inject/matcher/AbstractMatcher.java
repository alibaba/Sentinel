package com.google.inject.matcher;

import java.io.Serializable;

public abstract class AbstractMatcher<T> implements Matcher<T> {
   public Matcher<T> and(Matcher<? super T> other) {
      return new AndMatcher(this, other);
   }

   public Matcher<T> or(Matcher<? super T> other) {
      return new OrMatcher(this, other);
   }

   private static class OrMatcher<T> extends AbstractMatcher<T> implements Serializable {
      private final Matcher<? super T> a;
      private final Matcher<? super T> b;
      private static final long serialVersionUID = 0L;

      public OrMatcher(Matcher<? super T> a, Matcher<? super T> b) {
         this.a = a;
         this.b = b;
      }

      public boolean matches(T t) {
         return this.a.matches(t) || this.b.matches(t);
      }

      public boolean equals(Object other) {
         return other instanceof AbstractMatcher.OrMatcher && ((OrMatcher)other).a.equals(this.a) && ((OrMatcher)other).b.equals(this.b);
      }

      public int hashCode() {
         return 37 * (this.a.hashCode() ^ this.b.hashCode());
      }

      public String toString() {
         return "or(" + this.a + ", " + this.b + ")";
      }
   }

   private static class AndMatcher<T> extends AbstractMatcher<T> implements Serializable {
      private final Matcher<? super T> a;
      private final Matcher<? super T> b;
      private static final long serialVersionUID = 0L;

      public AndMatcher(Matcher<? super T> a, Matcher<? super T> b) {
         this.a = a;
         this.b = b;
      }

      public boolean matches(T t) {
         return this.a.matches(t) && this.b.matches(t);
      }

      public boolean equals(Object other) {
         return other instanceof AbstractMatcher.AndMatcher && ((AndMatcher)other).a.equals(this.a) && ((AndMatcher)other).b.equals(this.b);
      }

      public int hashCode() {
         return 41 * (this.a.hashCode() ^ this.b.hashCode());
      }

      public String toString() {
         return "and(" + this.a + ", " + this.b + ")";
      }
   }
}
