package com.alibaba.jvm.sandbox.core.util.matcher.structure;

/**
 * 访问权限控制
 * <p>
 * 把这个叫访问权限真的挺奇怪。其实更应该叫Feature(特征)什么的，不过之前就这么叫了，也就懒得修改了。
 * </p>
 * <p>
 * 在这里对所有类结构、行为的特征进行了统一的描述。
 * 其中一些特征是另外一些没有的，比如：行为结构中就不会存在Enum的类型。不过没关系，在行为上，{@link #isEnum()}返回的一定是false
 * </p>
 *
 * @author luanjia@taobao.com
 */
public interface Access {

    boolean isPublic();

    boolean isPrivate();

    boolean isProtected();

    boolean isStatic();

    boolean isFinal();

    boolean isInterface();

    boolean isNative();

    boolean isAbstract();

    boolean isEnum();

    boolean isAnnotation();

}
