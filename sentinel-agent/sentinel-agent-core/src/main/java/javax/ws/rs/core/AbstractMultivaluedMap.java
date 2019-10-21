package javax.ws.rs.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public abstract class AbstractMultivaluedMap<K, V> implements MultivaluedMap<K, V> {
   protected final Map<K, List<V>> store;

   public AbstractMultivaluedMap(Map<K, List<V>> store) {
      if (store == null) {
         throw new NullPointerException("Underlying store must not be 'null'.");
      } else {
         this.store = store;
      }
   }

   public final void putSingle(K key, V value) {
      List<V> values = this.getValues(key);
      values.clear();
      if (value != null) {
         values.add(value);
      } else {
         this.addNull(values);
      }

   }

   protected void addNull(List<V> values) {
   }

   protected void addFirstNull(List<V> values) {
   }

   public final void add(K key, V value) {
      List<V> values = this.getValues(key);
      if (value != null) {
         values.add(value);
      } else {
         this.addNull(values);
      }

   }

   public final void addAll(K key, V... newValues) {
      if (newValues == null) {
         throw new NullPointerException("Supplied array of values must not be null.");
      } else if (newValues.length != 0) {
         List<V> values = this.getValues(key);
         Object[] var4 = newValues;
         int var5 = newValues.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            V value = (V)var4[var6];
            if (value != null) {
               values.add(value);
            } else {
               this.addNull(values);
            }
         }

      }
   }

   public final void addAll(K key, List<V> valueList) {
      if (valueList == null) {
         throw new NullPointerException("Supplied list of values must not be null.");
      } else if (!valueList.isEmpty()) {
         List<V> values = this.getValues(key);
         Iterator var4 = valueList.iterator();

         while(var4.hasNext()) {
            V value = (V)var4.next();
            if (value != null) {
               values.add(value);
            } else {
               this.addNull(values);
            }
         }

      }
   }

   public final V getFirst(K key) {
      List<V> values = (List)this.store.get(key);
      return values != null && values.size() > 0 ? values.get(0) : null;
   }

   public final void addFirst(K key, V value) {
      List<V> values = this.getValues(key);
      if (value != null) {
         values.add(0, value);
      } else {
         this.addFirstNull(values);
      }

   }

   protected final List<V> getValues(K key) {
      List<V> l = (List)this.store.get(key);
      if (l == null) {
         l = new LinkedList();
         this.store.put(key, l);
      }

      return (List)l;
   }

   public String toString() {
      return this.store.toString();
   }

   public int hashCode() {
      return this.store.hashCode();
   }

   public boolean equals(Object o) {
      return this.store.equals(o);
   }

   public Collection<List<V>> values() {
      return this.store.values();
   }

   public int size() {
      return this.store.size();
   }

   public List<V> remove(Object key) {
      return (List)this.store.remove(key);
   }

   public void putAll(Map<? extends K, ? extends List<V>> m) {
      this.store.putAll(m);
   }

   public List<V> put(K key, List<V> value) {
      return (List)this.store.put(key, value);
   }

   public Set<K> keySet() {
      return this.store.keySet();
   }

   public boolean isEmpty() {
      return this.store.isEmpty();
   }

   public List<V> get(Object key) {
      return (List)this.store.get(key);
   }

   public Set<Entry<K, List<V>>> entrySet() {
      return this.store.entrySet();
   }

   public boolean containsValue(Object value) {
      return this.store.containsValue(value);
   }

   public boolean containsKey(Object key) {
      return this.store.containsKey(key);
   }

   public void clear() {
      this.store.clear();
   }

   public boolean equalsIgnoreValueOrder(MultivaluedMap<K, V> omap) {
      if (this == omap) {
         return true;
      } else if (!this.keySet().equals(omap.keySet())) {
         return false;
      } else {
         Iterator var2 = this.entrySet().iterator();

         while(var2.hasNext()) {
            Entry<K, List<V>> e = (Entry)var2.next();
            List<V> olist = (List)omap.get(e.getKey());
            if (((List)e.getValue()).size() != olist.size()) {
               return false;
            }

            Iterator var5 = ((List)e.getValue()).iterator();

            while(var5.hasNext()) {
               V v = (V)var5.next();
               if (!olist.contains(v)) {
                  return false;
               }
            }
         }

         return true;
      }
   }
}
