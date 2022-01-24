package com.alibaba.csp.sentinel.adapter.spring.webmvc;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.BaseWebMvcConfig;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.log.RecordLog;

import javax.servlet.http.HttpServletRequest;

/**
 * Exception interception statistics
 *
 * @author Roy
 * @date 2022/1/249:15
 */
public class SentinelAfterException {

    protected static BaseWebMvcConfig baseWebMvcConfig;

    /**
     * The afterCompletion method of the HandlerInterceptor does not catch exception statistics when using @ControllerAdvice for global exception fetching
     * Call SentinelAfterException.exit() method to realize exception statistics
     *
     * @param request
     * @param ex
     */
    public static void exit(HttpServletRequest request, Exception ex) {
        if (increaseReferece(request, baseWebMvcConfig.getRequestRefName(), -1) != 0) {
            return;
        }

        Entry entry = getEntryInRequest(request, baseWebMvcConfig.getRequestAttributeName());
        if (entry == null) {
            // should not happen
            RecordLog.warn("No entry found in request, key: {}",
                    SentinelAfterException.class.getSimpleName(), baseWebMvcConfig.getRequestAttributeName());
            return;
        }

        traceExceptionAndExit(entry, ex);
        removeEntryInRequest(request);
        ContextUtil.exit();
    }

    /**
     * @param request
     * @param rcKey
     * @param step
     * @return reference count after increasing (initial value as zero to be increased)
     */
    private static Integer increaseReferece(HttpServletRequest request, String rcKey, int step) {
        Object obj = request.getAttribute(rcKey);

        if (obj == null) {
            // initial
            obj = Integer.valueOf(0);
        }

        Integer newRc = (Integer) obj + step;
        request.setAttribute(rcKey, newRc);
        return newRc;
    }

    protected static Entry getEntryInRequest(HttpServletRequest request, String attrKey) {
        Object entryObject = request.getAttribute(attrKey);
        return entryObject == null ? null : (Entry) entryObject;
    }


    protected static void removeEntryInRequest(HttpServletRequest request) {
        request.removeAttribute(baseWebMvcConfig.getRequestAttributeName());
    }

    protected static void traceExceptionAndExit(Entry entry, Exception ex) {
        if (entry != null) {
            if (ex != null) {
                Tracer.traceEntry(ex, entry);
            }
            entry.exit();
        }
    }
}
