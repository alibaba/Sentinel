package com.alibaba.csp.sentinel.adapter.jaxrs.future;

import com.alibaba.csp.sentinel.AsyncEntry;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.util.AdaptiveUtils;

import javax.ws.rs.core.Response;
import java.util.concurrent.*;

/**
 * Wrap Future to ensure the adaptive degradation entry exit
 *
 * @author ylnxwlp
 */
public class AdaptiveResponseFutureWrapper implements Future<Response> {

    private final AsyncEntry entry;
    private final Future<Response> delegate;
    private final String resourceName;

    public AdaptiveResponseFutureWrapper(AsyncEntry entry, Future<Response> delegate, String resourceName) {
        this.entry = entry;
        this.delegate = delegate;
        this.resourceName = resourceName;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        try {
            return delegate.cancel(mayInterruptIfRunning);
        } finally {
            exitEntry();
        }
    }

    @Override
    public boolean isCancelled() {
        return delegate.isCancelled();
    }

    @Override
    public boolean isDone() {
        return delegate.isDone();
    }

    @Override
    public Response get() throws InterruptedException, ExecutionException {
        try {
            Response original = delegate.get();
            return processAndStripMetrics(original);
        } finally {
            exitEntry();
        }
    }

    @Override
    public Response get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            Response original = delegate.get(timeout, unit);
            return processAndStripMetrics(original);
        } finally {
            exitEntry();
        }
    }

    private Response processAndStripMetrics(Response original) {
        String metrics = (String) original.getHeaders().getFirst("X-Server-Metrics");
        if (metrics != null && !metrics.isEmpty()) {
            entry.setServerMetric(AdaptiveUtils.parseServiceMetrics(metrics, resourceName));
        }

        return Response.fromResponse(original)
                .header("X-Server-Metrics", null)
                .build();
    }

    private void exitEntry() {
        if (entry != null) {
            entry.exit();
        }
    }
}