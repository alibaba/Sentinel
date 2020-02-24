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
package com.alibaba.csp.sentinel.event;

import com.alibaba.csp.sentinel.event.degrade.DegradeEventHandlerProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lianglin
 * @since 1.7.1
 */
public class EventHandleManager {

    private final static EventHandleManager INSTANCE = new EventHandleManager();

    private List<EventHandler> eventHandlers;


    private EventHandleManager() {
        eventHandlers = new ArrayList<>();
        eventHandlers.add(DegradeEventHandlerProvider.getHandler());
    }

    public static EventHandleManager getInstance() {
        return INSTANCE;
    }

    public void accept(Event event) {
        if (event == null) {
            return;
        }
        for (EventHandler handler : eventHandlers) {
            if (handler != null) {
                handler.handle(event);
            }
        }
    }


}
