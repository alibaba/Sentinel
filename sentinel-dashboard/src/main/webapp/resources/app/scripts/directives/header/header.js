/**
 * @ngdoc directive
 * @name izzyposWebApp.directive:adminPosHeader
 * @description
 * # adminPosHeader
 */
angular.module('sentinelDashboardApp')
  .directive('header', ['AuthService', function () {
    return {
      templateUrl: 'app/scripts/directives/header/header.html',
      restrict: 'E',
      replace: true,
      controller: function ($scope, $state, $window, AuthService) {
          if (!$window.sessionStorage.getItem('sentinel_admin')) {
              $state.go('login');
          }

          $scope.logout = function () {
              AuthService.logout().success(function (data) {
                  if (data.code == 0) {
                      $window.sessionStorage.clear();

                      $state.go('login');
                  } else {
                      alert('logout error');
                  }
              });
          }
      }
    }
}]);
