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

  this.saveRule = function (rule) {
    if (rule.id === undefined || rule.id <= 0) {

    }
    // TODO: implement this
    return $http({
      url: '/paramFlow/rule/' + rule.id,
      data: {},
      method: 'GET'
    });
  };

  this.deleteRule = function (rule) {
    return $http({
      url: '/paramFlow/rule/' + rule.id,
      method: 'DELETE'
    });
  };
}]);
