package com.alibaba.jvm.sandbox.core.manager.impl;

import com.alibaba.jvm.sandbox.core.domain.CoreModule;
import com.alibaba.jvm.sandbox.core.manager.ModuleLifeCycleEventBus;
import com.alibaba.jvm.sandbox.core.manager.ModuleLifeCycleEventBus.ModuleLifeCycleEventListener;
import com.alibaba.jvm.sandbox.core.manager.ModuleResourceManager;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 默认实现自动释放连接释放管理
 * Created by luanjia@taobao.com on 2017/2/4.
 */
public class DefaultModuleResourceManager implements ModuleResourceManager, ModuleLifeCycleEventListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, List<WeakResource<?>>> moduleResourceListMapping
            = new HashMap<String, List<WeakResource<?>>>();

    @Override
    public synchronized <T> T append(String uniqueId, WeakResource<T> resource) {
        if (null == resource
                || null == resource.get()) {
            return null;
        }
        final List<WeakResource<?>> moduleResourceList;
        if (moduleResourceListMapping.containsKey(uniqueId)) {
            moduleResourceList = moduleResourceListMapping.get(uniqueId);
        } else {
            moduleResourceListMapping.put(uniqueId, moduleResourceList
                    = new ArrayList<WeakResource<?>>());
        }
        moduleResourceList.add(resource);
        logger.debug("append resource={} in module[id={};]", resource.get(), uniqueId);
        return resource.get();
    }

    @Override
    public <T> void remove(String uniqueId, T target) {
        if (null == target) {
            return;
        }
        synchronized (this) {
            final List<WeakResource<?>> moduleResourceList = moduleResourceListMapping.get(uniqueId);
            if (null == moduleResourceList) {
                return;
            }
            final Iterator<WeakResource<?>> resourceRefIt = moduleResourceList.iterator();
            while (resourceRefIt.hasNext()) {
                final WeakResource<?> resourceRef = resourceRefIt.next();

                // 删除掉无效的资源
                if (null == resourceRef) {
                    resourceRefIt.remove();
                    logger.debug("remove illegal resource in module[id={};]", uniqueId);
                    continue;
                }

                // 删除掉已经被GC掉的资源
                if (null == resourceRef.get()) {
                    resourceRefIt.remove();
                    logger.debug("remove empty resource in module[id={};]", uniqueId);
                    continue;
                }

                if (target.equals(resourceRef.get())) {
                    resourceRefIt.remove();
                    logger.debug("remove resource={} in module[id={};]", resourceRef.get(), uniqueId);
                }
            }//while
        }//sync
    }

    @Override
    public boolean onFire(CoreModule coreModule, ModuleLifeCycleEventBus.Event event) {

        // 只有模块卸载的时候才需要关注处理
        if (event != ModuleLifeCycleEventBus.Event.UNLOAD) {
            return true;
        }

        final String uniqueId = coreModule.getUniqueId();
        final List<WeakResource<?>> moduleResourceList;
        synchronized (this) {
            moduleResourceList = moduleResourceListMapping.remove(uniqueId);
        }
        if (CollectionUtils.isEmpty(moduleResourceList)) {
            logger.debug("module[id={};] mapping resources was empty.", uniqueId);
            return true;
        } else {
            logger.info("module[id={};] is unloading, will release {} resources.", uniqueId, moduleResourceList.size());
        }

        for (final WeakResource<?> resource : moduleResourceList) {
            if (null == resource
                    || null == resource.get()) {
                continue;
            }
            try {
                resource.release();
                logger.info("module[id={};] is unloading, resource={} was closed.", uniqueId, resource);
            } catch (Throwable cause) {
                logger.warn("module[id={};] is unloading, resource={} closing failed.",
                        uniqueId, resource, cause);
            }
        }
        return true;

    }

}