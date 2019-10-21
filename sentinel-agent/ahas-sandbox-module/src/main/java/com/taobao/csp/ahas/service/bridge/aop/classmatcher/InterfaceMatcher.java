package com.taobao.csp.ahas.service.bridge.aop.classmatcher;

import com.taobao.csp.ahas.service.bridge.aop.ClassInfo;
import org.objectweb.asm.ClassReader;
import java.io.IOException;
import java.io.InputStream;

public class InterfaceMatcher implements ClassMatcher {
   private String interfaceName;

   public InterfaceMatcher(String interfaceName) {
      this.interfaceName = interfaceName;
   }

   public boolean isMatched(String className, ClassInfo classInfo) {
      String[] interfaces = classInfo.getInterfaces();
      return this.isInterfaceMatched(classInfo.getClassLoader(), interfaces);
   }

   private boolean isInterfaceMatched(ClassLoader classLoader, String[] interfaces) {
      if (interfaces == null) {
         return false;
      } else {
         String[] var3 = interfaces;
         int var4 = interfaces.length;

         int var5;
         String name;
         for(var5 = 0; var5 < var4; ++var5) {
            name = var3[var5];
            if (this.interfaceName.equals(name.replaceAll("/", "."))) {
               return true;
            }
         }

         var3 = interfaces;
         var4 = interfaces.length;

         for(var5 = 0; var5 < var4; ++var5) {
            name = var3[var5];
            if (this.isInterfaceMatched(classLoader, name)) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean isInterfaceMatched(ClassLoader classLoader, String interfaceName) {
      if (classLoader == null) {
         return false;
      } else {
         InputStream inputStream = classLoader.getResourceAsStream(interfaceName.replaceAll("\\.", "/") + ".class");
         if (inputStream == null) {
            return false;
         } else {
            try {
               ClassReader classReader = new ClassReader(inputStream);
               boolean var5 = this.isInterfaceMatched(classLoader, classReader.getInterfaces());
               return var5;
            } catch (IOException var15) {
            } finally {
               try {
                  inputStream.close();
               } catch (IOException var14) {
               }

            }

            return false;
         }
      }
   }
}
