package com.alibaba.csp.util;

public final class IdUtil {
   public static String truncate(String id) {
      IdLexer lexer = new IdLexer(id);
      StringBuilder sb = new StringBuilder();
      String temp = "";

      while(true) {
         String r;
         while((r = lexer.nextToken()) != null) {
            if (!"(".equals(r) && !")".equals(r) && !",".equals(r)) {
               if (!".".equals(r)) {
                  temp = r;
               }
            } else {
               sb.append(temp).append(r);
               temp = "";
            }
         }

         return sb.toString();
      }
   }

   private IdUtil() {
   }

   private static class IdLexer {
      private String id;
      private int idx = 0;

      IdLexer(String id) {
         this.id = id;
      }

      String nextToken() {
         int oldIdx = this.idx;

         String result;
         for(result = null; this.idx != this.id.length(); ++this.idx) {
            char curChar = this.id.charAt(this.idx);
            if (curChar == '.' || curChar == '(' || curChar == ')' || curChar == ',') {
               if (this.idx == oldIdx) {
                  result = String.valueOf(curChar);
                  ++this.idx;
               } else {
                  result = this.id.substring(oldIdx, this.idx);
               }
               break;
            }
         }

         return result;
      }
   }
}
