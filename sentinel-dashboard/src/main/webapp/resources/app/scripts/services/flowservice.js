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

    function notNumberAtLeastZero(num) {
        return num === undefined || num === '' || isNaN(num) || num < 0;
    }

    function notNumberGreaterThanZero(num) {
        return num === undefined || num === '' || isNaN(num) || num <= 0;
    }

  this.checkRuleValid = function (rule) {
        if (rule.resource === undefined || rule.resource === '') {
            alert('Resource name cannot be empty');
            return false;
        }
        if (rule.count === undefined || rule.count < 0) {
            alert('Threshold should be at least 0');
            return false;
        }
        if (rule.strategy === undefined || rule.strategy < 0) {
            alert('Invalid flow strategy');
            return false;
        }
        if (rule.strategy == 1 || rule.strategy == 2) {
            if (rule.refResource === undefined || rule.refResource == '') {
                alert('Please input reference resource or entrance name');
                return false;
            }
        }
        if (rule.controlBehavior === undefined || rule.controlBehavior < 0) {
            alert('Invalid flow control behavior');
            return false;
        }
        if (rule.controlBehavior == 1 && notNumberGreaterThanZero(rule.warmUpPeriodSec)) {
            alert('Warm-up duration should be positive');
            return false;
        }
        if (rule.controlBehavior == 2 && notNumberGreaterThanZero(rule.maxQueueingTimeMs)) {
            alert('Queueing timeout should be positive');
            return false;
        }
        return true;
    };
}]);
