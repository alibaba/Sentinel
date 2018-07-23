var app = angular.module('sentinelDashboardApp');

app.service('MetricService', ['$http', function ($http) {

  this.queryAppSortedIdentities = function (params) {
    return $http({
      url: '/metric/queryTopResourceMetric.json',
      params: params,
      method: 'GET'
    });
  };

  this.queryByAppAndIdentity = function (params) {
    return $http({
      url: '/metric/queryByAppAndResource.json',
      params: params,
      method: 'GET'
    });
  };

  this.queryByMachineAndIdentity = function (ip, port, identity, startTime, endTime) {
    var param = {
      ip: ip,
      port: port,
      identity: identity,
      startTime: startTime.getTime(),
      endTime: endTime.getTime()
    };

    return $http({
      url: '/metric/queryByAppAndResource.json',
      params: param,
      method: 'GET'
    });
  };
}]);
