var app = angular.module('sentinelDashboardApp');

app.service('AuthService', ['$http', function ($http) {
  this.check = function () {
    return $http({
      url: '/auth/check',
      method: 'POST'
    });
  };

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
