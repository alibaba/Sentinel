var app = angular.module('sentinelDashboardApp');

app.service('GatewayFlowService', ['$http', function ($http) {
  this.queryRules = function (app, ip, port) {
    var param = {
      app: app,
      ip: ip,
      port: port
    };

    return $http({
      url: '/gateway/flow/list.json',
      params: param,
      method: 'GET'
    });
  };

  this.newRule = function (rule) {
    return $http({
      url: '/gateway/flow/new.json',
      data: rule,
      method: 'POST'
    });
  };

  this.saveRule = function (rule) {
    return $http({
      url: '/gateway/flow/save.json',
      data: rule,
      method: 'POST'
    });
  };

  this.deleteRule = function (rule) {
    var param = {
      id: rule.id,
      app: rule.app
    };

    return $http({
      url: '/gateway/flow/delete.json',
      params: param,
      method: 'POST'
    });
  };

  this.checkRuleValid = function (rule) {
    if (rule.resource === undefined || rule.resource === '') {
      alert('API名称不能为空');
      return false;
    }

    if (rule.paramItem != null) {
      if (rule.paramItem.parseStrategy == 2 ||
          rule.paramItem.parseStrategy == 3 ||
          rule.paramItem.parseStrategy == 4) {
        if (rule.paramItem.fieldName === undefined || rule.paramItem.fieldName === '') {
          alert('当参数属性为Header、URL参数、Cookie时，参数名称不能为空');
          return false;
        }

        if (rule.paramItem.pattern === '') {
          alert('匹配串不能为空');
          return false;
        }
      }
    }

    if (rule.count === undefined || rule.count < 0) {
      alert((rule.grade === 1 ? 'QPS阈值' : '线程数') + '必须大于等于 0');
      return false;
    }

    return true;
  };
}]);
