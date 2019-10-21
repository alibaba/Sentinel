package com.alibaba.jvm.sandbox.api.listener.ext;

/**
 * 可附件的
 * <p>
 * 继承类拥有线程不安全的携带附件操作
 * </p>
 *
 * @author luanjia@taobao.com
 * @since {@code sandbox-api:1.0.10}
 */
public interface Attachment {

    /**
     * 添加附件
     *
     * @param attachment 消息附件
     */
    void attach(Object attachment);

    /**
     * 获取所携带的附件
     *
     * @param <T> 附件类型(自动强制转换)，请使用者自行保障类型不会转换失败!
     * @return 消息所携带的附件
     */
    <T> T attachment();

}
