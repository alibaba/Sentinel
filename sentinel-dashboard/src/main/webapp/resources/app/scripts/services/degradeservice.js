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
}]);
