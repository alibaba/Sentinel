var app = angular.module('sentinelDashboardApp');

app.service('SystemServiceV2', ['$http', function ($http) {
  this.queryMachineRules = function (app, ip, port) {
    var param = {
      app: app
    };
    return $http({
      url: '/v2/system/rules.json',
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
      param.highestSystemLoad = rule.highestSystemLoad;
    } else if (rule.grade == 1) {// avgRt
      param.avgRt = rule.avgRt;
    } else if (rule.grade == 2) {// maxThread
      param.maxThread = rule.maxThread;
    } else if (rule.grade == 3) {// qps
      param.qps = rule.qps;
    } else if (rule.grade == 4) {// cpu
      param.highestCpuUsage = rule.highestCpuUsage;
    }

    return $http({
      url: '/v2/system/new.json',
      params: param,
      method: 'GET'
    });
  };

  this.saveRule = function (rule) {
    var param = {
      id: rule.id,
    };
    if (rule.grade == 0) {// avgLoad
      param.highestSystemLoad = rule.highestSystemLoad;
    } else if (rule.grade == 1) {// avgRt
      param.avgRt = rule.avgRt;
    } else if (rule.grade == 2) {// maxThread
      param.maxThread = rule.maxThread;
    } else if (rule.grade == 3) {// qps
      param.qps = rule.qps;
    } else if (rule.grade == 4) {// cpu
        param.highestCpuUsage = rule.highestCpuUsage;
    }

    return $http({
      url: '/v2/system/save.json',
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
      url: '/v2/system/delete.json',
      params: param,
      method: 'GET'
    });
  };
}]);
