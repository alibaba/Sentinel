var app = angular.module('sentinelDashboardApp');

app.controller('IdentityCtl', ['$scope', '$stateParams', 'IdentityService',
  'ngDialog', 'FlowService', 'DegradeService', 'MachineService',
  '$interval', '$location', '$timeout',
  function ($scope, $stateParams, IdentityService, ngDialog,
    FlowService, DegradeService, MachineService, $interval, $location, $timeout) {

    $scope.app = $stateParams.app;
    // $scope.rulesPageConfig = {
    // pageSize : 10,
    // currentPageIndex : 1,
    // totalPage : 1,
    // totalCount: 0,
    // };
    $scope.currentPage = 1;
    $scope.pageSize = 16;
    $scope.totalPage = 1;
    $scope.totalCount = 0;
    $scope.identities = [];
    // 数据自动刷新频率, 默认10s
    var DATA_REFRESH_INTERVAL = 30;

    $scope.isExpand = true;
    $scope.searchKey = '';
    $scope.firstExpandAll = false;
    $scope.isTreeView = true;

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

    var flowRuleDialog;
    var flowRuleDialogScope;
    $scope.addNewFlowRule = function (resource) {
      if (!$scope.macInputModel) {
        return;
      }
      var mac = $scope.macInputModel.split(':');
      flowRuleDialogScope = $scope.$new(true);
      flowRuleDialogScope.currentRule = {
        enable: false,
        strategy: 0,
        grade: 1,
        controlBehavior: 0,
        resource: resource,
        limitApp: 'default',
        app: $scope.app,
        ip: mac[0],
        port: mac[1]
      };

      flowRuleDialogScope.flowRuleDialog = {
        title: '新增流控规则',
        type: 'add',
        confirmBtnText: '新增',
        saveAndContinueBtnText: '新增并继续添加',
        showAdvanceButton: true
      };
      // $scope.flowRuleDialog = {
      //     showAdvanceButton : true
      // };
      flowRuleDialogScope.saveRule = saveFlowRule;
      flowRuleDialogScope.saveRuleAndContinue = saveFlowRuleAndContinue;
      flowRuleDialogScope.onOpenAdvanceClick = function () {
        flowRuleDialogScope.flowRuleDialog.showAdvanceButton = false;
      };
      flowRuleDialogScope.onCloseAdvanceClick = function () {
        flowRuleDialogScope.flowRuleDialog.showAdvanceButton = true;
      };

      flowRuleDialog = ngDialog.open({
        template: '/app/views/dialog/flow-rule-dialog.html',
        width: 680,
        overlay: true,
        scope: flowRuleDialogScope
      });
    };

    function saveFlowRule() {
      FlowService.newRule(flowRuleDialogScope.currentRule).success(function (data) {
        if (data.code == 0) {
          flowRuleDialog.close();
          var url = '/dashboard/flow/' + $scope.app;
          $location.path(url);
        } else {
          alert('失败!');
        }
      });
    }

    function saveFlowRuleAndContinue() {
      FlowService.newRule(flowRuleDialogScope.currentRule).success(function (data) {
        if (data.code == 0) {
          flowRuleDialog.close();
        } else {
          alert('失败!');
        }
      });
    }

    var degradeRuleDialog;
    var degradeRuleDialogScope;
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
      // console.info('searchKey=', searchKey);
      $timeout.cancel(searchHandler);
      searchHandler = $timeout(function () {
        $scope.searchKey = searchKey;
        $scope.isExpand = true;
        $scope.firstExpandAll = true;
        reInitIdentityDatas();
        $scope.firstExpandAll = false;
      }, 600);
    }

    $scope.initTreeTable = function () {
      // if (!$scope.table) {
        com_github_culmat_jsTreeTable.register(window);
        $scope.table = window.treeTable($('#identities'));
      // }
    }

    $scope.expandAll = function () {
      $scope.isExpand = true;
    };
    $scope.collapseAll = function () {
      $scope.isExpand = false;
    };
    $scope.treeView = function () {
      $scope.isTreeView = true;
      queryIdentities();
    }
    $scope.listView = function () {
      $scope.isTreeView = false;
      queryIdentities();
    }

    queryAppMachines();
    function queryAppMachines() {
      MachineService.getAppMachines($scope.app).success(
        function (data) {
          if (data.code == 0) {
            // $scope.machines = data.data;
            if (data.data) {
              $scope.machines = [];
              $scope.macsInputOptions = [];
              data.data.forEach(function (item) {
                if (item.health) {
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
    };

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
      // $interval.cancel(intervalId);
      queryIdentities();
      // intervalId = $interval(function () {
      //    queryIdentities();
      // }, DATA_REFRESH_INTERVAL * 1000);
    };

    function queryIdentities() {
      var mac = $scope.macInputModel.split(':');
      if (mac == null || mac.length < 2) {
        return;
      }
      if ($scope.isTreeView) {
        IdentityService.fetchIdentityOfMachine(mac[0], mac[1], $scope.searchKey).success(
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
      } else {
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
      }
    };
    $scope.queryIdentities = queryIdentities;
  }]);
