/**
 * @ngdoc directive
 * @name izzyposWebApp.directive:adminPosHeader
 * @description
 * # adminPosHeader
 */
angular.module('sentinelDashboardApp')
  .directive('header', ['VersionService', 'AuthService', 'KieService', function () {
    return {
      templateUrl: 'app/scripts/directives/header/header.html',
      restrict: 'E',
      replace: true,
      controller: function ($scope, $rootScope, $state, $window, VersionService, AuthService, KieService) {
        VersionService.version().success(function (data) {
          if (data.code == 0) {
            $scope.dashboardVersion = data.data;
          }
        });

        if (!$window.localStorage.getItem("session_sentinel_admin")) {
          AuthService.check().success(function (data) {
            if (data.code == 0) {
              $window.localStorage.setItem('session_sentinel_admin', JSON.stringify(data.data));
              handleLogout($scope, data.data.id)
            } else {
              $state.go('login');
            }
          });
        } else {
          try {
            var id = JSON.parse($window.localStorage.getItem("session_sentinel_admin")).id;
            handleLogout($scope, id);
          } catch (e) {
            // Historical version compatibility processing, fixes issue-1449
            // If error happens while parsing, remove item in localStorage and redirect to login page.
            $window.localStorage.removeItem("session_sentinel_admin");
            $state.go('login');
          }
        }

        function handleLogout($scope, id) {
          if (id == 'FAKE_EMP_ID') {
            $scope.showLogout = false;
          } else {
            $scope.showLogout = true;
          }
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
        
        headerInit = async () => {
          $scope.projects = [];
          $scope.currentProject = "";
          // 当前project下所有app
          $scope.apps = [];
          // 当前project下所有service的信息
          $scope.services = [];
          await KieService.getProjects().success(data => {
            if (data.success) {
              $scope.projects = data.data;
              $scope.currentProject = $scope.projects[0];
            }
          });
          await KieService.getKieInfos($scope.currentProject).success(data => {
            if (data.success) {
              $scope.services = data.data;
              // 用来控制app的下拉框是否显示
              $scope.appActives = [];
              $scope.services.forEach(val => {
                if (!$scope.apps.includes(val.app)) {
                  $scope.apps.push(val.app);
                  let tmp = {
                    'app': val.app,
                    'active': false
                  };
                  $scope.appActives.push(tmp);
                }
              });
            }
          });
        }
        
        headerInit();

        $scope.projectChange = () => {
          // project复选框change事件
          KieService.getKieInfos($scope.currentProject).success(data => {
            if (data.success) {
              $scope.services = data.data;
              $scope.apps.splice(0, $scope.apps.length);
              $scope.appActives.splice(0, $scope.appActives.length);
              $scope.services.forEach(val => {
                if (!$scope.apps.includes(val.app)) {
                  $scope.apps.push(val.app);
                  let tmp = {
                    'app': val.app,
                    'active': false
                  }
                  $scope.appActives.push(tmp);
                }
              })
            }
          });
        }
      }
    }
  }]);
