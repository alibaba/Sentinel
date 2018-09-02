# Sentinel JdbcDataSource 

Sentinel JdbcDataSource provides integration with jdbc from database, eg: MySQL.

The data source uses pull model.

To use Sentinel JdbcDataSource, you should add the following dependency:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-jdbc</artifactId>
    <version>x.y.z</version>
</dependency>
```

Then you can create an `JdbcDataSource` and register to rule managers.
For instance:

```java
// `jdbcTemplate` is a Spring JdbcTemplate which your application should supply, in order to execute sql query from your database
// `appName` is your app name
// `ruleRefreshSec` is the interval which pull data from database per seconds, if null 30 seconds by default
DataSource<List<Map<String, Object>>, List<FlowRule>> dataSource = new JdbcDataSource(jdbcTemplate, appName, new JdbcDataSource.JdbcFlowRuleParser(), ruleRefreshSec);
FlowRuleManager.register2Property(dataSource.getProperty());

DataSource<List<Map<String, Object>>, List<DegradeRule>> dataSource = new JdbcDataSource(jdbcTemplate, appName, new JdbcDataSource.JdbcDegradeRuleParser(), ruleRefreshSec);
FlowRuleManager.register2Property(dataSource.getProperty());

DataSource<List<Map<String, Object>>, List<SystemRule>> dataSource = new JdbcDataSource(jdbcTemplate, appName, new JdbcDataSource.JdbcSystemRuleParser(), ruleRefreshSec);
FlowRuleManager.register2Property(dataSource.getProperty());
```

Database ddl:
```sql
-- create table
-- 应用表
CREATE TABLE `sentinel_app` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'id，主键',
  `name` VARCHAR(100) NOT NULL COMMENT '应用名称',
  `chn_name` VARCHAR(100) COMMENT '应用中文名称',
  `description` VARCHAR(500) COMMENT '描述',
  `create_user_id` DATETIME DEFAULT NULL COMMENT '创建人id',
  `update_user_id` DATETIME DEFAULT NULL COMMENT '修改人id',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '修改时间',
  `enabled` TINYINT NOT NULL COMMENT '是否启用 0-禁用 1-启用',
  `deleted` TINYINT COMMENT '是否删除 0-正常 1-删除',
  INDEX name_idx(`name`) USING BTREE,
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

-- 流控规则表
CREATE TABLE `sentinel_flow_rule` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'id，主键',
  `app_id` INT NOT NULL COMMENT '应用id',
  `resource` VARCHAR(200) NOT NULL COMMENT '规则的资源描述',
  `description` VARCHAR(500) COMMENT '描述',
  `limit_app` VARCHAR(100) NOT NULL COMMENT '被限制的应用,授权时候为逗号分隔的应用集合，限流时为单个应用',
  `grade` TINYINT NOT NULL COMMENT '0-THREAD 1-QPS',
  `_count` DOUBLE NOT NULL COMMENT '数量',
  `strategy` INT NOT NULL COMMENT '0-直接 1-关联 2-链路',
  `ref_resource` VARCHAR(200) COMMENT '关联的资源',
  `control_behavior` TINYINT NOT NULL COMMENT '0-直接拒绝 1-冷启动 2-匀速器',
  `warm_up_period_sec` INT COMMENT '冷启动时间(秒)',
  `max_queueing_time_ms` INT COMMENT '匀速器最大排队时间(毫秒)',
  `create_user_id` DATETIME DEFAULT NULL COMMENT '创建人id',
  `update_user_id` DATETIME DEFAULT NULL COMMENT '修改人id',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '修改时间',
  `change_status` TINYINT COMMENT '保留字段,暂不使用 0-未改变 1-新增 2-修改 3-删除',
  `enabled` TINYINT NOT NULL COMMENT '是否启用 0-禁用 1-启用',
  `deleted` TINYINT NOT NULL COMMENT '是否删除 0-正常 1-删除',
  INDEX app_id_idx(`app_id`) USING BTREE,
  INDEX resource_idx(`resource`) USING BTREE,
  INDEX enabled_idx(`enabled`) USING BTREE,
  INDEX deleted_idx(`deleted`) USING BTREE,
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;


