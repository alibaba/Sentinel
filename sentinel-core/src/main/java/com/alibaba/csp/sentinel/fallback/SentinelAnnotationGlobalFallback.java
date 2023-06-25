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
package com.alibaba.csp.sentinel.fallback;

import java.lang.reflect.Method;

/**
 * Annotation global fallback interface
 *
 * @author luffy
 */
public interface SentinelAnnotationGlobalFallback {

     /**
      *
      * @param originalMethod the original method called
      * @param args The parameters of the method that was originally called
      * @param t The exception thrown by the method originally called
      * @return The result of the global fallback handler execution
      * @throws Throwable
      */
     Object handle(Method originalMethod, Object[] args, Throwable t) throws Throwable;

}
