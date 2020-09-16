package com.alibaba.csp.sentinel.serialization.test.beans;

public class NonStandardSetter extends Simple {
    private String resource;

    public String getResource() {
        return resource;
    }

    // non-standard setter method which is not void
    public NonStandardSetter setResource(String resource) {
        this.resource = resource;
        return this;
    }
}
