package javax.ws.rs.sse;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

public interface InboundSseEvent extends SseEvent {
   boolean isEmpty();

   String readData();

   <T> T readData(Class<T> var1);

   <T> T readData(GenericType<T> var1);

   <T> T readData(Class<T> var1, MediaType var2);

   <T> T readData(GenericType<T> var1, MediaType var2);
}
