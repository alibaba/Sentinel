var app = angular.module('sentinelDashboardApp');

app.service('VersionService', ['$http', function ($http) {
  this.version = function () {
    return $http({
      url: '/version',
      method: 'GET'
    });
  };
}]);
