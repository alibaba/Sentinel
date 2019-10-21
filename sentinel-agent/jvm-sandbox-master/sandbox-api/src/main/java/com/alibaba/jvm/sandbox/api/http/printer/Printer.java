package com.alibaba.jvm.sandbox.api.http.printer;

import java.util.concurrent.TimeUnit;

/**
 * 文本输出
 *
 * @author luanjia@taobao.com
 */
public interface Printer {

    /**
     * 输出文本
     *
     * @param string 文本
     * @return this
     */
    Printer print(String string);

    /**
     * 输出文本
     *
     * @param string 文本
     * @return this
     */
    Printer println(String string);

    /**
     * 刷盘
     *
     * @return this
     */
    Printer flush();

    /**
     * 挂起当前线程，等待被打断
     * <p>线程挂起等待输出被打断</p>
     * <p>因为HTTP请求是短连接，所以这里为了避免线程立即返回，可以使用这个方法来阻塞当前Http请求响应线程，直至阻塞被中断</p>
     * <p>被打断过的请求无法再次被挂起等待，当曾经被调用过{@link #broken()}之后，再次调用本方法将无法阻塞当前线程</p>
     * 有两种场景会打断本次挂起,任何以下场景都能结束并唤醒当前被阻塞的线程
     * <ol>
     * <li>网络I/O被中断</li>
     * <li>{@link #broken()}被调用</li>
     * <li>{@link Thread#interrupt()}被调用</li>
     * </ol>
     *
     * @return this
     */
    Printer waitingForBroken();

    /**
     * 挂起当前线程，等待被打断或超时
     * {@link #waitingForBroken()}
     *
     * @param time 超时时间
     * @param unit 超时时间单位
     * @return FALSE:在超时时间到达之前返回;TRUE:因超时而返回;
     */
    boolean waitingForBroken(long time, TimeUnit unit);

    /**
     * 打断
     * <p>将会唤醒因{@link #waitingForBroken()}或{@link #waitingForBroken(long, TimeUnit)}而被阻塞的线程</p>
     *
     * @return this
     */
    Printer broken();

    /**
     * 是否已经被打断
     *
     * @return TRUE:已被打断;FALSE:尚未被打断
     * 已经被打断的输出器无法再次被{@link #waitingForBroken()}挂起
     */
    boolean isBroken();

    /**
     * 关闭输出
     */
    void close();

}
