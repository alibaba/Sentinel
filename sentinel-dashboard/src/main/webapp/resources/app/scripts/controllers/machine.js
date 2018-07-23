var app = angular.module('sentinelDashboardApp');

app.controller('MachineCtl', ['$scope', '$stateParams', 'MachineService',
  function ($scope, $stateParams, MachineService) {
    $scope.app = $stateParams.app;
    $scope.propertyName = '';
    $scope.reverse = false;
    $scope.currentPage = 1;
    $scope.machines = [];
    $scope.machinesPageConfig = {
      pageSize: 10,
      currentPageIndex: 1,
      totalPage: 1,
      totalCount: 0,
    };

    $scope.sortBy = function (propertyName) {
      // console.log('machine sortBy ' + propertyName);
      $scope.reverse = ($scope.propertyName === propertyName) ? !$scope.reverse : false;
      $scope.propertyName = propertyName;
    };

    MachineService.getAppMachines($scope.app).success(
      function (data) {
        // console.log('get machines: ' + data.data[0].hostname)
        if (data.code == 0 && data.data) {
          $scope.machines = data.data;
          var health = 0;
          $scope.machines.forEach(function (item) {
            if (item.health) {
              health++;
            }
            if (!item.hostname) {
              item.hostname = '未知'
            }
          })
          $scope.healthCount = health;
          $scope.machinesPageConfig.totalCount = $scope.machines.length;
        } else {
          $scope.machines = [];
          $scope.healthCount = 0;
        }
      }
    );
  }]);
