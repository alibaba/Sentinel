var app = angular.module('sentinelDashboardApp');

app.controller('SystemCtl', ['$scope', '$stateParams', 'SystemService', 'ngDialog', 'MachineService',
  function ($scope, $stateParams, SystemService,
    ngDialog, MachineService) {
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
      let mac = $scope.macInputModel.split(':');
      SystemService.queryMachineRules($scope.app, mac[0], mac[1]).success(
        function (data) {
          if (data.code === 0 && data.data) {
            $scope.rules = data.data;
            $.each($scope.rules, function (idx, rule) {
              if (rule.highestSystemLoad >= 0) {
                rule.grade = 0;
              } else if (rule.avgRt >= 0) {
                rule.grade = 1;
              } else if (rule.maxThread >= 0) {
                rule.grade = 2;
              } else if (rule.qps >= 0) {
                rule.grade = 3;
              } else if (rule.highestCpuUsage >= 0) {
                  rule.grade = 4;
              }
            });
            $scope.rulesPageConfig.totalCount = $scope.rules.length;
          } else {
            $scope.rules = [];
            $scope.rulesPageConfig.totalCount = 0;
          }
        });
    }

    $scope.getMachineRules = getMachineRules;
    var systemRuleDialog;
    $scope.editRule = function (rule) {
      $scope.currentRule = angular.copy(rule);
      $scope.systemRuleDialog = {
        title: 'Edit System Rule',
        type: 'edit',
        confirmBtnText: 'Save'
      };
      systemRuleDialog = ngDialog.open({
        template: '/app/views/dialog/system-rule-dialog.html',
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
      };
      $scope.systemRuleDialog = {
        title: 'Add New System Rule',
        type: 'add',
        confirmBtnText: 'Add'
      };
      systemRuleDialog = ngDialog.open({
        template: '/app/views/dialog/system-rule-dialog.html',
        width: 680,
        overlay: true,
        scope: $scope
      });
    };

    $scope.saveRule = function () {
      if ($scope.systemRuleDialog.type === 'add') {
        addNewRule($scope.currentRule);
      } else if ($scope.systemRuleDialog.type === 'edit') {
        saveRule($scope.currentRule, true);
      }
    };

    var confirmDialog;
    $scope.deleteRule = function (rule) {
      $scope.currentRule = rule;
      var ruleTypeDesc = '';
      var ruleTypeCount = null;
      if (rule.highestSystemLoad != -1) {
        ruleTypeDesc = 'LOAD';
        ruleTypeCount = rule.highestSystemLoad;
      } else if (rule.avgRt != -1) {
        ruleTypeDesc = 'RT';
        ruleTypeCount = rule.avgRt;
      } else if (rule.maxThread != -1) {
        ruleTypeDesc = 'Thread Count';
        ruleTypeCount = rule.maxThread;
      } else if (rule.qps != -1) {
        ruleTypeDesc = 'QPS';
        ruleTypeCount = rule.qps;
      }else if (rule.highestCpuUsage != -1) {
          ruleTypeDesc = 'CPU 使用率';
          ruleTypeCount = rule.highestCpuUsage;
      }

      $scope.confirmDialog = {
        title: 'Delete Rule',
        type: 'delete_rule',
        attentionTitle: 'Please confirm the following rule to be deleted',
        attention: 'Type: ' + ruleTypeDesc + ', threshold: ' + ruleTypeCount,
        confirmBtnText: 'Delete',
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
        // } else if ($scope.confirmDialog.type == 'enable_rule') {
        //     $scope.currentRule.enable = true;
        //     saveRule($scope.currentRule);
        // } else if ($scope.confirmDialog.type == 'disable_rule') {
        //     $scope.currentRule.enable = false;
        //     saveRule($scope.currentRule);
        // } else if ($scope.confirmDialog.type == 'enable_all') {
        //     enableAll($scope.app);
        // } else if ($scope.confirmDialog.type == 'disable_all') {
        //     disableAll($scope.app);
      } else {
        console.error('error');
      }
    };

    function deleteRule(rule) {
      SystemService.deleteRule(rule).success(function (data) {
        if (data.code === 0) {
          getMachineRules();
          confirmDialog.close();
        } else if (data.msg != null) {
            alert('失败：' + data.msg);
        } else {
            alert('Failed');
        }
      });
    }

    function addNewRule(rule) {
      if (rule.grade == 4 && (rule.highestCpuUsage < 0 || rule.highestCpuUsage > 1)) {
        alert('CPU The value range should be [0.0, 1.0]，range 0% - 100%');
        return;
      }
      SystemService.newRule(rule).success(function (data) {
        if (data.code === 0) {
          getMachineRules();
          systemRuleDialog.close();
        } else if (data.msg != null) {
          alert('Failed: ' + data.msg);
        } else {
          alert('Failed');
        }
      });
    }

    function saveRule(rule, edit) {
      SystemService.saveRule(rule).success(function (data) {
        if (data.code === 0) {
          getMachineRules();
          if (edit) {
            systemRuleDialog.close();
          } else {
            confirmDialog.close();
          }
        } else if (data.msg != null) {
          alert('Failed: ' + data.msg);
        } else {
          alert('Failed');
        }
      });
    }
    queryAppMachines();
    function queryAppMachines() {
      MachineService.getAppMachines($scope.app).success(
        function (data) {
          if (data.code === 0) {
            // $scope.machines = data.data;
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
    };
    $scope.$watch('macInputModel', function () {
      if ($scope.macInputModel) {
        getMachineRules();
      }
    });
  }]);
