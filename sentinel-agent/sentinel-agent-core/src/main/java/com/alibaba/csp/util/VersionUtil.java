package com.alibaba.csp.util;//package com.taobao.csp.ahas.util;
//
//import com.alibaba.csp.sentinel.log.RecordLog;
//
//public final class VersionUtil {
//   public static String getVersion(String defaultVersion) {
//      try {
//         String version = VersionUtil.class.getPackage().getImplementationVersion();
//         return StringUtil.isBlank(version) ? defaultVersion : version;
//      } catch (Throwable var2) {
//         RecordLog.warn("Using default version, ignore exception", var2);
//         return defaultVersion;
//      }
//   }
//
//   private VersionUtil() {
//   }
//}
