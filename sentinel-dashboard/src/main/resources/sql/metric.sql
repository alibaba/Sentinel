SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for metric
-- ----------------------------
DROP TABLE IF EXISTS `metric`;
CREATE TABLE `metric` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app` varchar(255) NOT NULL COMMENT '应用名称',
  `blocked_qps` bigint(20) NOT NULL COMMENT '每秒拦截数',
  `count` int(11) NOT NULL COMMENT '实例数',
  `exception` bigint(20) NOT NULL COMMENT '获取失败实例数',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `passed_qps` bigint(20) NOT NULL COMMENT '每秒通过数',
  `resource` varchar(255) NOT NULL COMMENT '资源名称',
  `resource_code` int(11) NOT NULL COMMENT '资源标识',
  `rt` double NOT NULL COMMENT '响应时间',
  `success_qps` bigint(20) NOT NULL COMMENT '每秒成功数',
  `timestamp` datetime NOT NULL COMMENT '采集时间',
  PRIMARY KEY (`id`),
  KEY `IDX_ONE` (`timestamp`,`app`,`resource`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='指标数据采集表';

SET FOREIGN_KEY_CHECKS = 1;
