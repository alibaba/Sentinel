package com.taobao.csp.ahas.service.bridge.aop.classmatcher;

import com.taobao.csp.ahas.service.bridge.aop.ClassInfo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OrClassMatcher implements ClassMatcher {
   private List<ClassMatcher> matchers = new ArrayList(2);

   public OrClassMatcher or(ClassMatcher matcher) {
      if (matcher != null) {
         this.matchers.add(matcher);
      }

      return this;
   }

   public boolean isMatched(String className, ClassInfo classInfo) {
      Iterator var3 = this.matchers.iterator();

      ClassMatcher matcher;
      do {
         if (!var3.hasNext()) {
            return false;
         }

         matcher = (ClassMatcher)var3.next();
      } while(!matcher.isMatched(className, classInfo));

      return true;
   }
}
