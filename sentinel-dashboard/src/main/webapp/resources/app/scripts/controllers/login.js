var app = angular.module('sentinelDashboardApp');

app.controller('LoginCtl', ['$scope', '$state', '$window', 'AuthService',
  function ($scope, $state, $window, LoginService) {
    // If auth, jump to the index page directly
    if ($window.localStorage.getItem('session_sentinel_admin')) {
      $state.go('dashboard');
    }

    $scope.login = function () {
      if (!$scope.username) {
        alert('请输入用户名');
        return;
      }

      if (!$scope.password) {
        alert('请输入密码');
        return;
      }

      var param = {"username": $scope.username, "password": $scope.password};

      LoginService.login(param).success(function (data) {
        if (data.code == 0) {
          $window.localStorage.setItem('session_sentinel_admin', {
            username: data.data
          });

          $state.go('dashboard');
        } else {
          alert(data.msg);
        }
      });
    };
  }]
);