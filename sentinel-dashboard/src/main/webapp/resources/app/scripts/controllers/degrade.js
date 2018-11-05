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
        title: 'Edit Degrade Rule',
        type: 'edit',
        confirmBtnText: 'Save'
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
        title: 'Add Degrade Rule',
        type: 'add',
        confirmBtnText: 'Add'
      };
      degradeRuleDialog = ngDialog.open({
        template: '/app/views/dialog/degrade-rule-dialog.html',
        width: 680,
        overlay: true,
        scope: $scope
      });
    };

    $scope.saveRule = function () {
      if (!DegradeService.checkRuleValid($scope.currentRule)) {
        return;
      }
      if ($scope.degradeRuleDialog.type === 'add') {
        addNewRule($scope.currentRule);
      } else if ($scope.degradeRuleDialog.type === 'edit') {
        saveRule($scope.currentRule, true);
      }
    };

    function parseDegradeMode(grade) {
        switch (grade) {
            case 0:
              return 'RT';
            case 1:
              return 'Exception Ratio';
            case 2:
              return 'Exception Count';
            default:
              return 'Unknown';
        }
    }

    var confirmDialog;
    $scope.deleteRule = function (rule) {
      $scope.currentRule = rule;
      $scope.confirmDialog = {
        title: 'Delete Degrade Rule',
        type: 'delete_rule',
        attentionTitle: 'Please confirm the following rule to be deleted',
        attention: 'Resource name: ' + rule.resource +
            ', degrade strategy: ' + parseDegradeMode(rule.grade) + ', threshold: ' + rule.count,
        confirmBtnText: 'Delete',
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
          alert('Failed');
        }
      });
    };

    function addNewRule(rule) {
      DegradeService.newRule(rule).success(function (data) {
        if (data.code == 0) {
          getMachineRules();
          degradeRuleDialog.close();
        } else {
          alert('Failed');
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
          alert('Failed');
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
