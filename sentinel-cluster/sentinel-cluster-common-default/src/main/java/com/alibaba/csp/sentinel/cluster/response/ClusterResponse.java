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
package com.alibaba.csp.sentinel.cluster.response;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ClusterResponse<T> implements Response {

    private int id;
    private int type;
    private int status;

    private T data;

    public ClusterResponse() {}

    public ClusterResponse(int id, int type, int status, T data) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.data = data;
    }

    @Override
    public int getId() {
        return id;
    }

    public ClusterResponse<T> setId(int id) {
        this.id = id;
        return this;
    }

    @Override
    public int getType() {
        return type;
    }

    public ClusterResponse<T> setType(int type) {
        this.type = type;
        return this;
    }

    @Override
    public int getStatus() {
        return status;
    }

    public ClusterResponse<T> setStatus(int status) {
        this.status = status;
        return this;
    }

    public T getData() {
        return data;
    }

    public ClusterResponse<T> setData(T data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "ClusterResponse{" +
            "id=" + id +
            ", type=" + type +
            ", status=" + status +
            ", data=" + data +
            '}';
    }
}
