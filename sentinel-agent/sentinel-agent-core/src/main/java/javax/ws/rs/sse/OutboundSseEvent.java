package javax.ws.rs.sse;

import java.lang.reflect.Type;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

public interface OutboundSseEvent extends SseEvent {
   Class<?> getType();

   Type getGenericType();

   MediaType getMediaType();

   Object getData();

   public interface Builder {
      Builder id(String var1);

      Builder name(String var1);

      Builder reconnectDelay(long var1);

      Builder mediaType(MediaType var1);

      Builder comment(String var1);

      Builder data(Class var1, Object var2);

      Builder data(GenericType var1, Object var2);

      Builder data(Object var1);

      OutboundSseEvent build();
   }
}
