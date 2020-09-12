package com.alibaba.csp.sentinel.transport.config;

import com.alibaba.csp.sentinel.log.RecordLog;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;
import org.yaml.snakeyaml.Yaml;

import java.beans.beancontext.BeanContext;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

@Data
public class KieConfig {
    private static final String CONFIG_FILE_NAME = "application.yml";

    private static KieConfig instance = new KieConfig();

    private String app;

    private String service;

    private String version;

    private String project;

    private String environment;

    private Optional<String> getConfigPath(){
        URL url = getClass().getClassLoader().getResource(CONFIG_FILE_NAME);
        if (url == null){
            RecordLog.error(String.format("Can't find default config file: %s", CONFIG_FILE_NAME));
            return Optional.empty();
        }

        return Optional.ofNullable(url.getPath());
    }

    private void initConfig(String configPath){
        Yaml yaml = new Yaml();
        Map configMap;
        try {
            configMap = yaml.loadAs(new FileInputStream(configPath), Map.class);
        }catch (FileNotFoundException e){
            RecordLog.error(String.format("Load default config file： %s failed.", CONFIG_FILE_NAME));
            return;
        }

        Map sentinelMap = (Map)configMap.get("sentinel");
        Map sourceMap = (Map)sentinelMap.get("source");

        try {
            BeanUtils.copyProperties(instance, sourceMap);
        } catch (IllegalAccessException |InvocationTargetException e) {
            RecordLog.error(String.format("Parse config file: %s failed."), e);
        }
    }

    private KieConfig(){
        Optional<String> configPath = getConfigPath();

        configPath.ifPresent(this::initConfig);
    }

    public static KieConfig getInstance(){
        return instance;
    }
}