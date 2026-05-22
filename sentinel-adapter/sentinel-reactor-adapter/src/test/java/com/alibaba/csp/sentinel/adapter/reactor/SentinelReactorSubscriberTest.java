package com.alibaba.csp.sentinel.adapter.reactor;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.alibaba.csp.sentinel.AsyncEntry;

import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

import static org.junit.Assert.*;

public class SentinelReactorSubscriberTest {

    @Test
    public void testCurrentContextShouldUseCapturedAsyncContextWhenExitHappensDuringContextRead() throws Exception {
        AtomicBoolean allowCancelInContextRead = new AtomicBoolean(false);
        AtomicBoolean canceledInContextRead = new AtomicBoolean(false);
        AtomicReference<SentinelReactorSubscriber<Integer>> subscriberRef = new AtomicReference<>();

        CoreSubscriber<Integer> actual = new CoreSubscriber<Integer>() {
            @Override
            public Context currentContext() {
                SentinelReactorSubscriber<Integer> subscriber = subscriberRef.get();
                if (allowCancelInContextRead.get() && subscriber != null
                    && canceledInContextRead.compareAndSet(false, true)) {
                    subscriber.cancel();
                }
                return Context.empty();
            }

            @Override
            public void onSubscribe(Subscription s) {
            }

            @Override
            public void onNext(Integer integer) {
            }

            @Override
            public void onError(Throwable t) {
                fail("Unexpected error: " + t);
            }

            @Override
            public void onComplete() {
            }
        };

        SentinelReactorSubscriber<Integer> subscriber =
            new SentinelReactorSubscriber<>(new EntryConfig("testCurrentContextRace"), actual, false);
        subscriberRef.set(subscriber);
        subscriber.onSubscribe(new NoopSubscription());

        AsyncEntry currentEntry = getCurrentEntry(subscriber);
        assertNotNull(currentEntry);
        com.alibaba.csp.sentinel.context.Context asyncContextBeforeExit = currentEntry.getAsyncContext();
        assertNotNull(asyncContextBeforeExit);

        allowCancelInContextRead.set(true);
        Context reactorContext = subscriber.currentContext();

        assertTrue(canceledInContextRead.get());
        assertSame(asyncContextBeforeExit, reactorContext.get(SentinelReactorConstants.SENTINEL_CONTEXT_KEY));
        assertNull(currentEntry.getAsyncContext());
    }

    private static AsyncEntry getCurrentEntry(SentinelReactorSubscriber<?> subscriber) throws Exception {
        Field field = SentinelReactorSubscriber.class.getDeclaredField("currentEntry");
        field.setAccessible(true);
        return (AsyncEntry)field.get(subscriber);
    }

    private static final class NoopSubscription implements Subscription {
        @Override
        public void request(long n) {
        }

        @Override
        public void cancel() {
        }
    }
}
