package com.taobao.csp.ahas.module.util;

import com.taobao.csp.ahas.service.bridge.annotation.Order;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class OrderSortUtil {
   static <T> void sortByOrder(List<T> list) {
      if (list != null && !list.isEmpty()) {
         Collections.sort(list, new Comparator<T>() {
            public int compare(T f1, T f2) {
               Order order1 = f1 instanceof Class ? (Order)((Class)f1).getAnnotation(Order.class) : (Order)f1.getClass().getAnnotation(Order.class);
               Order order2 = f2 instanceof Class ? (Order)((Class)f2).getAnnotation(Order.class) : (Order)f2.getClass().getAnnotation(Order.class);
               Integer o1 = order1 != null ? order1.value() : Integer.MAX_VALUE;
               Integer o2 = order2 != null ? order2.value() : Integer.MAX_VALUE;
               return o1.compareTo(o2);
            }
         });
      }

   }
}
