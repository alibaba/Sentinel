package com.taobao.csp.ahas.service.bridge.aop.methodmatcher;

import com.taobao.csp.ahas.service.bridge.aop.MethodInfo;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class ParameterMethodMatcher implements MethodMatcher {
   public static final int LESS_THAN = -1;
   public static final int EQUAL = 0;
   public static final int GREAT_THAN = 1;
   public static final int DEFAULT_LENGTH = -1;
   private Map<Integer, String> parametersMap;
   private int parametersLength = -1;
   private int compareFlag;

   public ParameterMethodMatcher(int parametersLength, int compareFlag) {
      this.parametersLength = parametersLength;
      this.compareFlag = compareFlag;
   }

   public ParameterMethodMatcher(String[] parameters) {
      this.convertToMap(parameters);
   }

   public ParameterMethodMatcher(String[] parameters, int parametersLength, int compareFlag) {
      this.parametersMap = this.convertToMap(parameters);
      this.parametersLength = parametersLength;
      this.compareFlag = compareFlag;
   }

   private Map<Integer, String> convertToMap(String[] parameters) {
      HashMap<Integer, String> map = new HashMap(4);

      for(int i = 0; i < parameters.length; ++i) {
         if (parameters[i] != null) {
            map.put(i, parameters[i]);
         }
      }

      return map;
   }

   public boolean isMatched(String methodName, MethodInfo methodInfo) {
      String[] parameterTypes = methodInfo.getParameterTypes();
      int length = parameterTypes.length;
      boolean result = this.compareParametersLength(length);
      if (!result) {
         return false;
      } else if (this.parametersMap != null && !this.parametersMap.isEmpty()) {
         Set<Entry<Integer, String>> entries = this.parametersMap.entrySet();
         Iterator var7 = entries.iterator();

         Entry entry;
         int index;
         do {
            if (!var7.hasNext()) {
               return true;
            }

            entry = (Entry)var7.next();
            index = (Integer)entry.getKey();
            if (index >= length) {
               return false;
            }
         } while(parameterTypes[index].equals(entry.getValue()));

         return false;
      } else {
         return true;
      }
   }

   private boolean compareParametersLength(int length) {
      if (this.parametersLength != -1) {
         switch(this.compareFlag) {
         case -1:
            if (length >= this.parametersLength) {
               return false;
            }
            break;
         case 0:
            if (length != this.parametersLength) {
               return false;
            }
            break;
         case 1:
            if (length <= this.parametersLength) {
               return false;
            }
         }
      }

      return true;
   }
}
