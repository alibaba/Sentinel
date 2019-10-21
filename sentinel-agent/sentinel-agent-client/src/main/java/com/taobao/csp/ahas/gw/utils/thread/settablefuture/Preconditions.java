package com.taobao.csp.ahas.gw.utils.thread.settablefuture;

public final class Preconditions {
   private Preconditions() {
   }

   public static <T> T checkNotNull(T reference) {
      if (reference == null) {
         throw new NullPointerException();
      } else {
         return reference;
      }
   }

   public static <T> T checkNotNull(T reference, Object errorMessage) {
      if (reference == null) {
         throw new NullPointerException(String.valueOf(errorMessage));
      } else {
         return reference;
      }
   }

   public static void checkState(boolean b, String errorMessageTemplate, Object p1) {
      if (!b) {
         throw new IllegalStateException(format(errorMessageTemplate, p1));
      }
   }

   static String format(String template, Object... args) {
      template = String.valueOf(template);
      StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
      int templateStart = 0;

      int i;
      int placeholderStart;
      for(i = 0; i < args.length; templateStart = placeholderStart + 2) {
         placeholderStart = template.indexOf("%s", templateStart);
         if (placeholderStart == -1) {
            break;
         }

         builder.append(template, templateStart, placeholderStart);
         builder.append(args[i++]);
      }

      builder.append(template, templateStart, template.length());
      if (i < args.length) {
         builder.append(" [");
         builder.append(args[i++]);

         while(i < args.length) {
            builder.append(", ");
            builder.append(args[i++]);
         }

         builder.append(']');
      }

      return builder.toString();
   }
}
