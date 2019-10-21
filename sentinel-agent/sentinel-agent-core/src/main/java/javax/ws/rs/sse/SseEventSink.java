package javax.ws.rs.sse;

import java.util.concurrent.CompletionStage;

public interface SseEventSink extends AutoCloseable {
   boolean isClosed();

   CompletionStage<?> send(OutboundSseEvent var1);

   void close();
}
