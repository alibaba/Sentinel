package com.alibaba.csp.sentinel.adapter.zuul.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SEND_RESPONSE_FILTER_ORDER;


/**
 * @author tiger
 */
@ConfigurationProperties(prefix = SentinelZuulProperties.PREFIX)
public class SentinelZuulProperties {

    public static final String PREFIX = "sentinel.zuul";

    private boolean enabled = false;

    private Order order = new Order();

    public static class Order {

        private int post = SEND_RESPONSE_FILTER_ORDER - 10;

        private int pre = 10000;

        private int error = -1;

        public int getPost() {
            return post;
        }

        public void setPost(int post) {
            this.post = post;
        }

        public int getPre() {
            return pre;
        }

        public void setPre(int pre) {
            this.pre = pre;
        }

        public int getError() {
            return error;
        }

        public void setError(int error) {
            this.error = error;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
