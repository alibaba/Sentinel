angular.module('sentinelDashboardApp')
  .directive('sidebar', ['$location', '$stateParams', 'AppService', function () {
    return {
      templateUrl: 'app/scripts/directives/sidebar/sidebar.html',
      restrict: 'E',
      replace: true,
      scope: {
      },
      controller: function ($scope, $stateParams, $location, AppService) {
        // 当前project下所有app
        $scope.apps = [];
        // 用来控制app的下拉框是否显示
        $scope.appActives = [];
        // 当前project下所有service的信息
        $scope.services = [];

        sidebarInit = async () => {
          $scope.$on("rootToSidebar_kieInfos", (e,msg) => {
            console.log("sidebar init......");
            // got init kieInfos in msg
            console.log("msg", msg);
            $scope.services = msg;
            msg.forEach(val => {
              if (!$scope.apps.includes(val.app)) {
                $scope.apps.push(val.app);
                let tmp = {
                  'app': val.app,
                  'active': false
                };
                $scope.appActives.push(tmp);
              }
            })
          })
        }
        
        sidebarInit();

        // toggle side bar
        $scope.click = function ($event) {
          let entry = angular.element($event.target).scope().entry;
          entry.active = !entry.active;// toggle this clicked app bar

          $scope.apps.forEach(function (item) { // collapse other app bars
            if (item !== entry) {
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
