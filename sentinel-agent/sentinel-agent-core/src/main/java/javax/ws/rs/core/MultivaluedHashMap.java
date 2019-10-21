package javax.ws.rs.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MultivaluedHashMap<K, V> extends AbstractMultivaluedMap<K, V> implements Serializable {
   private static final long serialVersionUID = -6052320403766368902L;

   public MultivaluedHashMap() {
      super(new HashMap());
   }

   public MultivaluedHashMap(int initialCapacity) {
      super(new HashMap(initialCapacity));
   }

   public MultivaluedHashMap(int initialCapacity, float loadFactor) {
      super(new HashMap(initialCapacity, loadFactor));
   }

   public MultivaluedHashMap(MultivaluedMap<? extends K, ? extends V> map) {
      this();
      this.putAll(map);
   }

   private <T extends K, U extends V> void putAll(MultivaluedMap<T, U> map) {
      Iterator var2 = map.entrySet().iterator();

      while(var2.hasNext()) {
         Entry<T, List<U>> e = (Entry)var2.next();
         this.store.put(e.getKey(), new ArrayList((Collection)e.getValue()));
      }

   }

   public MultivaluedHashMap(Map<? extends K, ? extends V> map) {
      this();
      Iterator var2 = map.entrySet().iterator();

      while(var2.hasNext()) {
         Entry<? extends K, ? extends V> e = (Entry)var2.next();
         this.putSingle(e.getKey(), e.getValue());
      }

   }
}
