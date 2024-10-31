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
package com.alibaba.csp.sentinel.slots.block;

import com.alibaba.csp.sentinel.util.function.Function;
import com.alibaba.csp.sentinel.util.function.Predicate;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Unified rule management tool, mainly used for matching and caching of regular rules and simple rules.
 * @author quguai
 * @date 2023/10/9 20:35
 */
public class RuleManager<R> {

    private Map<String, List<R>> originalRules = new HashMap<>();
    private Map<Pattern, List<R>> regexRules = new HashMap<>();
    private Map<String, List<R>> regexCacheRules = new HashMap<>();
    private Map<String, List<R>> simpleRules = new HashMap<>();
    private Function<List<R>, List<R>> generator = Function.identity();

    private final Predicate<R> predicate;

    public RuleManager() {
        predicate = r -> r instanceof AbstractRule && ((AbstractRule) r).isRegex();
    }

    public RuleManager(Function<List<R>, List<R>> generator, Predicate<R> predicate) {
        this.generator = generator;
        this.predicate = predicate;
    }

    /**
     * Update rules from datasource, split rules map by regex,
     * rebuild the regex rule cache to reduce the performance loss caused by publish rules.
     *
     * @param rulesMap origin rules map
     */
    public void updateRules(Map<String, List<R>> rulesMap) {
        originalRules = rulesMap;
        Map<Pattern, List<R>> regexRules = new HashMap<>();
        Map<String, List<R>> simpleRules = new HashMap<>();
        for (Map.Entry<String, List<R>> entry : rulesMap.entrySet()) {
            String resource = entry.getKey();
            List<R> rules = entry.getValue();

            List<R> rulesOfSimple = new ArrayList<>();
            List<R> rulesOfRegex = new ArrayList<>();
            for (R rule : rules) {
                if (predicate.test(rule)) {
                    rulesOfRegex.add(rule);
                } else {
                    rulesOfSimple.add(rule);
                }
            }
            if (!rulesOfRegex.isEmpty()) {
                regexRules.put(Pattern.compile(resource), rulesOfRegex);
            }
            if (!rulesOfSimple.isEmpty()) {
                simpleRules.put(resource, rulesOfSimple);
            }
        }
        // rebuild regex cache rules
        setRules(regexRules, simpleRules);
    }

    /**
     * Get rules by resource name, save the rule list after regular matching to improve performance
     * @param resource resource name
     * @return matching rule list
     */
    public List<R> getRules(String resource) {
        List<R> result = new ArrayList<>(simpleRules.getOrDefault(resource, Collections.emptyList()));
        if (regexRules.isEmpty()) {
            return result;
        }
        if (regexCacheRules.containsKey(resource)) {
            result.addAll(regexCacheRules.get(resource));
            return result;
        }
        synchronized (this) {
            if (regexCacheRules.containsKey(resource)) {
                result.addAll(regexCacheRules.get(resource));
                return result;
            }
            List<R> compilers = matcherFromRegexRules(resource);
            regexCacheRules.put(resource, compilers);
            result.addAll(compilers);
            return result;
        }
    }

    /**
     * Get rules from regex rules and simple rules
     * @return rule list
     */
    public List<R> getRules() {
        List<R> rules = new ArrayList<>();
        for (Map.Entry<Pattern, List<R>> entry : regexRules.entrySet()) {
            rules.addAll(entry.getValue());
        }
        for (Map.Entry<String, List<R>> entry : simpleRules.entrySet()) {
            rules.addAll(entry.getValue());
        }
        return rules;
    }

    /**
     * Get origin rules, includes regex and simple rules
     * @return original rules
     */
    public Map<String, List<R>> getOriginalRules() {
        return originalRules;
    }

    /**
     * Determine whether has rule based on the resource name
     * @param resource resource name
     * @return whether
     */

    public boolean hasConfig(String resource) {
        if (resource == null) {
            return false;
        }
        return !getRules(resource).isEmpty();
    }

    /**
     * Is valid regex rules
     * @param rule rule
     * @return weather valid regex rule
     */
    public static boolean checkRegexResourceField(AbstractRule rule) {
        if (!rule.isRegex()) {
            return true;
        }
        String resourceName = rule.getResource();
        try {
            Pattern.compile(resourceName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private List<R> matcherFromRegexRules(String resource) {
        List<R> compilers = new ArrayList<>();
        for (Map.Entry<Pattern, List<R>> entry : regexRules.entrySet()) {
            if (entry.getKey().matcher(resource).matches()) {
                compilers.addAll(generator.apply(entry.getValue()));
            }
        }
        return compilers;
    }

    private synchronized void setRules(Map<Pattern, List<R>> regexRules, Map<String, List<R>> simpleRules) {
        this.regexRules = regexRules;
        this.simpleRules = simpleRules;
        if (regexRules.isEmpty()) {
            this.regexCacheRules = Collections.emptyMap();
            return;
        }
        // rebuild from regex cache rules
        Map<String, List<R>> rebuildCacheRule = new HashMap<>(regexCacheRules.size());
        for (String resource : regexCacheRules.keySet()) {
            rebuildCacheRule.put(resource, matcherFromRegexRules(resource));
        }
        this.regexCacheRules = rebuildCacheRule;
    }
}
