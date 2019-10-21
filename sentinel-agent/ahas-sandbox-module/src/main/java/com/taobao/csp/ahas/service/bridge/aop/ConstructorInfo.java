package com.taobao.csp.ahas.service.bridge.aop;

public class ConstructorInfo {
   private int access;
   private String[] parameterTypes;
   private String[] annotations;

   public ConstructorInfo(int access, String[] parameterTypes, String[] annotations) {
      this.access = access;
      this.parameterTypes = parameterTypes;
      this.annotations = annotations;
   }

   public int getAccess() {
      return this.access;
   }

   public void setAccess(int access) {
      this.access = access;
   }

   public String[] getParameterTypes() {
      return this.parameterTypes;
   }

   public void setParameterTypes(String[] parameterTypes) {
      this.parameterTypes = parameterTypes;
   }

   public String[] getAnnotations() {
      return this.annotations;
   }

   public void setAnnotations(String[] annotations) {
      this.annotations = annotations;
   }
}
