package com.alibaba.csp.sentinel.dashboard.service.api.exception;

/**
 * @author cdfive
 */
public class DashboardServiceException extends RuntimeException {

    public DashboardServiceException() {
        super();
    }

    public DashboardServiceException(String message) {
        super(message);
    }

    public DashboardServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DashboardServiceException(Throwable cause) {
        super(cause);
    }
}
