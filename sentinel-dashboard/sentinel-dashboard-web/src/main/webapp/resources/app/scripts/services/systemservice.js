var app = angular.module('sentinelDashboardApp');

app.service('SystemService', ['$http', function ($http) {
  this.queryMachineRules = function (app, ip, port) {
    var data = {
      app: app,
      ip: ip,
      port: port
    };
    return $http({
      url: 'system/rules',
      params: data,
      method: 'GET'
    });
  };

  this.newRule = function (rule) {
    var data = {
      app: rule.app,
      ip: rule.ip,
      port: rule.port
    };
    if (rule.grade == 0) {// avgLoad
      data.highestSystemLoad = rule.highestSystemLoad;
    } else if (rule.grade == 1) {// avgRt
      data.avgRt = rule.avgRt;
    } else if (rule.grade == 2) {// maxThread
      data.maxThread = rule.maxThread;
    } else if (rule.grade == 3) {// qps
      data.qps = rule.qps;
    } else if (rule.grade == 4) {// cpu
      data.highestCpuUsage = rule.highestCpuUsage;
    }

    return $http({
      url: '/system/rule',
      data: data,
      method: 'POST'
    });
  };

  this.saveRule = function (rule) {
    var data = {
      id: rule.id,
      app: rule.app,
      ip: rule.ip,
      port: rule.port
    };
    if (rule.grade == 0) {// avgLoad
      data.highestSystemLoad = rule.highestSystemLoad;
    } else if (rule.grade == 1) {// avgRt
      data.avgRt = rule.avgRt;
    } else if (rule.grade == 2) {// maxThread
      data.maxThread = rule.maxThread;
    } else if (rule.grade == 3) {// qps
      data.qps = rule.qps;
    } else if (rule.grade == 4) {// cpu
      data.highestCpuUsage = rule.highestCpuUsage;
    }

    return $http({
      url: '/system/rule/' + rule.id,
      data: data,
      method: 'PUT'
    });
  };

  this.deleteRule = function (rule) {
    var data = {
      id: rule.id,
      app: rule.app
    };

    return $http({
      url: '/system/rule/' + rule.id,
      data: data,
      method: 'DELETE',
      headers: {'Content-Type': 'application/json'}
    });
  };
}]);
