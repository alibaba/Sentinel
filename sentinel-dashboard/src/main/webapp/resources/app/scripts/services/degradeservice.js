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
    var param = {
      id: rule.id,
      resource: rule.resource,
      limitApp: rule.limitApp,
      count: rule.count,
      timeWindow: rule.timeWindow,
      grade: rule.grade,
      app: rule.app,
      ip: rule.ip,
      port: rule.port
    };
    return $http({
      url: '/degrade/new.json',
      params: param,
      method: 'GET'
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
    };
    return $http({
      url: '/degrade/save.json',
      params: param,
      method: 'GET'
    });
  };

  this.deleteRule = function (rule) {
    var param = {
      id: rule.id,
      app: rule.app
    };
    return $http({
      url: '/degrade/delete.json',
      params: param,
      method: 'GET'
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
      if (rule.timeWindow === undefined || rule.timeWindow === '' || rule.timeWindow <= 0) {
          alert('Degrade time window should be positive');
          return false;
      }
      // 异常比率类型.
      if (rule.grade == 1 && rule.count > 1) {
          alert('Exception ratio exceeds the range: [0.0 - 1.0]');
          return false;
      }
      return true;
  };
}]);
