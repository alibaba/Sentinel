package com.alibaba.csp.sentinel.dashboard.datasource.management;

import com.alibaba.fastjson.JSONObject;

/**
 * DataSource client management
 *
 * @author longqiang
 */
public interface DataSourceManagement<T> {

    /**
     * get or create data source open api client
     *
     * @param dataSourceMachineInfo machine info
     * @return T client
     */
    T getOrCreateClient(DataSourceMachineInfo dataSourceMachineInfo);

    /**
     * transfer json to DataSourceMachineInfo
     *
     * @param jsonObject machine info
     * @return T client
     */
    DataSourceMachineInfo transfer(JSONObject jsonObject);

}
