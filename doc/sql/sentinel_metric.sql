-- 创建监控数据表
CREATE TABLE `sentinel_metric` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'id，主键',
  `gmt_create` DATETIME COMMENT '创建时间',
  `gmt_modified` DATETIME COMMENT '修改时间',
  `app` VARCHAR(100) COMMENT '应用名称',
  `timestamp` DATETIME COMMENT '统计时间',
  `resource` VARCHAR(500) COMMENT '资源名称',
  `pass_qps` INT COMMENT '通过qps',
  `success_qps` INT COMMENT '成功qps',
  `block_qps` INT COMMENT '限流qps',
  `exception_qps` INT COMMENT '发送异常的次数',
  `rt` DOUBLE COMMENT '所有successQps的rt的和',
  `_count` INT COMMENT '本次聚合的总条数',
  `resource_code` INT COMMENT '资源的hashCode',
  INDEX app_idx(`app`) USING BTREE,
  INDEX resource_idx(`resource`) USING BTREE,
  INDEX timestamp_idx(`timestamp`) USING BTREE,
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;