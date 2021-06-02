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
          alert('Resource name cannot be empty');
          return false;
      }
      if (rule.grade === undefined || rule.grade < 0) {
          alert('Invalid strategy');
          return false;
      }
      if (rule.count === undefined || rule.count === '' || rule.count < 0) {
          alert('Degrade threshold should be at least 0');
          return false;
      }
      if (rule.timeWindow == undefined || rule.timeWindow === '' || rule.timeWindow <= 0) {
          alert('Degrade time must be greater than 0s');
          return false;
      }
      if (rule.minRequestAmount == undefined || rule.minRequestAmount <= 0) {
          alert('The minimum number of requests must be greater than 0');
          return false;
      }
      if (rule.statIntervalMs == undefined || rule.statIntervalMs <= 0) {
          alert('The duration of the statistics window must be greater than 0s');
          return false;
      }
      if (rule.statIntervalMs !== undefined && rule.statIntervalMs > 60 * 1000 * 2) {
          alert('The duration of the statistics window cannot exceed 120 minutes');
          return false;
      }
      // 异常比率类型.
      if (rule.grade == 1 && rule.count > 1) {
          alert('Exception ratio exceeds the range: [0.0 - 1.0]');
          return false;
      }
      if (rule.grade == 0) {
          if (rule.slowRatioThreshold == undefined) {
              alert('Slow call ratio cannot be empty');
              return false;
          }
          if (rule.slowRatioThreshold < 0 || rule.slowRatioThreshold > 1) {
              alert('Slow call ratio is out of range: [0.0 - 1.0]');
              return false;
          }
      }
      return true;
  };
}]);
