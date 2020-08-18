var app = angular.module('sentinelDashboardApp');

app.service('DegradeService', ['$http', function ($http) {
  this.queryMachineRules = function (app, ip, port) {
    var param = {
      app: app,
      ip: ip,
      port: port
    };
    return $http({
      url: 'degrade/rules.json',
      params: param,
      method: 'GET'
    });
  };

  this.newRule = function (rule) {
    return $http({
        url: '/degrade/rule',
        data: rule,
        method: 'POST'
    });
  };

  this.saveRule = function (rule) {
    var param = {
      id: rule.id,
      resource: rule.resource,
      limitApp: rule.limitApp,
      grade: rule.grade,
      count: rule.count,
      timeWindow: rule.timeWindow,
        statIntervalMs: rule.statIntervalMs,
        minRequestAmount: rule.minRequestAmount,
        slowRatioThreshold: rule.slowRatioThreshold,
    };
    return $http({
        url: '/degrade/rule/' + rule.id,
        data: param,
        method: 'PUT'
    });
  };

  this.deleteRule = function (rule) {
      return $http({
          url: '/degrade/rule/' + rule.id,
          method: 'DELETE'
      });
  };

  this.checkRuleValid = function (rule) {
      if (rule.resource === undefined || rule.resource === '') {
          alert('资源名称不能为空');
          return false;
      }
      if (rule.grade === undefined || rule.grade < 0) {
          alert('未知的降级策略');
          return false;
      }
      if (rule.count === undefined || rule.count === '' || rule.count < 0) {
          alert('降级阈值不能为空或小于 0');
          return false;
      }
      if (rule.timeWindow == undefined || rule.timeWindow === '' || rule.timeWindow <= 0) {
          alert('熔断时长必须大于 0s');
          return false;
      }
      if (rule.minRequestAmount == undefined || rule.minRequestAmount <= 0) {
          alert('最小请求数目需大于 0');
          return false;
      }
      if (rule.statIntervalMs == undefined || rule.statIntervalMs <= 0) {
          alert('统计窗口时长需大于 0s');
          return false;
      }
      if (rule.statIntervalMs !== undefined && rule.statIntervalMs > 60 * 1000 * 2) {
          alert('统计窗口时长不能超过 120 分钟');
          return false;
      }
      // 异常比率类型.
      if (rule.grade == 1 && rule.count > 1) {
          alert('异常比率超出范围：[0.0 - 1.0]');
          return false;
      }
      if (rule.grade == 0) {
          if (rule.slowRatioThreshold == undefined) {
              alert('慢调用比率不能为空');
              return false;
          }
          if (rule.slowRatioThreshold < 0 || rule.slowRatioThreshold > 1) {
              alert('慢调用比率超出范围：[0.0 - 1.0]');
              return false;
          }
      }
      return true;
  };
}]);
