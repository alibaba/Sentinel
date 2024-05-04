var app = angular.module('sentinelDashboardApp');

app.controller('FlowControllerV2', ['$scope', '$stateParams', 'FlowServiceV2', 'ngDialog',
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

    $scope.generateThresholdTypeShow = (rule) => {
      if (!rule.clusterMode) {
        return '单机';
      }
      if (rule.clusterConfig.thresholdType === 0) {
        return '集群均摊';
      } else if (rule.clusterConfig.thresholdType === 1) {
        return '集群总体';
      } else {
        return '集群';
      }
    };
    function extractIPAndPort(input) {
    // 假设输入字符串是 9 组的格式，并且我们想要前 8 组作为 IPv6 地址，最后一组作为端口号
    const parts = input.split(':');
    if (parts.length === 9) {
        // 提取前 8 组作为 IPv6 地址
        const ip = parts.slice(0, 8).join(':');
        // 尝试将最后一组转换为端口号（这里假设它是有效的端口号）
        const portStr = parts[8];
        const port = parseInt(portStr, 10);

        // 验证端口号是否在有效范围内
        if (port >= 1 && port <= 65535) {
            // 返回包含 IPv6 地址和端口号的数组
            return [ip, port];
        } else {
            // 端口号无效，返回包含 IPv6 地址和 null 的数组
            return [ip, null];
        }
    } else if(parts.length === 2){
    	const ip = parts[0];
        const port = parseInt(parts[1], 10);
      	return [ip, port];
    }else {
        // 输入字符串格式不正确，返回包含两个 null 的数组
        return [null, null];
    }
};
    getMachineRules();
    function getMachineRules() {
      if (!$scope.macInputModel) {
        return;
      }
      var mac = extractIPAndPort($scope.macInputModel);
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
      $scope.currentRule = angular.copy(rule);
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
      var mac = extractIPAndPort($scope.macInputModel);
      $scope.currentRule = {
        grade: 1,
        strategy: 0,
        controlBehavior: 0,
        app: $scope.app,
        ip: mac[0],
        port: mac[1],
        limitApp: 'default',
        clusterMode: false,
        clusterConfig: {
          thresholdType: 0,
          fallbackToLocalWhenFail: true
        }
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
