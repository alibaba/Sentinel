package com.alibaba.jvm.sandbox.api;

/**
 * 模块异常
 *
 * @author luanjia@taobao.com
 */
public class ModuleException extends Exception {

    // 模块ID
    private final String uniqueId;

    // 错误码
    private final ErrorCode errorCode;

    /**
     * 构造模块异常
     *
     * @param uniqueId  模块ID
     * @param errorCode 错误码
     */
    public ModuleException(final String uniqueId,
                           final ErrorCode errorCode) {
        this.uniqueId = uniqueId;
        this.errorCode = errorCode;
    }

    /**
     * 构造模块异常
     *
     * @param uniqueId  模块ID
     * @param errorCode 错误码
     * @param cause     错误原因
     */
    public ModuleException(final String uniqueId,
                           final ErrorCode errorCode,
                           final Throwable cause) {
        super(cause);
        this.uniqueId = uniqueId;
        this.errorCode = errorCode;
    }

    /**
     * 获取模块ID
     *
     * @return 模块ID
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 错误码
     */
    public enum ErrorCode {

        /**
         * 模块不存在
         */
        MODULE_NOT_EXISTED,

        /**
         * 模块加载失败
         */
        MODULE_LOAD_ERROR,

        /**
         * 模块卸载失败
         */
        MODULE_UNLOAD_ERROR,

        /**
         * 模块激活失败
         */
        MODULE_ACTIVE_ERROR,

        /**
         * 模块冻结失败
         */
        MODULE_FROZEN_ERROR

    }
}
