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
        if (!$window.localStorage.getItem('session_sentinel_admin')) {
          AuthService.check().success(function (data) {
            if (data.code == 0) {
              $window.localStorage.setItem('session_sentinel_admin', {
                username: data.data
              });
              if (data.data.id == 'FAKE_EMP_ID') {
                document.getElementById('li-logout').style.display = 'none';
              } else {
                document.getElementById('li-logout').style.display = 'block';
              }
            } else {
              $state.go('login');
            }
          });
        }

        $scope.logout = function () {
          AuthService.logout().success(function (data) {
            if (data.code == 0) {
              $window.localStorage.removeItem("session_sentinel_admin");
              $state.go('login');
            } else {
              alert('logout error');
            }
          });
        }
      }
    }
  }]);
