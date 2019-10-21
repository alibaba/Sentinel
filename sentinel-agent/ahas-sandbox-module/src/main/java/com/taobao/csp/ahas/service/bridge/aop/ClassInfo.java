package com.taobao.csp.ahas.service.bridge.aop;

public class ClassInfo {
   private int access;
   private String name;
   private String superName;
   private String[] interfaces;
   private String[] annotations;
   private ClassLoader classLoader;

   public ClassInfo(int access, String name, String superName, String[] interfaces, String[] annotations) {
      this.access = access;
      this.name = name;
      this.superName = superName;
      this.interfaces = interfaces;
      this.annotations = annotations;
   //   this.classLoader = classLoader;
   }

   public ClassInfo(int access, String name, String superName, String[] interfaces, String[] annotations,ClassLoader classLoader) {
      this.access = access;
      this.name = name;
      this.superName = superName;
      this.interfaces = interfaces;
      this.annotations = annotations;
      this.classLoader = classLoader;
   }

   public int getAccess() {
      return this.access;
   }

   public String getName() {
      return this.name;
   }

   public String getSuperName() {
      return this.superName;
   }

   public String[] getInterfaces() {
      return this.interfaces;
   }

   public String[] getAnnotations() {
      return this.annotations;
   }

   public ClassLoader getClassLoader() {
      return this.classLoader;
   }
}
