package com.alibaba.csp.sentinel.transport.util;

import java.util.List;

import com.alibaba.csp.sentinel.datasource.WritableDataSource;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

/**
 * Writable data source registry for modifying rules via HTTP API.
 *
 * @author Eric Zhao
 */
public final class WritableDataSourceRegistry {

    private static WritableDataSource<List<FlowRule>> flowDataSource = null;
    private static WritableDataSource<List<AuthorityRule>> authorityDataSource = null;
    private static WritableDataSource<List<DegradeRule>> degradeDataSource = null;
    private static WritableDataSource<List<SystemRule>> systemSource = null;

    public static synchronized void registerFlowDataSource(WritableDataSource<List<FlowRule>> datasource) {
        flowDataSource = datasource;
    }

    public static synchronized void registerAuthorityDataSource(WritableDataSource<List<AuthorityRule>> dataSource) {
        authorityDataSource = dataSource;
    }

    public static synchronized void registerDegradeDataSource(WritableDataSource<List<DegradeRule>> dataSource) {
        degradeDataSource = dataSource;
    }

    public static synchronized void registerSystemDataSource(WritableDataSource<List<SystemRule>> dataSource) {
        systemSource = dataSource;
    }

    public static WritableDataSource<List<FlowRule>> getFlowDataSource() {
        return flowDataSource;
    }

    public static WritableDataSource<List<AuthorityRule>> getAuthorityDataSource() {
        return authorityDataSource;
    }

    public static WritableDataSource<List<DegradeRule>> getDegradeDataSource() {
        return degradeDataSource;
    }

    public static WritableDataSource<List<SystemRule>> getSystemSource() {
        return systemSource;
    }

    private WritableDataSourceRegistry() {}
}
