package com.alibaba.csp.service.bridge.logger;//package com.taobao.csp.ahas.service.bridge.logger;
//
//import com.taobao.csp.ahas.middleware.logger.Level;
//import com.taobao.csp.ahas.middleware.logger.Logger;
//
//import java.util.List;
//
//public class AppLogger implements Logger {
//   private Logger delegate;
//
//   public AppLogger(Logger delegate) {
//      this.delegate = delegate;
//   }
//
//   public void debug(String message) {
//      this.delegate.debug(this.decideAppName(), message);
//   }
//
//   public void debug(String format, Object... args) {
//      this.delegate.debug(this.decideAppName(), format, args);
//   }
//
//   public void debug(String context, String message) {
//      this.delegate.debug(this.decideContextName(context), message);
//   }
//
//   public void debug(String context, String format, Object... args) {
//      this.delegate.debug(this.decideContextName(context), format, args);
//   }
//
//   public void info(String message) {
//      this.delegate.info(this.decideAppName(), message);
//   }
//
//   public void info(String format, Object... args) {
//      this.delegate.info(this.decideAppName(), format, args);
//   }
//
//   public void info(String context, String message) {
//      this.delegate.info(this.decideContextName(context), message);
//   }
//
//   public void info(String context, String format, Object... args) {
//      this.delegate.info(this.decideContextName(context), format, args);
//   }
//
//   public void warn(String message) {
//      this.delegate.warn(this.decideAppName(), message);
//   }
//
//   public void warn(String format, Object... args) {
//      this.delegate.warn(this.decideAppName(), format, args);
//   }
//
//   public void warn(String context, String message) {
//      this.delegate.warn(this.decideContextName(context), message);
//   }
//
//   public void warn(String context, String format, Object... args) {
//      this.delegate.warn(this.decideContextName(context), format, args);
//   }
//
//   public void warn(String s, Throwable throwable) {
//      this.delegate.warn(s, throwable);
//   }
//
//   public void error(String errorCode, String message) {
//      this.delegate.error(this.decideAppName(), errorCode, message);
//   }
//
//   public void error(String errorCode, String message, Throwable t) {
//      this.delegate.error(this.decideAppName(), errorCode, message, t);
//   }
//
//   public void error(String errorCode, String format, Object... objs) {
//      this.delegate.error(this.decideAppName(), errorCode, format, objs);
//   }
//
//   public void error(String context, String errorCode, String message) {
//      this.delegate.error(this.decideContextName(context), errorCode, message);
//   }
//
//   public void error(String context, String errorCode, String message, Throwable t) {
//      this.delegate.error(this.decideContextName(context), errorCode, message, t);
//   }
//
//   public void error(String context, String errorCode, String format, Object... args) {
//      this.delegate.error(this.decideContextName(context), errorCode, format, args);
//   }
//
//   public boolean isDebugEnabled() {
//      return this.delegate.isDebugEnabled();
//   }
//
//   public boolean isInfoEnabled() {
//      return this.delegate.isInfoEnabled();
//   }
//
//   public boolean isWarnEnabled() {
//      return this.delegate.isWarnEnabled();
//   }
//
//   public boolean isErrorEnabled() {
//      return this.delegate.isErrorEnabled();
//   }
//
//   public Object getDelegate() {
//      return this.delegate.getDelegate();
//   }
//
//   public void activateConsoleAppender(String s, String s1) {
//      this.delegate.activateConsoleAppender(s, s1);
//   }
//
//   public void activateAppender(String productName, String file, String encoding) {
//      this.delegate.activateAppender(productName, file, encoding);
//   }
//
//   public void activateAsyncAppender(String productName, String file, String encoding) {
//      this.delegate.activateAsyncAppender(productName, file, encoding);
//   }
//
//   public void activateAsyncAppender(String s, String s1, String s2, int i, int i1) {
//      this.delegate.activateAsyncAppender(s, s1, s2, i, i1);
//   }
//
//   public void activateAppenderWithTimeAndSizeRolling(String productName, String file, String encoding, String size) {
//      this.delegate.activateAppenderWithTimeAndSizeRolling(productName, file, encoding, size);
//   }
//
//   public void activateAppenderWithTimeAndSizeRolling(String productName, String file, String encoding, String size, String datePattern) {
//      this.delegate.activateAppenderWithTimeAndSizeRolling(productName, file, encoding, size, datePattern);
//   }
//
//   public void activateAppenderWithTimeAndSizeRolling(String productName, String file, String encoding, String size, String datePattern, int maxBackupIndex) {
//      this.delegate.activateAppenderWithTimeAndSizeRolling(productName, file, encoding, size, datePattern, maxBackupIndex);
//   }
//
//   public void activateAppenderWithSizeRolling(String s, String s1, String s2, String s3, int i) {
//      this.delegate.activateAppenderWithSizeRolling(s, s1, s2, s3, i);
//   }
//
//   public void activateAsync(int queueSize, int discardingThreshold) {
//      this.delegate.activateAsync(queueSize, discardingThreshold);
//   }
//
//   public void activateAsync(List<Object[]> args) {
//      this.delegate.activateAsync(args);
//   }
//
//   public void activateAppender(Logger logger) {
//      this.delegate.activateAppender(logger);
//   }
//
//   public void setLevel(Level level) {
//      this.delegate.setLevel(level);
//   }
//
//   public Level getLevel() {
//      return this.delegate.getLevel();
//   }
//
//   public void setAdditivity(boolean additivity) {
//      this.delegate.setAdditivity(additivity);
//   }
//
//   public String getProductName() {
//      return "ahas-agent";
//   }
//
//   private String decideContextName(String context) {
//      String app = this.decideAppName();
//      if (context != null && !context.isEmpty()) {
//         if (app != null && app.length() > 0) {
//            context = app + " " + context;
//         }
//      } else {
//         context = app;
//      }
//
//      return context;
//   }
//
//   private String decideAppName() {
//      return "agent";
//   }
//}
