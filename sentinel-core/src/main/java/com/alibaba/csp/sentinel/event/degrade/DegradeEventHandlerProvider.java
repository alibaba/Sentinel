/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.event.degrade;

import java.util.ServiceLoader;

/**
 * @author lianglin
 * @since  1.7.1
 */
public class DegradeEventHandlerProvider {

    private static final ServiceLoader<AbstractDegradeEventHandler> LOADER = ServiceLoader.load(AbstractDegradeEventHandler.class);

    private volatile static AbstractDegradeEventHandler handler = null;

    public static AbstractDegradeEventHandler getHandler() {
        if (handler != null) {
            return handler;
        }
        if (LOADER != null) {
            for (AbstractDegradeEventHandler eventHandler : LOADER) {
                if (eventHandler.getClass() != DefaultDegradeEventHandler.class) {
                    handler = eventHandler;
                    return handler;
                }
            }
        }
        if (handler == null) {
            handler = new DefaultDegradeEventHandler();
        }
        return handler;
    }

    private DegradeEventHandlerProvider() {
    }

}
