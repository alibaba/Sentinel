var app = angular.module('sentinelDashboardApp');

app.service('IdentityService', ['$http', function ($http) {

  this.fetchIdentityOfMachine = function (app,ip, port,hostname,searchKey) {
    var param = {
      app: app,
      ip: ip,
      port: port,
      hostname: hostname,
      searchKey: searchKey
    };
    return $http({
      url: 'resource/machineResource.json',
      params: param,
      method: 'GET'
    });
  };
  this.fetchClusterNodeOfMachine = function (app,ip, port,hostname,searchKey) {
    var param = {
      app: app,
      ip: ip,
      port: port,
      hostname: hostname,
      type: 'cluster',
      searchKey: searchKey
    };
    return $http({
      url: 'resource/machineResource.json',
      params: param,
      method: 'GET'
    });
  };
}]);
