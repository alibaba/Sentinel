var app = angular.module('sentinelDashboardApp');

app.service('FlowService', ['$http', function ($http) {
  this.queryMachineRules = function (app, ip, port) {
    var param = {
      app: app,
      ip: ip,
      port: port
    };
    return $http({
      url: 'flow/rules.json',
      params: param,
      method: 'GET'
    });
  };

  this.newRule = function (rule) {
    var param = {
      resource: rule.resource,
      limitApp: rule.limitApp,
      grade: rule.grade,
      count: rule.count,
      strategy: rule.strategy,
      refResource: rule.refResource,
      controlBehavior: rule.controlBehavior,
      warmUpPeriodSec: rule.warmUpPeriodSec,
      maxQueueingTimeMs: rule.maxQueueingTimeMs,
      app: rule.app,
      ip: rule.ip,
      port: rule.port
    };

    return $http({
      url: '/flow/new.json',
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
      strategy: rule.strategy,
      refResource: rule.refResource,
      controlBehavior: rule.controlBehavior,
      warmUpPeriodSec: rule.warmUpPeriodSec,
      maxQueueingTimeMs: rule.maxQueueingTimeMs,
    };

    return $http({
      url: '/flow/save.json',
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
      url: '/flow/delete.json',
      params: param,
      method: 'GET'
    });
  };
}]);
