package com.alibaba.csp.sentinel.transport.config;

import com.alibaba.csp.sentinel.log.RecordLog;
import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

@Data
public class KieConfig {
    private static String CONFIG_FILE_NAME = "application.yml";

    private static KieConfig instance = new KieConfig();

    private String app;

    private String service;

    private String version;

    private String project;

    private String environment;

    private String kieAddress;

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

        this.app = (String) sourceMap.get("app");
        this.service = (String) sourceMap.get("service");
        this.project = (String) sourceMap.get("project");
        this.environment = (String) sourceMap.get("environment");
        this.version = (String) sourceMap.get("version");
        this.kieAddress = (String) sourceMap.get("address");
    }

    private KieConfig(){
        Optional<String> configPath = getConfigPath();

        configPath.ifPresent(this::initConfig);
    }

    public static KieConfig getInstance(){
        return instance;
    }
}