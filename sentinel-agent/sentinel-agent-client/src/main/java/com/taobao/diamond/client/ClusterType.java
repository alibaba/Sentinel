package com.taobao.diamond.client;

public enum ClusterType {

    DIAMOND,
    BASESTONE;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

    static public ClusterType getByValue(String str) {
        if (str.equalsIgnoreCase("diamond")) {
            return DIAMOND;
        } else if (str.equalsIgnoreCase("basestone")) {
            return BASESTONE;
        } else {
            throw new IllegalArgumentException("illegal cluster type: " + str);
        }
    }
}
