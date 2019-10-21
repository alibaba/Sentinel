package javax.ws.rs.sse;

public interface Sse {
   OutboundSseEvent.Builder newEventBuilder();

   default OutboundSseEvent newEvent(String data) {
      if (data == null) {
         throw new IllegalArgumentException("Parameter 'data' must not be null.");
      } else {
         return this.newEventBuilder().data((Class)String.class, data).build();
      }
   }

   default OutboundSseEvent newEvent(String name, String data) {
      if (data == null) {
         throw new IllegalArgumentException("Parameter 'data' must not be null.");
      } else if (name == null) {
         throw new IllegalArgumentException("Parameter 'name' must not be null.");
      } else {
         return this.newEventBuilder().data((Class)String.class, data).name(name).build();
      }
   }

   SseBroadcaster newBroadcaster();
}
