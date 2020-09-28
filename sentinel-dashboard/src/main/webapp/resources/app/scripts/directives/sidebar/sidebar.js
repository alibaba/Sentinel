angular.module('sentinelDashboardApp')
  .directive('sidebar', ['$location', '$stateParams', 'AppService', function () {
    return {
      templateUrl: 'app/scripts/directives/sidebar/sidebar.html',
      restrict: 'E',
      replace: true,
      scope: false,
      controller: function ($scope, $rootScope,$stateParams, $location, AppService) {
        // toggle side bar
        $scope.serviceClick = function ($event) {
          console.log("serviceClick");
          let entry = angular.element($event.target).scope().entry;
          console.log("entry", entry);
          window.localStorage.setItem('currentIp', entry.ip);
          window.localStorage.setItem('currentPort', entry.port);
          entry.active = !entry.active;// toggle this clicked service bar
          $scope.services.forEach(function (item) { // collapse other service bars
            if (item !== entry) {
              item.active = false;
            }
          });
        };

        $scope.addSearchApp = function () {
          let findApp = false;
          for (let i = 0; i < $scope.apps.length; i++) {
            if ($scope.apps[i].app === $scope.searchApp) {
              findApp = true;
              break;
            }
          }
          if (!findApp) {
            $scope.apps.push({ app: $scope.searchApp });
          }
        };
        
        $scope.appClick = function ($event) {
          console.log("appClick");
          let appActive = angular.element($event.target).scope().appActive;
          appActive.active = !appActive.active;
          $scope.appActives.forEach(val => {
            if (val !== appActive) {
              val.active = false;
            }
          })
        }
        
      }
    };
  }]);
