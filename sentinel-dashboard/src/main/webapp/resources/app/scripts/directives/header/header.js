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
        $scope.projects = [];
        $scope.currentProject = "";
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
          console.log("header init......");
          await $rootScope.$on("headerToRoot_kieInfos", (e,msg) => {
            console.log("headerToRoot_kieInfos");
            console.log("msg", msg);
            $rootScope.$broadcast("rootToSidebar_kieInfos", msg);
          });
          await KieService.getProjects().success(data => {
            if (data.success) {
              console.log("headerInit Projects:");
              console.log(data.data);

              $scope.projects = data.data;
              $scope.currentProject = $scope.projects[0];
            }
          });
          await KieService.getKieInfos($scope.currentProject).success(data => {
            if (data.success) {
              console.log("headerInit KieInfo:");
              console.log(data.data);
              $scope.$emit('headerToRoot_kieInfos', data.data);
            }
          });
          console.log('header init complete!');
        }
        
        headerInit();

        $scope.projectChange = () => {
          // project复选框change事件
          console.log("projectChange");
          console.log("$scope.currentProject", $scope.currentProject);
        }
      }
    }
  }]);
