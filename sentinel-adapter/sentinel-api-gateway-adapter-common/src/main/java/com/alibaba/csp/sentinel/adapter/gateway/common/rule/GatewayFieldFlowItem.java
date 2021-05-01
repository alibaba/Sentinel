package com.alibaba.csp.sentinel.adapter.gateway.common.rule;


/**
 * Special hotspot limit configuration for the gateway's personalized parameter field values
 * @see com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowItem
 * @author Renyansong
 **/
public class GatewayFieldFlowItem {

    /**
     * param value
     */
    private String object;

    /**
     * limit count
     */
    private Integer count;

    /**
     * param class type
     */
    private String classType;

    public GatewayFieldFlowItem() {}

    public GatewayFieldFlowItem(String object, Integer count, String classType) {
        this.object = object;
        this.count = count;
        this.classType = classType;
    }

    public String getObject() {
        return object;
    }

    public GatewayFieldFlowItem setObject(String object) {
        this.object = object;
        return this;
    }

    public Integer getCount() {
        return count;
    }

    public GatewayFieldFlowItem setCount(Integer count) {
        this.count = count;
        return this;
    }

    public String getClassType() {
        return classType;
    }

    public GatewayFieldFlowItem setClassType(String classType) {
        this.classType = classType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        GatewayFieldFlowItem item = (GatewayFieldFlowItem)o;

        if (object != null ? !object.equals(item.object) : item.object != null) { return false; }
        if (count != null ? !count.equals(item.count) : item.count != null) { return false; }
        return classType != null ? classType.equals(item.classType) : item.classType == null;
    }

    @Override
    public int hashCode() {
        int result = object != null ? object.hashCode() : 0;
        result = 31 * result + (count != null ? count.hashCode() : 0);
        result = 31 * result + (classType != null ? classType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GatewayFieldFlowItem{" +
                "object=" + object +
                ", count=" + count +
                ", classType='" + classType + '\'' +
                '}';
    }

}