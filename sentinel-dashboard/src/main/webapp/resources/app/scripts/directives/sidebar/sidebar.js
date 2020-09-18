angular.module('sentinelDashboardApp')
  .directive('sidebar', ['$location', '$stateParams', 'AppService', function () {
    return {
      templateUrl: 'app/scripts/directives/sidebar/sidebar.html',
      restrict: 'E',
      replace: true,
      scope: false,
      controller: function ($scope, $rootScope,$stateParams, $location, AppService) {
        sidebarInit = async () => {
          await $rootScope.$on('sidebarToRoot_id', (e, msg) => {
            $rootScope.$broadcast('rootToRules_id', msg);
          });
        }
        
        sidebarInit();

        // toggle side bar
        $scope.serviceClick = function ($event) {
          console.log("serviceClick");
          let entry = angular.element($event.target).scope().entry;
          entry.active = !entry.active;// toggle this clicked app bar
          $scope.service_id = entry.id;
          $scope.services.forEach(function (item) { // collapse other app bars
            if (item !== entry) {
              item.active = false;
            }
          });
        };

        // FIX ME 更改了维度，app->service
        /**
         * @deprecated
         */
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
        
        $scope.rulesClick = function ($event) {
          console.log("rulesClick");
          $scope.$emit('sidebarToRoot_id', $scope.service_id);
        }
      }
    };
  }]);
