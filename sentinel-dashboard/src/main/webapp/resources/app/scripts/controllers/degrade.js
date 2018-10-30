var app = angular.module('sentinelDashboardApp');

app.controller('DegradeCtl', ['$scope', '$stateParams', 'DegradeService', 'ngDialog', 'MachineService',
  function ($scope, $stateParams, DegradeService, ngDialog, MachineService) {
    //初始化
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
      DegradeService.queryMachineRules($scope.app, mac[0], mac[1]).success(
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

    var degradeRuleDialog;
    $scope.editRule = function (rule) {
      $scope.currentRule = rule;
      $scope.degradeRuleDialog = {
        title: '编辑降级规则',
        type: 'edit',
        confirmBtnText: '保存'
      };
      degradeRuleDialog = ngDialog.open({
        template: '/app/views/dialog/degrade-rule-dialog.html',
        width: 680,
        overlay: true,
        scope: $scope
      });
    };

    $scope.addNewRule = function () {
      var mac = $scope.macInputModel.split(':');
      $scope.currentRule = {
        grade: 0,
        app: $scope.app,
        ip: mac[0],
        port: mac[1],
        limitApp: 'default'
      };
      $scope.degradeRuleDialog = {
        title: '新增降级规则',
        type: 'add',
        confirmBtnText: '新增'
      };
      degradeRuleDialog = ngDialog.open({
        template: '/app/views/dialog/degrade-rule-dialog.html',
        width: 680,
        overlay: true,
        scope: $scope
      });
    };

      function checkRuleValid(rule) {
          if (rule.resource === undefined || rule.resource === '') {
              alert('资源名称不能为空');
              return false;
          }
          if (rule.grade === undefined || rule.grade < 0) {
              alert('未知的降级类型');
              return false;
          }
          if (rule.count === undefined || rule.count === '' || rule.count < 0) {
              alert('降级阈值不能为空或小于 0');
              return false;
          }
          if (rule.timeWindow === undefined || rule.timeWindow === '' || rule.timeWindow <= 0) {
              alert('降级时间窗口必须大于 0');
              return false;
          }
          // 异常比率类型.
          if (rule.grade == 1 && rule.count > 1) {
              alert('异常比率超出范围：[0.0 - 1.0]');
              return false;
          }
          return true;
      }

    $scope.saveRule = function () {
      if (!checkRuleValid($scope.currentRule)) {
        return;
      }
      if ($scope.degradeRuleDialog.type === 'add') {
        addNewRule($scope.currentRule);
      } else if ($scope.degradeRuleDialog.type === 'edit') {
        saveRule($scope.currentRule, true);
      }
    };

    var confirmDialog;
    $scope.deleteRule = function (rule) {
      $scope.currentRule = rule;
      $scope.confirmDialog = {
        title: '删除降级规则',
        type: 'delete_rule',
        attentionTitle: '请确认是否删除如下降级规则',
        attention: '资源名: ' + rule.resource + ', 降级应用: ' + rule.limitApp
          + ', 阈值类型: ' + (rule.grade == 0 ? 'RT' : '异常比例') + ', 阈值: ' + rule.count,
        confirmBtnText: '删除',
      };
      confirmDialog = ngDialog.open({
        template: '/app/views/dialog/confirm-dialog.html',
        scope: $scope,
        overlay: true

      });
    };

    $scope.confirm = function () {
      if ($scope.confirmDialog.type == 'delete_rule') {
        deleteRule($scope.currentRule);
      } else {
        console.error('error');
      }
    };

    function deleteRule(rule) {
      DegradeService.deleteRule(rule).success(function (data) {
        if (data.code == 0) {
          getMachineRules();
          confirmDialog.close();
        } else {
          alert('失败!');
        }
      });
    };

    function addNewRule(rule) {
      DegradeService.newRule(rule).success(function (data) {
        if (data.code == 0) {
          getMachineRules();
          degradeRuleDialog.close();
        } else {
          alert('失败!');
        }
      });
    };

    function saveRule(rule, edit) {
      DegradeService.saveRule(rule).success(function (data) {
        if (data.code == 0) {
          getMachineRules();
          if (edit) {
            degradeRuleDialog.close();
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
