package com.alibaba.jvm.sandbox.api;

/**
 * 沙箱环境模块
 * <p>
 * 要求模块的实现必须符合JDK6的SPI规范
 * </p>
 * <ol>
 * <li>必须实现{@link Module}接口</li>
 * <li>必须拥有无参构造函数</li>
 * <li>必须在{@code META-INF/services/com.alibaba.jvm.sandbox.api.Module}文件中注册</li>
 * </ol>
 * <p>
 * 模块加载时，将会调用模块实现类的默认构造函数完成模块类的实例化。但类完成了实例化并不代表模块加载完成。
 * </p>
 *
 * @author luanjia@taobao.com
 */
public interface Module {

}