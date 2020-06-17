package com.alibaba.csp.sentinel.adapter.dubbo.origin;

/**
 * Global origin parser registry for Dubbo.
 *
 * @author tc
 * @date 2020/6/10
 */
public final class DubboOriginParserRegistry {

    private static volatile DubboOriginParser dubboOriginParser = new DefaultDubboOriginParser();

    public static DubboOriginParser getDubboOriginParser() {
        return dubboOriginParser;
    }

    public static void setDubboOriginParser(DubboOriginParser dubboOrigin) {
        DubboOriginParserRegistry.dubboOriginParser = dubboOrigin;
    }

    private DubboOriginParserRegistry() {}

}
