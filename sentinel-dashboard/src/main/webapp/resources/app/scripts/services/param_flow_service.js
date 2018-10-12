/**
 * Parameter flow control service.
 * 
 * @author Eric Zhao
 */
angular.module('sentinelDashboardApp').service('ParamFlowService', ['$http', function ($http) {
  this.queryMachineRules = function(app, ip, port) {
    var param = {
      app: app,
      ip: ip,
      port: port
    };
    return $http({
      url: '/paramFlow/rules',
      params: param,
      method: 'GET'
    });
  };

  this.addNewRule = function(rule) {
    return $http({
      url: '/paramFlow/rule',
      data: rule,
      method: 'POST'
    });
  };

  this.saveRule = function (entity) {
    return $http({
      url: '/paramFlow/rule/' + entity.id,
      data: entity,
      method: 'PUT'
    });
  };

  this.deleteRule = function (entity) {
    return $http({
      url: '/paramFlow/rule/' + entity.id,
      method: 'DELETE'
    });
  };
}]);
