package com.taobao.csp.ahas.gw.utils.ds;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Ring<T> {
   AtomicInteger count = new AtomicInteger();
   private List<T> itemList = new CopyOnWriteArrayList();

   public void addItem(T t) {
      if (t != null) {
         this.itemList.add(t);
      }

   }

   public T pollItem() {
      if (this.itemList.isEmpty()) {
         return null;
      } else if (this.itemList.size() == 1) {
         return this.itemList.get(0);
      } else {
         if (this.count.intValue() > 2147473647) {
            this.count.set(this.count.get() % this.itemList.size());
         }

         int index = Math.abs(this.count.getAndIncrement()) % this.itemList.size();
         return this.itemList.get(index);
      }
   }

   public T peekItem() {
      if (this.itemList.isEmpty()) {
         return null;
      } else if (this.itemList.size() == 1) {
         return this.itemList.get(0);
      } else {
         int index = Math.abs(this.count.get()) % this.itemList.size();
         return this.itemList.get(index);
      }
   }

   public List<T> listItems() {
      return Collections.unmodifiableList(this.itemList);
   }
}
