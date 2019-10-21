package com.taobao.csp.ahas.service.bridge.aop;

public class MethodInfo {
   private int access;
   private String name;
   private String[] parameterTypes;
   private String[] throwsTypes;
   private String[] annotations;
   private String desc;

   public MethodInfo(int access, String name, String[] parameterTypes, String[] throwsTypes, String[] annotations, String desc) {
      this.access = access;
      this.name = name;
      this.parameterTypes = parameterTypes;
      this.throwsTypes = throwsTypes;
      this.annotations = annotations;
      this.desc = desc;
   }

   public int getAccess() {
      return this.access;
   }

   public String getName() {
      return this.name;
   }

   public String[] getParameterTypes() {
      return this.parameterTypes;
   }

   public String[] getThrowsTypes() {
      return this.throwsTypes;
   }

   public String[] getAnnotations() {
      return this.annotations;
   }
}
