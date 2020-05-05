package com.alibaba.csp.sentinel.dashboard.common.exception;

/**
 * Certain exception which can be used in all modules of sentinel-dashboard.
 *
 * @author cdfive
 */
public class DashboardException extends RuntimeException {

    public DashboardException() {
        super();
    }

    public DashboardException(String message) {
        super(message);
    }

    public DashboardException(String message, Throwable cause) {
        super(message, cause);
    }

    public DashboardException(Throwable cause) {
        super(cause);
    }
}
