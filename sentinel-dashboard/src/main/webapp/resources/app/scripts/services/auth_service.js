var app = angular.module('sentinelDashboardApp');

app.service('AuthService', ['$http', function ($http) {
  this.login = function (param) {
    return $http({
      url: '/auth/login',
      params: param,
      method: 'POST'
    });
  };

  this.logout = function () {
    return $http({
      url: '/auth/logout',
      method: 'POST'
    });
  };
}]);
