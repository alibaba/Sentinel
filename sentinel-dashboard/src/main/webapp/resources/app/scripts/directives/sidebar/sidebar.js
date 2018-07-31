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
              $scope.apps.forEach(function (item) {
                if (item.app === initHashApp) {
                  item.active = true;
                }
              });
            }
          }
        );

        // toggle side bar
        $scope.click = function ($event) {
          let element = angular.element($event.target);
          let entry = angular.element($event.target).scope().entry;
          entry.active = !entry.active;

          if (entry.active === false) {
            element.parent().children('ul').hide();
          } else {
            element.parent().parent().children('li').children('ul').hide();
            element.parent().children('ul').show();
          }
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
