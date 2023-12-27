var app = angular.module('sentinelDashboardApp');

app.service('GatewayApiService', ['$http', function ($http) {
  this.queryApis = function (app, ip, port) {
    var param = {
      app: app,
      ip: ip,
      port: port
    };
    return $http({
      url: '/gateway/api/list.json',
      params: param,
      method: 'GET'
    });
  };

  this.newApi = function (api) {
    return $http({
      url: '/gateway/api/new.json',
      data: api,
      method: 'POST'
    });
  };

  this.saveApi = function (api) {
    return $http({
      url: '/gateway/api/save.json',
      data: api,
      method: 'POST'
    });
  };

  this.deleteApi = function (api) {
    var param = {
      id: api.id,
      app: api.app
    };
    return $http({
      url: '/gateway/api/delete.json',
      params: param,
      method: 'POST'
    });
  };

  this.checkApiValid = function (api, apiNames) {
    if (api.apiName === undefined || api.apiName === '') {
      alert('API名称不能为空');
      return false;
    }

    if (api.predicateItems == null || api.predicateItems.length === 0) {
      // Should never happen since no remove button will display when only one predicateItem.
      alert('至少有一个匹配规则');
      return false;
    }

    for (var i = 0; i < api.predicateItems.length; i++) {
      var predicateItem = api.predicateItems[i];
      var pattern = predicateItem.pattern;
      if (pattern === undefined || pattern === '') {
        alert('匹配串不能为空，请检查');
        return false;
      }
    }

    if (apiNames.indexOf(api.apiName) !== -1) {
      alert('API名称(' + api.apiName + ')已存在');
      return false;
    }

    return true;
  };
}]);
