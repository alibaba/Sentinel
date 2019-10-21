package com.alibaba.jvm.sandbox.core.util.matcher.structure;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 类结构
 *
 * @author luanjia@taobao.com
 */
public interface ClassStructure {

    /**
     * 获取Java类型名称
     *
     * @return Java类名
     */
    String getJavaClassName();

    /**
     * 获取类加载的ClassLoader
     *
     * @return 类加载的ClassLoader
     */
    ClassLoader getClassLoader();

    /**
     * 获取父类的类结构
     *
     * @return 父类的类结构
     */
    ClassStructure getSuperClassStructure();

    /**
     * 获取声明接口的类结构集合
     *
     * @return 声明接口的类结构集合
     */
    List<ClassStructure> getInterfaceClassStructures();

    /**
     * 获取家族父类结构集合
     * <p>
     * 1. Java的类继承是单继承，所以家族父类结构的范围就是从当前类一直获取父类，一直到java.lang.Object为止
     * 2. 返回的集合类是有序的Set集合，集合顺序为类的继承顺序
     * </p>
     *
     * @return 家族父类结构集合
     */
    LinkedHashSet<ClassStructure> getFamilySuperClassStructures();

    /**
     * 获取家族接口类结构集合
     * <p>
     * 1. 因为Java的接口是多继承的关系，所以整个家族接口类是包括了所有父类接口所实现的接口类型
     * 2. 返回的集合类是无序Set集合
     * 3. 如果一个接口在整个家族接口中多次实现了一个接口，那么这个接口将只会出现一次
     * </p>
     *
     * @return 家族接口类结构集合
     */
    Set<ClassStructure> getFamilyInterfaceClassStructures();

    /**
     * 获取类型结构集合
     * <p>
     * 一个类的家族类型非常庞大，他包括了所有和这个类拥有血缘关系的类结构。
     * 只要有一个类、接口和当前类沾亲带故都将会被纳入到本次统计中来。
     * </p>
     * <p>
     * 因为接口是有多继承的缘故，所以返回的类型集合中对相同的类型进行去重处理
     * </p>
     * <p>
     * 血缘关系定义如下
     * 1. 当前类的所有家族类
     * 2. 当前类的所有家族类所声明的家族接口
     * 3. 当前类的所有家族接口
     * </p>
     *
     * @return 类型结构集合
     */
    Set<ClassStructure> getFamilyTypeClassStructures();

    /**
     * 获取声明元注释的类结构集合
     *
     * @return 声明元注释的类结构集合
     */
    List<ClassStructure> getAnnotationTypeClassStructures();

    /**
     * 获取家族元注释类型类结构集合
     * <p>
     * 获取一个类所有家族类型，并从这些家族类型上继承下可以作用于当前类的元注释
     * </p>
     *
     * @return 家族元注释类型类结构集合
     */
    Set<ClassStructure> getFamilyAnnotationTypeClassStructures();

    /**
     * 获取行为(构造函数和方法)的结构集合
     *
     * @return 行为结构集合
     */
    List<BehaviorStructure> getBehaviorStructures();

    /**
     * 获取结构的访问修饰描述
     *
     * @return 访问修饰描述
     */
    Access getAccess();

}
