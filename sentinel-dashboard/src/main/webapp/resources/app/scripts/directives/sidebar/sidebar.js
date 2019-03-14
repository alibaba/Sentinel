/**
 * @ngdoc directive
 * @name izzyposWebApp.directive:adminPosHeader
 * @description # adminPosHeader
 */

angular.module('sentinelDashboardApp')
  .directive('sidebar', ['$location', '$stateParams', 'AppService', function () {
    return {
      templateUrl: 'app/scripts/directives/sidebar/sidebar.html',
      restrict: 'E',
      replace: true,
      scope: {
      },
      controller: function ($scope, $stateParams, $location, AppService) {
        $scope.app = $stateParams.app;
        $scope.collapseVar = 0;

        // app
        AppService.getApps().success(
          function (data) {
            if (data.code === 0) {
              let initHashApp = $location.path().split('/')[3];
              $scope.apps = data.data;
              $scope.apps = $scope.apps.map(function (item) {
                if (item.app === initHashApp) {
                  item.active = true;
                }
                var heathyCount = 0;
                for (var i in item.machines) {
                  if (item.machines[i].healthy) {
                      heathyCount++;
                  }
                }
                item.heathyCount = heathyCount;
                if (item.shown) {
                  return item;
                }
              });
            }
          }
        );

        // toggle side bar
        $scope.click = function ($event) {
          let entry = angular.element($event.target).scope().entry;
          entry.active = !entry.active;// toggle this clicked app bar

          $scope.apps.forEach(function (item) {// collapse other app bars
            if (item != entry) {
              item.active = false;
            }
          });
        };

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
      }
    };
  }]);
