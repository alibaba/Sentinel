package com.alibaba.csp.sentinel.adapter.gateway.zuul.enums;

import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
/**
 * @author jzh
 */
public enum BlockResponseEntryEnum {
    DEFAULT("OK", HttpStatus.OK),
    FLOWEXCEPTION("com.alibaba.csp.sentinel.slots.block.flow.FlowException", HttpStatus.TOO_MANY_REQUESTS),
    RUNTIMEEXCEPTION("java.lang.RuntimeException", HttpStatus.INTERNAL_SERVER_ERROR);

    static Map<String, BlockResponseEntryEnum> enumMap = new HashMap<>();

    static {
        for (BlockResponseEntryEnum typeEnum : BlockResponseEntryEnum.values()) {
            enumMap.put(typeEnum.type, typeEnum);
        }
    }

    private String type;
    private HttpStatus httpStatus;

    BlockResponseEntryEnum(String type, HttpStatus httpStatus) {
        this.type = type;
        this.httpStatus = httpStatus;
    }

    public static BlockResponseEntryEnum getByStatus(String status) {
        return enumMap.get(status);
    }

    public static Map<String, BlockResponseEntryEnum> getEnumMap() {
        return enumMap;
    }

    public static void setEnumMap(Map<String, BlockResponseEntryEnum> enumMap) {
        BlockResponseEntryEnum.enumMap = enumMap;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
