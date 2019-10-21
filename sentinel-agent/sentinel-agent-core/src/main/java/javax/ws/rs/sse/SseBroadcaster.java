package javax.ws.rs.sse;

import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface SseBroadcaster extends AutoCloseable {
   void onError(BiConsumer<SseEventSink, Throwable> var1);

   void onClose(Consumer<SseEventSink> var1);

   void register(SseEventSink var1);

   CompletionStage<?> broadcast(OutboundSseEvent var1);

   void close();
}
