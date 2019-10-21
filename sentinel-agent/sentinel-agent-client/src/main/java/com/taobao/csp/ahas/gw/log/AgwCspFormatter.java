package com.taobao.csp.ahas.gw.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

class AgwCspFormatter extends Formatter {
   private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

   public String format(LogRecord record) {
      StringBuilder builder = new StringBuilder(1000);
      builder.append(this.df.format(new Date(record.getMillis()))).append(" ");
      builder.append(this.formatMessage(record));
      String throwable = "";
      if (record.getThrown() != null) {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         pw.println();
         record.getThrown().printStackTrace(pw);
         pw.close();
         throwable = sw.toString();
      }

      builder.append(throwable);
      if ("".equals(throwable)) {
         builder.append("\n");
      }

      return builder.toString();
   }
}
