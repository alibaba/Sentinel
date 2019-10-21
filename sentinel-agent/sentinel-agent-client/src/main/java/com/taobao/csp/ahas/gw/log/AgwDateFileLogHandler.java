package com.taobao.csp.ahas.gw.log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

class AgwDateFileLogHandler extends Handler {
   private final ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
      public SimpleDateFormat initialValue() {
         return new SimpleDateFormat("yyyy-MM-dd");
      }
   };
   private volatile FileHandler handler;
   private final String pattern;
   private final int limit;
   private final int count;
   private final boolean append;
   private volatile boolean initialized = false;
   private volatile long startDate = System.currentTimeMillis();
   private volatile long endDate;
   private final Object monitor = new Object();

   AgwDateFileLogHandler(String pattern, int limit, int count, boolean append) throws SecurityException {
      this.pattern = pattern;
      this.limit = limit;
      this.count = count;
      this.append = append;
      this.rotateDate();
      this.initialized = true;
   }

   public void close() throws SecurityException {
      this.handler.close();
   }

   public void flush() {
      this.handler.flush();
   }

   public void publish(LogRecord record) {
      if (this.shouldRotate(record)) {
         synchronized(this.monitor) {
            if (this.shouldRotate(record)) {
               this.rotateDate();
            }
         }
      }

      if (System.currentTimeMillis() - this.startDate > 90000000L) {
         String msg = record.getMessage();
         record.setMessage("missed file rolling at: " + new Date(this.endDate) + "\n" + msg);
      }

      this.handler.publish(record);
   }

   private boolean shouldRotate(LogRecord record) {
      return this.endDate <= record.getMillis() || !this.logFileExits();
   }

   public void setFormatter(Formatter newFormatter) {
      super.setFormatter(newFormatter);
      if (this.handler != null) {
         this.handler.setFormatter(newFormatter);
      }

   }

   private boolean logFileExits() {
      try {
         SimpleDateFormat format = (SimpleDateFormat)this.dateFormatThreadLocal.get();
         String fileName = this.pattern.replace("%d", format.format(new Date()));
         if (this.count != 1) {
            fileName = fileName + ".0";
         }

         File logFile = new File(fileName);
         return logFile.exists();
      } catch (Throwable var4) {
         return false;
      }
   }

   private void rotateDate() {
      this.startDate = System.currentTimeMillis();
      if (this.handler != null) {
         this.handler.close();
      }

      SimpleDateFormat format = (SimpleDateFormat)this.dateFormatThreadLocal.get();
      String newPattern = this.pattern.replace("%d", format.format(new Date()));
      Calendar next = Calendar.getInstance();
      next.set(11, 0);
      next.set(12, 0);
      next.set(13, 0);
      next.set(14, 0);
      next.add(5, 1);
      this.endDate = next.getTimeInMillis();

      try {
         this.handler = new FileHandler(newPattern, this.limit, this.count, this.append);
         if (this.initialized) {
            this.handler.setEncoding(this.getEncoding());
            this.handler.setErrorManager(this.getErrorManager());
            this.handler.setFilter(this.getFilter());
            this.handler.setFormatter(this.getFormatter());
            this.handler.setLevel(this.getLevel());
         }
      } catch (SecurityException var5) {
         var5.printStackTrace();
      } catch (IOException var6) {
         var6.printStackTrace();
      }

   }
}
