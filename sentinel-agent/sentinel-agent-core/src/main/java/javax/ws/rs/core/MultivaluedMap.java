package javax.ws.rs.core;

import java.util.List;
import java.util.Map;

public interface MultivaluedMap<K, V> extends Map<K, List<V>> {
   void putSingle(K var1, V var2);

   void add(K var1, V var2);

   V getFirst(K var1);

   void addAll(K var1, V... var2);

   void addAll(K var1, List<V> var2);

   void addFirst(K var1, V var2);

   boolean equalsIgnoreValueOrder(MultivaluedMap<K, V> var1);
}
