/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.traffic.rule.router.match;

import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.traffic.TrafficContext;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author panxiaojun233
 */
public class MethodMatch {

    private StringMatch name;
    private StringMatch path;
    private Integer argumentCount;
    private List<ArgumentMatch> arguments;
    private List<StringMatch> argumentParameters;
    private StringMatch group;
    private StringMatch version;
    private Map<String, StringMatch> headers;

    public StringMatch getName() {
        return name;
    }

    public void setName(StringMatch name) {
        this.name = name;
    }

    public StringMatch getPath() {
        return path;
    }

    public void setPath(StringMatch path) {
        this.path = path;
    }

    public Integer getArgumentCount() {
        return argumentCount;
    }

    public void setArgumentCount(Integer argumentCount) {
        this.argumentCount = argumentCount;
    }

    public List<ArgumentMatch> getArguments() {
        return arguments;
    }

    public void setArguments(List<ArgumentMatch> arguments) {
        this.arguments = arguments;
    }

    public List<StringMatch> getArgumentParameters() {
        return argumentParameters;
    }

    public void setArgumentParameters(List<StringMatch> argumentParameters) {
        this.argumentParameters = argumentParameters;
    }

    public StringMatch getGroup() {
        return group;
    }

    public void setGroup(StringMatch group) {
        this.group = group;
    }

    public StringMatch getVersion() {
        return version;
    }

    public void setVersion(StringMatch version) {
        this.version = version;
    }

    public Map<String, StringMatch> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, StringMatch> headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        return "MethodMatch{" +
                "name=" + name +
                ", path=" + path +
                ", argumentCount=" + argumentCount +
                ", arguments=" + arguments +
                ", argumentParameters=" + argumentParameters +
                ", group=" + group +
                ", version=" + version +
                ", headers=" + headers +
                '}';
    }

    public boolean isMatch(TrafficContext context) {
        if (getName() != null && !getName().isMatch(context.getMethodName())) {
            return false;
        }

        if (getPath() != null) {
            if (!getPath().isMatch(context.getPath())) {
                return false;
            }
        }

        if (getGroup() != null) {
            if (!getGroup().isMatch(context.getGroup())) {
                return false;
            }
        }

        if (getVersion() != null) {
            if (!getVersion().isMatch(context.getVersion())) {
                return false;
            }
        }

        Integer argc = getArgumentCount();
        List<Object> arguments = context.getArgs();
        if (argc != null &&
                ((argc != 0 && (arguments == null || arguments.size() == 0)) || (argc != arguments.size()))) {
            return false;
        }

        List<StringMatch> argp = getArgumentParameters();
        // todo string or class
        List<String> parameterTypes = context.getParamTypes();
        if (argp != null && argp.size() > 0) {
            if (parameterTypes == null || parameterTypes.size() == 0) {
                return false;
            }
            if (argp.size() != parameterTypes.size()) {
                return false;
            }

            for (int index = 0; index < argp.size(); index++) {
                boolean match = argp.get(index).isMatch(parameterTypes.get(index));
                if (!match) {
                    return false;
                }
            }
        }

        List<ArgumentMatch> args = getArguments();
        if (args != null && args.size() > 0) {
            if (arguments == null || arguments.size() == 0) {
                return false;
            }

            for (ArgumentMatch argumentMatch : args) {
                int index = argumentMatch.getIndex();
                if (index >= arguments.size()) {
                    throw new IndexOutOfBoundsException("Sentinel Method Arg index >= parameters.length");
                }
                if (!argumentMatch.isMatch(arguments.get(index))) {
                    return false;
                }
            }
        }

        // TODO Match headers
        if (getHeaders() != null) {
            for (String key : getHeaders().keySet()) {
                String value = context.getHeaders().get(key);
                if (StringUtil.isNotEmpty(value)) {
                    return getHeaders().get(key).isMatch(value);
                }
            }
        }

        return false;
    }
}

