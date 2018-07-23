var app = angular.module('sentinelDashboardApp');

app.service('MachineService', ['$http', function ($http) {
  this.getAppMachines = function (app) {
    return $http({
      url: 'app/' + app + '/machines.json',
      method: 'GET'
    });
  };
}]);
