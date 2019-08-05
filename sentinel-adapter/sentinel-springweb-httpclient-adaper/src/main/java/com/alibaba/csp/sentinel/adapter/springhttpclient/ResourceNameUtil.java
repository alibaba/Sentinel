package com.alibaba.csp.sentinel.adapter.springhttpclient;

import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jyy
 * @Since spring 3.1 or above because UriComponentsBuilder
 */
public class ResourceNameUtil {
    private ResourceNameUtil() {
    }
    public static String getResourceName(URI uri) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUri(uri);
        UriComponents uriComponents = uriComponentsBuilder.build();
        List<String> resources = Arrays.asList(uriComponents.getScheme(),
                uriComponents.getHost(),
                uriComponents.getPort()+"",
                uriComponents.getPath()
        );
        return resources.stream().filter(StringUtil::isNotEmpty).collect(Collectors.joining(","));
    }
}
