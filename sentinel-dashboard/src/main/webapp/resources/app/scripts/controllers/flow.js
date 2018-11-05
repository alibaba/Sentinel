var app = angular.module('sentinelDashboardApp');

app.controller('FlowCtl', ['$scope', '$stateParams', 'FlowService', 'ngDialog',
  'MachineService',
  function ($scope, $stateParams, FlowService, ngDialog,
    MachineService) {
    $scope.app = $stateParams.app;

    $scope.rulesPageConfig = {
      pageSize: 10,
      currentPageIndex: 1,
      totalPage: 1,
      totalCount: 0,
    };
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

    getMachineRules();
    function getMachineRules() {
      if (!$scope.macInputModel) {
        return;
      }
      var mac = $scope.macInputModel.split(':');
      FlowService.queryMachineRules($scope.app, mac[0], mac[1]).success(
        function (data) {
          if (data.code == 0 && data.data) {
            $scope.rules = data.data;
            $scope.rulesPageConfig.totalCount = $scope.rules.length;
          } else {
            $scope.rules = [];
            $scope.rulesPageConfig.totalCount = 0;
          }
        });
    };
    $scope.getMachineRules = getMachineRules;

    var flowRuleDialog;
    $scope.editRule = function (rule) {
      $scope.currentRule = rule;
      $scope.flowRuleDialog = {
        title: '编辑流控规则',
        type: 'edit',
        confirmBtnText: '保存',
        showAdvanceButton: rule.controlBehavior == 0 && rule.strategy == 0
      };
      flowRuleDialog = ngDialog.open({
        template: '/app/views/dialog/flow-rule-dialog.html',
        width: 680,
        overlay: true,
        scope: $scope
      });
    };

    $scope.addNewRule = function () {
      var mac = $scope.macInputModel.split(':');
      $scope.currentRule = {
        grade: 1,
        strategy: 0,
        controlBehavior: 0,
        app: $scope.app,
        ip: mac[0],
        port: mac[1],
        limitApp: 'default'
      };
      $scope.flowRuleDialog = {
        title: '新增流控规则',
        type: 'add',
        confirmBtnText: '新增',
        showAdvanceButton: true,
      };
      flowRuleDialog = ngDialog.open({
        template: '/app/views/dialog/flow-rule-dialog.html',
        width: 680,
        overlay: true,
        scope: $scope
      });
    };

    $scope.saveRule = function () {
      if (!FlowService.checkRuleValid($scope.currentRule)) {
        return;
      }
      if ($scope.flowRuleDialog.type === 'add') {
        addNewRule($scope.currentRule);
      } else if ($scope.flowRuleDialog.type === 'edit') {
        saveRule($scope.currentRule, true);
      }
    };

    var confirmDialog;
    $scope.deleteRule = function (rule) {
      $scope.currentRule = rule;
      $scope.confirmDialog = {
        title: '删除流控规则',
        type: 'delete_rule',
        attentionTitle: '请确认是否删除如下流控规则',
        attention: '资源名: ' + rule.resource + ', 流控应用: ' + rule.limitApp
          + ', 阈值类型: ' + (rule.grade == 0 ? '线程数' : 'QPS') + ', 阈值: ' + rule.count,
        confirmBtnText: '删除',
      };
      confirmDialog = ngDialog.open({
        template: '/app/views/dialog/confirm-dialog.html',
        scope: $scope,
        overlay: true
      });
    };

    $scope.confirm = function () {
      if ($scope.confirmDialog.type === 'delete_rule') {
        deleteRule($scope.currentRule);
      } else {
        console.error('error');
      }
    };

    function deleteRule(rule) {
      FlowService.deleteRule(rule).success(function (data) {
        if (data.code == 0) {
          getMachineRules();
          confirmDialog.close();
        } else {
          alert('失败!');
        }
      });
    };

    function addNewRule(rule) {
      FlowService.newRule(rule).success(function (data) {
        if (data.code == 0) {
          getMachineRules();
          flowRuleDialog.close();
        } else {
          alert('失败!');
        }
      });
    };

    $scope.onOpenAdvanceClick = function () {
      $scope.flowRuleDialog.showAdvanceButton = false;
    };
    $scope.onCloseAdvanceClick = function () {
      $scope.flowRuleDialog.showAdvanceButton = true;
    };

    function saveRule(rule, edit) {
      FlowService.saveRule(rule).success(function (data) {
        if (data.code == 0) {
          getMachineRules();
          if (edit) {
            flowRuleDialog.close();
          } else {
            confirmDialog.close();
          }
        } else {
          alert('失败!');
        }
      });
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
        getMachineRules();
      }
    });
  }]);
