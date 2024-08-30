package com.alibaba.csp.sentinel.cluster.client;

import org.junit.Test;
import org.mockito.Spy;
import org.junit.runner.RunWith;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.junit.MockitoJUnitRunner;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;

@RunWith(MockitoJUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DefaultClusterTokenClientTest {

    @Spy
    DefaultClusterTokenClient client = new DefaultClusterTokenClient();
    final int testInterval = 60;

    @Test
    public void testClientCacheWithRemoteOK() throws Exception {
        client.setInterval(testInterval);
        client.resetCache();
        doReturn(new TokenResult(TokenResultStatus.OK)).when(client).requestToken(anyLong(), anyInt(), anyBoolean());
        int prefetch = 10;
        TokenResult ret = client.requestTokenWithCache(1L, 1, prefetch);
        // first should remote
        Assert.assertTrue(!ret.isFromCached());
        Assert.assertEquals((long)TokenResultStatus.OK, (long)ret.getStatus());
        Assert.assertEquals(0, ret.getWaitInMs());

        Thread.sleep(testInterval);
        Assert.assertEquals(prefetch, client.currentRuleCached(1L));
        for (int i = 0;i < prefetch * 3; i++) {
            ret = client.requestTokenWithCache(1L, 1, prefetch);
            Assert.assertTrue(ret.isFromCached());
            Assert.assertEquals((long) TokenResultStatus.OK, (long) ret.getStatus());
            Assert.assertEquals(0, ret.getWaitInMs());
        }
        Assert.assertEquals(-1 * prefetch * 2, client.currentRuleCached(1L));
        for (int cnt = 1; cnt <= prefetch / 2; cnt++) {
            ret = client.requestTokenWithCache(1L, cnt, prefetch);
            Assert.assertTrue(ret.isFromCached());
            Assert.assertEquals((long) TokenResultStatus.FAIL, (long) ret.getStatus());
        }
        ret = client.requestTokenWithCache(1L, prefetch / 2 + 1, prefetch);
        Assert.assertTrue(!ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.OK, (long) ret.getStatus());

        ret = client.requestTokenWithCache(1L, prefetch + 1, prefetch);
        Assert.assertTrue(!ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.OK, (long) ret.getStatus());
        Assert.assertEquals(0, ret.getWaitInMs());

        Thread.sleep(testInterval * 2);
        Assert.assertEquals(prefetch, client.currentRuleCached(1L));
        // should refill prefetch * 2 in once to make sure we have at least prefetch count in cache
        ret = client.requestTokenWithCache(1L, prefetch, prefetch);
        Assert.assertTrue(ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.OK, (long) ret.getStatus());
        Assert.assertEquals(0, ret.getWaitInMs());
        Assert.assertEquals(0, client.currentRuleCached(1L));

        Thread.sleep(testInterval);
        Assert.assertEquals(prefetch, client.currentRuleCached(1L));
        ret = client.requestTokenWithCache(1L, prefetch / 2, prefetch);
        Assert.assertTrue(ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.OK, (long) ret.getStatus());
        Assert.assertEquals(0, ret.getWaitInMs());
        Assert.assertEquals(prefetch / 2, client.currentRuleCached(1L));

        Thread.sleep(testInterval);
        Assert.assertEquals(prefetch / 2, client.currentRuleCached(1L));
        ret = client.requestTokenWithCache(1L, prefetch + 1, prefetch);
        // use less than half will not refill, so cache is not enough
        Assert.assertTrue(!ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.OK, (long) ret.getStatus());
        Assert.assertEquals(0, ret.getWaitInMs());

        ret = client.requestTokenWithCache(1L, 1, prefetch);
        Assert.assertTrue(ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.OK, (long) ret.getStatus());
        Assert.assertEquals(0, ret.getWaitInMs());
        Assert.assertEquals(prefetch / 2 - 1, client.currentRuleCached(1L));
        // refill at least prefetch at once, so we can get at most 1.5 * prefetch in cache
        Thread.sleep(testInterval);
        Assert.assertEquals(prefetch, client.currentRuleCached(1L));
        ret = client.requestTokenWithCache(1L, prefetch, prefetch);
        Assert.assertTrue(ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.OK, (long) ret.getStatus());
        Assert.assertEquals(0, ret.getWaitInMs());
        Assert.assertEquals(0, client.currentRuleCached(1L));
    }

    @Test
    public void testClientCacheWithRemoteWait() throws Exception {
        client.setInterval(testInterval);
        client.resetCache();
        doReturn(new TokenResult(TokenResultStatus.OK)).when(client).requestToken(anyLong(), anyInt(), anyBoolean());
        int prefetch = 10;
        TokenResult ret = client.requestTokenWithCache(1L, 1, prefetch);
        // first should remote
        Assert.assertTrue(!ret.isFromCached());
        Assert.assertEquals((long)TokenResultStatus.OK, (long)ret.getStatus());
        Assert.assertEquals(0, ret.getWaitInMs());

        Thread.sleep(testInterval);

        TokenResult waitResult = new TokenResult(TokenResultStatus.SHOULD_WAIT);
        waitResult.setWaitInMs(testInterval * 4);
        doReturn(waitResult).when(client).requestToken(anyLong(), anyInt(), anyBoolean());

        ret = client.requestTokenWithCache(1L, prefetch + 1, prefetch);
        Assert.assertTrue(!ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.SHOULD_WAIT, (long) ret.getStatus());
        Assert.assertEquals(waitResult.getWaitInMs(), ret.getWaitInMs());

        for (int i = 0;i < prefetch * 3; i++) {
            ret = client.requestTokenWithCache(1L, 1, prefetch);
            Assert.assertTrue(ret.isFromCached());
            Assert.assertEquals((long) TokenResultStatus.OK, (long) ret.getStatus());
            Assert.assertEquals(0, ret.getWaitInMs());
        }
        Assert.assertEquals(-1 * prefetch * 2, client.currentRuleCached(1L));

        for (int cnt = 1; cnt <= prefetch / 2; cnt++) {
            ret = client.requestTokenWithCache(1L, cnt, prefetch);
            Assert.assertTrue(ret.isFromCached());
            Assert.assertEquals((long) TokenResultStatus.FAIL, (long) ret.getStatus());
        }
        ret = client.requestTokenWithCache(1L, prefetch / 2 + 1, prefetch);
        Assert.assertTrue(!ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.SHOULD_WAIT, (long) ret.getStatus());
        Assert.assertEquals(waitResult.getWaitInMs(), ret.getWaitInMs());

        Thread.sleep(testInterval * 2);
        // prefetch count will be 2 * prefetch, and last status became should wait
        Assert.assertEquals(-1 * prefetch * 2, client.currentRuleCached(1L));
        // refill will be waited until the timeout
        ret = client.requestTokenWithCache(1L, 1, prefetch);
        Assert.assertTrue(ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.SHOULD_WAIT, (long) ret.getStatus());
        Assert.assertTrue(String.format("wait ms not as expected: %d", ret.getWaitInMs()), waitResult.getWaitInMs() - testInterval * 3 <= ret.getWaitInMs());
        Assert.assertTrue(String.format("wait ms not as expected: %d", ret.getWaitInMs()), waitResult.getWaitInMs() - testInterval > ret.getWaitInMs());

        ret = client.requestTokenWithCache(1L, prefetch / 2 + 1, prefetch);
        Assert.assertTrue(ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.SHOULD_WAIT, (long) ret.getStatus());
        Assert.assertTrue(String.format("wait ms not as expected: %d", ret.getWaitInMs()), waitResult.getWaitInMs() - testInterval * 3 <= ret.getWaitInMs());
        Assert.assertTrue(String.format("wait ms not as expected: %d", ret.getWaitInMs()), waitResult.getWaitInMs() - testInterval > ret.getWaitInMs());

        ret = client.requestTokenWithCache(1L, prefetch + 1, prefetch);
        Assert.assertTrue(!ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.SHOULD_WAIT, (long) ret.getStatus());
        Assert.assertEquals(waitResult.getWaitInMs(), ret.getWaitInMs());

        Thread.sleep(testInterval);
        Assert.assertEquals(-1 * prefetch * 2 - 1 - prefetch / 2 - 1, client.currentRuleCached(1L));
        // wait the timeout
        Thread.sleep(waitResult.getWaitInMs());
        // the prefetch count should be added to the count
        Assert.assertEquals(- 1 - prefetch / 2 - 1, client.currentRuleCached(1L));

        doReturn(new TokenResult(TokenResultStatus.OK)).when(client).requestToken(anyLong(), anyInt(), anyBoolean());
        Thread.sleep(waitResult.getWaitInMs());
        Thread.sleep(testInterval);
        Assert.assertEquals(prefetch, client.currentRuleCached(1L));
        ret = client.requestTokenWithCache(1L, prefetch + 1, prefetch);
        Assert.assertTrue(!ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.OK, (long) ret.getStatus());

        doReturn(waitResult).when(client).requestToken(anyLong(), anyInt(), anyBoolean());

        ret = client.requestTokenWithCache(1L, prefetch - 1, prefetch);
        Assert.assertTrue(ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.OK, (long) ret.getStatus());
        Assert.assertEquals(0, ret.getWaitInMs());
        Assert.assertEquals(1, client.currentRuleCached(1L));

        Thread.sleep(testInterval);
        // refill will be waiting and the last state became waiting, but local has some tokens
        ret = client.requestTokenWithCache(1L, 1, prefetch);
        Assert.assertTrue(ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.OK, (long) ret.getStatus());
        Assert.assertEquals(0, ret.getWaitInMs());

        Assert.assertEquals(0, client.currentRuleCached(1L));

        ret = client.requestTokenWithCache(1L, prefetch, prefetch);
        Assert.assertTrue(ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.SHOULD_WAIT, (long) ret.getStatus());
        Assert.assertTrue(String.format("wait ms not as expected: %d", ret.getWaitInMs()), waitResult.getWaitInMs() - testInterval * 2 <= ret.getWaitInMs());
        Assert.assertTrue(String.format("wait ms not as expected: %d", ret.getWaitInMs()), waitResult.getWaitInMs() > ret.getWaitInMs());
        ret = client.requestTokenWithCache(1L, prefetch - 1, prefetch);
        Assert.assertTrue(ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.SHOULD_WAIT, (long) ret.getStatus());

        ret = client.requestTokenWithCache(1L, 1, prefetch);
        Assert.assertTrue(ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.FAIL, (long) ret.getStatus());

        ret = client.requestTokenWithCache(1L, prefetch / 2 + 1, prefetch);
        Assert.assertTrue(!ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.SHOULD_WAIT, (long) ret.getStatus());
        Assert.assertEquals(waitResult.getWaitInMs(), ret.getWaitInMs());

        Assert.assertEquals(-1 * prefetch * 2 + 1, client.currentRuleCached(1L));
        Thread.sleep(testInterval);
        Assert.assertEquals(-1 * prefetch * 2 + 1, client.currentRuleCached(1L));
        // refill will be waiting
        ret = client.requestTokenWithCache(1L, 1, prefetch);
        Assert.assertTrue(ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.FAIL, (long) ret.getStatus());
        ret = client.requestTokenWithCache(1L, prefetch / 2 + 1, prefetch);
        Assert.assertTrue(!ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.SHOULD_WAIT, (long) ret.getStatus());
        Assert.assertEquals(waitResult.getWaitInMs(), ret.getWaitInMs());
        ret = client.requestTokenWithCache(1L, prefetch + 1, prefetch);
        Assert.assertTrue(!ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.SHOULD_WAIT, (long) ret.getStatus());
        Assert.assertEquals(waitResult.getWaitInMs(), ret.getWaitInMs());

        Thread.sleep(waitResult.getWaitInMs() + testInterval);
        // the prefetch count should be added to the count
        Assert.assertEquals(-1 * prefetch, client.currentRuleCached(1L));
        Thread.sleep(waitResult.getWaitInMs() + testInterval);
        Assert.assertEquals(prefetch, client.currentRuleCached(1L));
    }

    @Test
    public void testClientCacheWithRemoteBlocked() throws Exception {
        client.setInterval(testInterval);
        client.resetCache();
        doReturn(new TokenResult(TokenResultStatus.OK)).when(client).requestToken(anyLong(), anyInt(), anyBoolean());
        int prefetch = 10;
        TokenResult ret = client.requestTokenWithCache(1L, 1, prefetch);
        // first should remote
        Assert.assertTrue(!ret.isFromCached());
        Assert.assertEquals((long)TokenResultStatus.OK, (long)ret.getStatus());
        Assert.assertEquals(0, ret.getWaitInMs());

        Thread.sleep(testInterval);
        // begin test while remote refused
        doReturn(new TokenResult(TokenResultStatus.BLOCKED)).when(client).requestToken(anyLong(), anyInt(), anyBoolean());

        ret = client.requestTokenWithCache(1L, prefetch + 1, prefetch);
        Assert.assertTrue(!ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.BLOCKED, (long) ret.getStatus());
        Assert.assertEquals(0, ret.getWaitInMs());

        for (int i = 0;i < prefetch * 3; i++) {
            ret = client.requestTokenWithCache(1L, 1, prefetch);
            Assert.assertTrue(ret.isFromCached());
            Assert.assertEquals((long) TokenResultStatus.OK, (long) ret.getStatus());
            Assert.assertEquals(0, ret.getWaitInMs());
        }
        Assert.assertEquals(-1 * prefetch * 2, client.currentRuleCached(1L));

        for (int cnt = 1; cnt <= prefetch / 2; cnt++) {
            ret = client.requestTokenWithCache(1L, cnt, prefetch);
            Assert.assertTrue(ret.isFromCached());
            Assert.assertEquals((long) TokenResultStatus.FAIL, (long) ret.getStatus());
        }
        ret = client.requestTokenWithCache(1L, prefetch / 2 + 1, prefetch);
        Assert.assertTrue(!ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.BLOCKED, (long) ret.getStatus());

        Thread.sleep(testInterval);
        // refill will be blocked
        ret = client.requestTokenWithCache(1L, 1, prefetch);
        Assert.assertTrue(ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.BLOCKED, (long) ret.getStatus());
        ret = client.requestTokenWithCache(1L, prefetch / 2 + 1, prefetch);
        Assert.assertTrue(ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.BLOCKED, (long) ret.getStatus());
        ret = client.requestTokenWithCache(1L, prefetch + 1, prefetch);
        Assert.assertTrue(!ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.BLOCKED, (long) ret.getStatus());
        Assert.assertEquals(0, ret.getWaitInMs());


        doReturn(new TokenResult(TokenResultStatus.OK)).when(client).requestToken(anyLong(), anyInt(), anyBoolean());
        Thread.sleep(testInterval * 2);
        Assert.assertEquals(prefetch, client.currentRuleCached(1L));

        doReturn(new TokenResult(TokenResultStatus.BLOCKED)).when(client).requestToken(anyLong(), anyInt(), anyBoolean());

        ret = client.requestTokenWithCache(1L, prefetch + 1, prefetch);
        Assert.assertTrue(!ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.BLOCKED, (long) ret.getStatus());
        Assert.assertEquals(0, ret.getWaitInMs());

        for (int i = 0;i < prefetch / 2 + 1; i++) {
            ret = client.requestTokenWithCache(1L, 1, prefetch);
            Assert.assertTrue(ret.isFromCached());
            Assert.assertEquals((long) TokenResultStatus.OK, (long) ret.getStatus());
            Assert.assertEquals(0, ret.getWaitInMs());
        }
        Thread.sleep(testInterval);
        Assert.assertEquals(prefetch - prefetch / 2 - 1, client.currentRuleCached(1L));
        // refill will be blocked and the last state became blocked, but local has some tokens
        ret = client.requestTokenWithCache(1L, prefetch - prefetch / 2 - 1, prefetch);
        Assert.assertTrue(ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.OK, (long) ret.getStatus());
        Assert.assertEquals(0, ret.getWaitInMs());
        Assert.assertEquals(0, client.currentRuleCached(1L));

        for (int cnt = 1; cnt <= prefetch / 2; cnt++) {
            ret = client.requestTokenWithCache(1L, cnt, prefetch);
            Assert.assertTrue(ret.isFromCached());
            Assert.assertEquals((long) TokenResultStatus.BLOCKED, (long) ret.getStatus());
        }
        ret = client.requestTokenWithCache(1L, prefetch / 2 + 1, prefetch);
        Assert.assertTrue(ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.BLOCKED, (long) ret.getStatus());

        Thread.sleep(testInterval);
        // refill will be blocked
        ret = client.requestTokenWithCache(1L, 1, prefetch);
        Assert.assertTrue(ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.BLOCKED, (long) ret.getStatus());
        ret = client.requestTokenWithCache(1L, prefetch / 2 + 1, prefetch);
        Assert.assertTrue(ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.BLOCKED, (long) ret.getStatus());
        ret = client.requestTokenWithCache(1L, prefetch + 1, prefetch);
        Assert.assertTrue(!ret.isFromCached());
        Assert.assertEquals((long) TokenResultStatus.BLOCKED, (long) ret.getStatus());
        Assert.assertEquals(0, ret.getWaitInMs());
    }

    @Test
    public void testConcurrencyRequestClientCache() throws Exception {
        client.setInterval(1);
        client.resetCache();
        doReturn(new TokenResult(TokenResultStatus.OK)).when(client).requestToken(anyLong(), anyInt(), anyBoolean());
        int prefetch = 200;

        ScheduledExecutorService testScheduler = Executors.newScheduledThreadPool(16,
                new NamedThreadFactory("test-scheduler", true));

        AtomicInteger blocked = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        AtomicInteger ok = new AtomicInteger();
        AtomicInteger cached = new AtomicInteger();
        AtomicInteger notCached = new AtomicInteger();
        AtomicBoolean stopped = new AtomicBoolean(false);
        for (int concurrency = 0; concurrency < 8; concurrency++) {
            testScheduler.submit(() -> {
                System.out.println("running begin");
                for (int loop = 0; loop < 200; loop++) {
                    for (int cnt = 1; cnt < prefetch * 2; cnt++) {
                        TokenResult ret = client.requestTokenWithCache(1L, cnt, prefetch);
                        if (cnt > prefetch * 1.5) {
                            Assert.assertTrue(!ret.isFromCached());
                            Assert.assertEquals((long) TokenResultStatus.OK, (long) ret.getStatus());
                            notCached.incrementAndGet();
                        } else {
                            if (ret.getStatus() == TokenResultStatus.BLOCKED) {
                                Assert.assertTrue(ret.isFromCached());
                                blocked.incrementAndGet();
                                cached.incrementAndGet();
                            } else if (ret.getStatus() == TokenResultStatus.FAIL) {
                                Assert.assertTrue(ret.isFromCached());
                                failed.incrementAndGet();
                                cached.incrementAndGet();
                            } else {
                                ok.incrementAndGet();
                                if (ret.isFromCached()) {
                                    cached.incrementAndGet();
                                } else {
                                    notCached.incrementAndGet();
                                }
                            }
                        }
                        Assert.assertEquals(0, ret.getWaitInMs());
                        if (cnt % 50 == 0) {
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                    if (stopped.get()) {
                        break;
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {}
                }
                System.out.println("running done");
            });
        }

        testScheduler.submit(() -> {
            for (; !stopped.get() ;) {
                System.out.println("current rule cached: " + client.currentRuleCached(1L));
                System.out.println("current failed: " + failed.get() + ", passed: " + ok.get() + ", cached: " + cached.get() + ", not cached: " + notCached.get());
                try {
                    Thread.sleep(3);
                } catch (InterruptedException e) {
                }
            }
        });
        Thread.sleep(2000);

        stopped.set(true);
        testScheduler.shutdown();
        testScheduler.awaitTermination(1, TimeUnit.SECONDS);

        System.out.println("current rule cached: " + client.currentRuleCached(1L));
        System.out.println("current failed: " + failed.get() + ", passed: " + ok.get() + ", cached: " + cached.get()
                + ", not cached: " + notCached.get());
        Assert.assertTrue(blocked.get() + failed.get() < cached.get());
        Assert.assertTrue(cached.get() + notCached.get() > blocked.get() + ok.get() + failed.get());
        Assert.assertTrue(failed.get() > 0);
        Assert.assertTrue(ok.get() > 0);
        Assert.assertTrue(client.currentRuleCached(1L) >= prefetch / 2);
    }
}
