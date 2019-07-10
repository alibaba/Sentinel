var app = angular.module('sentinelDashboardApp');

app.controller('GatewayIdentityCtl', ['$scope', '$stateParams', 'IdentityService',
  'ngDialog', 'GatewayFlowService', 'GatewayApiService', 'DegradeService', 'MachineService',
  '$interval', '$location', '$timeout',
  function ($scope, $stateParams, IdentityService, ngDialog,
    GatewayFlowService, GatewayApiService, DegradeService, MachineService, $interval, $location, $timeout) {

    $scope.app = $stateParams.app;

    $scope.currentPage = 1;
    $scope.pageSize = 16;
    $scope.totalPage = 1;
    $scope.totalCount = 0;
    $scope.identities = [];

    $scope.searchKey = '';

    $scope.macsInputConfig = {
      searchField: ['text', 'value'],
      persist: true,
      create: false,
      maxItems: 1,
      render: {
        item: function (data, escape) {
          return '<div>' + escape(data.text) + '</div>';
        }
      },
      onChange: function (value, oldValue) {
        $scope.macInputModel = value;
      }
    };
    $scope.table = null;

    getApiNames();
    function getApiNames() {
      if (!$scope.macInputModel) {
        return;
      }

      var mac = $scope.macInputModel.split(':');
      GatewayApiService.queryApis($scope.app, mac[0], mac[1]).success(
        function (data) {
          if (data.code == 0 && data.data) {
            $scope.apiNames = [];

            data.data.forEach(function (api) {
              $scope.apiNames.push(api["apiName"]);
            });
          }
        });
    }

    var gatewayFlowRuleDialog;
    var gatewayFlowRuleDialogScope;
    $scope.addNewGatewayFlowRule = function (resource) {
      if (!$scope.macInputModel) {
        return;
      }
      var mac = $scope.macInputModel.split(':');
      gatewayFlowRuleDialogScope = $scope.$new(true);

      gatewayFlowRuleDialogScope.apiNames = $scope.apiNames;

      gatewayFlowRuleDialogScope.intervalUnits = [{val: 0, desc: '秒'}, {val: 1, desc: '分'}, {val: 2, desc: '时'}, {val: 3, desc: '天'}];

      gatewayFlowRuleDialogScope.currentRule = {
        grade: 1,
        app: $scope.app,
        ip: mac[0],
        port: mac[1],
        resourceMode: gatewayFlowRuleDialogScope.apiNames.indexOf(resource) == -1 ? 0 : 1,
        resource: resource,
        interval: 1,
        intervalUnit: 0,
        controlBehavior: 0,
        burst: 0,
        maxQueueingTimeoutMs: 0
      };

      gatewayFlowRuleDialogScope.gatewayFlowRuleDialog = {
        title: '新增网关流控规则',
        type: 'add',
        confirmBtnText: '新增',
        saveAndContinueBtnText: '新增并继续添加',
        showAdvanceButton: true
      };

      gatewayFlowRuleDialogScope.useRouteID = function() {
        gatewayFlowRuleDialogScope.currentRule.resource = '';
      };

      gatewayFlowRuleDialogScope.useCustormAPI = function() {
        gatewayFlowRuleDialogScope.currentRule.resource = '';
      };

      gatewayFlowRuleDialogScope.useParamItem = function () {
        gatewayFlowRuleDialogScope.currentRule.paramItem = {
          parseStrategy: 0,
          matchStrategy: 0
        };
      };

      gatewayFlowRuleDialogScope.notUseParamItem = function () {
        gatewayFlowRuleDialogScope.currentRule.paramItem = null;
      };

      gatewayFlowRuleDialogScope.useParamItemVal = function() {
        gatewayFlowRuleDialogScope.currentRule.paramItem.pattern = "";
      };

      gatewayFlowRuleDialogScope.notUseParamItemVal = function() {
        gatewayFlowRuleDialogScope.currentRule.paramItem.pattern = null;
      };

      gatewayFlowRuleDialogScope.saveRule = saveGatewayFlowRule;
      gatewayFlowRuleDialogScope.saveRuleAndContinue = saveGatewayFlowRuleAndContinue;
      gatewayFlowRuleDialogScope.onOpenAdvanceClick = function () {
        gatewayFlowRuleDialogScope.gatewayFlowRuleDialog.showAdvanceButton = false;
      };
      gatewayFlowRuleDialogScope.onCloseAdvanceClick = function () {
        gatewayFlowRuleDialogScope.gatewayFlowRuleDialog.showAdvanceButton = true;
      };

      gatewayFlowRuleDialog = ngDialog.open({
        template: '/app/views/dialog/gateway/flow-rule-dialog.html',
        width: 780,
        overlay: true,
        scope: gatewayFlowRuleDialogScope
      });
    };

    function saveGatewayFlowRule() {
      if (!GatewayFlowService.checkRuleValid(gatewayFlowRuleDialogScope.currentRule)) {
        return;
      }
      GatewayFlowService.newRule(gatewayFlowRuleDialogScope.currentRule).success(function (data) {
        if (data.code === 0) {
          gatewayFlowRuleDialog.close();
          let url = '/dashboard/gateway/flow/' + $scope.app;
          $location.path(url);
        } else {
          alert('失败!');
        }
      }).error((data, header, config, status) => {
          alert('未知错误');
      });
    }

    function saveGatewayFlowRuleAndContinue() {
        if (!GatewayFlowService.checkRuleValid(gatewayFlowRuleDialogScope.currentRule)) {
            return;
        }
      GatewayFlowService.newRule(gatewayFlowRuleDialogScope.currentRule).success(function (data) {
        if (data.code == 0) {
          gatewayFlowRuleDialog.close();
        } else {
          alert('失败!');
        }
      });
    }

    var degradeRuleDialog;
    $scope.addNewDegradeRule = function (resource) {
      if (!$scope.macInputModel) {
        return;
      }
      var mac = $scope.macInputModel.split(':');
      degradeRuleDialogScope = $scope.$new(true);
      degradeRuleDialogScope.currentRule = {
        enable: false,
        grade: 0,
        strategy: 0,
        resource: resource,
        limitApp: 'default',
        app: $scope.app,
        ip: mac[0],
        port: mac[1]
      };

      degradeRuleDialogScope.degradeRuleDialog = {
        title: '新增降级规则',
        type: 'add',
        confirmBtnText: '新增',
        saveAndContinueBtnText: '新增并继续添加'
      };
      degradeRuleDialogScope.saveRule = saveDegradeRule;
      degradeRuleDialogScope.saveRuleAndContinue = saveDegradeRuleAndContinue;

      degradeRuleDialog = ngDialog.open({
        template: '/app/views/dialog/degrade-rule-dialog.html',
        width: 680,
        overlay: true,
        scope: degradeRuleDialogScope
      });
    };

    function saveDegradeRule() {
        if (!DegradeService.checkRuleValid(degradeRuleDialogScope.currentRule)) {
            return;
        }
      DegradeService.newRule(degradeRuleDialogScope.currentRule).success(function (data) {
        if (data.code == 0) {
          degradeRuleDialog.close();
          var url = '/dashboard/degrade/' + $scope.app;
          $location.path(url);
        } else {
          alert('失败!');
        }
      });
    }

    function saveDegradeRuleAndContinue() {
        if (!DegradeService.checkRuleValid(degradeRuleDialogScope.currentRule)) {
            return;
        }
      DegradeService.newRule(degradeRuleDialogScope.currentRule).success(function (data) {
        if (data.code == 0) {
          degradeRuleDialog.close();
        } else {
          alert('失败!');
        }
      });
    }

    var searchHandler;
    $scope.searchChange = function (searchKey) {
      $timeout.cancel(searchHandler);
      searchHandler = $timeout(function () {
        $scope.searchKey = searchKey;
        reInitIdentityDatas();
      }, 600);
    };

    function queryAppMachines() {
      MachineService.getAppMachines($scope.app).success(
        function (data) {
          if (data.code === 0) {
            if (data.data) {
              $scope.machines = [];
              $scope.macsInputOptions = [];
              data.data.forEach(function (item) {
                if (item.healthy) {
                  $scope.macsInputOptions.push({
                    text: item.ip + ':' + item.port,
                    value: item.ip + ':' + item.port
                  });
                }
              });
            }
            if ($scope.macsInputOptions.length > 0) {
              $scope.macInputModel = $scope.macsInputOptions[0].value;
            }
          } else {
            $scope.macsInputOptions = [];
          }
        }
      );
    }

    // Fetch all machines by current app name.
    queryAppMachines();

    $scope.$watch('macInputModel', function () {
      if ($scope.macInputModel) {
        reInitIdentityDatas();
      }
    });

    $scope.$on('$destroy', function () {
      $interval.cancel(intervalId);
    });

    var intervalId;
    function reInitIdentityDatas() {
      getApiNames();
      queryIdentities();
    };

    function queryIdentities() {
      var mac = $scope.macInputModel.split(':');
      if (mac == null || mac.length < 2) {
        return;
      }

      IdentityService.fetchClusterNodeOfMachine(mac[0], mac[1], $scope.searchKey).success(
        function (data) {
          if (data.code == 0 && data.data) {
            $scope.identities = data.data;
            $scope.totalCount = $scope.identities.length;
          } else {
            $scope.identities = [];
            $scope.totalCount = 0;
          }
        }
      );
    };
    $scope.queryIdentities = queryIdentities;
  }]);
