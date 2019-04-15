package com.alibaba.csp.sentinel.demo.file.rule;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.FileInJarReadableDataSource;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.List;

/**
 * <p>
 * This Demo shows how to use {@link FileInJarReadableDataSource} to read {@link Rule}s from jarfile. The
 * {@link FileInJarReadableDataSource} will automatically fetches the backend file every 3 seconds, and
 * inform the listener if the file is updated.
 * </p>
 * <p>
 * Each {@link ReadableDataSource} has a {@link SentinelProperty} to hold the deserialized config data.
 * {@link PropertyListener} will listen to the {@link SentinelProperty} instead of the datasource.
 * {@link Converter} is used for telling how to deserialize the data.
 * </p>
 * <p>
 * {@link FlowRuleManager#register2Property(SentinelProperty)},
 * {@link DegradeRuleManager#register2Property(SentinelProperty)},
 * {@link SystemRuleManager#register2Property(SentinelProperty)} could be called for listening the
 * {@link Rule}s change.
 * </p>
 * <p>
 * For other kinds of data source, such as <a href="https://github.com/alibaba/nacos">Nacos</a>,
 * Zookeeper, Git, or even CSV file, We could implement {@link ReadableDataSource} interface to read these
 * configs.
 * </p>
 *
 * @author dingq
 */
public class JarFileDataSourceDemo {

    public static void main(String[] args) throws Exception {
        JarFileDataSourceDemo demo = new JarFileDataSourceDemo();
        demo.listenRules();

        // Start to require tokens, rate will be limited by rule of FlowRule.json in jar.
        FlowQpsRunner runner = new FlowQpsRunner();
        runner.simulateTraffic();
        runner.tick();
    }

    private void listenRules() throws Exception {
        // Modify the path with your real path.
        String jarPath = System.getProperty("user.dir") + "/sentinel-demo/sentinel-demo-dynamic-file-rule/target/"
            + "sentinel-demo-dynamic-file-rule.jar";
        // eg: if flowRuleInJarName full path is 'sentinel-demo-dynamic-file-rule.jar!/classes/FlowRule.json',
        // your flowRuleInJarName is 'classes/FlowRule.json'
        String flowRuleInJarPath = "FlowRule.json";

        FileInJarReadableDataSource<List<FlowRule>> flowRuleDataSource = new FileInJarReadableDataSource<>(
                jarPath,flowRuleInJarPath, flowRuleListParser);
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
    }

    private Converter<String, List<FlowRule>> flowRuleListParser = source -> JSON.parseObject(source,
            new TypeReference<List<FlowRule>>() {});
}
