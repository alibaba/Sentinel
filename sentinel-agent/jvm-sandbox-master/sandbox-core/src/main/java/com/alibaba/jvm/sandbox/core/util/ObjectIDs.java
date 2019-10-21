package com.alibaba.jvm.sandbox.core.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 给指定的Java对象分配JVM唯一ID <br>
 * <p>
 * 1. 该ID为int型
 * 2. Object如果被GC回收，对应的ID会被失效，内存也会被释放
 * 3. int为[0,{@link Integer#MAX_VALUE}]之间的整数
 * 4. 这个类当前无需考虑对象溢出的情况，因为我不需要，我放入的对象数量很少，就这么简单
 * </p>
 *
 * @author luanjia@taobao.com
 */
public class ObjectIDs {

    /**
     * 空对象映射
     */
    public static final int NULL_ID = 0;

    /**
     * 对象ID序列生成器，生成范围[1,{@link Integer#MAX_VALUE}]之间的整数
     */
    private final Sequencer objectIDSequencer = new Sequencer();

    /**
     * 全局读写锁:用于维护世界的和平
     * <p>
     * 用于维护{@link #identityObjectMapping}和{@link #objectIDMapping}之间数据的一致性
     * </p>
     */
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    // 全局<对象:ID>映射表
    private final WeakHashMap<Object, Integer> objectIDMapping
            = new WeakHashMap<Object, Integer>();


    // --- ObjectID : Object 的映射关系维护 ----------------------------------------+
    private final ReferenceQueue<Object> rQueue = new ReferenceQueue<Object>(); //|
    private final HashMap<Integer, IdentityWeakReference> identityObjectMapping //|
            = new HashMap<Integer, IdentityWeakReference>();                    //|
    // ---------------------------------------------------------------------------+


    private ObjectIDs() {

    }

    /**
     * 映射Java对象为对象ID(JVM唯一)
     * <p>
     * 1. 如果{@code object}为null，则返回空对象映射{@link #NULL_ID}
     * 2. 如果{@code object}之前从未映射，则会为此Object分配一个ID
     * 3. 如果{@code object}之前已经映射，则会返回之前已经分配的ID
     * </p>
     *
     * @param object 待映射的Java对象
     * @return 对象ID
     */
    public int identity(final Object object) {

        if (null == object) {
            return NULL_ID;
        }

        rwLock.readLock().lock();
        try {
            final Integer objectID = objectIDMapping.get(object);
            if (null != objectID) {
                return objectID;
            }
        } finally {
            rwLock.readLock().unlock();
            expungeIdentityObjectMapping();
        }

        rwLock.writeLock().lock();
        try {
            final Integer nextObjectID;
            if (objectIDMapping.containsKey(object)) {
                nextObjectID = objectIDMapping.get(object);
            } else {
                mapping(
                        nextObjectID = objectIDSequencer.next(),
                        object
                );
            }
            return nextObjectID;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * 维持{@code [object:objectID]}和{@code [objectID:object]}两个集合的映射关系
     *
     * @param objectID 对象ID
     * @param object   对象
     */
    private void mapping(final Integer objectID,
                         final Object object) {
        rwLock.writeLock().lock();
        try {
            // 映射 [object : objectID]
            objectIDMapping.put(object, objectID);

            // 映射 [objectID : object]
            identityObjectMapping.put(objectID, new IdentityWeakReference(objectID, object));
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * 清理失效的 [objectID : object] 映射
     */
    private void expungeIdentityObjectMapping() {
        for (Object x; (x = rQueue.poll()) != null; ) {
            synchronized (rQueue) {
                rwLock.writeLock().lock();
                try {
                    identityObjectMapping.remove(((IdentityWeakReference) x).objectID);
                } finally {
                    rwLock.writeLock().unlock();
                }
            }
        }
    }

    /**
     * 映射{@code objectID}为Java对象
     *
     * @param objectID 对象ID
     * @param <T>      映射回的对象类型
     * @return Java对象
     */

    public <T> T getObject(final int objectID) {

        if (NULL_ID == objectID) {
            return null;
        }

        rwLock.readLock().lock();
        try {
            final Object object;
            final IdentityWeakReference reference = identityObjectMapping.get(objectID);
            if (null != reference
                    && null != (object = reference.get())) {
                return (T) object;
            } else {
                return null;
            }
        } finally {
            rwLock.readLock().unlock();
            expungeIdentityObjectMapping();
        }

    }

    // 带ObjectID标记的弱对象引用
    private class IdentityWeakReference extends WeakReference<Object> {

        // 对应的对象ID
        private final Integer objectID;

        private IdentityWeakReference(final Integer objectID,
                                      final Object referent) {
            super(referent, rQueue);
            this.objectID = objectID;
        }

    }


    /**
     * 全局单例
     */
    public static final ObjectIDs instance = new ObjectIDs();

}