-- 熔断降级规则表
CREATE TABLE `sentinel_degrade_rule` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'id，主键',
  `app_id` INT NOT NULL COMMENT '应用id',
  `resource` VARCHAR(200) NOT NULL COMMENT '规则的资源描述',
  `description` VARCHAR(500) COMMENT '描述',
  `limit_app` VARCHAR(100) NOT NULL COMMENT '被限制的应用,授权时候为逗号分隔的应用集合，限流时为单个应用',
  `grade` TINYINT NOT NULL COMMENT '0-根据响应时间 1-根据异常比例',		
  `_count` DOUBLE NOT NULL COMMENT '数量',
  `time_window` INT COMMENT '降级后恢复时间',
  `create_user_id` DATETIME DEFAULT NULL COMMENT '创建人id',
  `update_user_id` DATETIME DEFAULT NULL COMMENT '修改人id',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '修改时间',
  `change_status` TINYINT COMMENT '保留字段,暂不使用 0-未改变 1-新增 2-修改 3-删除',
  `enabled` TINYINT NOT NULL COMMENT '是否启用 0-禁用 1-启用',
  `deleted` TINYINT NOT NULL COMMENT '是否删除 0-正常 1-删除',
  INDEX app_id_idx(`app_id`) USING BTREE,
  INDEX resource_idx(`resource`) USING BTREE,
  INDEX enabled_idx(`enabled`) USING BTREE,
  INDEX deleted_idx(`deleted`) USING BTREE,
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

-- 系统负载保护规则表
CREATE TABLE `sentinel_system_rule` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'id，主键',
  `app_id` INT NOT NULL COMMENT '应用id',
  `resource` VARCHAR(200) NOT NULL COMMENT '规则的资源描述',
  `description` VARCHAR(500) COMMENT '描述',
  `limit_app` VARCHAR(100) NOT NULL COMMENT '被限制的应用,授权时候为逗号分隔的应用集合，限流时为单个应用',
  `highest_system_load` DOUBLE NOT NULL COMMENT '最大系统负载',
  `qps` DOUBLE NOT NULL COMMENT 'QPS',
  `avg_rt` LONG NOT NULL COMMENT '平均响应时间',
  `max_thread` LONG NOT NULL COMMENT '最大线程数',
  `create_user_id` DATETIME DEFAULT NULL COMMENT '创建人id',
  `update_user_id` DATETIME DEFAULT NULL COMMENT '修改人id',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '修改时间',
  `change_status` TINYINT COMMENT '保留字段,暂不使用 0-未改变 1-新增 2-修改 3-删除',
  `enabled` TINYINT NOT NULL COMMENT '是否启用 0-禁用 1-启用',
  `deleted` TINYINT NOT NULL COMMENT '是否删除 0-正常 1-删除',
  INDEX app_id_idx(`app_id`) USING BTREE,
  INDEX resource_idx(`resource`) USING BTREE,
  INDEX enabled_idx(`enabled`) USING BTREE,
  INDEX deleted_idx(`deleted`) USING BTREE,
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;
```

Insert some data for test: 
```sql
-- add a app named demo_app
INSERT INTO sentinel_app(id,NAME,chn_name,create_time,enabled,deleted) VALUES(1,'demo_app','示例项目',NOW(),1,0);
-- add one flow rule of demo_app
INSERT INTO sentinel_flow_rule(app_id,resource,limit_app,grade,_count,strategy,control_behavior,create_time,enabled,deleted) 
VALUES(1,'com.demo.FooService:hello(java.lang.String)','default',1,5,0,0,NOW(),1,0);
```


