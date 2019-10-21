package com.google.inject.internal.util;

import java.util.logging.Logger;

public final class Stopwatch {
   private static final Logger logger = Logger.getLogger(Stopwatch.class.getName());
   private long start = System.currentTimeMillis();

   public long reset() {
      long now = System.currentTimeMillis();

      long var3;
      try {
         var3 = now - this.start;
      } finally {
         this.start = now;
      }

      return var3;
   }

   public void resetAndLog(String label) {
      logger.fine(label + ": " + this.reset() + "ms");
   }
}
