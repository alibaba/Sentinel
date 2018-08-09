var app = angular.module('sentinelDashboardApp');

app.service('SystemService', ['$http', function ($http) {
  this.queryMachineRules = function (app, ip, port) {
    var param = {
      app: app,
      ip: ip,
      port: port
    };
    return $http({
      url: 'system/rules.json',
      params: param,
      method: 'GET'
    });
  };

  this.newRule = function (rule) {
    var param = {
      app: rule.app,
      ip: rule.ip,
      port: rule.port
    };
    if (rule.grade == 0) {// avgLoad
      param.avgLoad = rule.avgLoad;
    } else if (rule.grade == 1) {// avgRt
      param.avgRt = rule.avgRt;
    } else if (rule.grade == 2) {// maxThread
      param.maxThread = rule.maxThread;
    } else if (rule.grade == 3) {// qps
      param.qps = rule.qps;
    }

    return $http({
      url: '/system/new.json',
      params: param,
      method: 'GET'
    });
  };

  this.saveRule = function (rule) {
    var param = {
      id: rule.id,
    };
    if (rule.grade == 0) {// avgLoad
      param.avgLoad = rule.avgLoad;
    } else if (rule.grade == 1) {// avgRt
      param.avgRt = rule.avgRt;
    } else if (rule.grade == 2) {// maxThread
      param.maxThread = rule.maxThread;
    } else if (rule.grade == 3) {// qps
      param.qps = rule.qps;
    }
    return $http({
      url: '/system/save.json',
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
      url: '/system/delete.json',
      params: param,
      method: 'GET'
    });
  };
}]);
