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
package com.alibaba.csp.sentinel.property;

/**
 * <p>
 * This class holds current value of the config, and is responsible for informing all {@link PropertyListener}s
 * added on this when the config is updated.
 * </p>
 * <p>
 * Note that not every {@link #updateValue(Object newValue)} invocation should inform the listeners, only when
 * {@code newValue} is not Equals to the old value, informing is needed.
 * </p>
 *
 * @param <T> the target type.
 * @author Carpenter Lee
 */
public interface SentinelProperty<T> {

    /**
     * <p>
     * Add a {@link PropertyListener} to this {@link SentinelProperty}. After the listener is added,
     * {@link #updateValue(Object)} will inform the listener if needed.
     * </p>
     * <p>
     * This method can invoke multi times to add more than one listeners.
     * </p>
     *
     * @param listener listener to add.
     */
    void addListener(PropertyListener<T> listener);

    /**
     * Remove the {@link PropertyListener} on this. After removing, {@link #updateValue(Object)}
     * will not inform the listener.
     *
     * @param listener the listener to remove.
     */
    void removeListener(PropertyListener<T> listener);

    /**
     * Update the {@code newValue} as the current value of this property and inform all {@link PropertyListener}s
     * added on this only when new {@code newValue} is not Equals to the old value.
     *
     * @param newValue the new value.
     * @return true if the value in property has been updated, otherwise false
     */
    boolean updateValue(T newValue);
}
