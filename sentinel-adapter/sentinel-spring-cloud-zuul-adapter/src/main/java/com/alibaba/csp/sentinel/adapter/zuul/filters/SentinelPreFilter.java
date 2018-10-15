package com.alibaba.csp.sentinel.adapter.zuul.filters;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.adapter.servlet.callback.RequestOriginParser;
import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlCleaner;
import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.csp.sentinel.adapter.servlet.util.FilterUtil;
import com.alibaba.csp.sentinel.adapter.zuul.properties.SentinelZuulProperties;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SERVICE_ID_KEY;

/**
 * @author tiger
 */
public class SentinelPreFilter extends AbstractSentinelFilter {

    private static final String EMPTY_ORIGIN = "";

    private Logger logger = LoggerFactory.getLogger(SentinelPreFilter.class);
//    RecordLog.warn("[RedisDataSource] Error when loading initial config", ex);

    public SentinelPreFilter(SentinelZuulProperties sentinelZuulProperties) {
        super(sentinelZuulProperties);
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    /**
     * This run before route filter so we can get more accurate RT time.
     */
    @Override
    public int filterOrder() {
        return getSentinelZuulProperties().getOrder().getPre();
    }

    @Override
    public Object run() throws ZuulException {
        // todo set up origin
        RequestContext ctx = RequestContext.getCurrentContext();
        try {
            // service target
            String origin = parseOrigin(ctx.getRequest());
            String serviceTarget = (String) ctx.get(SERVICE_ID_KEY);
            logger.info("[Sentinel Pre filter] serviceTarget:{}, serviceOrigin:{}", serviceTarget, origin);
            ContextUtil.enter(serviceTarget, origin);
            SphU.entry(serviceTarget, EntryType.IN);
            // url target
            String uriTarget = FilterUtil.filterTarget(ctx.getRequest());
            // Clean and unify the URL.
            // For REST APIs, you have to clean the URL (e.g. `/foo/1` and `/foo/2` -> `/foo/:id`), or
            // the amount of context and resources will exceed the threshold.
            UrlCleaner urlCleaner = WebCallbackManager.getUrlCleaner();
            if (urlCleaner != null) {
                uriTarget = urlCleaner.clean(uriTarget);
            }
            // Parse the request origin using registered origin parser.
            logger.info("[Sentinel Pre filter] uriTarget:{}, urlOrigin:{}", uriTarget, origin);
            ContextUtil.enter(uriTarget, origin);
            SphU.entry(uriTarget, EntryType.IN);
        } catch (BlockException ex) {
            // do the logic when flow control happens. // todo
            logger.warn("[Sentinel Flow Control happen]");
//            ctx.setThrowable(ex);
            throw new ZuulRuntimeException(ex);
        } catch (Exception ex) {
            throw new ZuulRuntimeException(ex);
        }
        return null;
    }

    // todo check origin
    private String parseOrigin(HttpServletRequest request) {
        RequestOriginParser originParser = WebCallbackManager.getRequestOriginParser();
        String origin = EMPTY_ORIGIN;
        if (originParser != null) {
            origin = originParser.parseOrigin(request);
            if (StringUtil.isEmpty(origin)) {
                return EMPTY_ORIGIN;
            }
        }
        return origin;
    }
}
