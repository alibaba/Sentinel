package com.alibaba.csp.sentinel.cluster.server.processor;

import com.alibaba.csp.sentinel.cluster.request.ClusterRequest;
import com.alibaba.csp.sentinel.cluster.response.ClusterResponse;

import java.util.Objects;

/**
 * Abstraction layer fault tolerance
 *
 * @param <T> type of request body
 * @param <R> type of response body
 * @author howiekang
 * @since 1.8
 */
public abstract class AbstractRequestProcessor<T, R> implements RequestProcessor<T, R> {

    /**
     * Process the cluster request.
     *
     * @param request Sentinel cluster request
     * @return the response after processed
     */
    @Override
    public ClusterResponse<R> processRequest(ClusterRequest<T> request) {
        // logical extraction
        // see issues https://github.com/alibaba/Sentinel/issues/1906
        T t = request.getData();
        if (Objects.isNull(t)) {
            return null;
        }

        return doProcessRequest(request, t);
    }

    /**
     * Process the cluster request.
     *
     * @param request Sentinel cluster request
     * @return the response after processed
     */
    public abstract ClusterResponse<R> doProcessRequest(ClusterRequest<T> request, T t);
}
