var app = angular.module('sentinelDashboardApp');

app.service('SystemServiceV2', ['$http', function ($http) {
  this.queryMachineRules = function (app, ip, port) {
    var param = {
      app: app,
      ip: ip,
      port: port
    };
    return $http({
      url: '/v2/system/rules',
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
      url: '/v2/system/rule',
      data: param,
      method: 'POST'
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
      url: '/v2/system/rule/' + rule.id,
      data: param,
      method: 'PUT'
    });
  };

  this.deleteRule = function (rule) {
    return $http({
      url: '/v2/flow/rule/' + rule.id,
      method: 'DELETE'
    });
  };
}]);
